package com.mdimension.jchronic;

import java.util.Calendar;

import junit.framework.TestCase;

import com.mdimension.jchronic.repeaters.RepeaterDayName;
import com.mdimension.jchronic.tags.Pointer;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;
import com.mdimension.jchronic.utils.Token;

public class RepeaterDayNameTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testMatch() {
    Token token = new Token("saturday");
    RepeaterDayName repeater = RepeaterDayName.scan(token);
    assertEquals(RepeaterDayName.DayName.SATURDAY, repeater.getType());

    token = new Token("sunday");
    repeater = RepeaterDayName.scan(token);
    assertEquals(RepeaterDayName.DayName.SUNDAY, repeater.getType());
  }

  public void testNextFuture() {
    Span span;
    
    RepeaterDayName mondays = new RepeaterDayName(RepeaterDayName.DayName.MONDAY);
    mondays.setStart(_now);
    span = mondays.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 21), span.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 22), span.getEndCalendar());

    span = mondays.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 28), span.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 29), span.getEndCalendar());
  }

  public void testNextPast() {
    Span span;
    
    RepeaterDayName mondays = new RepeaterDayName(RepeaterDayName.DayName.MONDAY);
    mondays.setStart(_now);
    span = mondays.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 14), span.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 15), span.getEndCalendar());

    span = mondays.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 7), span.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 8), span.getEndCalendar());
  }
}
