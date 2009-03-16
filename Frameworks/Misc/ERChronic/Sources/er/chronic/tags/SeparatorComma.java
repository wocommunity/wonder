package er.chronic.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class SeparatorComma extends Separator {
  private static final Pattern COMMA_PATTERN = Pattern.compile("^,$");

  public SeparatorComma(Separator.SeparatorType type) {
    super(type);
  }

  @Override
  public String toString() {
    return super.toString() + "-comma";
  }

  public static SeparatorComma scan(Token token, Options options) {
    Map<Pattern, Separator.SeparatorType> scanner = new HashMap<Pattern, Separator.SeparatorType>();
    scanner.put(SeparatorComma.COMMA_PATTERN, Separator.SeparatorType.COMMA);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new SeparatorComma(scanner.get(scannerItem));
      }
    }
    return null;
  }

}
