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
    int magic = this.in.read() << 8 | this.in.read();
    if (magic != LZWHash.COMPRESS_MAGIC_NUMBER) {
      throw new RuntimeException("Bad magic number " + magic);
    }
    int flags = this.in.read();
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
