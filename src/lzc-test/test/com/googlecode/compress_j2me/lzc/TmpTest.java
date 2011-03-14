package com.googlecode.compress_j2me.lzc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import org.junit.Test;

import com.googlecode.j2me.compress.LZWStream;

public class TmpTest {

  @Test
  public void test() throws FileNotFoundException, IOException {
    InputStream in = TestUtil.file2in("./tmp/bash.Z");
    OutputStream out = TestUtil.file2out("./tmp/bash");
    LZWStream.uncompress(in, out);
  }

  public static void main(String[] args) throws IOException {
    new TmpTest().test();
  }
}
