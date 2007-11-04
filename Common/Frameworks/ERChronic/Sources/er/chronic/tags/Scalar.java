package er.chronic.tags;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.utils.StringUtils;
import er.chronic.utils.Token;

public class Scalar extends Tag<Integer> {
  private static final Pattern SCALAR_PATTERN = Pattern.compile("^\\d*$");
  public static Set<String> TIMES = new HashSet<String>();

  static {
    Scalar.TIMES.add("am");
    Scalar.TIMES.add("pm");
    Scalar.TIMES.add("morning");
    Scalar.TIMES.add("afternoon");
    Scalar.TIMES.add("evening");
    Scalar.TIMES.add("night");
  }

  public Scalar(Integer type) {
    super(type);
  }

  public static List<Token> scan(List<Token> tokens, Options options) {
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      Token postToken = null;
      if (i < tokens.size() - 1) {
        postToken = tokens.get(i + 1);
      }
      Scalar t;
      t = Scalar.scan(token, postToken, options);
      if (t != null) {
        token.tag(t);
      }
      t = ScalarDay.scan(token, postToken, options);
      if (t != null) {
        token.tag(t);
      }
      t = ScalarMonth.scan(token, postToken, options);
      if (t != null) {
        token.tag(t);
      }
      t = ScalarYear.scan(token, postToken, options);
      if (t != null) {
        token.tag(t);
      }
    }
    return tokens;
  }

  public static Scalar scan(Token token, Token postToken, Options options) {
    if (Scalar.SCALAR_PATTERN.matcher(token.getWord()).matches()) {
      if (token.getWord() != null && token.getWord().length() > 0 && !(postToken != null && Scalar.TIMES.contains(postToken.getWord()))) {
        return new Scalar(Integer.valueOf(token.getWord()));
      }
    }
    else {
      Integer intStrValue = StringUtils.integerValue(token.getWord());
      if (intStrValue != null) {
        return new Scalar(intStrValue);
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "scalar";
  }
}
