package er.chronic.handlers;

import java.util.List;

import er.chronic.Options;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public interface IHandler {
  public Span handle(List<Token> tokens, Options options);
}
