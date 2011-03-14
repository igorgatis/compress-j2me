package com.googlecode.compress_j2me.lzc;

import java.io.IOException;

import com.googlecode.j2me.compress.LZWInputStream;
import com.googlecode.j2me.compress.LZWOutputStream;
import com.googlecode.j2me.compress.LZWStream;

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
        LZWStream.uncompress(System.in, System.out);
      } else {
        LZWStream.compress(System.in, System.out);
      }
    } else {
      if (uncompress.booleanValue()) {
        LZWInputStream.uncompress(System.in, System.out);
      } else {
        LZWOutputStream.compress(System.in, System.out);
      }
    }
  }
}
