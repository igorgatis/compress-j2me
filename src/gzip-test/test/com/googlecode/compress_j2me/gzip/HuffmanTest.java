package com.googlecode.compress_j2me.gzip;

import junit.framework.Assert;

import org.junit.Test;

public class HuffmanTest {

  static int rpath(int path, int len) {
    int v = 0;
    for (int i = 0; i < len; i++) {
      v <<= 1;
      if ((path & (1 << i)) != 0) {
        v |= 1;
      }
    }
    return v;
  }

  @Test
  public void testBuildTreeSample1() {
    char[] node_len = new char[] { 2, 1, 3, 3 };
    int[] tree = Huffman.buildCodeTree(3, node_len);
    Assert.assertEquals(0, TreeNode.pointer(tree, rpath(0x02, 2)));
    Assert.assertEquals(1, TreeNode.pointer(tree, rpath(0x00, 1)));
    Assert.assertEquals(2, TreeNode.pointer(tree, rpath(0x06, 3)));
    Assert.assertEquals(3, TreeNode.pointer(tree, rpath(0x07, 3)));
    Assert.assertEquals("[1]-1[[0]-1[[2]-1[3]]]", TreeNode.treeToString(tree));
  }

  @Test
  public void testBuildTreeSample2() {
    char[] node_len = new char[] { 3, 3, 3, 3, 3, 2, 4, 4 };
    int[] tree = Huffman.buildCodeTree(4, node_len);
    Assert.assertEquals(0, TreeNode.pointer(tree, rpath(0x02, 3)));
    Assert.assertEquals(1, TreeNode.pointer(tree, rpath(0x03, 3)));
    Assert.assertEquals(2, TreeNode.pointer(tree, rpath(0x04, 3)));
    Assert.assertEquals(3, TreeNode.pointer(tree, rpath(0x05, 3)));
    Assert.assertEquals(4, TreeNode.pointer(tree, rpath(0x06, 3)));
    Assert.assertEquals(5, TreeNode.pointer(tree, rpath(0x00, 2)));
    Assert.assertEquals(6, TreeNode.pointer(tree, rpath(0x0E, 4)));
    Assert.assertEquals(7, TreeNode.pointer(tree, rpath(0x0F, 4)));
    Assert.assertEquals("[[5]-1[[0]-1[1]]]-1[[[2]-1[3]]-1[[4]-1[[6]-1[7]]]]",
        TreeNode.treeToString(tree));
  }

  @Test
  public void testFewSizes() {
    for (int len = 1; len < 16; len++) {
      for (int j = 1; j < (1 << len) && j < 288; j++) {
        char[] node_len = new char[j];
        for (int k = 0; k < j; k++) {
          node_len[k] = (char) len;
        }
        Huffman.buildCodeTree(len, node_len);
      }
    }
  }
}