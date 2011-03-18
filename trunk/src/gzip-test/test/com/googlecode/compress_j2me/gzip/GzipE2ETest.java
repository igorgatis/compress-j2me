// Gzip implementation for J2ME
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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class GzipE2ETest extends UnitTest {

  private AssertiveOutputStream baos;

  @Test
  public void testGunzipEmpty() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/empty.gz"), baos = h2out(""));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzipA() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/a.gz"), baos = s2out("a"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzipHelloWorldFile() throws IOException {
    Gzip gzip = Gzip
        .gunzip(file2in("samples/helloworld.gz"), baos = s2out("hello world"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals("helloworld.txt", gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  //  @Test
  //  public void test_a() throws IOException {
  //    GzipStream.compress(TestUtil.s2in("a"), baos = h2out("1F9D906100"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1F9D906100"), baos = s2out("a"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
  //
  //  @Test
  //  public void test_aa() throws IOException {
  //    GzipStream.compress(TestUtil.s2in("aa"), baos = h2out("1F9D9061C200"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1F9D9061C200"), baos = s2out("aa"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
  //
  //  @Test
  //  public void test_aaa() throws IOException {
  //    GzipStream.compress(TestUtil.s2in("aaa"), baos = h2out("1F9D90610202"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1F9D90610202"), baos = s2out("aaa"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
  //
  //  @Test
  //  public void test_aaaa() throws IOException {
  //    GzipStream.compress(TestUtil.s2in("aaaa"), baos = h2out("1F9D9061028601"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1F9D9061028601"), baos = s2out("aaaa"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
  //
  //  @Test
  //  public void test_aaaaa() throws IOException {
  //    GzipStream.compress(TestUtil.s2in("aaaaa"), baos = h2out("1F9D9061020604"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1F9D9061020604"), baos = s2out("aaaaa"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
  //
  //  @Test
  //  public void test_abcde() throws IOException {
  //    GzipStream.compress(TestUtil.s2in("abcde"),
  //        baos = h2out("1F9D9061C48C215306"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1F9D9061C48C215306"), baos = s2out("abcde"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
  //
  //  @Test
  //  public void test_JOEYNJOEYNJOEY() throws IOException {
  //    GzipStream.compress(TestUtil.s2in("JOEYNJOEYNJOEY"),
  //        baos = h2out("1f9d904a9e14c9e224e0c08202b300"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1f9d904a9e14c9e224e0c08202b300"),
  //        baos = s2out("JOEYNJOEYNJOEY"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
  //
  //  @Test
  //  public void test_WED() throws IOException {
  //    GzipStream.compress(file2in("samples/wed.txt"),
  //        baos = h2out("1f9d902fae142112b0484183028514a402"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //    GzipStream.uncompress(h2in("1f9d902fae142112b0484183028514a402"),
  //        baos = file2out("samples/wed.txt"));
  //    Assert.assertEquals(baos.expectedSize(), baos.size());
  //  }
}
