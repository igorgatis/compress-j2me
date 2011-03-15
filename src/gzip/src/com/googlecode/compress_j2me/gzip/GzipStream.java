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

public class GzipStream {

  InputStream in;
  OutputStream out;

  private int buffer;
  private int offset;
  private int size;

  public GzipStream(InputStream input) {
    this.in = input;
  }

  public GzipStream(OutputStream output) {
    this.out = output;
  }

  public int size() {
    return this.size;
  }

  public int readCode(int numBits) throws IOException {
    if (this.in == null) {
      throw new IOException("Not a input stream");
    }
    while (this.offset < numBits) {
      int tmp = this.in.read();
      if (tmp < 0) {
        return -1;
      }
      this.buffer |= tmp << this.offset;
      this.offset += 8;
    }
    int mask = (1 << numBits) - 1;
    int code = this.buffer & mask;
    this.buffer >>>= numBits;
    this.offset -= numBits;
    //System.err.println(numBits + ":" + code);
    return code;
  }

  public void writeCode(int code, int numBits) throws IOException {
    if (this.out == null) {
      throw new IOException("Not a output stream");
    }
    //System.err.println(numBits + ":" + code);
    int mask = (1 << numBits) - 1;
    this.buffer |= (mask & code) << this.offset;
    this.offset += numBits;

    while (this.offset > 8) {
      this.out.write((byte) this.buffer);
      this.size++;
      this.buffer >>>= 8;
      this.offset -= 8;
    }
  }

  public void end() throws IOException {
    if (this.out == null) {
      throw new IOException("Not a output stream");
    }
    while (this.offset > 0) {
      this.out.write((byte) this.buffer);
      this.size++;
      this.buffer >>>= 8;
      this.offset -= 8;
    }
    this.out.flush();
  }

  public static int compress(InputStream in, OutputStream out)
      throws IOException {
    //    GzipStream lzwOut = new GzipStream(out);
    //
    //    LZWHash hash = new LZWHash(1 << LZWHash.MAX_MASK_SIZE);
    //    int w_code = -1;
    //
    //    // LZC header.
    //    lzwOut.writeCode(0x1F, 8);
    //    lzwOut.writeCode(0x9D, 8);
    //    // block_mode=true, mask_size=LZWHash.MAX_MASK_SIZE
    //    lzwOut.writeCode(LZWHash.BLOCK_MODE_MASK | (0x1F & LZWHash.MAX_MASK_SIZE),
    //        8);
    //
    //    int mask_size = LZWHash.INITIAL_MASK_SIZE;
    //    int k;
    //    while ((k = in.read()) >= 0) {
    //      // Returns code of w+k if present in dictionary.
    //      // Otherwise, add w+k and returns -1.
    //      int wk_code = hash.putOrGet(w_code, (byte) k);
    //      if (wk_code >= 0) {
    //        w_code = (char) wk_code;
    //      } else {
    //        lzwOut.writeCode(w_code, mask_size);
    //        // Increasing mask size if possible.
    //        if (wk_code != -2 && hash.size() > (1 << mask_size)) {
    //          mask_size++;
    //        }
    //        w_code = (char) k;
    //      }
    //      // Flush whenever hash is full. Unix compress would observe compression
    //      // rate in order to decide when to flush - this is intentionally left
    //      // unimplemented to reduce code size (and complexity).
    //      if (w_code < LZWHash.CLEAR_CODE
    //          && hash.size() >= (1 << LZWHash.MAX_MASK_SIZE)) {
    //        lzwOut.writeCode(LZWHash.CLEAR_CODE, mask_size);
    //        hash.reset();
    //        mask_size = LZWHash.INITIAL_MASK_SIZE;
    //      }
    //    }
    //    if (w_code >= 0) {
    //      lzwOut.writeCode(w_code, mask_size);
    //    }
    //    lzwOut.end();
    //    out.flush();
    //    return lzwOut.size();
    return -1;
  }

  public static final int MAGIC_NUMBER = 0x1F8B;

  private static final byte FTEXT_FLAG = 0x01;
  private static final byte FHCRC_FLAG = 0x02;
  private static final byte FEXTRA_FLAG = 0x04;
  private static final byte FNAME_FLAG = 0x08;
  private static final byte FCOMMENT_FLAG = 0x10;
  private static final byte FRESERVED_FLAG = (byte) 0xE0;
  private static final byte CM_DEFLATE = 8;

  private static int read32(GzipStream gzipIn) throws IOException {
    return gzipIn.in.read() << 24 | gzipIn.in.read() << 16
        | gzipIn.in.read() << 8 | gzipIn.in.read();
  }

  public static int uncompress(InputStream in, OutputStream out)
      throws IOException {
    GzipStream gzipIn = new GzipStream(in);

    int magic = gzipIn.in.read() << 8 | gzipIn.in.read();
    if (magic != MAGIC_NUMBER) {
      throw new RuntimeException("Bad magic number " + magic);
    }
    int cm = gzipIn.in.read();
    if (cm != CM_DEFLATE) {
      throw new RuntimeException("Unsupported CM=" + cm);
    }
    int flg = gzipIn.in.read();
    int mtime = read32(gzipIn);
    int xfl = gzipIn.in.read();
    int os = gzipIn.in.read();

    if ((flg & FEXTRA_FLAG) != 0) {
      //readExtraFields();
    }
    if ((flg & FNAME_FLAG) != 0) {
      //readZeroTerminatedString(); // filename
    }
    if ((flg & FCOMMENT_FLAG) != 0) {
      //readZeroTerminatedString(); // filename
    }
    if ((flg & FHCRC_FLAG) != 0) {
      int crc = gzipIn.in.read() << 8 | gzipIn.in.read();
    }
    // Read compressed blocks.
    int crc32 = read32(gzipIn);
    int isize = read32(gzipIn);
    return -1;
  }
}
