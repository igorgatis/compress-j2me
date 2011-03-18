package com.googlecode.compress_j2me.gzip;

import java.io.IOException;

public class Huffman {

  public static final int RIGHT_CHILD_OFFSET = 0;
  public static final int LEFT_CHILD_OFFSET = 16;
  public static final int CHILD_CONTENT_MASK = 0xFFFF;
  public static final int MAX_CHILD_INDEX = (1 << 13) - 1;
  public static final int MAX_POINTER_INDEX = CHILD_CONTENT_MASK
      - MAX_CHILD_INDEX;

  static int appendChild(int[] tree, int nodeCount, int path, int pathLength,
      int pointer) {
    int idx = 0;
    while (pathLength-- > 0) {
      int child_offset = 0;
      if ((path & (1 << pathLength)) == 0) {
        child_offset = LEFT_CHILD_OFFSET;
      }
      int child_content = (tree[idx] >>> child_offset) & CHILD_CONTENT_MASK;
      if (child_content == 0) {
        // Add new child.
        if (nodeCount > CHILD_CONTENT_MASK) {
          throw new RuntimeException("Too many nodes: " + nodeCount);
        }
        if (pathLength > 0) {
          tree[idx] |= nodeCount << child_offset;
          idx = nodeCount;
          nodeCount++;
        } else {
          tree[idx] |= (pointer + CHILD_CONTENT_MASK) << child_offset;
        }
      } else if (child_content <= MAX_CHILD_INDEX) {
        // Child exists.
        idx = child_content;
      } else {
        throw new RuntimeException("Invalid tree");
      }
    }
    return nodeCount;
  }

  public static int[] buildCodeTree(int maxBits, char[] node_len) {
    if (node_len.length > MAX_POINTER_INDEX) {
      throw new RuntimeException("Too many leaves: " + node_len.length);
    }
    // Step 1: Count the number of codes for each code length.
    char[] bl_count = new char[maxBits + 1];
    for (int n = 0; n < node_len.length; n++) {
      int len = node_len[n];
      if (len > 0) {
        bl_count[len]++;
      }
    }
    // Step 2: Find the numerical value of the smallest code for each code length.
    char[] next_code = new char[bl_count.length + 1];
    char code = 0;
    for (int bits = 1; bits <= bl_count.length; bits++) {
      code = (char) ((code + bl_count[bits - 1]) << 1);
      next_code[bits] = code;
    }
    // Step 3: Assign numerical values to all codes.
    int maxChildCount = Math.min((1 << (maxBits + 1)) - 1, maxBits
        * node_len.length + 1);
    int[] tree = new int[maxChildCount];
    int nodeCount = 1; // Root already exists.
    for (int n = 0; n < node_len.length; n++) {
      int len = node_len[n];
      if (len > 0) {
        int path = next_code[len];
        next_code[len]++;
        nodeCount = appendChild(tree, nodeCount, path, len, n);
      }
    }
    // Shrinks tree if occupations is below 80%.
    if ((nodeCount / (double) tree.length) < 0.8) {
      //System.out.println(tree.length + "=>" + nodeCount);
      int[] shorter_tree = new int[nodeCount];
      System.arraycopy(tree, 0, shorter_tree, 0, nodeCount);
      tree = shorter_tree;
    }
    return tree;
  }

  public static int decodeSymbol(Crc32Stream in, int[] tree) throws IOException {
    int node = 0;
    do {
      int child_offset = 0;
      if (in.readBits(1) == 1) {
        child_offset = LEFT_CHILD_OFFSET;
      }
      int child_content = (tree[node] >>> child_offset) & CHILD_CONTENT_MASK;
      if (child_content <= MAX_CHILD_INDEX) {
        node = child_content;
      } else {
        return child_content - MAX_CHILD_INDEX;
      }
    } while (node > 0);
    return -1;
  }
}
