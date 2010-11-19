package ns.foundation;

import java.io.UnsupportedEncodingException;

public class _NSStringUtilities {
  public static final String UTF8_ENCODING = "UTF-8";
  private static String _encoding = "UTF-8";

  public static String capitalizedString(String string) {
    if (string == null)
      return null;
    String capitalizedString = string;
    int length = capitalizedString.length();
    if (length > 0) {
      char character = capitalizedString.charAt(0);
      if (!(Character.isUpperCase(character))) {
        StringBuilder buffer = new StringBuilder(length);
        buffer.append(Character.toUpperCase(character));
        if (length > 1)
          buffer.append(capitalizedString.substring(1));

        capitalizedString = new String(buffer);
      }
    }
    return capitalizedString;
  }

  public static void setDefaultEncoding(String encoding) throws UnsupportedEncodingException {
    if (encoding == null) {
      _encoding = "UTF-8";
    } else {
      String testText = "encoding";
      testText.getBytes(encoding);
      _encoding = encoding;
    }
  }

  public static String defaultEncoding() {
    return _encoding;
  }

  public static byte[] bytesForString(String text, String encoding) {
    String stringEncoding = encoding == null ? defaultEncoding() : encoding;
    if (stringEncoding == null)
      return text.getBytes();
    try {
      return text == null ? null : text.getBytes(stringEncoding);
    } catch (UnsupportedEncodingException e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
  }
}
