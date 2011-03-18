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
    char[] node_len = new char[] {
        2, 1, 3, 3 };
    int[] tree = Huffman.buildCodeTree(3, node_len);
    Assert.assertEquals(0, TreeNode.pointer(tree, rpath(0x02, 2)));
    Assert.assertEquals(1, TreeNode.pointer(tree, rpath(0x00, 1)));
    Assert.assertEquals(2, TreeNode.pointer(tree, rpath(0x06, 3)));
    Assert.assertEquals(3, TreeNode.pointer(tree, rpath(0x07, 3)));
    Assert.assertEquals("(1)-1((0)-1((2)-1(3)))", TreeNode.treeToString(tree));
  }

  @Test
  public void testBuildTreeSample2() {
    char[] node_len = new char[] {
        3, 3, 3, 3, 3, 2, 4, 4 };
    int[] tree = Huffman.buildCodeTree(4, node_len);
    Assert.assertEquals(0, TreeNode.pointer(tree, rpath(0x02, 3)));
    Assert.assertEquals(1, TreeNode.pointer(tree, rpath(0x03, 3)));
    Assert.assertEquals(2, TreeNode.pointer(tree, rpath(0x04, 3)));
    Assert.assertEquals(3, TreeNode.pointer(tree, rpath(0x05, 3)));
    Assert.assertEquals(4, TreeNode.pointer(tree, rpath(0x06, 3)));
    Assert.assertEquals(5, TreeNode.pointer(tree, rpath(0x00, 2)));
    Assert.assertEquals(6, TreeNode.pointer(tree, rpath(0x0E, 4)));
    Assert.assertEquals(7, TreeNode.pointer(tree, rpath(0x0F, 4)));
    Assert.assertEquals("((5)-1((0)-1(1)))-1(((2)-1(3))-1((4)-1((6)-1(7))))",
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

  @Test
  public void testFixedLiteralsTree() {
    int[] tree = Huffman.FIXED_LITERALS_TREE;
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
    Assert.assertEquals(256, TreeNode.pointer(tree, 0x00));
  }

  @Test
  public void testFixedAlphabetLengthsTree() {
    int[] tree = Huffman.FIXED_ALPHABET_LENGTHS_TREE;
    for (int i = 0; i <= 18; i++) {
      int rpath = HuffmanTest.rpath(i, 5);
      Assert.assertEquals("i=" + i, i, TreeNode.pointer(tree, rpath));
    }
  }
}