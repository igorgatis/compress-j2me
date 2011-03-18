package com.googlecode.compress_j2me.gzip;

public class TreeNode {
  int left;
  int right;

  public TreeNode(int content) {
    set(content);
  }

  public void set(int content) {
    left = Huffman.leftChild(content);
    right = Huffman.rightChild(content);
  }

  @Override
  public String toString() {
    return "(" + left + "," + right + ")";
  }

  private static void printTree(int[] tree, int idx, String prefix) {
    int value = Huffman.getValue(idx);
    if (value >= 0) {
      System.out.println(prefix + "(" + value + ")");
      return;
    }
    TreeNode node = new TreeNode(tree[idx]);
    if (node.right > 0) {
      printTree(tree, node.right, prefix + "  ");
    }
    System.out.println(prefix + "[" + idx + "]");
    if (node.left > 0) {
      printTree(tree, node.left, prefix + "  ");
    }
  }

  public static void printTree(int[] tree, int idx) {
    printTree(tree, idx, "");
  }

  private static StringBuffer treeToString(int[] tree, int idx,
      StringBuffer buffer) {
    int value = Huffman.getValue(idx);
    if (value >= 0) {
      buffer.append(value);
      return buffer;
    }
    TreeNode node = new TreeNode(tree[idx]);
    if (node.left > 0) {
      buffer.append('(');
      treeToString(tree, node.left, buffer);
      buffer.append(')');
    }
    buffer.append(-1);
    if (node.right > 0) {
      buffer.append('(');
      treeToString(tree, node.right, buffer);
      buffer.append(')');
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
    int value = Huffman.getValue(idx);
    if (value >= 0) {
      StringBuffer buffer = new StringBuffer(2 * prefix.length());
      for (int i = 0; i < prefix.length(); i++) {
        buffer.append("  ");
      }
      if (labels != null) {
        System.out.println(prefix + " " + labels[value]);
      } else {
        System.out.println(prefix + " " + value);
      }
      return;
    }
    TreeNode node = new TreeNode(tree[idx]);
    if (node.left > 0) {
      printCodes(tree, node.left, prefix + "0", labels);
    }
    if (node.right > 0) {
      printCodes(tree, node.right, prefix + "1", labels);
    }
  }

  public static void printCodes(int[] tree, String[] labels) {
    printCodes(tree, 0, "", labels);
  }

  public static void printCodes(int[] tree) {
    printCodes(tree, 0, "", null);
  }

  private static int pointer(int[] tree, int idx, int reservedPath) {
    int value = Huffman.getValue(idx);
    if (value >= 0) {
      return value;
    }
    TreeNode node = new TreeNode(tree[idx]);
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
