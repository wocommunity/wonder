package er.woinstaller.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that delegates requests to the underlying
 * RandomAccessFile, making sure that only bytes from a certain
 * range can be read.
 */
public class BoundedInputStream extends InputStream {
  private InputStream _inputStream;
  private long _remaining;

  public BoundedInputStream(InputStream inputStream, long start, long remaining) throws IOException {
    _inputStream = inputStream;
    long skip = start;
    while (skip > 0) {
    	skip -= _inputStream.skip(skip);
    }
    _remaining = remaining;
  }

  @Override
  public int read() throws IOException {
    if (_remaining-- <= 0) {
      return -1;
    }
    return _inputStream.read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (_remaining <= 0) {
      return -1;
    }

    if (len <= 0) {
      return 0;
    }

    if (len > _remaining) {
      len = (int) _remaining;
    }
    int ret = -1;
    ret = _inputStream.read(b, off, len);
    if (ret > 0) {
      _remaining -= ret;
    }
    return ret;
  }

}
