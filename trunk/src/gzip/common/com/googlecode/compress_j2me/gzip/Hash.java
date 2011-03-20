package com.googlecode.compress_j2me.gzip;

class Hash {

  private static final int HASH_PRIME = 31;

  private short[] entries;
  private short[] parent;
  private byte[] data;
  private int[] dataOffset;
  private int size;

  public Hash(int size) {
    if (size > 0x8FFF) {
      throw new RuntimeException("invalid table size");
    }
    this.entries = new short[(int) (size * 1.33)];
    this.parent = new short[size];
    this.data = new byte[size];
    this.dataOffset = new int[size];
    reset();
  }

  public void reset() {
    this.size = 0;
    for (int i = 0; i < this.entries.length; i++) {
      this.entries[i] = -1;
    }
    for (int i = 0; i < this.parent.length; i++) {
      this.parent[i] = -1;
    }
    //    this.size = CLEAR_CODE + 1;
  }

  public int size() {
    return this.size;
  }

  private int calcHash(char prefixCode, byte ch) {
    int hash = (((0xFFFF & prefixCode) << 8) ^ (ch & 0xFF)) * HASH_PRIME;
    return Math.abs(hash) % this.entries.length;
  }

  private int find(int prefixCode, byte ch) {
    int hash = calcHash((char) prefixCode, ch);
    int q = 1;
    while (this.entries[hash] < 0) {
      int idx = this.entries[hash];
      if (this.parent[idx] == prefixCode && this.data[idx] == ch) {
        return hash;
      }
      hash = Math.abs(hash + q * q) % this.entries.length;
      q++;
    }
    return -1;
  }

  short put(short prefixCode, byte ch, int inputOffset) {
    int hash = find(prefixCode, ch);
    if (hash < 0 && this.size >= this.data.length) {
      // Table is full.
      return -1;
    }
    // New hash entry.
    this.entries[hash] = (short) this.size;
    // Data associated to entry.
    this.parent[this.size] = prefixCode;
    this.data[this.size] = ch;
    this.dataOffset[this.size] = inputOffset;
    this.size++;
    return this.entries[hash];
  }

  int get(byte[] buffer, int start, int length) {
    int offset = -1;
    short prefixCode = -1;
    for (int i = 0; i < length; i++) {
      int hash = find(prefixCode, buffer[start + i]);
      if (hash < 0) {
        return offset;
      }
      short idx = this.entries[hash];
      offset = this.dataOffset[idx];
      prefixCode = idx;
    }
    return offset;
  }
}
