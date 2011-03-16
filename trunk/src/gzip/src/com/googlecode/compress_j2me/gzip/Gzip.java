package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Gzip {

  public static final int GZIP_MAGIC_NUMBER = 0x1F8B;

  private static final byte FTEXT_FLAG = 0x01;
  private static final byte FHCRC_FLAG = 0x02;
  private static final byte FEXTRA_FLAG = 0x04;
  private static final byte FNAME_FLAG = 0x08;
  private static final byte FCOMMENT_FLAG = 0x10;
  private static final byte FRESERVED_FLAG = (byte) 0xE0;
  private static final byte CM_DEFLATE = 8;

  private static final byte BFINAL_FLAG = (byte) 0x80;
  private static final byte BTYPE_FLAG = 0x60;

  private static int inflate(Crc32Stream gzipIn, OutputStream out)
      throws IOException {
    byte[] buffer = new byte[128];
    int blockHeader;
    do {
      blockHeader = gzipIn.read();
      int blockType = blockHeader & BTYPE_FLAG;
      if (blockType == 0) {
        int len = gzipIn.readBytes(2);
        int nlen = gzipIn.readBytes(2);
        if ((len ^ 0xFF) != nlen) {
          throw new IOException("Invalid block.");
        }
        while (len-- > 0) {
          int ch = gzipIn.read();
          if (ch < 0) {
            throw new IOException("Unexpected EOF.");
          }
          out.write(ch);
        }
      } else if (blockType == 0x60) {
        throw new IOException("Invalid block.");
      } else {
        // That's either dynamic or fixed huffman.
        if (blockType == 0x40) {
          // Dynamic huffman.
          // read representation of code trees
        }
        //       loop (until end of block code recognized)
        //          decode literal/length value from input stream
        //          if value < 256
        //             copy value (literal byte) to output stream
        //          otherwise
        //             if value = end of block (256)
        //                break from loop
        //             otherwise (value = 257..285)
        //                decode distance from input stream
        //
        //                move backwards distance bytes in the output
        //                stream, and copy length bytes from this
        //                position to the output stream.
        //       end loop
      }
    } while ((blockHeader & BFINAL_FLAG) == 0);
    return -1;
  }

  public static int inflate(InputStream in, OutputStream out)
      throws IOException {
    return inflate(new Crc32Stream(in), out);
  }

  public static int gunzip(InputStream in, OutputStream out) throws IOException {
    Crc32Stream gzipIn = new Crc32Stream(in);

    int magic = gzipIn.readBytes(2);
    if (magic != GZIP_MAGIC_NUMBER) {
      throw new IOException("Bad magic number " + magic);
    }
    int cm = gzipIn.readBytes(1);
    if (cm != CM_DEFLATE) {
      throw new IOException("Unsupported CM=" + cm);
    }
    int flg = gzipIn.readBytes(1);
    int mtime = gzipIn.readBytes(4);
    int xfl = gzipIn.readBytes(1);
    int os = gzipIn.readBytes(1);

    if ((flg & FEXTRA_FLAG) != 0) {
      int xlen = gzipIn.readBytes(2);
      while (xlen-- > 0) {
        gzipIn.read();
      }
    }
    if ((flg & FNAME_FLAG) != 0) {
      int ch;
      while ((ch = gzipIn.read()) > 0) {
      }
      //readZeroTerminatedString(); // filename
    }
    if ((flg & FCOMMENT_FLAG) != 0) {
      int ch;
      while ((ch = gzipIn.read()) > 0) {
      }
      //readZeroTerminatedString(); // filename
    }
    if ((flg & FHCRC_FLAG) != 0) {
      int headerCrc16 = gzipIn.getCrc32() & 0xFFFF;
      int expectedHeaderCrc16 = gzipIn.readBytes(2);
      if (expectedHeaderCrc16 != headerCrc16) {
        throw new IOException("Header CRC check failed.");
      }
      //gzipIn.resetCrc32();
    }
    // Uncompress body.
    gzipIn.resetCrc32();
    inflate(gzipIn, out);
    // Read footer.
    int bodyCrc32 = gzipIn.getCrc32();
    int expectedBodyCrc32 = gzipIn.readBytes(4);
    if (expectedBodyCrc32 != bodyCrc32) {
      throw new IOException("CRC check failed.");
    }
    int isize = gzipIn.readBytes(4);
    return -1;
  }
}
