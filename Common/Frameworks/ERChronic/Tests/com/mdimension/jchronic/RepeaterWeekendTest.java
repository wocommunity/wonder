package com.mdimension.jchronic;

import java.util.Calendar;

import junit.framework.TestCase;

import com.mdimension.jchronic.repeaters.RepeaterWeekend;
import com.mdimension.jchronic.tags.Pointer;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;

public class RepeaterWeekendTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testNextFuture() {
    RepeaterWeekend weekends = new RepeaterWeekend();
    weekends.setStart(_now);

    Span nextWeekend = weekends.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 19), nextWeekend.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 21), nextWeekend.getEndCalendar());
  }

  public void testNextPast() {
    RepeaterWeekend weekends = new RepeaterWeekend();
    weekends.setStart(_now);
    Span lastWeekend = weekends.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 12), lastWeekend.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 14), lastWeekend.getEndCalendar());
  }

  public void testThisFuture() {
    RepeaterWeekend weekends = new RepeaterWeekend();
    weekends.setStart(_now);

    Span thisWeekend = weekends.thisSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 19), thisWeekend.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 21), thisWeekend.getEndCalendar());
  }

  public void testThisPast() {
    RepeaterWeekend weekends = new RepeaterWeekend();
    weekends.setStart(_now);

    Span thisWeekend = weekends.thisSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 12), thisWeekend.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 14), thisWeekend.getEndCalendar());
  }

  public void testThisNone() {
    RepeaterWeekend weekends = new RepeaterWeekend();
    weekends.setStart(_now);

    Span thisWeekend = weekends.thisSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 19), thisWeekend.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 21), thisWeekend.getEndCalendar());
  }

  public void testOffset() {
    Span span = new Span(_now, Calendar.SECOND, 1);

    Span offsetSpan;

    offsetSpan = new RepeaterWeekend().getOffset(span, 3, Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 9, 2), offsetSpan.getBeginCalendar());
    assertEquals(Time.construct(2006, 9, 2, 0, 0, 1), offsetSpan.getEndCalendar());

    offsetSpan = new RepeaterWeekend().getOffset(span, 1, Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 12), offsetSpan.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 12, 0, 0, 1), offsetSpan.getEndCalendar());

    offsetSpan = new RepeaterWeekend().getOffset(span, 0, Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 12), offsetSpan.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 12, 0, 0, 1), offsetSpan.getEndCalendar());
  }
}
