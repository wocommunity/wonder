package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.repeaters.Repeater;
import er.chronic.tags.Ordinal;
import er.chronic.tags.Pointer;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public abstract class ORRHandler implements IHandler {
  public Span handle(List<Token> tokens, Span outerSpan, Options options) {
    Repeater<?> repeater = tokens.get(1).getTag(Repeater.class);
    repeater.setStart(Time.cloneAndAdd(outerSpan.getBeginCalendar(), Calendar.SECOND, -1));
    Integer ordinalValue = tokens.get(0).getTag(Ordinal.class).getType();
    Span span = null;
    for (int i = 0; i < ordinalValue.intValue(); i++) {
      span = repeater.nextSpan(Pointer.PointerType.FUTURE);
      if (span.getBegin() > outerSpan.getEnd()) {
        span = null;
        break;
      }
    }
    return span;
  }
}
