package ns.foundation;

import java.util.Arrays;


public class NSMutableData extends NSData {

  public NSMutableData() {
    this(128);
  }

  public NSMutableData(NSData aData) {
    super(aData.immutableBytes(), aData.immutableRange(), false);
  }

  public NSMutableData(int size) {
    super(new byte[size]);
  }
  
  public NSMutableData(byte bytes[]) {
    super(bytes);
  }
  
  public NSMutableData(byte bytes[], NSRange range) {
    super(bytes, range);
  }
  
  public NSMutableData(byte bytes[], NSRange range, boolean noCopy) {
    super(bytes, range, noCopy);
  }

  public void setLength(int length) {
    byte[] data = new byte[ length ]; // inits to zeroes
    int limit = length > _bytes.length ? _bytes.length : length;
    System.arraycopy(_bytes, 0, data, 0, limit);
    this._bytes = data;
  }

  /**
   * Appends the specified data to the end of this data.
   */
  public void appendData(NSData data) {
    appendBytes(data.bytes());
  }

  public void appendByte(byte b) {
    setLength(_bytes.length + 1);
    _bytes[_bytes.length - 1] = b;
  }

  public void appendBytes(byte[] b) {
    int origLen = _bytes.length;
    setLength(origLen + b.length);
    System.arraycopy(b, 0, _bytes, origLen, b.length);
  }

  /**
   * Increases the size of the byte array by the specified amount.
   */
  public void increaseLengthBy(int increment) {
    setLength(length() + increment);
  }

  public void resetBytesInRange(NSRange aRange) {
    Arrays.fill(_bytes, aRange.location(), aRange.maxRange(), (byte)0);
  }

  public void setData(NSData aData) {
    _bytes = aData.bytes();
  }
}
