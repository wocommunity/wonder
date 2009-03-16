package er.googlechart.util;

/**
 * Extended encoding (see http://code.google.com/apis/chart/#extended)
 * 
 * @author mschrag
 */
public class GCExtendedEncoding extends GCMappedEncoding {
  private static String EXTENDED_ENCODING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.";

  @Override
  protected String encodingKey() {
    return "e";
  }

  @Override
  protected int numberOfEncodingValues() {
    return (26 + 26 + 10 + 2) * (26 + 26 + 10 + 2);
  }

  @Override
  protected String missingValue() {
    return "__";
  }

  @Override
  protected String encode(int value) {
    if (value < 0 || value >= numberOfEncodingValues()) {
      throw new IllegalArgumentException("The value " + value + " cannot be encoded with Extended Encoding.");
    }
    int numberOfRows = GCExtendedEncoding.EXTENDED_ENCODING.length();
    int row = value / numberOfRows;
    int col = value % numberOfRows;
    return String.valueOf(GCExtendedEncoding.EXTENDED_ENCODING.charAt(row)) + String.valueOf(GCExtendedEncoding.EXTENDED_ENCODING.charAt(col));
  }
}