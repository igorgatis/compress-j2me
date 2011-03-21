// LZC implementation for J2ME
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

package com.googlecode.compress_j2me.lzc;

import java.io.IOException;

public class Main {

  private static void printUsageAndExit(int code) {
    System.err.println("java -jar open.lzw-cmd.jar [-d] [--alt]");
    System.err.println("  -d     Decompress (compress is selected by default)");
    System.err.println("  --alt  Uses alternate implementation");
    System.err.println();
    System.exit(code);
  }

  public static void main(String[] args) throws IOException {
    Boolean uncompress = null;
    Boolean alt = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-d")) {
        if (uncompress != null) {
          printUsageAndExit(1);
        }
        uncompress = new Boolean(true);
      } else if (args[i].equals("--alt")) {
        if (alt != null) {
          printUsageAndExit(1);
        }
        alt = new Boolean(true);
      } else {
        System.err.println("ERROR: invalid option '" + args[i] + "'");
        printUsageAndExit(1);
      }
    }
    if (uncompress == null) {
      uncompress = new Boolean(false);
    }
    if (alt == null) {
      alt = new Boolean(false);
    }
    if (alt.booleanValue()) {
      if (uncompress.booleanValue()) {
        LZCStream.uncompress(System.in, System.out);
      } else {
        LZCStream.compress(System.in, System.out);
      }
    } else {
      if (uncompress.booleanValue()) {
        LZCInputStream.uncompress(System.in, System.out);
      } else {
        LZCOutputStream.compress(System.in, System.out);
      }
    }
  }
}
