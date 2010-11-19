package ns.foundation;

public abstract class NSCoder {
  public abstract void encodeBoolean(boolean paramBoolean);

  public abstract void encodeByte(byte paramByte);

  public abstract void encodeBytes(byte[] paramArrayOfByte);

  public abstract void encodeChar(char paramChar);

  public abstract void encodeShort(short paramShort);

  public abstract void encodeInt(int paramInt);

  public abstract void encodeLong(long paramLong);

  public abstract void encodeFloat(float paramFloat);

  public abstract void encodeDouble(double paramDouble);

  public abstract void encodeObject(Object paramObject);

  public abstract void encodeClass(Class<?> paramClass);

  public abstract void encodeObjects(Object... paramArrayOfObject);

  public abstract boolean decodeBoolean();

  public abstract byte decodeByte();

  public abstract byte[] decodeBytes();

  public abstract char decodeChar();

  public abstract short decodeShort();

  public abstract int decodeInt();

  public abstract long decodeLong();

  public abstract float decodeFloat();

  public abstract double decodeDouble();

  public abstract Object decodeObject();

  public abstract Class<?> decodeClass();

  public abstract Object[] decodeObjects();

  public void finishCoding() {
  }
}