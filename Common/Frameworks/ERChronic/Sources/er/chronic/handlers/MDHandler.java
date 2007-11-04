package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.repeaters.Repeater;
import er.chronic.tags.Tag;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public abstract class MDHandler implements IHandler {
  public Span handle(Repeater<?> month, Tag<Integer> day, List<Token> timeTokens, Options options) {
    month.setStart((Calendar) options.getNow().clone());
    Span span = month.thisSpan(options.getContext());
    Calendar dayStart = Time.construct(span.getBeginCalendar().get(Calendar.YEAR), span.getBeginCalendar().get(Calendar.MONTH) + 1, day.getType().intValue());
    return Handler.dayOrTime(dayStart, timeTokens, options);
  }
}
