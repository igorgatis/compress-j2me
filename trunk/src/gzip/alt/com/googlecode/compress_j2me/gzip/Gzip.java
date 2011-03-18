package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Gzip {

  public static final int GZIP_MAGIC_NUMBER = 0x1F8B;
  public static final int DEFAULT_WINDOW_BITS = 15;

  private static final byte FTEXT = 0x01;
  private static final byte FHCRC = 0x02;
  private static final byte FEXTRA = 0x04;
  private static final byte FNAME = 0x08;
  private static final byte FCOMMENT = 0x10;
  private static final byte FRESERVED = (byte) 0xE0;
  private static final byte CM_DEFLATE = 8;

  private static final byte BFINAL_MASK = (byte) 0x80;
  private static final byte BTYPE_MASK = 0x60;
  private static final byte BTYPE_NO_COMPRESSION = 0x00;
  private static final byte BTYPE_STATIC_HUFFMAN = 0x20;
  private static final byte BTYPE_DYNAMIC_HUFFMAN = 0x40;
  private static final byte BTYPE_RESERVED = 0x60;

  static final int[] CANONICAL_LIT_CODES;
  static {
    char[] node_len = new char[288];
    int i = 0;
    while (i < 144) {
      node_len[i++] = 8;
    }
    while (i < 256) {
      node_len[i++] = 9;
    }
    while (i < 280) {
      node_len[i++] = 7;
    }
    while (i < node_len.length) {
      node_len[i++] = 8;
    }
    CANONICAL_LIT_CODES = Huffman.buildCodeTree(9, node_len);
  }

  static final int[] CANONICAL_DIST_CODES;
  static {
    char[] node_len = new char[19];
    for (int i = 0; i < node_len.length; i++) {
      node_len[i] = 5;
    }
    CANONICAL_DIST_CODES = Huffman.buildCodeTree(5, node_len);
  }

  private static final byte[] HUFF2PERM = { 16, 17, 18, 0, 8, 7, 9, 6, 10, 5,
      11, 4, 12, 3, 13, 2, 14, 1, 15 };

  private static int decodeSymbol(Crc32Stream crcIn, int[] huff)
      throws IOException {
    int state = 0;
    for (;;) {
      int m = huff[state];
      if (crcIn.readBits(1) == 1) {
        m = (m >>> 16);
      } else {
        m &= 0xFFFF;
      }
      if (m >= 0x8000) {
        m &= 0x7FFF;
        if (m == 0x7FFF) {
          throw new IOException("invalid Huffman code");
        }
        return m;
      }
      state = m;
    }
  }

  private static void inflateHuffman(Crc32Stream in, WindowedStream out,
      int[] litCodes, int[] distCodes) throws IOException {
    //       loop (until end of block code recognized)
    //          decode literal/length value from input stream
    //          if value < 256
    //             copy value (literal byte) to output stream
    //          otherwise
    //             if value = end of block (256)
    //                break from loop
    //             otherwise (value = 257..285)
    //                decode distance from input stream
    //
    //                move backwards distance bytes in the output
    //                stream, and copy length bytes from this
    //                position to the output stream.
    //       end loop
  }

  private static void inflateDynamicHuffman(Crc32Stream in, WindowedStream out)
      throws IOException {
    int hlit = in.readBits(5) + 257;
    int hdist = in.readBits(5) + 1;
    int hclen = in.readBits(4) + 4;
    char[] h2CodeLen = new char[19];
    for (int i = 0; i < hclen; i++) {
      h2CodeLen[HUFF2PERM[i]] = (char) in.readBits(3);
    }

    int[] huff2 = Huffman.buildCodeTree(7, h2CodeLen);

    int[] tmpCodeLen = new int[hlit + hdist];
    int p = 0;
    int prev = -1;
    while (p < tmpCodeLen.length) {
      int repeat;
      int s = decodeSymbol(in, huff2);
      switch (s) {
      case 16:
        if (prev < 0) {
          throw new IOException("repeat code at beginning");
        }
        repeat = 3 + in.readBits(2);
        break;
      case 17:
        prev = 0;
        repeat = 3 + in.readBits(3);
        break;
      case 18:
        prev = 0;
        repeat = 11 + in.readBits(7);
        break;
      default:
        tmpCodeLen[p++] = s;
        prev = s;
        continue;
      }
      if ((p + repeat) > tmpCodeLen.length) {
        throw new IOException("repeat code beyond actual length");
      }
      while (repeat-- > 0) {
        tmpCodeLen[p++] = prev;
      }
    }

    char[] litCodeLen = new char[286];
    System.arraycopy(tmpCodeLen, 0, litCodeLen, 0, hlit);
    char[] distCodeLen = new char[32];
    System.arraycopy(tmpCodeLen, hlit, distCodeLen, 0, hdist);

    int[] litCodes = Huffman.buildCodeTree(15, litCodeLen);
    int[] distCodes = Huffman.buildCodeTree(15, distCodeLen);
    inflateHuffman(in, out, litCodes, distCodes);
  }

  private static void inflateRawBlock(Crc32Stream in, OutputStream out)
      throws IOException {
    int len = in.readBytes(2);
    int nlen = in.readBytes(2);
    if ((len ^ 0xFF) != nlen) {
      throw new IOException("Invalid block.");
    }
    while (len-- > 0) {
      int ch = in.read();
      if (ch < 0) {
        throw new IOException("Unexpected EOF.");
      }
      out.write(ch);
    }
  }

  private static int inflate(Crc32Stream in, OutputStream out)
      throws IOException {
    WindowedStream stream = new WindowedStream(out, DEFAULT_WINDOW_BITS);
    int blockHeader;
    do {
      blockHeader = in.read();
      int blockType = (blockHeader & BTYPE_MASK);
      switch (blockType) {
      case BTYPE_NO_COMPRESSION:
        inflateRawBlock(in, out);
        break;
      case BTYPE_STATIC_HUFFMAN:
        inflateHuffman(in, stream, CANONICAL_LIT_CODES, CANONICAL_DIST_CODES);
        break;
      case BTYPE_DYNAMIC_HUFFMAN:
        inflateDynamicHuffman(in, stream);
        break;
      default:
      case BTYPE_RESERVED:
        throw new IOException("Invalid block.");
      }
    } while ((blockHeader & BFINAL_MASK) == 0);
    return -1;
  }

  public static int inflate(InputStream in, OutputStream out)
      throws IOException {
    return inflate(new Crc32Stream(in), out);
  }

  static Gzip readHeader(Crc32Stream crcIn) throws IOException {
    Gzip gzip = new Gzip();
    int magic = crcIn.readBytes(2);
    if (magic != GZIP_MAGIC_NUMBER) {
      throw new IOException("Bad magic number " + magic);
    }
    int cm = crcIn.readBytes(1);
    if (cm != CM_DEFLATE) {
      throw new IOException("Unsupported CM=" + cm);
    }
    int flg = crcIn.readBytes(1);
    int mtime = crcIn.readBytes(4);
    int xfl = crcIn.readBytes(1);
    int os = crcIn.readBytes(1);

    if ((flg & FEXTRA) != 0) {
      int xlen = crcIn.readBytes(2);
      while (xlen-- > 0) {
        crcIn.read();
      }
    }
    if ((flg & FNAME) != 0) {
      gzip.filename = crcIn.readZeroTerminatedString();
    }
    if ((flg & FCOMMENT) != 0) {
      gzip.comment = crcIn.readZeroTerminatedString();
    }
    if ((flg & FHCRC) != 0) {
      int headerCrc16 = crcIn.getCrc32() & 0xFFFF;
      int expectedHeaderCrc16 = crcIn.readBytes(2);
      if (expectedHeaderCrc16 != headerCrc16) {
        throw new IOException("Header CRC check failed.");
      }
    }
    return gzip;
  }

  public static Gzip gunzip(InputStream in, OutputStream out)
      throws IOException {
    Crc32Stream crc32stream = new Crc32Stream(in);
    Gzip gzip = readHeader(crc32stream);
    // Uncompress body.
    crc32stream.resetCrc32();
    inflate(crc32stream, out);
    // Read footer.
    int bodyCrc32 = crc32stream.getCrc32();
    int expectedBodyCrc32 = crc32stream.readBytes(4);
    if (expectedBodyCrc32 != bodyCrc32) {
      throw new IOException("CRC check failed.");
    }
    int isize = crc32stream.readBytes(4);
    return gzip;
  }

  private String filename;
  private String comment;

  public Gzip(String file, String comment) {
    this.filename = file;
    this.comment = comment;
  }

  public Gzip() {
  }

  public String getFilename() {
    return filename;
  }

  public String getComment() {
    return comment;
  }
}
