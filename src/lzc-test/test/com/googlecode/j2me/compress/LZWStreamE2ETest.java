package com.googlecode.j2me.compress;

import java.io.IOException;

import static com.googlecode.j2me.compress.TestUtil.*;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.j2me.compress.LZWStream;

public class LZWStreamE2ETest {

  private AssertiveOutputStream baos;

  @Test
  public void testEmpty() throws IOException {
    LZWStream.compress(TestUtil.s2in(""), baos = h2out("1F9D90"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1F9D90"), baos = s2out(""));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_a() throws IOException {
    LZWStream.compress(TestUtil.s2in("a"), baos = h2out("1F9D906100"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1F9D906100"), baos = s2out("a"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_aa() throws IOException {
    LZWStream.compress(TestUtil.s2in("aa"), baos = h2out("1F9D9061C200"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1F9D9061C200"), baos = s2out("aa"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_aaa() throws IOException {
    LZWStream.compress(TestUtil.s2in("aaa"), baos = h2out("1F9D90610202"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1F9D90610202"), baos = s2out("aaa"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_aaaa() throws IOException {
    LZWStream.compress(TestUtil.s2in("aaaa"), baos = h2out("1F9D9061028601"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1F9D9061028601"), baos = s2out("aaaa"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_aaaaa() throws IOException {
    LZWStream.compress(TestUtil.s2in("aaaaa"), baos = h2out("1F9D9061020604"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1F9D9061020604"), baos = s2out("aaaaa"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_abcde() throws IOException {
    LZWStream.compress(TestUtil.s2in("abcde"), baos = h2out("1F9D9061C48C215306"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1F9D9061C48C215306"), baos = s2out("abcde"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_JOEYNJOEYNJOEY() throws IOException {
    LZWStream.compress(TestUtil.s2in("JOEYNJOEYNJOEY"),
        baos = h2out("1f9d904a9e14c9e224e0c08202b300"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1f9d904a9e14c9e224e0c08202b300"),
        baos = s2out("JOEYNJOEYNJOEY"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }

  @Test
  public void test_WED() throws IOException {
    LZWStream.compress(file2in("samples/wed.txt"),
        baos = h2out("1f9d902fae142112b0484183028514a402"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
    LZWStream.uncompress(h2in("1f9d902fae142112b0484183028514a402"),
        baos = file2out("samples/wed.txt"));
    Assert.assertEquals(baos.expectedSize(), baos.size());
  }
}
