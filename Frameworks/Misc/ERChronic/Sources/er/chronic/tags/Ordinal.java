package er.chronic.tags;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class Ordinal extends Tag<Integer> {
  public static Pattern ORDINAL_PATTERN = Pattern.compile("^(\\d*)(st|nd|rd|th)$");

  public Ordinal(Integer type) {
    super(type);
  }

  public static List<Token> scan(List<Token> tokens, Options options) {
    for (Token token : tokens) {
      Ordinal t;
      t = Ordinal.scan(token, options);
      if (t != null) {
        token.tag(t);
      }
      t = OrdinalDay.scan(token);
      if (t != null) {
        token.tag(t);
      }
    }
    return tokens;
  }

  public static Ordinal scan(Token token, Options options) {
    Matcher ordinalMatcher = ORDINAL_PATTERN.matcher(token.getWord());
    if (ordinalMatcher.find()) {
      return new Ordinal(Integer.valueOf(ordinalMatcher.group(1)));
    }
    return null;
  }

  @Override
  public String toString() {
    return "ordinal";
  }
}
