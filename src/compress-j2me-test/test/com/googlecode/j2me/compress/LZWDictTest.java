package com.googlecode.j2me.compress;


import org.junit.Assert;
import org.junit.Test;

import com.googlecode.j2me.compress.ByteBuffer;
import com.googlecode.j2me.compress.LZWDict;
import com.googlecode.j2me.compress.LZWHash;

public class LZWDictTest {

  @Test
  public void testCtor() {
    LZWDict dict = new LZWDict(1 << 16);
    Assert.assertEquals(257, dict.size());
    ByteBuffer buffer = new ByteBuffer(128);
    for (int i = 0; i < 256; i++) {
      dict.get((char) i, buffer);
      Assert.assertEquals(1, buffer.size());
      Assert.assertEquals((byte) i, buffer.rawBuffer()[0]);
    }
  }

  @Test
  public void testCodeEntries() {
    LZWDict dict = new LZWDict(1 << 16);
    ByteBuffer buffer = new ByteBuffer(128);
    int kMaxSize = 65536;
    int code = LZWHash.CLEAR_CODE + 1;
    for (int p = 0; p < 256 && dict.size() < kMaxSize; p++) {
      for (int i = 0; i < 256 && dict.size() < kMaxSize; i++) {
        char newCode = dict.put((char) p, (byte) i);
        Assert.assertEquals(code, newCode);
        dict.get(newCode, buffer);
        if (buffer.size() != 2) {
          dict.get(newCode, buffer);
          System.out.println();
        }
        Assert.assertEquals(2, buffer.size());
        code++;
      }
    }
    Assert.assertEquals(kMaxSize, dict.size());
    ArrayIndexOutOfBoundsException exc = null;
    try {
      dict.put((char) 258, (byte) 0);
    } catch (ArrayIndexOutOfBoundsException e) {
      exc = e;
    }
    Assert.assertNotNull(exc);
  }

  @Test
  public void testReset() {
    LZWDict dict = new LZWDict(1 << 16);
    Assert.assertEquals(257, dict.size());
    ByteBuffer buffer = new ByteBuffer(128);
    for (int i = 0; i < 256; i++) {
      dict.get((char) i, buffer);
      Assert.assertEquals(1, buffer.size());
      Assert.assertEquals((byte) i, buffer.rawBuffer()[0]);
    }
    char code = dict.put((char) 12, (byte) 13);
    Assert.assertEquals(258, dict.size());
    dict.get(code, buffer);
    Assert.assertEquals(2, buffer.size());
    Assert.assertEquals((byte) 12, buffer.rawBuffer()[0]);
    Assert.assertEquals((byte) 13, buffer.rawBuffer()[1]);
    dict.reset();
    Assert.assertEquals(257, dict.size());
    for (int i = 0; i < 256; i++) {
      dict.get((char) i, buffer);
      Assert.assertEquals(1, buffer.size());
      Assert.assertEquals((byte) i, buffer.rawBuffer()[0]);
    }
  }
}
