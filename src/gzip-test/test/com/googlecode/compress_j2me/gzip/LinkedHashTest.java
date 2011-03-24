// GZIP implementation for J2ME
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

package com.googlecode.compress_j2me.gzip;

import org.junit.Assert;
import org.junit.Test;

public class LinkedHashTest extends UnitTest {

  @Test
  public void testCtor() {
    LinkedHash hash = new LinkedHash(Gzip.DEFLATE_HASH_SIZE);
    Assert.assertEquals(0, hash.size());
  }

  private void populate(LinkedHash hash) {
    for (int i = 1; i < 10; i++) {
      Assert.assertEquals(i, hash.put(0x00, (byte) i, 100 + i));
      Assert.assertEquals(0xAB00 | i, hash.put(0xAB, (byte) i, 200 + i));
      Assert.assertEquals(0xABCD00 | i, hash.put(0xABCD, (byte) i, 300 + i));
    }
  }

  @Test
  public void testReset() {
    LinkedHash hash = new LinkedHash(Gzip.DEFLATE_HASH_SIZE);
    Assert.assertEquals(0, hash.size());
    Assert.assertEquals(0xABCD05, hash.put(0xABCD, (byte) 0x05, 13));
    Assert.assertEquals(1, hash.size());
    Assert.assertEquals(13, hash.get(h2b("ABCD05"), 0, 3));
    hash.reset();
    Assert.assertEquals(-1, hash.get(h2b("ABCD05"), 0, 3));
  }

  @Test
  public void testPut() {
    LinkedHash hash = new LinkedHash(Gzip.DEFLATE_HASH_SIZE);
    int k = 0;
    for (int i = 0; i < 20; i++) {
      Assert.assertEquals(i, hash.put(0, (byte) i, -1));
      Assert.assertEquals(0, hash.size());
    }
    for (int i = 0; i < 20; i++) {
      Assert.assertEquals(i, hash.put(0, (byte) i, 0));
      Assert.assertEquals(++k, hash.size());
    }
    for (int i = 0; i < 20; i++) {
      Assert.assertEquals(i, hash.put(0, (byte) i, 0));
      Assert.assertEquals(20, hash.size());
      Assert.assertEquals(i, hash.put(0, (byte) i, 3));
      Assert.assertEquals(20, hash.size());
    }
    for (int i = 0; i < 20; i++) {
      Assert.assertEquals(0xAB00 | i, hash.put(0xAB, (byte) i, 0));
      Assert.assertEquals(++k, hash.size());
      Assert.assertEquals(0xABCD00 | i, hash.put(0xABCD, (byte) i, 0));
      Assert.assertEquals(++k, hash.size());
      //Assert.assertEquals(0xABCDEF00 | i, hash.put(0xABCDEF, (byte) i, 0));
      //Assert.assertEquals(++k, hash.size());
    }
  }

  @Test
  public void testGet() {
    LinkedHash hash = new LinkedHash(Gzip.DEFLATE_HASH_SIZE);
    populate(hash);

    Assert.assertEquals(-1, hash.get(h2b("05"), 0, 1));
    Assert.assertEquals(-1, hash.get(h2b("0005"), 1, 1));
    Assert.assertEquals(-1, hash.get(h2b("0005"), 0, 2));

    Assert.assertEquals(-1, hash.get(h2b("AB05"), 0, 2));
    Assert.assertEquals(-1, hash.get(h2b("00AB05"), 1, 2));
    Assert.assertEquals(-1, hash.get(h2b("AB0005"), 0, 3));

    Assert.assertEquals(305, hash.get(h2b("ABCD05"), 0, 3));
    Assert.assertEquals(305, hash.get(h2b("ABCD0500"), 0, 3));
    Assert.assertEquals(305, hash.get(h2b("00ABCD05"), 1, 3));
    Assert.assertEquals(305, hash.get(h2b("00ABCD0500"), 1, 3));
    Assert.assertEquals(-1, hash.get(h2b("ABCD0005"), 0, 3));
    Assert.assertEquals(-1, hash.get(h2b("AB00CD05"), 0, 3));
    Assert.assertEquals(-1, hash.get(h2b("ABCD0005"), 0, 4));
    Assert.assertEquals(-1, hash.get(h2b("AB00CD05"), 0, 4));
  }

}
