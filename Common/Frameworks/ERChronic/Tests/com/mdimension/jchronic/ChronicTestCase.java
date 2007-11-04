package com.mdimension.jchronic;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.mdimension.jchronic.handlers.Handler;
import com.mdimension.jchronic.repeaters.EnumRepeaterDayPortion;
import com.mdimension.jchronic.repeaters.RepeaterDayName;
import com.mdimension.jchronic.repeaters.RepeaterDayPortion;
import com.mdimension.jchronic.repeaters.RepeaterTime;
import com.mdimension.jchronic.repeaters.RepeaterDayName.DayName;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;
import com.mdimension.jchronic.utils.Token;

public class ChronicTestCase extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testPostNormalizeAmPmAliases() {
    List<Token> tokens = new LinkedList<Token>();

    // affect wanted patterns
    tokens.add(new Token("5:00"));
    tokens.add(new Token("morning"));
    tokens.get(0).tag(new RepeaterTime("5:00"));
    tokens.get(1).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.MORNING));

    assertEquals(RepeaterDayPortion.DayPortion.MORNING, tokens.get(1).getTags().get(0).getType());

    tokens = Handler.dealiasAndDisambiguateTimes(tokens, new Options());

    assertEquals(RepeaterDayPortion.DayPortion.AM, tokens.get(1).getTags().get(0).getType());
    assertEquals(2, tokens.size());

    // don't affect unwanted patterns
    tokens = new LinkedList<Token>();
    tokens.add(new Token("friday"));
    tokens.add(new Token("morning"));
    tokens.get(0).tag(new RepeaterDayName(DayName.FRIDAY));
    tokens.get(1).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.MORNING));

    assertEquals(RepeaterDayPortion.DayPortion.MORNING, tokens.get(1).getTags().get(0).getType());

    tokens = Handler.dealiasAndDisambiguateTimes(tokens, new Options());

    assertEquals(RepeaterDayPortion.DayPortion.MORNING, tokens.get(1).getTags().get(0).getType());
    assertEquals(2, tokens.size());
  }

  public void testGuess() {
    Span span;

    span = new Span(Time.construct(2006, 8, 16, 0), Time.construct(2006, 8, 17, 0));
    assertEquals(Time.construct(2006, 8, 16, 12), Chronic.guess(span).getBeginCalendar());

    span = new Span(Time.construct(2006, 8, 16, 0), Time.construct(2006, 8, 17, 0, 0, 1));
    assertEquals(Time.construct(2006, 8, 16, 12), Chronic.guess(span).getBeginCalendar());

    span = new Span(Time.construct(2006, 11), Time.construct(2006, 12));
    assertEquals(Time.construct(2006, 11, 16), Chronic.guess(span).getBeginCalendar());
  }
}
