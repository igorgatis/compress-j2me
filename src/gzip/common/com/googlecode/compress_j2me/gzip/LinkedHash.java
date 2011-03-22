package com.googlecode.compress_j2me.gzip;

class LinkedHash {

  private static final int HASH_PRIME = 31;

  private short[] entries;
  private int[] keys;
  private int[] markers;
  private int size;

  LinkedHash(int size) {
    if (size > 0x8FFF) {
      throw new RuntimeException("invalid table size");
    }
    this.entries = new short[(int) (size * 1.333)];
    for (int i = 0; i < this.entries.length; i++) {
      this.entries[i] = -1;
    }
    this.keys = new int[size];
    this.markers = new int[size];
  }

  int size() {
    return this.size;
  }

  int newKey(int oldKey, byte lastByte) {
    int newKey = ((0x0000FFFF & oldKey) << 8) | (0xFF & lastByte);
    int newLen = Math.min(3, ((oldKey >>> 24) & 0x0F) + 1);
    newKey |= newLen << 24;
    return newKey;
  }

  int keyLen(int key) {
    return (key >>> 24) & 0x0F;
  }

  private int calcHash(int key) {
    int hash = HASH_PRIME + (key & 0xFF);
    hash = hash * HASH_PRIME + ((key >>> 8) & 0xFF);
    hash = hash * HASH_PRIME + ((key >>> 16) & 0xFF);
    hash = hash * HASH_PRIME + ((key >>> 24) & 0xFF);
    return Math.abs(hash) % this.entries.length;
  }

  private int find(int key) {
    int hash = calcHash(key);
    int q = 1;
    int idx;
    while ((idx = this.entries[hash]) >= 0) {
      if (this.keys[idx] == key) {
        return hash;
      }
      hash = Math.abs(hash + q * q) % this.entries.length;
      q++;
    }
    // Not present.
    return hash;
  }

  int put(int key, int marker) {
    if (this.size == this.markers.length) {
      // Table is full. Need to remove an item.
    }
    int hash = find(key);
    if (this.entries[hash] < 0) {
      this.entries[hash] = (short) this.size;
      this.keys[this.size] = key;
      this.markers[this.size] = marker;
      this.size++;
      return key;
    }
    return key;
  }

  private int toKey(byte[] buffer, int start, int length) {
    length = Math.min(3, length - start);
    int key = length << 24;
    switch (length) {
    case 3:
      key |= buffer[start] << 16;
    case 2:
      key |= buffer[start + 1] << 8;
    case 1:
      key |= buffer[start + 2];
    }
    return key;
  }

  int get(byte[] buffer, int start, int length) {
    int key = toKey(buffer, start, length);
    int hash = find(key);
    int idx = this.entries[hash];
    if (idx >= 0) {
      return this.markers[idx];
    }
    return -1;
  }
}
