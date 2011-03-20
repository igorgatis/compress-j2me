// Gzip implementation for J2ME
// Copyright 2011 Igor Gatis  All rights reserved.
// http://code.google.com/p/compress-j2me/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//     * Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//
//     * Neither the name of Google Inc. nor the names of its contributors may
//       be used to endorse or promote products derived from this software
//       without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Gzip {

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

  private static final int INFLATE_WINDOW_BITS = 15;

  static final int DEFLATE_HASH_SIZE = 1 << 11;
  private static final int DEFLATE_WINDOW_BITS = 11;
  static final int MAX_DEFLATE_DISTANCE = 1 << DEFLATE_WINDOW_BITS;
  static final int MAX_DEFLATE_LENGTH = 256;

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
        if (distTree == null) {
          throw new IOException("no distance tree");
        }
        int length = Huffman.decodeLength(litLenCode, in);
        int distCode = Huffman.decodeSymbol(in, distTree);
        int distance = Huffman.decodeDistance(distCode, in);
        out.copyFromEnd(distance, length);
      }
    }
  }

  private static void inflateDynamicHuffman(ZStream in, ZStream out)
      throws IOException {
    int hlit = in.readBits(5) + 257;
    int hdist = in.readBits(5) + 1;
    int hclen = in.readBits(4) + 4;

    // Build tree which will be used to decode lengths for nodes of
    // literal/length and distance trees.
    // Will read 4-19 items. Each item ranges from 0 to 7 (3 bits).
    char[] hcLengths = new char[19];
    for (int i = 0; i < hclen; i++) {
      hcLengths[Huffman.HC_PERM[i]] = (char) in.readBits(3);
    }
    int[] hcTree = Huffman.buildCodeTree(7, hcLengths);

    char[] litCodeLens = Huffman.readLengths(in, hcTree, hlit);
    int[] litLenTree = Huffman.buildCodeTree(15, litCodeLens);

    char[] distCodeLens = Huffman.readLengths(in, hcTree, hdist);
    int[] distTree = null;
    // Check corner case where there are only literals and no lengths.
    if (distCodeLens.length != 1 || distCodeLens[0] != 0) {
      distTree = Huffman.buildCodeTree(15, distCodeLens);
    }
    inflateHuffman(in, out, litLenTree, distTree);
  }

  private static long inflate(ZStream in, ZStream out) throws IOException {
    boolean finalBlock = false;
    do {
      finalBlock = in.readBits(1) != 0;
      int blockType = in.readBits(2);
      switch (blockType) {
      case BTYPE_NO_COMPRESSION:
        in.alignBytes(); // Discard the rest of header.
        inflateRawBlock(in, out);
        break;
      case BTYPE_STATIC_HUFFMAN:
        inflateHuffman(in, out, Huffman.CANONICAL_LITLENS_TREE,
            Huffman.CANONICAL_DISTANCES_TREE);
        break;
      case BTYPE_DYNAMIC_HUFFMAN:
        inflateDynamicHuffman(in, out);
        break;
      default:
      case BTYPE_RESERVED:
        throw new IOException("Invalid block.");
      }
    } while (!finalBlock);
    in.alignBytes();
    return out.getOutputSize();
  }

  public static long inflate(InputStream in, OutputStream out)
      throws IOException {
    ZStream outStream = new ZStream(out, true, INFLATE_WINDOW_BITS);
    return inflate(new ZStream(in, false, 0), outStream);
  }

  private static long simpleDeflate(ZStream in, ZStream out) throws IOException {
    out.writeBits(1, 1); // This is a final block.
    out.writeBits(BTYPE_STATIC_HUFFMAN, 2);

    Hash hash = new Hash(1 << DEFLATE_HASH_SIZE);
    byte[] buffer = new byte[MAX_DEFLATE_LENGTH];
    int inputOffset = 0;
    int marker = -1;
    int length = 0;
    int bufferSize;
    short prefixCode = -1;
    while ((bufferSize = in.read(buffer, 0, buffer.length)) > 0) {
      for (int i = 0; i < bufferSize; i++, inputOffset++) {
        int ch = 0xFF & buffer[i];
        int distance = (int) (inputOffset - marker);
        if (marker >= 0 && distance < MAX_DEFLATE_DISTANCE && //
            length < MAX_DEFLATE_LENGTH && //
            ch == in.byteAt(distance + length)) {
          length++;
          if (length < 3) {
            prefixCode = hash.put(prefixCode, (byte) ch, inputOffset);
          }
        } else {
          if (length > 1) {
            Huffman.encodeLength(length, out);
            Huffman.encodeDistance(distance, out);
          } else {
            Huffman.encodeLiteral(ch, out);
          }
          marker = hash.get(buffer, i + 1, bufferSize - i - 1);
          prefixCode = -1;
          length = 0;
        }
      }
    }
    return out.getOutputSize();
  }

  public static long deflate(InputStream in, OutputStream out)
      throws IOException {
    ZStream inStream = new ZStream(in, true, INFLATE_WINDOW_BITS);
    return simpleDeflate(inStream, new ZStream(out, false, 0));
  }

  //---------------------------------------------------------------------------
  // Gunzip specific.
  //---------------------------------------------------------------------------

  private static Gzip readHeader(ZStream in) throws IOException {
    Gzip gzip = new Gzip();
    in.resetCrc();
    if (in.readLittleEndian(2) != ZStream.GZIP_MAGIC_NUMBER) {
      throw new IOException("Bad magic number");
    }
    if (in.readLittleEndian(1) != ZStream.CM_DEFLATE) {
      throw new IOException("Unsupported compression method");
    }
    int flg = in.readLittleEndian(1);
    // mtime=4, xfl=1, os=1
    in.skipBytes(6);
    if ((flg & ZStream.FEXTRA) != 0) {
      int xlen = in.readLittleEndian(2);
      while (xlen-- > 0) {
        in.read();
      }
    }
    if ((flg & ZStream.FNAME) != 0) {
      gzip.filename = in.readZeroTerminatedString();
    }
    if ((flg & ZStream.FCOMMENT) != 0) {
      gzip.comment = in.readZeroTerminatedString();
    }
    if ((flg & ZStream.FHCRC) != 0) {
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
    int actualCrc = out.getCrc();
    int expectedCrc = in.readLittleEndian(4);
    if (expectedCrc != actualCrc) {
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
    return gunzip(new ZStream(in, false, 0), new ZStream(out, true,
        INFLATE_WINDOW_BITS));
  }
}
