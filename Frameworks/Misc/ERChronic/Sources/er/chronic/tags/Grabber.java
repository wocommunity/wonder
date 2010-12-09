package er.chronic.tags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class Grabber extends Tag<Grabber.Relative> {
  private static final Pattern THIS_PATTERN = Pattern.compile("this");
  private static final Pattern NEXT_PATTERN = Pattern.compile("next");
  private static final Pattern LAST_PATTERN = Pattern.compile("last");

  public static enum Relative {
    LAST, NEXT, THIS
  }

  public Grabber(Grabber.Relative type) {
    super(type);
  }

  public static List<Token> scan(List<Token> tokens, Options options) {
    for (Token token : tokens) {
      Grabber t = Grabber.scanForAll(token, options);
      if (t != null) {
        token.tag(t);
      }
    }
    return tokens;
  }

  public static Grabber scanForAll(Token token, Options options) {
    Map<Pattern, Grabber.Relative> scanner = new HashMap<Pattern, Grabber.Relative>();
    scanner.put(Grabber.LAST_PATTERN, Grabber.Relative.LAST);
    scanner.put(Grabber.NEXT_PATTERN, Grabber.Relative.NEXT);
    scanner.put(Grabber.THIS_PATTERN, Grabber.Relative.THIS);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new Grabber(scanner.get(scannerItem));
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "grabber-" + getType();
  }
}
