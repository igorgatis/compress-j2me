package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WindowedStream {

  private byte[] circularBuffer;
  private int bufferMask;
  private int offset;
  private int length;

  private InputStream in;
  private OutputStream out;

  public WindowedStream(InputStream input, int bits) {
    this.in = input;
    this.bufferMask = (1 << bits) - 1;
    this.circularBuffer = new byte[1 << bits];
  }

  public WindowedStream(OutputStream output, int bits) {
    this.out = output;
    this.bufferMask = (1 << bits) - 1;
    this.circularBuffer = new byte[1 << bits];
  }

  public void write(int ch) throws IOException {
    this.out.write(ch);
    this.offset = (this.offset + 1) & this.bufferMask;
    this.circularBuffer[this.offset] = (byte) ch;
    if (this.length < this.circularBuffer.length) {
      this.length++;
    }
  }

  public void copy(int start, int length) throws IOException {
    int localOffset = (this.offset + start) & this.bufferMask;
    for (int i = 0; i < length; i++) {
      int idx = (localOffset + i) & this.bufferMask;
      write(this.circularBuffer[idx]);
    }
  }
}
