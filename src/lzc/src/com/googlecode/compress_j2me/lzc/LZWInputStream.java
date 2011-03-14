package com.googlecode.compress_j2me.lzc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class LZWInputStream extends InputStream {

  private InputStream in;

  // Bit stream fields.
  private int buffer;
  private int offset;

  // LZW stream fields.
  private int max_mask_size;
  private LZWDict dict;
  private boolean block_mode;
  private ByteBuffer entry;
  private int mask_size;
  private int w_code;
  private int buffer_read_offset;
  private int code_count;

  public LZWInputStream(InputStream input) {
    this.in = input;
    entry = new ByteBuffer(128);
    max_mask_size = -1;
    mask_size = LZWHash.INITIAL_MASK_SIZE;
    w_code = -1;
  }

  public void setNoHeader() {
    if (max_mask_size < 0) {
      max_mask_size = LZWHash.MAX_MASK_SIZE;
    }
  }

  private int readCode(int numBits) throws IOException {
    while (this.offset < numBits) {
      int tmp = this.in.read();
      if (tmp < 0) {
        return -1;
      }
      this.buffer |= tmp << this.offset;
      this.offset += 8;
    }
    int mask = (1 << numBits) - 1;
    int code = this.buffer & mask;
    this.buffer >>>= numBits;
    this.offset -= numBits;
    return code;
  }

  private void readHeader() throws IOException {
    int magic = readCode(8) << 8 | readCode(8);
    if (magic != LZWHash.COMPRESS_MAGIC_NUMBER) {
      throw new RuntimeException("Bad magic number " + magic);
    }
    int flags = readCode(8);
    block_mode = (flags & LZWHash.BLOCK_MODE_MASK) != 0;
    max_mask_size = flags & LZWHash.MAX_MASK_SIZE_MASK;
    if (max_mask_size > LZWHash.MAX_MASK_SIZE) {
      throw new RuntimeException("Cannot handle " + max_mask_size + " bits");
    }
    dict = new LZWDict(1 << max_mask_size);
  }

  private int uncompress() throws IOException {
    int k = readCode(mask_size);
    code_count++;
    if (k < 0) {
      return -1;
    }
    if (k == LZWHash.CLEAR_CODE) {
      // Skips codes to reach end of block.
      for (; block_mode && (code_count % 8 != 0); code_count++) {
        readCode(mask_size);
      }
      dict.reset();
      entry.reset();
      w_code = -1;
      mask_size = LZWHash.INITIAL_MASK_SIZE;
      return 0;
    }

    int dict_size = dict.size();
    if (k < dict_size) {
      dict.get((char) k, entry);
    } else if (k == dict_size) {
      entry.append(entry.rawBuffer()[0]);
    } else {
      throw new IOException("Invalid code " + k);
      //return -1; // Exits returning error code. 
    }

    // Should output uncompressed bytes at this point. They are available in
    // entry field.

    // Reset buffer_read_offset to make entry's content available for reading.
    buffer_read_offset = 0;
    if (w_code >= 0) {
      if (dict_size < (1 << max_mask_size)) {
        dict.put((char) w_code, entry.rawBuffer()[0]);
        if ((dict_size + 1) >= (1 << mask_size) && mask_size < max_mask_size) {
          mask_size++;
          code_count = 0;
        }
      }
    }
    w_code = (char) k;
    return entry.size();
  }

  // -------------------------------------------------------------------------
  // InputStream API.
  // -------------------------------------------------------------------------

  public int read() throws IOException {
    if (max_mask_size < 0) {
      readHeader();
    }
    while (buffer_read_offset >= this.entry.size()) {
      if (uncompress() < 0) {
        return -1;
      }
    }
    return 0xFF & entry.rawBuffer()[buffer_read_offset++];
  }

  // -------------------------------------------------------------------------
  // Static API.
  // -------------------------------------------------------------------------

  public static void uncompress(InputStream in, OutputStream out)
      throws IOException {
    LZWInputStream lzwIn = new LZWInputStream(in);
    byte[] buffer = new byte[128];
    int bytesRead;
    while ((bytesRead = lzwIn.read(buffer)) >= 0) {
      out.write(buffer, 0, bytesRead);
    }
    out.flush();
  }
}
