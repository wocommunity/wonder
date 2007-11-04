package er.chronic.tags;

import java.util.List;

import er.chronic.Options;
import er.chronic.utils.Token;

public class Separator extends Tag<Separator.SeparatorType> {
  public static enum SeparatorType {
    COMMA, DASH, SLASH, AT, NEWLINE, IN
  }

  public Separator(Separator.SeparatorType type) {
    super(type);
  }

  public static List<Token> scan(List<Token> tokens, Options options) {
    for (Token token : tokens) {
      Separator t;
      t = SeparatorComma.scan(token, options);
      if (t != null) {
        token.tag(t);
      }
      t = SeparatorSlashOrDash.scan(token, options);
      if (t != null) {
        token.tag(t);
      }
      t = SeparatorAt.scan(token, options);
      if (t != null) {
        token.tag(t);
      }
      t = SeparatorIn.scan(token, options);
      if (t != null) {
        token.tag(t);
      }
    }
    return tokens;
  }

  @Override
  public String toString() {
    return "separator";
  }
}
