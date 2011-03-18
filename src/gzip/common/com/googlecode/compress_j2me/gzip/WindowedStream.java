package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.OutputStream;

class WindowedStream {

  private byte[] circularBuffer;
  private int bufferMask;
  private int bufferOffset;
  private int bufferSize;

  //private InputStream in;
  private OutputStream out;
  private long outputSize;

  //  public WindowedStream(InputStream input, int bits) {
  //    this.in = input;
  //    this.bufferMask = (1 << bits) - 1;
  //    this.circularBuffer = new byte[1 << bits];
  //  }

  public WindowedStream(OutputStream output, int bits) {
    this.out = output;
    this.bufferMask = (1 << bits) - 1;
    this.circularBuffer = new byte[1 << bits];
  }

  public long getOutputSize() {
    return outputSize;
  }

  public void write(int ch) throws IOException {
    this.out.write(ch);
    this.bufferOffset = (this.bufferOffset + 1) & this.bufferMask;
    this.circularBuffer[this.bufferOffset] = (byte) ch;
    if (this.bufferSize < this.circularBuffer.length) {
      this.bufferSize++;
    }
    this.outputSize++;
  }

  public void copyFromEnd(int distance, int length) throws IOException {
    if (distance > this.bufferSize) {
      throw new IOException("invalid distance");
    }
    int start = this.bufferSize - distance;
    int localOffset = (this.bufferOffset + start) & this.bufferMask;
    for (int i = 0; i < length; i++) {
      int idx = (localOffset + i) & this.bufferMask;
      write(this.circularBuffer[idx]);
    }
  }
}
