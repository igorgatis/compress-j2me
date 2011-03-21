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

import org.junit.Assert;
import org.junit.Test;

public class LZCDictTest {

  @Test
  public void testCtor() {
    LZCDict dict = new LZCDict(1 << 16);
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
    LZCDict dict = new LZCDict(1 << 16);
    ByteBuffer buffer = new ByteBuffer(128);
    int kMaxSize = 65536;
    int code = LZCHash.CLEAR_CODE + 1;
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
    LZCDict dict = new LZCDict(1 << 16);
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
