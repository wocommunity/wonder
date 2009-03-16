package er.chronic.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class SeparatorSlashOrDash extends Separator {
  private static final Pattern SLASH_PATTERN = Pattern.compile("^/$");
  private static final Pattern DASH_PATTERN = Pattern.compile("^-$");

  public SeparatorSlashOrDash(Separator.SeparatorType type) {
    super(type);
  }

  @Override
  public String toString() {
    return super.toString() + "-slashordash-" + getType();
  }

  public static SeparatorSlashOrDash scan(Token token, Options options) {
    Map<Pattern, Separator.SeparatorType> scanner = new HashMap<Pattern, Separator.SeparatorType>();
    scanner.put(SeparatorSlashOrDash.DASH_PATTERN, Separator.SeparatorType.DASH);
    scanner.put(SeparatorSlashOrDash.SLASH_PATTERN, Separator.SeparatorType.SLASH);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new SeparatorSlashOrDash(scanner.get(scannerItem));
      }
    }
    return null;
  }
}
