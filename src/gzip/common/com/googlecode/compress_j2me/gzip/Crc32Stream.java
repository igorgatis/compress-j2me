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

class Crc32Stream {

  static final int[] crc_table;

  static {
    crc_table = new int[256];
    for (int n = 0; n < 256; n++) {
      int c = n;
      for (int k = 0; k < 8; k++) {
        if ((c & 0x01) != 0) {
          c = 0xEDB88320 ^ (c >>> 1);
        } else {
          c = c >>> 1;
        }
      }
      crc_table[n] = c;
    }
  }

  private InputStream in;
  private OutputStream out;

  private int rawCrc32 = 0 ^ 0xFFFFFFFF;

  private int bitBuffer;
  private int bitOffset;
  private int outputSize;

  Crc32Stream(InputStream input) {
    this.in = input;
  }

  Crc32Stream(OutputStream output) {
    this.out = output;
  }

  void resetCrc32() {
    this.rawCrc32 = 0 ^ 0xFFFFFFFF;
  }

  int getCrc32() {
    return (int) (this.rawCrc32 ^ 0xFFFFFFFF);
  }

  private void updateCrc(byte ch) {
    int c = this.rawCrc32;
    c = crc_table[(c & 0xFF) ^ (0xFF & ch)] ^ (c >>> 8);
    this.rawCrc32 = c;
  }

  int read() throws IOException {
    int ch = this.in.read();
    if (ch >= 0) {
      updateCrc((byte) ch);
    }
    return ch;
  }

  int read(byte[] buffer, int start, int length) throws IOException {
    for (int i = 0; i < length; i++) {
      int ch = read();
      if (ch < 0) {
        return i;
      }
      buffer[start + i] = (byte) ch;
    }
    return length;
  }

  String readZeroTerminatedString() throws IOException {
    StringBuffer buffer = new StringBuffer(128);
    int ch;
    while ((ch = read()) > 0) {
      buffer.append((char) ch);
    }
    return buffer.toString();
  }

  private static void checkNoEOF(int ch) throws IOException {
    if (ch < 0) {
      throw new IOException("Unexpected EOF.");
    }
  }

  int readBytes(int n) throws IOException {
    int v = 0;
    for (int i = 0; i < n; i++) {
      int ch = read();
      checkNoEOF(ch);
      ch <<= i << 3;
      v |= ch;
    }
    return v;
  }

  int readBits(int numBits) throws IOException {
    while (this.bitOffset < numBits) {
      int tmp = read();
      if (tmp < 0) {
        checkNoEOF(tmp);
        //return -1;
      }
      this.bitBuffer |= tmp << this.bitOffset;
      this.bitOffset += 8;
    }
    int mask = (1 << numBits) - 1;
    int code = this.bitBuffer & mask;
    this.bitBuffer >>>= numBits;
    this.bitOffset -= numBits;
    //System.err.println(numBits + ":" + code);
    return code;
  }

  //  public int size() {
  //    return this.outputSize;
  //  }

  //  public void writeCode(int code, int numBits) throws IOException {
  //    if (this.out == null) {
  //      throw new IOException("Not a output stream");
  //    }
  //    //System.err.println(numBits + ":" + code);
  //    int mask = (1 << numBits) - 1;
  //    this.bitBuffer |= (mask & code) << this.bitOffset;
  //    this.bitOffset += numBits;
  //
  //    while (this.bitOffset > 8) {
  //      this.out.write((byte) this.bitBuffer);
  //      this.outputSize++;
  //      this.bitBuffer >>>= 8;
  //      this.bitOffset -= 8;
  //    }
  //  }
  //
  //  public void end() throws IOException {
  //    if (this.out == null) {
  //      throw new IOException("Not a output stream");
  //    }
  //    while (this.bitOffset > 0) {
  //      this.out.write((byte) this.bitBuffer);
  //      this.outputSize++;
  //      this.bitBuffer >>>= 8;
  //      this.bitOffset -= 8;
  //    }
  //    this.out.flush();
  //  }
}
