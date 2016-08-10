package er.ajax.example2.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

public class StringUtils {
  /**
   * Returns a trimmed version of the string or null if the original string was null.
   * 
   * @param str the string to trim
   * @return a trimmed version of the string (or null if str was null)
   */
  public static String trim(String str) {
    String trimmedStr = str;
    if (trimmedStr != null) {
      trimmedStr = str.trim();
    }
    return trimmedStr;
  }

  public static String randomString(int length) {
	StringBuilder foo = new StringBuilder();
    for (int ii = 0; ii < length; ii++) {
      foo.append((char) (new Random().nextInt(74) + 48));
    }
    return foo.toString();
  }

  public static String randomStringAlphaNumeric(int length) {
    return randomStringAlphaNumeric(length, null);
  }

  public static String randomStringAlphaNumeric(int length, String banCharacters) {
	StringBuilder randomBuffer = new StringBuilder();
    do {
      randomBuffer.append(UUID.randomUUID().toString());
      if (banCharacters != null) {
        randomBuffer = new StringBuilder(randomBuffer.toString().replaceAll("[" + banCharacters + "]", ""));
      }
    } while (randomBuffer.length() < length);
    String randomStr = randomBuffer.toString();
    return randomStr.substring(randomStr.length() - length, randomStr.length());
  }

  /**
   * Returns a random string that is not confusing for someone to read.  For instance
   * it removes 0, o, O, 1, and l, which might otherwise be confusing to people.
   * 
   * @param length the desired length
   * @return an unconfusing string
   */
  public static String randomUnconfusingString(int length) {
    return randomStringAlphaNumeric(length, "0oO1l");
  }

  public static String strip(String str) {
    String stripped = str;
    if (stripped != null) {
      stripped = stripped.replaceAll("<[^>]*>", " ");
      stripped = stripped.replaceAll("\\s+", " ");
      stripped = stripped.replaceAll("&#8217;", "'");
      stripped = stripped.replaceAll("&#169;", "(C)");
      stripped = stripped.replaceAll("&#215;", " x ");
      stripped = stripped.replaceAll("&#8230;", "...");
      stripped = stripped.replaceAll("&#8212;", " -- ");
      stripped = stripped.replaceAll("&#8211;", " - ");
      stripped = stripped.replaceAll("&#8220;", "\"");
      stripped = stripped.replaceAll("&#8221;", "\"");
      stripped = stripped.replaceAll("&#174;", "(C)");
      stripped = stripped.replaceAll("&#174;", "(R)");
      stripped = stripped.replaceAll("&#8482;", "(TM)");
      stripped = stripped.trim();
    }
    return stripped;
  }

  // From http://www.rgagnon.com/javadetails/java-0306.html
  public static String stringToHTMLString(String string) {
    String htmlString;
    if (string == null) {
      htmlString = null;
    }
    else {
      int len = string.length();
      StringBuilder sb = new StringBuilder(len);
      // true if last char was blank
      boolean lastWasBlankChar = false;
      char c;

      for (int i = 0; i < len; i++) {
        c = string.charAt(i);
        if (c == ' ') {
          // blank gets extra work,
          // this solves the problem you get if you replace all
          // blanks with &nbsp;, if you do that you loss 
          // word breaking
          if (lastWasBlankChar) {
            lastWasBlankChar = false;
            sb.append("&nbsp;");
          }
          else {
            lastWasBlankChar = true;
            sb.append(' ');
          }
        }
        else {
          lastWasBlankChar = false;
          //
          // HTML Special Chars
          if (c == '"') {
            sb.append("&quot;");
          }
          else if (c == '&') {
            sb.append("&amp;");
          }
          else if (c == '<') {
            sb.append("&lt;");
          }
          else if (c == '>') {
            sb.append("&gt;");
          }
          else if (c == '\n') {
            // Handle Newline
            sb.append("<br/>");
          }
          else {
            int ci = 0xffff & c;
            if (ci < 160)
              // nothing special only 7 Bit
              sb.append(c);
            else {
              // Not 7 Bit use the unicode system
              sb.append("&#");
              sb.append(Integer.valueOf(ci).toString());
              sb.append(';');
            }
          }
        }
      }
      htmlString = sb.toString();
    }
    return htmlString;
  }

  public static String stringToJavascriptString(String string) {
    if (string != null) {
      string = string.replaceAll("\n", "\\\\n");
      string = string.replaceAll("\"", "\\\"");
      string = string.replaceAll("\r", "");
    }
    return string;
  }

  public static String stringToXMLString(String string) {
    String _xmlString = "";
    if (string == null) {
      _xmlString = null;
    }
    else {
      int len = string.length();
      StringBuilder sb = new StringBuilder(len);
      char c;
      for (int i = 0; i < len; i++) {
        c = string.charAt(i);
        // XML Special Chars
        if (c == '"') {
          sb.append("&quot;");
        }
        else if (c == '&') {
          sb.append("&amp;");
        }
        else if (c == '<') {
          sb.append("&lt;");
        }
        else if (c == '>') {
          sb.append("&gt;");
        }
        else if (c == '\'') {
          sb.append("&apos");
        }
        else {
          sb.append(c);
        }
      }
      _xmlString = sb.toString();
    }
    return new String(string);
  }

