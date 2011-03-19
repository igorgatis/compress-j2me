package com.googlecode.compress_j2me.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ZStream {

  //---------------------------------------------------------------------------
  // Circular buffer feature.
  //---------------------------------------------------------------------------

  private boolean hasCircularBuffer;
  private byte[] circularBuffer;
  private int bufferMask;
  private int bufferOffset;
  private int bufferSize;

  private ZStream(boolean keepCrc, int bitsCircularBuffer) {
    this.keepCrc = keepCrc;
    if (bitsCircularBuffer > 0) {
      this.hasCircularBuffer = true;
    } else {
      bitsCircularBuffer = 8;
    }
    this.bufferMask = (1 << bitsCircularBuffer) - 1;
    this.circularBuffer = new byte[1 << bitsCircularBuffer];
  }

  //---------------------------------------------------------------------------
  // CRC feature.
  //---------------------------------------------------------------------------

  static final int[] CRC_TABLE;
  static {
    CRC_TABLE = new int[256];
    for (int n = 0; n < 256; n++) {
      int c = n;
      for (int k = 0; k < 8; k++) {
        if ((c & 0x01) != 0) {
          c = 0xEDB88320 ^ (c >>> 1);
        } else {
          c = c >>> 1;
        }
      }
      CRC_TABLE[n] = c;
    }
  }

  private boolean keepCrc;
  private int rawCrc = 0 ^ 0xFFFFFFFF;

  public void setKeepCrc(boolean keepCrc) {
    this.keepCrc = keepCrc;
  }

  void resetCrc() {
    this.rawCrc = 0 ^ 0xFFFFFFFF;
    this.keepCrc = true;
  }

  int getCrc() {
    return (int) (this.rawCrc ^ 0xFFFFFFFF);
  }

  private void updateCrc(byte ch) {
    int c = this.rawCrc;
    c = CRC_TABLE[(c & 0xFF) ^ (0xFF & ch)] ^ (c >>> 8);
    this.rawCrc = c;
  }

  //---------------------------------------------------------------------------
  // Output stream
  //---------------------------------------------------------------------------

  private OutputStream out;
  private long outputSize;

  public ZStream(OutputStream output, boolean keepCrc, int bitsCircularBuffer) {
    this(keepCrc, bitsCircularBuffer);
    this.out = output;
  }

  public long getOutputSize() {
    return outputSize;
  }

  void write(int ch) throws IOException {
    this.out.write(ch);
    this.outputSize++;
    if (this.keepCrc && ch >= 0) {
      updateCrc((byte) ch);
    }
    if (this.hasCircularBuffer) {
      this.circularBuffer[this.bufferOffset] = (byte) ch;
      this.bufferOffset = (this.bufferOffset + 1) & this.bufferMask;
      if (this.bufferSize < this.circularBuffer.length) {
        this.bufferSize++;
      }
    }
  }

  void copyFromEnd(int distance, int length) throws IOException {
    if (!this.hasCircularBuffer) {
      throw new IOException("buffer unavailable");
    }
    if (distance > this.bufferSize) {
      throw new IOException("invalid distance");
    }
    int start = this.bufferOffset - distance;
    start = (start + this.circularBuffer.length) & this.bufferMask;
    for (int i = 0; i < length; i++) {
      int idx = (start + i) & this.bufferMask;
      write(this.circularBuffer[idx]);
    }
  }

  void flush() throws IOException {
    this.out.flush();
  }

  //---------------------------------------------------------------------------
  // Output stream
  //---------------------------------------------------------------------------
  private InputStream in;

  public ZStream(InputStream input, boolean keepCrc, int bitsCircularBuffer) {
    this(keepCrc, bitsCircularBuffer);
    this.in = input;
  }

  public ZStream(InputStream input) {
    this(input, false, 0);
  }

  private int readInternal() throws IOException {
    int ch = this.in.read();
    if (this.keepCrc && ch >= 0) {
      updateCrc((byte) ch);
    }
    return ch;
  }

  int read() throws IOException {
    if (this.bitOffset != 0) {
      throw new IOException("Unaligned byte");
    }
    return readInternal();
  }

  int read(byte[] buffer, int start, int length) throws IOException {
    for (int i = 0; i < length; i++) {
      int ch = read();
      if (ch < 0) {
        return i;
      }
      buffer[start + i] = (byte) ch;
    }
    return length;
  }

  String readZeroTerminatedString() throws IOException {
    StringBuffer buffer = new StringBuffer(128);
    int ch;
    while ((ch = read()) > 0) {
      buffer.append((char) ch);
    }
    return buffer.toString();
  }

  private static void checkNoEOF(int ch) throws IOException {
    if (ch < 0) {
      throw new IOException("Unexpected EOF.");
    }
  }
  
  void skipBytes(int n) throws IOException {
    while (n-- > 0) {
      int ch = read();
      checkNoEOF(ch);
    }
  }

  int readLittleEndian(int n) throws IOException {
    int v = 0;
    for (int i = 0; i < n; i++) {
      int ch = read();
      checkNoEOF(ch);
      v |= ch << (i << 3);
    }
    return v;
  }

  //---------------------------------------------------------------------------
  // Bit stream feature.
  //---------------------------------------------------------------------------

  private int bitBuffer;
  private int bitOffset;
  
  void alignBytes() throws IOException {
    readBits(this.bitOffset);
  }

  int readBits(int numBits) throws IOException {
    while (this.bitOffset < numBits) {
      int tmp = readInternal();
      if (tmp < 0) {
        checkNoEOF(tmp);
        //return -1;
      }
      this.bitBuffer |= tmp << this.bitOffset;
      this.bitOffset += 8;
    }
    int mask = (1 << numBits) - 1;
    int code = this.bitBuffer & mask;
    this.bitBuffer >>>= numBits;
    this.bitOffset -= numBits;
    //System.err.println(numBits + ":" + code);
    return code;
  }

  void writeBits(int ch, int numBits) throws IOException {
    if (this.out == null) {
      throw new IOException("Not a output stream");
    }
    //System.err.println(numBits + ":" + code);
    int mask = (1 << numBits) - 1;
    this.bitBuffer |= (mask & ch) << this.bitOffset;
    this.bitOffset += numBits;

    while (this.bitOffset > 8) {
      write((byte) this.bitBuffer);
      this.outputSize++;
      this.bitBuffer >>>= 8;
      this.bitOffset -= 8;
    }
  }

  void end() throws IOException {
    if (this.out == null) {
      throw new IOException("Not a output stream");
    }
    while (this.bitOffset > 0) {
      write((byte) this.bitBuffer);
      this.outputSize++;
      this.bitBuffer >>>= 8;
      this.bitOffset -= 8;
    }
    flush();
  }
}
