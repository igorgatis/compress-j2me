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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;

public class UnitTest {

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

  static void pump(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer)) > 0) {
      out.write(buffer, 0, read);
    }
    out.flush();
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
