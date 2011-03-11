package com.googlecode.j2me.compress;

class LZWDict {

  private byte[] data;
  private char[] parent;
  private int resetSize;
  private int size;

  public LZWDict(int dicSize) {
    this.data = new byte[dicSize];
    this.parent = new char[dicSize];
    this.size = 0;
    for (int i = 0; i < 256; i++) {
      this.data[i] = (byte) i;
    }
    this.size = LZWHash.CLEAR_CODE + 1;
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
    while (code > LZWHash.CLEAR_CODE) {
      buffer.append(this.data[code]);
      code = this.parent[code];
    }
    buffer.append(this.data[code]);
    buffer.reverse();
  }
}
