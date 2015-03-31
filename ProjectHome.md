# What is this project about? #

This project contains a set java implementations of commonly used compression algorithms.

Libraries were carefully written to be compatible with both J2SE and J2ME platforms, keeping code size as small as possible.

**Users**: please [let us know](mailto:compress-j2me@googlegroups.com) which projects this library is being used on. Also, make sure file bugs.

# GZIP & Deflate #

Smallest java full implementation of GZIP algorithm.

### Status ###
Decompression and compression are available at this time. Library size is 6.5k.

Compression algorithm uses [fixed Huffman block](http://www.gzip.org/zlib/rfc-deflate.html#fixed). [Dynamic Huffman block](http://www.gzip.org/zlib/rfc-deflate.html#dyn) is on the way.

### Compatibility ###
  * 100% compatible with both J2SE and J2ME.
  * 100% compatible with [Deflate](http://www.gzip.org/zlib/rfc-deflate.html) and [GZIP](http://www.gzip.org/zlib/rfc-gzip.html) RFCs.
  * Produces and checks CRCs.


# LZC (aka unix compress) #

Smallest java implementation of LZC algorithm.

### Comes in two flavors ###
  1. `LZWOutputStream` and `LZWInputStream`: 4.3k bytes
  1. Static class `LZWStream` (aka alternative API): 3.5k bytes.

### Compatibility ###
  * 100% compatible with both J2SE and J2ME.
  * 100% compatible with unix compress (.Z file) format.

# Helping #

[Checkout the code](http://code.google.com/p/compress-j2me/source/checkout) and send me patches.

Or just buy me a beer: [![](https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8XMHFB47JSKK4&lc=US&item_name=Igor%20Gatis&item_number=compress%2dj2me&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted)