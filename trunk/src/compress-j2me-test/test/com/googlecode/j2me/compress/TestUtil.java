package com.googlecode.j2me.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Assert;

public class TestUtil {

  public static ByteArrayInputStream s2in(String content) {
    return new ByteArrayInputStream(s2b(content));
  }

  public static ByteArrayInputStream h2in(String content) {
    return new ByteArrayInputStream(h2b(content));
  }

  public static ByteArrayInputStream file2in(String fileName) {
    try {
      return new ByteArrayInputStream(readFile(fileName));
    } catch (IOException e) {
      Assert.fail(e.toString());
      return null; // makes compiler happy.
    }
  }

  public static AssertiveOutputStream s2out(String content) {
    return new AssertiveOutputStream(s2b(content));
  }

  public static AssertiveOutputStream h2out(String content) {
    return new AssertiveOutputStream(h2b(content));
  }

  public static AssertiveOutputStream file2out(String fileName) {
    try {
      return new AssertiveOutputStream(readFile(fileName));
    } catch (IOException e) {
      Assert.fail(e.toString());
      return null; // makes compiler happy.
    }
  }

  public static final byte[] s2b(String content) {
    byte[] data = new byte[content.length()];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) content.charAt(i);
    }
    return data;
  }

  public static final byte h2b(char ch) {
    ch = Character.toLowerCase(ch);
    if (ch >= '0' && ch <= '9') {
      return (byte) (ch - '0');
    }
    if (ch >= 'a' && ch <= 'f') {
      return (byte) (ch - 'a' + 10);
    }
    throw new RuntimeException("invalid hex=" + ch);
  }

  public static final byte[] h2b(String hex) {
    hex = hex.replaceAll("\\s", "");
    byte[] data = new byte[hex.length() / 2];
    for (int i = 0; i < hex.length(); ++i) {
      data[i / 2] <<= 4;
      data[i / 2] |= h2b(hex.charAt(i));
    }
    return data;
  }

  public static final byte[] readFile(String fileName) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
    FileInputStream origFile = new FileInputStream(fileName);
    byte[] data = new byte[128];
    int read;
    while ((read = origFile.read(data)) > 0) {
      buffer.write(data, 0, read);
    }
    return buffer.toByteArray();
  }

  public static String toHex(int ch) {
    char[] H = "0123456789ABCDEF".toCharArray();
    return "" + H[(0xF0 & ch) >> 4] + H[ch & 0x0F];
  }

  //  public static String toHex(byte[] data) {
  //    return toHex(data, data.length);
  //  }
  //
  //  public static String toHex(byte[] data, int len) {
  //    StringBuffer buffer = new StringBuffer();
  //    for (int i = 0; i < len; i++) {
  //      buffer.append(toHex(data[i]));
  //      //buffer.append(' ');
  //    }
  //    return buffer.toString();
  //  }
  //
  //  public static void check(byte[] data1, byte[] data2) throws IOException {
  //    String str1 = toHex(data1);
  //    String str2 = toHex(data2);
  //    if (str1.equals(str2) == false) {
  //      if (str1.length() < 80 && str2.length() < 80) {
  //        System.out.println(str1.length() + ":'" + str1 + "' != "
  //            + str2.length() + ":'" + str2 + "'");
  //      } else {
  //        for (int i = 0; i < str1.length() && i < str2.length(); i++) {
  //          if (str1.charAt(i) != str2.charAt(i)) {
  //            int idx = Math.max(i - 16, 0);
  //            int len1 = Math.min(i + 16, str1.length());
  //            int len2 = Math.min(i + 16, str2.length());
  //            Assert.fail("ERROR: " + (i / 2) + " " + (str1.length() / 2)
  //                + ":'..." + str1.substring(idx, len1) + "...' != "
  //                + (str2.length() / 2) + ":'..." + str2.substring(idx, len2)
  //                + "...' (MainTest.java:" + line + ")");
  //            break;//i++;
  //          }
  //        }
  //      }
  //    }
  //  }

}
