package er.ajax.example2.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

public class StringHelper {
  public String append(String str, Object obj) {
    return str + obj;
  }

  public String highlight(String str, String search) {
    return highlight(str, search, "highlight");
  }

  public String highlight(String str, String search, String className) {
    String result;
    try {
      Pattern pattern = Pattern.compile("(" + search + ")", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(str);
      result = matcher.replaceAll("<span class = \"" + className + "\">$1</span>");
    }
    catch (PatternSyntaxException e) {
      result = str;
    }
    return result;
  }

  public String shortClassName(String className) {
    String shortClassName = className;
    int lastDotIndex = className.lastIndexOf('.');
    if (lastDotIndex != -1) {
      shortClassName = className.substring(lastDotIndex + 1);
    }
    return shortClassName;
  }

  public String pluralize(String str) {
    return ERXLocalizer.currentLocalizer().plurifiedString(str, 2);
  }

  public String pluralize(String str, int count) {
    return ERXLocalizer.currentLocalizer().plurifiedString(str, count);
  }

  /**
   * Truncates a string to the given length then appends a character to the end three times.<br/>  
   * If characters are not cut off the end, the origional string is returned.  
   * <p><b>Example:</b> truncate("123456", 3, '$') --> "123$$$"  |  truncate("123", 6, '$') --> "123" </p>
   * @param str - input string
   * @param length - number of characters from str to display including periods.
   * @param repeater - the character to be repeated 3 times at the end
   * @return a fancy string, baby
   */
  public String truncate(String str, int length, char repeater) {
    String value;
    if (str == null || str.length() <= length) {
      value = str;
    }
    else {
      value = str.substring(0, length) + repeater + repeater + repeater;
    }
    return value;
  }

  /**
   * Truncates a string to the given length then appends '...' <br/>
   * If characters are not cut off the end, the origional string is returned.
   * <p><b>Example:</b> truncate("123456", 3) --> "123..."  |  truncate("123", 6) --> "123" </p>
   * @param str - input string
   * @param length - number of characters from str to display including periods.
   * @return a fancy string, baby
   */
  public String truncate(String str, int length) {
    String value;
    if (str == null || str.length() <= length) {
      value = str;
    }
    else {
      value = str.substring(0, length) + "...";
    }
    return value;
  }

  public String humanize(String str, boolean lowercase) {
    String results = ERXStringUtilities.displayNameForKey(str);
    if (lowercase) {
      results = results.toLowerCase();
    }
    return results;
  }

  public String humanize(String str) {
    return humanize(str, true);
  }
  
  public String capitalize(String str) {
  	return ERXStringUtilities.capitalize(str);
  }
}
