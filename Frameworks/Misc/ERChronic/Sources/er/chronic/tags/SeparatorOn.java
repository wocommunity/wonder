package er.chronic.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class SeparatorOn extends Separator {
  private static final Pattern ON_PATTERN = Pattern.compile("^(on|@)$");

  public SeparatorOn(Separator.SeparatorType type) {
    super(type);
  }

  @Override
  public String toString() {
    return super.toString() + "-on";
  }

  public static SeparatorOn scan(Token token, Options options) {
    Map<Pattern, Separator.SeparatorType> scanner = new HashMap<>();
    scanner.put(SeparatorOn.ON_PATTERN, Separator.SeparatorType.ON);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new SeparatorOn(scanner.get(scannerItem));
      }
    }
    return null;
  }
}