  public static void appendField(StringBuffer _buffer, String _label, String _value) {
    _buffer.append(_label);
    _buffer.append(": ");
    _buffer.append(_value);
    _buffer.append("\n");
  }

  public static String md5(String _encrypt) {
    try {
      byte[] _digest = MessageDigest.getInstance("MD5").digest(_encrypt.getBytes());

      StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < _digest.length; i++) {
        String hexDigitStr = Integer.toHexString(0xFF & _digest[i]);
        if (hexDigitStr.length() == 1) {
          hexString.append("0");
        }
        hexString.append(hexDigitStr);
      }

      return (hexString.toString());
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Every VM should have MD5.");
    }
  }

  public static String toLowerCase(String _str) {
    return (_str == null) ? null : _str.toLowerCase();
  }

  public static boolean quicksilverContains(String _str, String _searchString) {
    boolean equals;
    if (_str == null) {// || _searchString == null || _searchString.length() == 0) {
      equals = false;
    }
    else {
      equals = true;
      if (_searchString != null && _searchString.length() > 0) {
        int searchStringLength = _searchString.length();
        int strLength = _str.length();
        int strPos = 0;
        for (int searchStringPos = 0; equals && searchStringPos < searchStringLength; searchStringPos++) {
          char searchStringCh = Character.toLowerCase(_searchString.charAt(searchStringPos));
          boolean searchStringChFound = false;
          for (; !searchStringChFound && strPos < strLength; strPos++) {
            char strCh = _str.charAt(strPos);
            searchStringChFound = (Character.toLowerCase(strCh) == searchStringCh);
          }
          if (!searchStringChFound) {
            equals = false;
          }
        }
      }
    }
    return equals;
  }

  @SuppressWarnings("unchecked")
  public static NSArray filteredArrayWithQuicksilverContains(NSArray _elements, String _displayKey, String _contains) {
    NSMutableArray matchingElements = new NSMutableArray();
    Enumeration elementsEnum = _elements.objectEnumerator();
    while (elementsEnum.hasMoreElements()) {
      Object element = elementsEnum.nextElement();
      String displayStr = (String) NSKeyValueCoding.Utility.valueForKey(element, _displayKey);
      if (StringUtils.quicksilverContains(displayStr, _contains)) {
        matchingElements.addObject(element);
      }
    }
    return matchingElements;
  }

  public static void joinStrings(StringBuffer _buffer, List _strings, String _joinWith) {
    Iterator stringsIter = _strings.iterator();
    while (stringsIter.hasNext()) {
      String str = (String) stringsIter.next();
      _buffer.append(str);
      if (stringsIter.hasNext()) {
        _buffer.append(_joinWith);
      }
    }
  }

  public static String toErrorString(Throwable _throwable) {
	StringBuilder messageBuffer = new StringBuilder();
    boolean foundInternalError = false;
    Throwable t = _throwable;
    while (t != null) {
      Throwable oldThrowable = ExceptionUtils.getMeaningfulException(t);
      String message = t.getMessage();
      if (message == null) {
        if (!foundInternalError) {
          message = "Your request produced an error.";
          foundInternalError = true;
        }
        else {
          message = "";
        }
      }
      message = message.replaceAll("<[^>]+>", "");
      message = message.trim();
      messageBuffer.append(message);
      if (!message.endsWith(".")) {
        messageBuffer.append(". ");
      }
      else {
        messageBuffer.append(" ");
      }
      t = oldThrowable.getCause();
      if (t == oldThrowable) {
        t = null;
      }
    }
    return messageBuffer.toString();
  }

  public static String compact(String string) {
    if (string != null) {
      return string.replace(" ", "");
    }
    return null;
  }

  public static boolean empty(String str) {
    return ComparisonUtils.empty(str);
  }

  public static boolean empty(String str, boolean trim) {
    return ComparisonUtils.empty(str, trim);
  }

  public static boolean notEmpty(String str) {
    return ComparisonUtils.notEmpty(str);
  }

  public static boolean notEmpty(String str, boolean trim) {
    return ComparisonUtils.notEmpty(str, trim);
  }

  public static String fullName(String firstName, String lastName, String defaultDisplayName) {
	StringBuilder displayNameBuffer = new StringBuilder();
    boolean hasFirstName = false;
    if (ComparisonUtils.notEmpty(firstName)) {
      displayNameBuffer.append(firstName);
      hasFirstName = true;
    }
    if (ComparisonUtils.notEmpty(lastName)) {
      if (hasFirstName) {
        displayNameBuffer.append(' ');
      }
      displayNameBuffer.append(lastName);
    }
    if (displayNameBuffer.length() == 0 && defaultDisplayName != null) {
      displayNameBuffer.append(defaultDisplayName);
    }
    return displayNameBuffer.toString();
  }
}
