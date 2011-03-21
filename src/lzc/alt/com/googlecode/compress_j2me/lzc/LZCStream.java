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
import java.io.InputStream;
import java.io.OutputStream;

public class LZCStream {

  private InputStream in;
  private OutputStream out;

  private int buffer;
  private int offset;
  private int size;

  LZCStream(InputStream input) {
    this.in = input;
  }

  LZCStream(OutputStream output) {
    this.out = output;
  }

  int size() {
    return this.size;
  }

  int readCode(int numBits) throws IOException {
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

  void writeCode(int code, int numBits) throws IOException {
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

  void end() throws IOException {
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

    // LZC header.
    out.write((byte) 0x1F);
    out.write((byte) 0x9D);
    // block_mode=true, mask_size=LZWHash.MAX_MASK_SIZE
    int flags = LZCHash.BLOCK_MODE_MASK | (0x1F & LZCHash.MAX_MASK_SIZE);
    out.write((byte) flags);

    LZCStream lzwOut = new LZCStream(out);
    LZCHash hash = new LZCHash(1 << LZCHash.MAX_MASK_SIZE);
    int w_code = -1;

    int mask_size = LZCHash.INITIAL_MASK_SIZE;
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
      if (w_code < LZCHash.CLEAR_CODE
          && hash.size() >= (1 << LZCHash.MAX_MASK_SIZE)) {
        lzwOut.writeCode(LZCHash.CLEAR_CODE, mask_size);
        hash.reset();
        mask_size = LZCHash.INITIAL_MASK_SIZE;
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

    int magic = in.read() << 8 | in.read();
    if (magic != LZCHash.COMPRESS_MAGIC_NUMBER) {
      throw new RuntimeException("Bad magic number " + magic);
    }
    int flags = in.read();
    boolean block_mode = (flags & LZCHash.BLOCK_MODE_MASK) != 0;
    int max_mask_size = flags & LZCHash.MAX_MASK_SIZE_MASK;
    if (max_mask_size > LZCHash.MAX_MASK_SIZE) {
      throw new RuntimeException("Cannot handle " + max_mask_size + " bits");
    }
    LZCStream lzwIn = new LZCStream(in);
    LZCDict dict = new LZCDict(1 << max_mask_size);
    int w_code = -1;
    int bytes = 0;
    int code_count = 0;

    int k;
    ByteBuffer entry = new ByteBuffer(128);
    int mask_size = LZCHash.INITIAL_MASK_SIZE;
    while ((k = lzwIn.readCode(mask_size)) >= 0) {
      code_count++;
      if (k == LZCHash.CLEAR_CODE) {
        // Skips codes to reach end of block.
        for (; block_mode && (code_count % 8 != 0); code_count++) {
          lzwIn.readCode(mask_size);
        }
        entry.reset();
        dict.reset();
        w_code = -1;
        mask_size = LZCHash.INITIAL_MASK_SIZE;
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
