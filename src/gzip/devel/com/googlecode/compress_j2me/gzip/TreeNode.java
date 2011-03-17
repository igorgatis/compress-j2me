package com.googlecode.compress_j2me.gzip;

public class TreeNode {
  int pointer;
  int left;
  int right;

  public TreeNode(int content) {
    set(content);
  }

  public void set(int content) {
    left = (content >>> Huffman.LEFT_CHILD_OFFSET) & Huffman.CHILD_INDEX_MASK;
    right = (content >>> Huffman.RIGHT_CHILD_OFFSET) & Huffman.CHILD_INDEX_MASK;
    pointer = (content >>> Huffman.POINTER_OFFSET) & Huffman.POINTER_MASK;
    if (pointer == Huffman.NO_POINTER) {
      pointer = -1;
    }
  }

  @Override
  public String toString() {
    return "(" + pointer + "," + left + "," + right + ")";
  }

  private static void printTree(int[] tree, int idx, String prefix) {
    TreeNode node = new TreeNode(tree[idx]);
    if (node.right > 0) {
      printTree(tree, node.right, prefix + "  ");
    }
    System.out.println(prefix + node.pointer);
    if (node.left > 0) {
      printTree(tree, node.left, prefix + "  ");
    }
  }

  public static void printTree(int[] tree, int idx) {
    printTree(tree, idx, "");
  }

  private static StringBuffer treeToString(int[] tree, int idx,
      StringBuffer buffer) {
    TreeNode node = new TreeNode(tree[idx]);
    if (node.left > 0) {
      buffer.append('[');
      treeToString(tree, node.left, buffer);
      buffer.append(']');
    }
    buffer.append(node.pointer);
    if (node.right > 0) {
      buffer.append('[');
      treeToString(tree, node.right, buffer);
      buffer.append(']');
    }
    return buffer;
  }

  public static String treeToString(int[] tree) {
    String str = treeToString(tree, 0, new StringBuffer()).toString();
    return str;
  }

  public static void printNodes(int[] tree) {
    TreeNode node = new TreeNode(0);
    for (int i = 0; i < tree.length; i++) {
      node.set(tree[i]);
      System.out.println(node);
    }
  }

  private static void printCodes(int[] tree, int idx, String prefix,
      String[] labels) {
    TreeNode node = new TreeNode(tree[idx]);
    if (node.left > 0) {
      printCodes(tree, node.left, prefix + "0", labels);
    }
    StringBuffer buffer = new StringBuffer(2 * prefix.length());
    for (int i = 0; i < prefix.length(); i++) {
      buffer.append("  ");
    }
    if (node.pointer == -1) {
      //System.out.println(buffer + "+");
    } else {
      //System.out.println(buffer + prefix + " " + labels[node.pointer]);
      System.out.println(prefix + " " + labels[node.pointer]);
    }
    if (node.right > 0) {
      printCodes(tree, node.right, prefix + "1", labels);
    }
  }

  public static void printCodes(int[] tree, String[] labels) {
    printCodes(tree, 0, "", labels);
  }

  private static int pointer(int[] tree, int idx, int reservedPath) {
    TreeNode node = new TreeNode(tree[idx]);
    if (node.pointer != -1) {
      return node.pointer;
    }
    if ((reservedPath & 0x01) != 0) {
      return pointer(tree, node.right, reservedPath >>> 1);
    } else {
      return pointer(tree, node.left, reservedPath >>> 1);
    }
  }

  public static int pointer(int[] tree, int reservedPath) {
    return pointer(tree, 0, reservedPath);
  }

}
