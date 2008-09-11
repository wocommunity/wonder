package er.chronic.tags;

import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.Token;

public class ScalarYear extends Scalar {
  // DIFF: Changed the year pattern to only allow [12]xxx or [0456789]x -- so 1989, or 78 but not 12 
  public static final Pattern YEAR_PATTERN = Pattern.compile("^([12]\\d\\d\\d|[0456789]\\d)$");

  public ScalarYear(Integer type) {
    super(type);
  }

  @Override
  public String toString() {
    return super.toString() + "-year-" + getType();
  }

  public static ScalarYear scan(Token token, Token postToken, Options options) {
    if (ScalarYear.YEAR_PATTERN.matcher(token.getWord()).matches()) {
      int scalarValue = Integer.parseInt(token.getWord());
      if (!(postToken != null && Scalar.TIMES.contains(postToken.getWord()))) {
//        if (scalarValue <= 37) {
//          scalarValue += 2000;
//        }
//        else if (scalarValue <= 137 && scalarValue >= 69) {
//          scalarValue += 1900;
//        }
        return new ScalarYear(Integer.valueOf(scalarValue));
      }
    }
    return null;
  }
}
