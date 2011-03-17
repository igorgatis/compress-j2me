package com.googlecode.compress_j2me.gzip;

public class Huffman {

  public static final int RIGHT_CHILD_OFFSET = 0;
  public static final int LEFT_CHILD_OFFSET = 11;
  public static final int POINTER_OFFSET = 22;
  public static final int CHILD_INDEX_MASK = 0x7FF;
  public static final int POINTER_MASK = 0x1FF;
  public static final int NO_POINTER = POINTER_MASK;

  static int appendChild(int[] tree, int nodeCount, int path, int pathLength,
      int pointer) {
    int idx = 0;
    while (pathLength-- > 0) {
      int child_offset = 0;
      if ((path & (1 << pathLength)) == 0) {
        child_offset = LEFT_CHILD_OFFSET;
      }
      int child_index = (tree[idx] >>> child_offset) & CHILD_INDEX_MASK;
      if (child_index > 0) {
        idx = child_index; // child exists.
      } else {
        if (nodeCount > CHILD_INDEX_MASK) {
          throw new RuntimeException("Too many nodes: " + nodeCount);
        }
        // Records new child.
        tree[idx] |= nodeCount << child_offset;
        // Makes new child point to NULL pointer.
        tree[nodeCount] = NO_POINTER << POINTER_OFFSET;
        nodeCount++;
        // Goes to new child.
        idx = nodeCount - 1;
      }
    }
    tree[idx] = pointer << POINTER_OFFSET;
    return nodeCount;
  }

  public static int[] buildCodeTree(int maxBits, char[] node_len) {
    if (node_len.length > POINTER_MASK) {
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
    tree[0] = NO_POINTER << POINTER_OFFSET;
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
}
