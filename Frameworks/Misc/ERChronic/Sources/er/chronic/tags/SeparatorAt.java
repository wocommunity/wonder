package er.chronic.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class SeparatorAt extends Separator {
  private static final Pattern AT_PATTERN = Pattern.compile("^(at|@)$");

  public SeparatorAt(Separator.SeparatorType type) {
    super(type);
  }

  @Override
  public String toString() {
    return super.toString() + "-at";
  }

  public static SeparatorAt scan(Token token, Options options) {
    Map<Pattern, Separator.SeparatorType> scanner = new HashMap<>();
    scanner.put(SeparatorAt.AT_PATTERN, Separator.SeparatorType.AT);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new SeparatorAt(scanner.get(scannerItem));
      }
    }
    return null;
  }
}
