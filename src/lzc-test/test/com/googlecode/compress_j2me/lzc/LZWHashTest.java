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
