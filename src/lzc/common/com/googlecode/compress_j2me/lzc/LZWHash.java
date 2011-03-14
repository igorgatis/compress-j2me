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

class LZWHash {

  public static final int COMPRESS_MAGIC_NUMBER = 0x1F9D;
  public static final int BLOCK_MODE_MASK = 0x80;
  public static final int MAX_MASK_SIZE_MASK = 0x1F;

  public static final int MAX_MASK_SIZE = 16;
  public static final int INITIAL_MASK_SIZE = 9;
  public static final char CLEAR_CODE = 256;

  private static final int HASH_PRIME = 31;

  private char[] entries;
  private boolean[] entriesFree;
  private char[] parent;
  private byte[] data;
  private int size;

  public LZWHash(int size) {
    this.entries = new char[(int) (size * 1.33)];
    this.entriesFree = new boolean[this.entries.length];
    this.parent = new char[size];
    this.data = new byte[size];
    reset();
  }

  public void reset() {
    this.size = 0;
    for (int i = 0; i < this.entriesFree.length; i++) {
      this.entriesFree[i] = true;
    }
    this.size = CLEAR_CODE + 1;
  }

  public int size() {
    return this.size;
  }

  private int calcHash(char prefixCode, byte ch) {
    int hash = (((0xFFFF & prefixCode) << 8) ^ (ch & 0xFF)) * HASH_PRIME;
    return Math.abs(hash) % this.entries.length;
  }

  public int putOrGet(int prefixCode, byte ch) {
    if (prefixCode < 0) {
      return 0xFF & ch;
    }
    int hash = calcHash((char) prefixCode, ch);
    int q = 1;
    while (!this.entriesFree[hash]) {
      char idx = this.entries[hash];
      if (this.parent[idx] == prefixCode && this.data[idx] == ch) {
        return idx;
      }
      hash = Math.abs(hash + q * q) % this.entries.length;
      q++;
    }
    if (this.size >= this.data.length) {
      return -2;
    }
    this.entries[hash] = (char) this.size; // new code.
    this.entriesFree[hash] = false;
    this.parent[this.size] = (char) prefixCode;
    this.data[this.size] = ch;
    this.size++;
    return -1; // new word added.
  }
}
