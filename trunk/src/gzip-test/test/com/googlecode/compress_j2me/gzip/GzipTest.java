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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

public class GzipTest extends UnitTest {

  private AssertiveOutputStream baos;

  @Test
  public void testGzipEmpty() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/empty"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/empty"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/empty"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void testGzipA() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/a"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/a"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/a"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void testGzipABCDEx10() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/ABCDEx10"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/ABCDEx10"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/ABCDEx10"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void testGzip0xF0FF() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/0xF0FF"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/0xF0FF"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/0xF0FF"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void testGzipAx10() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/Ax10.txt"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/Ax10.txt"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/Ax10.txt"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void testGzipHelloWorld() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/hello_world.txt"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/hello_world.txt"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/hello_world.txt"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void testGzipGoogleLogo() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/google_logo.png"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/google_logo.png"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/google_logo.png"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }


  @Test
  public void testGzipBash() throws IOException {
    ByteArrayOutputStream out;
    Gzip.gzip(file2in("samples/bash"), out = new ByteArrayOutputStream());

    ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
    Gzip.gunzip(bais, baos = file2out("samples/bash"));
    
    bais = new ByteArrayInputStream(out.toByteArray());
    GZIPInputStream gzipIn = new GZIPInputStream(bais);
    pump(gzipIn, baos = file2out("samples/bash"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }
}
