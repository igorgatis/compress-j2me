package com.googlecode.compress_j2me.gzip;

import java.io.IOException;

class Huffman {

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

  static int decodeSymbol(ZStream in, int[] tree) throws IOException {
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

  //---------------------------------------------------------------------------
  // Deflate specific.
  //---------------------------------------------------------------------------

  static final int END_OF_BLOCK_CODE = 256;

  // 0x01FF=length, 0xE000=extra bits.
  private static final int _LIT_LEN_EXTRA_OFFSET = 9;
  private static final int _LIT_LEN_MASK = 0x1FF;
  private static final char[] LITERALS_LENGTHS = new char[] { //
      0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0x20B, 0x20D, 0x20F, 0x211,
      0x413, 0x417, 0x41B, 0x41F, 0x623, 0x62B, 0x633, 0x63B, 0x843, 0x853,
      0x863, 0x873, 0xA83, 0xAA3, 0xAC3, 0xAE3, 0x102 };

  static int literalLength(int litCode, ZStream in) throws IOException {
    int codedLength = LITERALS_LENGTHS[litCode];
    int length = codedLength & _LIT_LEN_MASK;
    int extraBits = codedLength >>> _LIT_LEN_EXTRA_OFFSET;
    if (extraBits > 0) {
      length += in.readBits(extraBits);
    }
    return length;
  }

  // 0xF0=distance, 0x0F=extra bits.
  private static final int _LIT_DIST_EXTRA_OFFSET = 9;
  private static final int _LIT_DIST_MASK = 0xFFFF;
  private static final int[] LITERALS_DISTANCES = new int[] { //
      0x01, 0x02, 0x03, 0x04, 0x10005, 0x10007, 0x20009, 0x2000D, 0x30011,
      0x30019, 0x40021, 0x40031, 0x50041, 0x50061, 0x60081, 0x600C1, 0x70101,
      0x70181, 0x80201, 0x80301, 0x90401, 0x90601, 0xA0801, 0xA0C01, 0xB1001,
      0xB1801, 0xC2001, 0xC3001, 0xD4001, 0xD6001, };

  static int literalDistance(int litCode, ZStream in) throws IOException {
    int codedDistance = LITERALS_DISTANCES[litCode];
    int distance = codedDistance & _LIT_DIST_MASK;
    int extraBits = codedDistance >>> _LIT_DIST_EXTRA_OFFSET;
    if (extraBits > 0) {
      distance += in.readBits(extraBits);
    }
    return distance;
  }

  static final int[] FIXED_LITERALS_TREE;
  static {
    char[] node_len = new char[288];
    int i = 0;
    while (i < 144) {
      node_len[i++] = 8;
    }
    while (i < 256) {
      node_len[i++] = 9;
    }
    while (i < 280) {
      node_len[i++] = 7;
    }
    while (i < node_len.length) {
      node_len[i++] = 8;
    }
    FIXED_LITERALS_TREE = Huffman.buildCodeTree(9, node_len);
  }

  static final int[] FIXED_ALPHABET_LENGTHS_TREE;
  static {
    char[] node_len = new char[19];
    for (int i = 0; i < node_len.length; i++) {
      node_len[i] = 5;
    }
    FIXED_ALPHABET_LENGTHS_TREE = Huffman.buildCodeTree(5, node_len);
  }

  static final byte[] HUFF2PERM = { //
      16, 17, 18, 0, 8, 7, 9, 6, 10, 5, //
      11, 4, 12, 3, 13, 2, 14, 1, 15 //
  };

  static char[] readLengths(ZStream in, int[] hcTree, int size)
      throws IOException {
    char[] lengths = new char[size];
    for (int i = 0; i < lengths.length; i++) {
      int code = Huffman.decodeSymbol(in, hcTree);
      int toCopy = 0;
      int repeat = 0;
      switch (code) {
      case 16:
        toCopy = lengths[i - 1];
        repeat = 3 + in.readBits(2);
        break;
      case 17:
        toCopy = lengths[0];
        repeat = 3 + in.readBits(3);
        break;
      case 18:
        toCopy = lengths[0];
        repeat = 11 + in.readBits(3);
        break;
      default:
        lengths[i] = (char) code;
        continue;
      }
      for (; repeat > 0 && i < lengths.length; i++) {
        lengths[i] = (char) toCopy;
      }
    }
    return lengths;
  }

}
