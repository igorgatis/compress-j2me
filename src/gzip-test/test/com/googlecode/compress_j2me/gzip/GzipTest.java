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

import org.bolet.jgz.GZipInputStream;
import org.junit.Assert;
import org.junit.Test;

public class GzipTest extends UnitTest {

  private AssertiveOutputStream baos;

  @Test
  public void testGunzipEmpty() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/empty.gz"),
        baos = file2out("samples/empty"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzipA() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/a.gz"),
        baos = file2out("samples/a"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzipABCDEx10() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/ABCDEx10.gz"),
        baos = file2out("samples/ABCDEx10"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzip0xF0FF() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/0xF0FF.gz"),
        baos = file2out("samples/0xF0FF"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzipHelloWorldFile() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/helloworld.txt.gz"),
        baos = file2out("samples/helloworld.txt"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals("helloworld.txt", gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzipGoogleLogo() throws IOException {
    Gzip gzip = Gzip.gunzip(file2in("samples/google_logo.png.gz"),
        baos = file2out("samples/google_logo.png"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }

  @Test
  public void testGunzipBash() throws IOException {
    pump(new GZipInputStream(file2in("samples/bash.gz")),
        baos = file2out("samples/bash"));
    Gzip gzip = Gzip.gunzip(file2in("samples/bash.gz"),
        baos = file2out("samples/bash"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    Assert.assertEquals(null, gzip.getFilename());
    Assert.assertEquals(null, gzip.getComment());
  }
}
