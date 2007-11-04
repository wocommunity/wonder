package er.chronic.tags;

import java.util.regex.Matcher;

import er.chronic.utils.Token;

public class OrdinalDay extends Ordinal {
  public OrdinalDay(Integer type) {
    super(type);
  }

  @Override
  public String toString() {
    return super.toString() + "-day-" + getType();
  }

  public static OrdinalDay scan(Token token) {
    Matcher ordinalMatcher = Ordinal.ORDINAL_PATTERN.matcher(token.getWord());
    if (ordinalMatcher.find()) {
      int ordinalValue = Integer.parseInt(ordinalMatcher.group(1));
      if (!(ordinalValue > 31)) {
        return new OrdinalDay(Integer.valueOf(ordinalValue));
      }
    }
    return null;
  }
}
