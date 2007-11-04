package er.chronic.tags;

import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class ScalarMonth extends Scalar {
  private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d\\d?$");

  public ScalarMonth(Integer type) {
    super(type);
  }

  @Override
  public String toString() {
    return super.toString() + "-month-" + getType();
  }

  public static ScalarMonth scan(Token token, Token postToken, Options options) {
    if (ScalarMonth.MONTH_PATTERN.matcher(token.getWord()).matches()) {
      int scalarValue = Integer.parseInt(token.getWord());
      if (!(scalarValue > 12 || (postToken != null && Scalar.TIMES.contains(postToken.getWord())))) {
        return new ScalarMonth(Integer.valueOf(scalarValue));
      }
    }
    return null;
  }
}
