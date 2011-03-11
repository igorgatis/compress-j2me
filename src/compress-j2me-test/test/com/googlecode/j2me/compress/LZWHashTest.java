package com.googlecode.j2me.compress;


import org.junit.Assert;
import org.junit.Test;

import com.googlecode.j2me.compress.LZWHash;

public class LZWHashTest {

  @Test
  public void testCtor() {
    LZWHash hash = new LZWHash(1 << 16);
    Assert.assertEquals(257, hash.size());
    for (int i = 0; i < 256; i++) {
      Assert.assertEquals(i, hash.putOrGet(-1, (byte) i));
    }
  }

  @Test
  public void testPut() {
    LZWHash hash = new LZWHash(1 << 16);
    int kMaxSize = 65536;
    int code = LZWHash.CLEAR_CODE + 1;
    for (int p = 0; p < 256 && hash.size() < kMaxSize; p++) {
      for (int i = 0; i < 256 && hash.size() < kMaxSize; i++) {
        Assert.assertEquals(-1, hash.putOrGet((char) p, (byte) i));
        Assert.assertEquals(code, hash.putOrGet((char) p, (byte) i));
        code++;
      }
    }
    Assert.assertEquals(kMaxSize, hash.size());
    Assert.assertEquals(-2, hash.putOrGet((char) 258, (byte) 0));
  }

  @Test
  public void testReset() {
    LZWHash hash = new LZWHash(1 << 16);
    Assert.assertEquals(257, hash.size());
    for (int i = 0; i < 256; i++) {
      Assert.assertEquals(i, hash.putOrGet(-1, (byte) i));
    }
    Assert.assertEquals(-1, hash.putOrGet((char) 0, (byte) 0));
    Assert.assertEquals(258, hash.size());
    Assert.assertEquals(LZWHash.CLEAR_CODE + 1,
        hash.putOrGet((char) 0, (byte) 0));
    hash.reset();
    Assert.assertEquals(257, hash.size());
    for (int i = 0; i < 256; i++) {
      Assert.assertEquals(i, hash.putOrGet(-1, (byte) i));
    }
  }
}
