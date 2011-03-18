package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Gzip {

  private static final byte BTYPE_NO_COMPRESSION = 0x00;
  private static final byte BTYPE_STATIC_HUFFMAN = 0x01;
  private static final byte BTYPE_DYNAMIC_HUFFMAN = 0x02;
  private static final byte BTYPE_RESERVED = 0x03;

  public static final int DEFAULT_WINDOW_BITS = 15;

  private static final int END_OF_BLOCK_CODE = 256;

  // 0x01FF=length, 0xE000=extra bits.
  static final int _LIT_LEN_EXTRA_OFFSET = 9;
  static final int _LIT_LEN_MASK = 0x1FF;
  static final char[] LITERALS_LENGTHS = new char[] { //
      0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0x20B, 0x20D, 0x20F, 0x211,
      0x413, 0x417, 0x41B, 0x41F, 0x623, 0x62B, 0x633, 0x63B, 0x843, 0x853,
      0x863, 0x873, 0xA83, 0xAA3, 0xAC3, 0xAE3, 0x102 };

  static int literalLength(int litCode, Crc32Stream in) throws IOException {
    int length = litCode & _LIT_LEN_MASK;
    int extraBits = litCode >>> _LIT_LEN_EXTRA_OFFSET;
    if (extraBits > 0) {
      length += in.readBits(extraBits);
    }
    return length;
  }

  // 0xF0=distance, 0x0F=extra bits.
  static final int _LIT_DIST_EXTRA_OFFSET = 9;
  static final int _LIT_DIST_MASK = 0xFFFF;
  static final int[] LITERALS_DISTANCES = new int[] { //
      0x01, 0x02, 0x03, 0x04, 0x10005, 0x10007, 0x20009, 0x2000D, 0x30011,
      0x30019, 0x40021, 0x40031, 0x50041, 0x50061, 0x60081, 0x600C1, 0x70101,
      0x70181, 0x80201, 0x80301, 0x90401, 0x90601, 0xA0801, 0xA0C01, 0xB1001,
      0xB1801, 0xC2001, 0xC3001, 0xD4001, 0xD6001, };

  static int literalDistance(int litCode, Crc32Stream in) throws IOException {
    int distance = litCode & _LIT_DIST_MASK;
    int extraBits = litCode >>> _LIT_DIST_EXTRA_OFFSET;
    if (extraBits > 0) {
      distance += in.readBits(extraBits);
    }
    return distance;
  }

  static final int[] FIXED_LITERALS_TREE;
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
    FIXED_LITERALS_TREE = Huffman.buildCodeTree(9, node_len);
  }

  private static void inflateHuffman(Crc32Stream in, WindowedStream out,
      int[] litLenTree, int[] distTree) throws IOException {
    int litLenCode = 0;
    while ((litLenCode = Huffman.decodeSymbol(in, litLenTree)) != END_OF_BLOCK_CODE) {
      if (litLenCode < END_OF_BLOCK_CODE) {
        out.write(litLenCode);
      } else {
        litLenCode -= (END_OF_BLOCK_CODE + 1);
        int length = literalLength(litLenCode, in);
        int distCode = in.readBits(5);
        int distance = literalDistance(distCode, in);
        out.copyFromEnd(distance, length);
      }
    }
  }

  static final int[] FIXED_ALPHABET_LENGTHS_TREE;
  static {
    char[] node_len = new char[19];
    for (int i = 0; i < node_len.length; i++) {
      node_len[i] = 5;
    }
    FIXED_ALPHABET_LENGTHS_TREE = Huffman.buildCodeTree(5, node_len);
  }

  private static final byte[] HUFF2PERM = { //
      16, 17, 18, 0, 8, 7, 9, 6, 10, 5, //
      11, 4, 12, 3, 13, 2, 14, 1, 15 //
  };

  private static char[] readLengths(Crc32Stream in, int[] hcTree, int size)
      throws IOException {
    char[] lengths = new char[size];
    for (int i = 0; i < lengths.length; i++) {
      int code = Huffman.decodeSymbol(in, hcTree);
      int toCopy = 0;
      int repeat = 0;
      switch (code) {
      case 16:
        toCopy = lengths[i - 1];
        repeat = 3 + in.readBits(2);
        break;
      case 17:
        toCopy = lengths[0];
        repeat = 3 + in.readBits(3);
        break;
      case 18:
        toCopy = lengths[0];
        repeat = 11 + in.readBits(3);
        break;
      default:
        lengths[i] = (char) code;
        continue;
      }
      for (; repeat > 0 && i < lengths.length; i++) {
        lengths[i] = (char) toCopy;
      }
    }
    return lengths;
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
    int[] hcTree = Huffman.buildCodeTree(7, h2CodeLen);
    char[] litCodeLen = readLengths(in, hcTree, hlit);
    char[] distCodeLen = readLengths(in, hcTree, hdist);
    int[] litLenTree = Huffman.buildCodeTree(15, litCodeLen);
    int[] distTree = Huffman.buildCodeTree(15, distCodeLen);
    inflateHuffman(in, out, litLenTree, distTree);
  }

  private static void inflateRawBlock(Crc32Stream in, WindowedStream out)
      throws IOException {
    int len = in.readBytes(2);
    int nlen = in.readBytes(2);
    if ((len ^ nlen) != 0xFFFF) {
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

  private static int inflate(Crc32Stream in, WindowedStream out)
      throws IOException {
    boolean finalBlock = false;
    do {
      finalBlock = in.readBits(1) != 0;
      int blockType = in.readBits(2);
      switch (blockType) {
      case BTYPE_NO_COMPRESSION:
        in.readBits(5); // Discard the rest of header.
        inflateRawBlock(in, out);
        break;
      case BTYPE_STATIC_HUFFMAN:
        inflateHuffman(in, out, FIXED_LITERALS_TREE,
            FIXED_ALPHABET_LENGTHS_TREE);
        break;
      case BTYPE_DYNAMIC_HUFFMAN:
        inflateDynamicHuffman(in, out);
        break;
      default:
      case BTYPE_RESERVED:
        throw new IOException("Invalid block.");
      }
    } while (!finalBlock);
    return -1;
  }

  public static int inflate(InputStream in, OutputStream out)
      throws IOException {
    WindowedStream stream = new WindowedStream(out, DEFAULT_WINDOW_BITS);
    return inflate(new Crc32Stream(in), stream);
  }

  public static final int GZIP_MAGIC_NUMBER = 0x1F8B;
  //private static final byte FTEXT = 0x01;
  private static final byte FHCRC = 0x02;
  private static final byte FEXTRA = 0x04;
  private static final byte FNAME = 0x08;
  private static final byte FCOMMENT = 0x10;
  //private static final byte FRESERVED = (byte) 0xE0;
  private static final byte CM_DEFLATE = 8;

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
    /* int mtime = */crcIn.readBytes(4);
    /* int xfl = */crcIn.readBytes(1);
    /* int os = */crcIn.readBytes(1);

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
    WindowedStream stream = new WindowedStream(out, DEFAULT_WINDOW_BITS);
    inflate(crc32stream, stream);
    // Read footer.
    int bodyCrc32 = crc32stream.getCrc32();
    int expectedBodyCrc32 = crc32stream.readBytes(4);
    if (expectedBodyCrc32 != bodyCrc32) {
      throw new IOException("CRC check failed.");
    }
    int isize = crc32stream.readBytes(4);
    if ((stream.getOutputSize() & 0xFFFFFFFF) != isize) {
      throw new IOException("Size mismatches.");
    }
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
