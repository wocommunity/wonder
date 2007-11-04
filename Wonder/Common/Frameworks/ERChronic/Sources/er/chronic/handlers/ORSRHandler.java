package er.chronic.handlers;

import java.util.List;

import er.chronic.Options;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class ORSRHandler extends ORRHandler {

  public Span handle(List<Token> tokens, Options options) {
    Span outerSpan = Handler.getAnchor(tokens.subList(3, 4), options);
    return handle(tokens.subList(0, 2), outerSpan, options);
  }

}
