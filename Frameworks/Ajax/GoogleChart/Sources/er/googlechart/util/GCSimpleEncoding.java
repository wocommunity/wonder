package er.googlechart.util;

/**
 * Simple encoding (see http://code.google.com/apis/chart/#simple)
 * 
 * @author mschrag
 */
public class GCSimpleEncoding extends GCMappedEncoding {
  private static String SIMPLE_ENCODING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  @Override
  protected String encodingKey() {
    return "s";
  }

  @Override
  protected int numberOfEncodingValues() {
    return 26 + 26 + 10;
  }

  @Override
  protected String missingValue() {
    return "_";
  }

  @Override
  protected String encode(int value) {
    if (value < 0 || value >= numberOfEncodingValues()) {
      throw new IllegalArgumentException("The value " + value + " cannot be encoded with Simple Encoding.");
    }
    return String.valueOf(GCSimpleEncoding.SIMPLE_ENCODING.charAt(value));
  }
}