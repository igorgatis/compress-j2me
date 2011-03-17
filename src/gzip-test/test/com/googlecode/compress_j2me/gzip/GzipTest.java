package com.googlecode.compress_j2me.gzip;

import junit.framework.Assert;

import org.junit.Test;

public class GzipTest {

  @Test
  public void testBuildCanonicalTree() {
    int[] tree = Gzip.CANONICAL_LIT_CODES;
    for (int i = 0; i <= 286; i++) {
      int rpath = 0;
      if (i < 144) {
        rpath = HuffmanTest.rpath(0x30 + i, 8);
      } else if (i < 256) {
        rpath = HuffmanTest.rpath(0x190 + (i - 144), 9);
      } else if (i < 280) {
        rpath = HuffmanTest.rpath(0x00 + (i - 256), 7);
      } else {
        rpath = HuffmanTest.rpath(0xC0 + (i - 280), 8);
      }
      Assert.assertEquals("i=" + i, i, TreeNode.pointer(tree, rpath));
    }
  }

  @Test
  public void testBuildDictReaderTree() {
    int[] tree = Gzip.CANONICAL_DIST_CODES;
    for (int i = 0; i <= 18; i++) {
      int rpath = HuffmanTest.rpath(i, 5);
      Assert.assertEquals("i=" + i, i, TreeNode.pointer(tree, rpath));
    }
  }

}
