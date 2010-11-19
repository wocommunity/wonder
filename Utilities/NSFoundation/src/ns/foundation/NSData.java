package ns.foundation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class NSData implements Serializable, Cloneable {

  public static final NSData EmptyData = new NSData();
  protected byte[] _bytes;
  protected NSRange _range;

  public NSData() {
    this(new byte[0], NSRange.ZeroRange, false);
  }

  public NSData(byte[] bytes) {
    this(bytes, new NSRange(0, bytes.length), false);
  }

  public NSData(byte[] bytes, int offset, int count) {
    this(bytes,new NSRange(offset, count), false);
  }

  public NSData(byte[] bytes, NSRange range) {
    this(bytes, range, false);
  }

  public NSData(byte[] bytes, NSRange range, boolean noCopy) {
    if (noCopy) {
      _bytes = bytes;
      _range = range;
    } else {
      _bytes = new byte[range.length()];
      for (int i = 0, j = range.location(); i < bytes.length; i++, j++) {
        _bytes[i] = bytes[j];
      }
      _range = new NSRange(0, _bytes.length);
    }
  }

  public NSData(NSData otherData) {
    this(otherData.bytes());
  }

  // private static byte[] bytesWithEncoding(String value, String encoding) {
  // try {
  // return value.getBytes(encoding);
  // } catch (UnsupportedEncodingException e) {
  // throw new RuntimeException(e);
  // }
  // }
  //	
  // public NSData(String value, String encoding) throws UnsupportedEncodingException {
  // this(bytesWithEncoding(value, encoding));
  // }

  public byte[] bytes() {
    return bytes(0, _range.length());
  }

  public byte[] bytes(int offset, int length) {
    byte[] result = new byte[length];
    for (int i = 0, j = _range.location() + offset; i < result.length; i++, j++) {
      result[i] = _bytes[j];
    }
    return result;
  }

  public byte[] bytes(NSRange range) {
    return bytes(range.location(), range.length());
  }

  protected byte[] bytesNoCopy() {
    return _bytes;
  }

  public int _offset() {
    return _range.location();
  }
  
  public void writeToStream(OutputStream stream) throws IOException {
    stream.write(bytesNoCopy(), _offset(), length());
  }

  @Override
  public Object clone() {
    return this;
  }

  protected byte[] immutableBytes() {
    return _bytes;
  }

  protected NSRange immutableRange() {
    return _range;
  }

  public boolean isEqualToData(NSData otherData) {
    return this.equals(otherData);
  }

  public int length() {
    return _range.length();
  }

  public NSData subdataWithRange(NSRange range) {
    return new NSData(bytes(range));
  }

  // public void writeToStream(OutputStream stream) throws IOException {
  // stream.write(_bytes);
  // }

  @Override
  public String toString() {
    return Arrays.toString(bytes());
  }
  
  public String _hexString() {
    byte bytes[] = bytes();
    StringBuilder result = new StringBuilder(2 * length());
    for(int i = 0; i < length(); i++) {
      int b = bytes[i] & 255;
      result.append(Integer.toHexString(b).toUpperCase());
    }

    return result.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_bytes);
    result = prime * result + ((_range == null) ? 0 : _range.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NSData other = (NSData) obj;
    if (!Arrays.equals(_bytes, other._bytes)) {
      return false;
    }
    if (_range == null) {
      if (other._range != null) {
        return false;
      }
    } else if (!_range.equals(other._range)) {
      return false;
    }
    return true;
  }

}
