package com.googlecode.compress_j2me.lzc;

// Byte version of StringBuffer.
class ByteBuffer {
  private byte[] content;
  private int length;

  public ByteBuffer(int size) {
    this.content = new byte[size];
  }

  public void reset() {
    this.length = 0;
  }

  public int size() {
    return this.length;
  }

  public byte[] rawBuffer() {
    return this.content;
  }

  public void append(byte ch) {
    if (this.length >= this.content.length) {
      byte[] oldBuffer = this.content;
      this.content = new byte[oldBuffer.length + 16];
      System.arraycopy(oldBuffer, 0, this.content, 0, length);
    }
    this.content[this.length++] = ch;
  }

  public void reverse() {
    for (int i = 0; i < this.length / 2; i++) {
      byte swp = this.content[i];
      this.content[i] = this.content[this.length - 1 - i];
      this.content[this.length - 1 - i] = swp;
    }
  }
}
