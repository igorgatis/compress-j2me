package com.googlecode.compress_j2me.lzc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class LZWOutputStream extends OutputStream {

  private OutputStream out;

  // Bit stream fields.
  private int buffer;
  private int offset;
  private int size;

  // LZW stream fields.
  private LZWHash hash;
  private int w_code;
  private int mask_size;

  public LZWOutputStream(OutputStream output) {
    this.out = output;
    w_code = -1;
    mask_size = LZWHash.INITIAL_MASK_SIZE;
  }

  public void setNoHeader() {
    hash = new LZWHash(1 << LZWHash.MAX_MASK_SIZE);
  }

  private void writeBuffer(int left) throws IOException {
    while (this.offset > left) {
      this.out.write((byte) this.buffer);
      this.size++;
      this.buffer >>>= 8;
      this.offset -= 8;
    }
  }

  private void writeCode(int code, int numBits) throws IOException {
    int mask = (1 << numBits) - 1;
    this.buffer |= (mask & code) << this.offset;
    this.offset += numBits;
    writeBuffer(8);
  }

  private void writeHeader() throws IOException {
    writeCode(0x1F, 8);
    writeCode(0x9D, 8);
    // block_mode=true, mask_size=LZWHash.MAX_MASK_SIZE
    writeCode(LZWHash.BLOCK_MODE_MASK | (0x1F & LZWHash.MAX_MASK_SIZE), 8);
  }

  private void compress(int k) throws IOException {
    // Returns code of w+k if present in dictionary.
    // Otherwise, add w+k and returns -1.
    int wk_code = hash.putOrGet(w_code, (byte) k);
    if (wk_code >= 0) {
      w_code = (char) wk_code;
    } else {
      writeCode(w_code, mask_size);
      // Increasing mask size if possible.
      if (wk_code != -2 && hash.size() > (1 << mask_size)) {
        mask_size++;
      }
      w_code = (char) k;
    }
    // Flush whenever hash is full. Unix compress would observe compression
    // rate in order to decide when to flush - this is intentionally left
    // unimplemented to reduce code size (and complexity).
    if (w_code < LZWHash.CLEAR_CODE
        && hash.size() >= (1 << LZWHash.MAX_MASK_SIZE)) {
      writeCode(LZWHash.CLEAR_CODE, mask_size);
      hash.reset();
      mask_size = LZWHash.INITIAL_MASK_SIZE;
    }
  }

  public void end() throws IOException {
    if (hash == null) {
      writeHeader();
    }
    if (w_code >= 0) {
      writeCode(w_code, mask_size);
    }
    writeBuffer(0);
    this.out.flush();
  }
  
  public int size() {
    return size;
  }

  // -------------------------------------------------------------------------
  // OutputStream API.
  // -------------------------------------------------------------------------

  public void write(int b) throws IOException {
    if (hash == null) {
      writeHeader();
      hash = new LZWHash(1 << LZWHash.MAX_MASK_SIZE);
    }
    compress(b & 0xFF);
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void close() throws IOException {
    end();
    out.flush();
  }

  // -------------------------------------------------------------------------
  // Static API.
  // -------------------------------------------------------------------------

  public static void compress(InputStream in, OutputStream out)
      throws IOException {
    LZWOutputStream lzwOut = new LZWOutputStream(out);
    byte[] buffer = new byte[128];
    int bytesRead;
    while ((bytesRead = in.read(buffer)) >= 0) {
      lzwOut.write(buffer, 0, bytesRead);
    }
    lzwOut.flush();
    lzwOut.end();
  }
}
