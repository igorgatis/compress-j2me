package com.googlecode.compress_j2me.lzc;

import static com.googlecode.compress_j2me.lzc.TestUtil.h2in;

import com.googlecode.j2me.compress.LZWStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Assert;


import org.junit.Test;

public class LZWStreamTest {

  private static byte[] toByteStream(int x, int bits) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (; bits > 0; bits -= 8, x >>>= 8) {
      baos.write((byte) x);
    }
    return baos.toByteArray();
  }

  private void checkReadBits(byte[] data, int bits, int value, int step)
      throws IOException {
    LZWStream stream = new LZWStream(new ByteArrayInputStream(data));
    for (int j = 0, x = value; j + step < bits; j += step, x >>>= step) {
      int mask = (1 << step) - 1;
      int expected = x & mask;
      int actual = stream.readCode(step);
      if (expected != actual) {
        Assert.assertEquals(expected, actual);
      }
    }
  }

  @Test
  public void test_readCode() throws IOException {
    LZWStream stream = new LZWStream(h2in("5A"));
    Assert.assertEquals(0x5A, stream.readCode(8));
    stream = new LZWStream(h2in("BADC"));
    Assert.assertEquals(0xA, stream.readCode(4));
    Assert.assertEquals(0xB, stream.readCode(4));
    Assert.assertEquals(0xC, stream.readCode(4));
    Assert.assertEquals(0xD, stream.readCode(4));
    stream = new LZWStream(h2in("01"));
    Assert.assertEquals(0x01, stream.readCode(1));
    stream = new LZWStream(h2in("0B"));
    Assert.assertEquals(0x01, stream.readCode(1));
    Assert.assertEquals(0x01, stream.readCode(1));
    Assert.assertEquals(0x00, stream.readCode(1));
    Assert.assertEquals(0x01, stream.readCode(1));
    stream = new LZWStream(h2in("0B"));
    Assert.assertEquals(0x03, stream.readCode(2));
    Assert.assertEquals(0x02, stream.readCode(2));

    for (int value = 0, bits = 8; value < (1 << bits); value++) {
      byte[] data = toByteStream(value, bits);
      for (int steps = 1; steps <= bits; steps++) {
        checkReadBits(data, bits, value, steps);
      }
    }
    for (int value = 0, bits = 16; value < (1 << bits); value++) {
      byte[] data = toByteStream(value, bits);
      for (int steps = 1; steps <= bits; steps++) {
        checkReadBits(data, bits, value, steps);
      }
    }
    for (int value = 0, bits = 24; value < 250000; value++) {
      byte[] data = toByteStream(value, bits);
      for (int steps = 1; steps <= bits; steps++) {
        checkReadBits(data, bits, value, steps);
      }
    }
  }
}
