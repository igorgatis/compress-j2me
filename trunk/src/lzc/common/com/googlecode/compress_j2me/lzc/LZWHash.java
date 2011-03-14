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
