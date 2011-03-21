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

class LZCDict {

  private byte[] data;
  private char[] parent;
  private int resetSize;
  private int size;

  public LZCDict(int dicSize) {
    this.data = new byte[dicSize];
    this.parent = new char[dicSize];
    this.size = 0;
    for (int i = 0; i < 256; i++) {
      this.data[i] = (byte) i;
    }
    this.size = LZCHash.CLEAR_CODE + 1;
    this.resetSize = this.size;
  }

  public void reset() {
    this.size = this.resetSize;
  }

  public int size() {
    return this.size;
  }

  public char put(char prefixCode, byte ch) {
    if (prefixCode >= this.size) {
      throw new ArrayIndexOutOfBoundsException(Integer.toString(prefixCode));
    }
    this.data[this.size] = ch;
    this.parent[this.size] = prefixCode;
    this.size++;
    return (char) (this.size - 1);
  }

  public void get(char code, ByteBuffer buffer) {
    if (code >= this.size) {
      throw new ArrayIndexOutOfBoundsException(Integer.toString(code));
    }
    buffer.reset();
    // CLEAR_CODE delimits start of composite strings.
    while (code > LZCHash.CLEAR_CODE) {
      buffer.append(this.data[code]);
      code = this.parent[code];
    }
    buffer.append(this.data[code]);
    buffer.reverse();
  }
}
