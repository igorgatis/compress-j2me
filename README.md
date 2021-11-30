# What is this project about?

This project contains a set java implementations of commonly used compression algorithms.

Libraries were carefully written to be compatible with both J2SE and J2ME platforms, keeping code size as small as possible.

*Users*: please [let us know](mailto:compress-j2me@googlegroups.com) which projects this library is being used on. Also, make sure file bugs.

# GZIP & Deflate

Smallest java full implementation of GZIP algorithm.

## Status
Decompression and compression are available at this time. Library size is 6.5k.

Compression algorithm uses [fixed fixed Huffman block](http://www.gzip.org/zlib/rfc-deflate.html). [dyn Dynamic Huffman block](http://www.gzip.org/zlib/rfc-deflate.html) is on the way. 

## Compatibility
  * 100% compatible with both J2SE and J2ME.
  * 100% compatible with [Deflate](http://www.gzip.org/zlib/rfc-deflate.html) and [GZIP](http://www.gzip.org/zlib/rfc-gzip.html) RFCs.
  * Produces and checks CRCs.

# LZC (aka unix compress)

Smallest java implementation of LZC algorithm.

## Comes in two flavors
  * `LZWOutputStream` and `LZWInputStream`: 4.3k bytes
  * Static class `LZWStream` (aka alternative API): 3.5k bytes.

## Compatibility
  * 100% compatible with both J2SE and J2ME.
  * 100% compatible with unix compress (.Z file) format.

# Helping

[Checkout the code](https://github.com/igorgatis/compress-j2me) and send me patches.
