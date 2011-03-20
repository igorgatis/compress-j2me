// Gzip implementation for J2ME
// Copyright 2011 Igor Gatis  All rights reserved.
// http://code.google.com/p/compress-j2me/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//     * Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//
//     * Neither the name of Google Inc. nor the names of its contributors may
//       be used to endorse or promote products derived from this software
//       without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

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
    int value = Huffman.nodeLabel(idx);
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
    int value = Huffman.nodeLabel(idx);
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
    int value = Huffman.nodeLabel(idx);
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
    int value = Huffman.nodeLabel(idx);
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
