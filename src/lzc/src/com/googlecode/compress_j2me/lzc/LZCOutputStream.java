// LZC implementation for J2ME
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

package com.googlecode.compress_j2me.lzc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LZCOutputStream extends OutputStream {

  private OutputStream out;

  // Bit stream fields.
  private int buffer;
  private int offset;
  private int size;

  // LZW stream fields.
  private LZCHash hash;
  private int w_code;
  private int mask_size;

  public LZCOutputStream(OutputStream output) {
    this.out = output;
    w_code = -1;
    mask_size = LZCHash.INITIAL_MASK_SIZE;
  }

  public void setNoHeader() {
    hash = new LZCHash(1 << LZCHash.MAX_MASK_SIZE);
  }

  private void writeBuffer(int left) throws IOException {
    while (this.offset > left) {
      this.out.write((byte) this.buffer);
      this.size++;
      this.buffer >>>= 8;
      this.offset -= 8;
    }
  }

  private void writeCode(int code, int numBits) throws IOException {
    int mask = (1 << numBits) - 1;
    this.buffer |= (mask & code) << this.offset;
    this.offset += numBits;
    writeBuffer(8);
  }

  private void writeHeader() throws IOException {
    this.out.write((byte) 0x1F);
    this.out.write((byte) 0x9D);
    // block_mode=true, mask_size=LZWHash.MAX_MASK_SIZE
    int flags = LZCHash.BLOCK_MODE_MASK | (0x1F & LZCHash.MAX_MASK_SIZE);
    this.out.write((byte) flags);
  }

  private void compress(int k) throws IOException {
    // Returns code of w+k if present in dictionary.
    // Otherwise, add w+k and returns -1.
    int wk_code = hash.putOrGet(w_code, (byte) k);
    if (wk_code >= 0) {
      w_code = (char) wk_code;
    } else {
      writeCode(w_code, mask_size);
      // Increasing mask size if possible.
      if (wk_code != -2 && hash.size() > (1 << mask_size)) {
        mask_size++;
      }
      w_code = (char) k;
    }
    // Flush whenever hash is full. Unix compress would observe compression
    // rate in order to decide when to flush - this is intentionally left
    // unimplemented to reduce code size (and complexity).
    if (w_code < LZCHash.CLEAR_CODE
        && hash.size() >= (1 << LZCHash.MAX_MASK_SIZE)) {
      writeCode(LZCHash.CLEAR_CODE, mask_size);
      hash.reset();
      mask_size = LZCHash.INITIAL_MASK_SIZE;
    }
  }

  public void end() throws IOException {
    if (hash == null) {
      writeHeader();
    }
    if (w_code >= 0) {
      writeCode(w_code, mask_size);
    }
    writeBuffer(0);
    this.out.flush();
  }

  public int size() {
    return size;
  }

  // -------------------------------------------------------------------------
  // OutputStream API.
  // -------------------------------------------------------------------------

  public void write(int b) throws IOException {
    if (hash == null) {
      writeHeader();
      hash = new LZCHash(1 << LZCHash.MAX_MASK_SIZE);
    }
    compress(b & 0xFF);
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void close() throws IOException {
    end();
    out.flush();
  }

  // -------------------------------------------------------------------------
  // Static API.
  // -------------------------------------------------------------------------

  public static void compress(InputStream in, OutputStream out)
      throws IOException {
    LZCOutputStream lzwOut = new LZCOutputStream(out);
    byte[] buffer = new byte[128];
    int bytesRead;
    while ((bytesRead = in.read(buffer)) >= 0) {
      lzwOut.write(buffer, 0, bytesRead);
    }
    lzwOut.flush();
    lzwOut.end();
  }
}
