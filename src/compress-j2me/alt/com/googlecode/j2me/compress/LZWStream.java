package com.googlecode.j2me.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.j2me.compress.ByteBuffer;
import com.googlecode.j2me.compress.LZWDict;
import com.googlecode.j2me.compress.LZWHash;


public class LZWStream {

  private InputStream in;
  private OutputStream out;

  private int buffer;
  private int offset;
  private int size;

  public LZWStream(InputStream input) {
    this.in = input;
  }

  public LZWStream(OutputStream output) {
    this.out = output;
  }

  public int size() {
    return this.size;
  }

  public int readCode(int numBits) throws IOException {
    if (this.in == null) {
      throw new IOException("Not a input stream");
    }
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
    //System.err.println(numBits + ":" + code);
    return code;
  }

  public void writeCode(int code, int numBits) throws IOException {
    if (this.out == null) {
      throw new IOException("Not a output stream");
    }
    //System.err.println(numBits + ":" + code);
    int mask = (1 << numBits) - 1;
    this.buffer |= (mask & code) << this.offset;
    this.offset += numBits;

    while (this.offset > 8) {
      this.out.write((byte) this.buffer);
      this.size++;
      this.buffer >>>= 8;
      this.offset -= 8;
    }
  }

  public void end() throws IOException {
    if (this.out == null) {
      throw new IOException("Not a output stream");
    }
    while (this.offset > 0) {
      this.out.write((byte) this.buffer);
      this.size++;
      this.buffer >>>= 8;
      this.offset -= 8;
    }
    this.out.flush();
  }

  // w = NIL;
  // for (every character k in the uncompressed data) do
  //   if ((w + k) exists in the dictionary) then
  //     w = w + k;
  //   else
  //     add (w + k) to the dictionary;
  //     output codeOf(w)
  //     w = k;
  //   endif
  // done
  // output codeOf(w)

  public static int compress(InputStream in, OutputStream out)
      throws IOException {
    LZWStream lzwOut = new LZWStream(out);

    LZWHash hash = new LZWHash(1 << LZWHash.MAX_MASK_SIZE);
    int w_code = -1;

    // LZC header.
    lzwOut.writeCode(0x1F, 8);
    lzwOut.writeCode(0x9D, 8);
    // block_mode=true, mask_size=LZWHash.MAX_MASK_SIZE
    lzwOut.writeCode(LZWHash.BLOCK_MODE_MASK | (0x1F & LZWHash.MAX_MASK_SIZE),
        8);

    int mask_size = LZWHash.INITIAL_MASK_SIZE;
    int k;
    while ((k = in.read()) >= 0) {
      // Returns code of w+k if present in dictionary.
      // Otherwise, add w+k and returns -1.
      int wk_code = hash.putOrGet(w_code, (byte) k);
      if (wk_code >= 0) {
        w_code = (char) wk_code;
      } else {
        lzwOut.writeCode(w_code, mask_size);
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
        lzwOut.writeCode(LZWHash.CLEAR_CODE, mask_size);
        hash.reset();
        mask_size = LZWHash.INITIAL_MASK_SIZE;
      }
    }
    if (w_code >= 0) {
      lzwOut.writeCode(w_code, mask_size);
    }
    lzwOut.end();
    out.flush();
    return lzwOut.size();
  }

  // w = NIL;
  // while (read a char k) do
  //   if (index k exists in dictionary) then
  //     entry = dictionary entry for k;
  //   else if (k == currSizeDict)
  //     entry = w + w[0];
  //   else
  //     signal invalid code;
  //   endif
  //   output entry;
  //   if w is not empty then
  //     add w + entry[0] to the dictionary;
  //   endif
  //   w = entry;
  // done

  public static int uncompress(InputStream in, OutputStream out)
      throws IOException {
    LZWStream lzwIn = new LZWStream(in);

    int magic = lzwIn.readCode(8) << 8 | lzwIn.readCode(8);
    if (magic != LZWHash.COMPRESS_MAGIC_NUMBER) {
      throw new RuntimeException("Bad magic number " + magic);
    }
    int flags = lzwIn.readCode(8);
    boolean block_mode = (flags & LZWHash.BLOCK_MODE_MASK) != 0;
    int max_mask_size = flags & LZWHash.MAX_MASK_SIZE_MASK;
    if (max_mask_size > LZWHash.MAX_MASK_SIZE) {
      throw new RuntimeException("Cannot handle " + max_mask_size + " bits");
    }
    LZWDict dict = new LZWDict(1 << max_mask_size);
    int w_code = -1;
    int bytes = 0;
    int code_count = 0;

    int k;
    ByteBuffer entry = new ByteBuffer(128);
    int mask_size = LZWHash.INITIAL_MASK_SIZE;
    while ((k = lzwIn.readCode(mask_size)) >= 0) {
      code_count++;
      if (k == LZWHash.CLEAR_CODE) {
        // Skips codes to reach end of block.
        for (; block_mode && (code_count % 8 != 0); code_count++) {
          lzwIn.readCode(mask_size);
        }
        entry.reset();
        dict.reset();
        w_code = -1;
        mask_size = LZWHash.INITIAL_MASK_SIZE;
        continue;
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
      out.write(entry.rawBuffer(), 0, entry.size());
      bytes += entry.size();
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
    }
    return bytes;
  }
}
