package com.googlecode.j2me.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;

public class AssertiveOutputStream extends OutputStream {
  private ByteArrayOutputStream baos;
  private byte[] expected;
  private int offset;

  public AssertiveOutputStream(byte[] expected) {
    this.baos = new ByteArrayOutputStream(expected.length);
    this.expected = expected;
  }

  @Override
  public void write(int b) throws IOException {
    byte expectedByte = this.expected[this.offset];
    if (expectedByte != b) {
      Assert.assertEquals(expectedByte, b);
    }
    this.offset++;
    this.baos.write(b);
  }

  public byte[] getBytes() {
    return this.baos.toByteArray();
  }

  public int expectedSize() {
    return this.expected.length;
  }

  public int size() {
    return this.baos.size();
  }
}