package er.chronic.tags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class TimeZone extends Tag<Object> {
  private static final Pattern TIMEZONE_PATTERN = Pattern.compile("[pmce][ds]t");
  private static final Pattern TMINUS_PATTERN = Pattern.compile("(tzminus)?\\d{4}");
  private static final Object TZ = new Object();
  
  public TimeZone() {
    super(null);
  }

  public static List<Token> scan(List<Token> tokens, Options options) {
    for (Token token : tokens) {
      TimeZone t = TimeZone.scanForAll(token, options);
      if (t != null) {
        token.tag(t);
      }
    }
    return tokens;
  }

  public static TimeZone scanForAll(Token token, Options options) {
    Map<Pattern, Object> scanner = new HashMap<>();
    scanner.put(TimeZone.TIMEZONE_PATTERN, TZ);
    scanner.put(TimeZone.TMINUS_PATTERN, TZ);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new TimeZone();
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "timezone";
  }
}
