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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

public class GzipTest extends UnitTest {

  private ByteArrayOutputStream baos;
  private AssertiveOutputStream aos;

  private void gunzip(InputStream in, AssertiveOutputStream out)
      throws IOException {
    pump(new GZIPInputStream(in), out);
    Assert.assertEquals(out.expectedSize(), out.size());
  }

  @Test
  public void test() throws IOException {
    String path = "samples/test.txt";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipEmpty() throws IOException {
    String path = "samples/empty";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipA() throws IOException {
    String path = "samples/a";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipABCDEx10() throws IOException {
    String path = "samples/ABCDEx10";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzip0xF0FF() throws IOException {
    String path = "samples/0xF0FF";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipAx10() throws IOException {
    String path = "samples/Ax10.txt";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipA9B1A9C1() throws IOException {
    String path = "samples/A9B1A9C1.txt";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipHelloWorld() throws IOException {
    String path = "samples/helloworld.txt";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipGoogleLogo() throws IOException {
    String path = "samples/google_logo.png";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }

  @Test
  public void testGzipBash() throws IOException {
    String path = "samples/bash";
    Gzip.gzip(file2in(path), baos = new ByteArrayOutputStream());
    writeFile(path + ".gz2", baos.toByteArray());

    gunzip(b2in(baos.toByteArray()), aos = file2out(path));

    Gzip.gunzip(b2in(baos.toByteArray()), aos = file2out(path));
    Assert.assertEquals(aos.expectedSize(), aos.size());
  }
}
