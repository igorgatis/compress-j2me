package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Gzip {

  public static final int GZIP_MAGIC_NUMBER = 0x8B1F;
  //private static final byte FTEXT = 0x01;
  private static final byte FHCRC = 0x02;
  private static final byte FEXTRA = 0x04;
  private static final byte FNAME = 0x08;
  private static final byte FCOMMENT = 0x10;
  //private static final byte FRESERVED = (byte) 0xE0;
  private static final byte CM_DEFLATE = 8;

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

  //---------------------------------------------------------------------------
  // Inflate specific.
  //---------------------------------------------------------------------------

  private static final byte BTYPE_NO_COMPRESSION = 0x00;
  private static final byte BTYPE_STATIC_HUFFMAN = 0x01;
  private static final byte BTYPE_DYNAMIC_HUFFMAN = 0x02;
  private static final byte BTYPE_RESERVED = 0x03;

  private static final int DEFAULT_WINDOW_BITS = 15;

  private static void inflateRawBlock(ZStream in, ZStream out)
      throws IOException {
    int len = in.readLittleEndian(2);
    int nlen = in.readLittleEndian(2);
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

  private static void inflateHuffman(ZStream in, ZStream out, int[] litLenTree,
      int[] distTree) throws IOException {
    int litLenCode = 0;
    while ((litLenCode = Huffman.decodeSymbol(in, litLenTree)) != Huffman.END_OF_BLOCK_CODE) {
      if (litLenCode < Huffman.END_OF_BLOCK_CODE) {
        out.write(litLenCode);
      } else {
        litLenCode -= (Huffman.END_OF_BLOCK_CODE + 1);
        int length = Huffman.literalLength(litLenCode, in);
        int distCode = Huffman.decodeSymbol(in, distTree);
        int distance = Huffman.literalDistance(distCode, in);
        out.copyFromEnd(distance, length);
      }
    }
  }

  private static void inflateDynamicHuffman(ZStream in, ZStream out)
      throws IOException {
    int hlit = in.readBits(5) + 257;
    int hdist = in.readBits(5) + 1;
    int hclen = in.readBits(4) + 4;
    char[] h2CodeLen = new char[19];
    for (int i = 0; i < hclen; i++) {
      h2CodeLen[Huffman.HUFF2PERM[i]] = (char) in.readBits(3);
    }
    int[] hcTree = Huffman.buildCodeTree(7, h2CodeLen);
    char[] litCodeLen = Huffman.readLengths(in, hcTree, hlit);
    char[] distCodeLen = Huffman.readLengths(in, hcTree, hdist);
    int[] litLenTree = Huffman.buildCodeTree(15, litCodeLen);
    int[] distTree = Huffman.buildCodeTree(15, distCodeLen);
    inflateHuffman(in, out, litLenTree, distTree);
  }

  private static int inflate(ZStream in, ZStream out) throws IOException {
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
        inflateHuffman(in, out, Huffman.FIXED_LITERALS_TREE,
            Huffman.FIXED_ALPHABET_LENGTHS_TREE);
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
    ZStream outStream = new ZStream(out, true, DEFAULT_WINDOW_BITS);
    return inflate(new ZStream(in, false, 0), outStream);
  }

  //---------------------------------------------------------------------------
  // Gunzip specific.
  //---------------------------------------------------------------------------

  private static Gzip readHeader(ZStream in) throws IOException {
    Gzip gzip = new Gzip();
    in.resetCrc();
    if (in.readLittleEndian(2) != GZIP_MAGIC_NUMBER) {
      throw new IOException("Bad magic number");
    }
    if (in.readLittleEndian(1) != CM_DEFLATE) {
      throw new IOException("Unsupported compression method");
    }
    int flg = in.readLittleEndian(1);
    // mtime=4, xfl=1, os=1
    in.skipBytes(6);
    if ((flg & FEXTRA) != 0) {
      int xlen = in.readLittleEndian(2);
      while (xlen-- > 0) {
        in.read();
      }
    }
    if ((flg & FNAME) != 0) {
      gzip.filename = in.readZeroTerminatedString();
    }
    if ((flg & FCOMMENT) != 0) {
      gzip.comment = in.readZeroTerminatedString();
    }
    if ((flg & FHCRC) != 0) {
      int headerCrc16 = in.getCrc() & 0xFFFF;
      int expectedHeaderCrc16 = in.readLittleEndian(2);
      if (expectedHeaderCrc16 != headerCrc16) {
        throw new IOException("Header CRC check failed.");
      }
    }
    in.setKeepCrc(false);
    return gzip;
  }

  private static void readFooter(ZStream in, ZStream out) throws IOException {
    // Read footer.
    if (out.getCrc() != in.readLittleEndian(4)) {
      throw new IOException("CRC check failed.");
    }
    if ((out.getOutputSize() & 0xFFFFFFFF) != in.readLittleEndian(4)) {
      throw new IOException("Size mismatches.");
    }
  }

  private static Gzip gunzip(ZStream in, ZStream out) throws IOException {
    Gzip gzip = readHeader(in);
    out.resetCrc();
    inflate(in, out);
    readFooter(in, out);
    return gzip;
  }

  public static Gzip gunzip(InputStream in, OutputStream out)
      throws IOException {
    return gunzip(new ZStream(in, false, 0), new ZStream(in, true,
        DEFAULT_WINDOW_BITS));
  }
}
