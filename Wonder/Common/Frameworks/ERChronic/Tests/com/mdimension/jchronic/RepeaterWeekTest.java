package com.mdimension.jchronic;

import java.util.Calendar;

import junit.framework.TestCase;

import com.mdimension.jchronic.repeaters.RepeaterWeek;
import com.mdimension.jchronic.tags.Pointer;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;

public class RepeaterWeekTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testNextFuture() {
    RepeaterWeek weeks = new RepeaterWeek();
    weeks.setStart(_now);

    Span nextWeek = weeks.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 20), nextWeek.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 27), nextWeek.getEndCalendar());

    Span nextNextWeek = weeks.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 27), nextNextWeek.getBeginCalendar());
    assertEquals(Time.construct(2006, 9, 3), nextNextWeek.getEndCalendar());
  }

  public void testNextPast() {
    RepeaterWeek weeks = new RepeaterWeek();
    weeks.setStart(_now);
    Span lastWeek = weeks.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 6), lastWeek.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 13), lastWeek.getEndCalendar());

    Span lastLastWeek = weeks.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 7, 30), lastLastWeek.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 6), lastLastWeek.getEndCalendar());
  }

  public void testThisFuture() {
    RepeaterWeek weeks = new RepeaterWeek();
    weeks.setStart(_now);

    Span thisWeek = weeks.thisSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 16, 15), thisWeek.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 20), thisWeek.getEndCalendar());
  }

  public void testThisPast() {
    RepeaterWeek weeks = new RepeaterWeek();
    weeks.setStart(_now);

    Span thisWeek = weeks.thisSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 13, 0), thisWeek.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 14), thisWeek.getEndCalendar());
  }

  public void testOffset() {
    Span span = new Span(_now, Calendar.SECOND, 1);

    Span offsetSpan = new RepeaterWeek().getOffset(span, 3, Pointer.PointerType.FUTURE);

    assertEquals(Time.construct(2006, 9, 6, 14), offsetSpan.getBeginCalendar());
    assertEquals(Time.construct(2006, 9, 6, 14, 0, 1), offsetSpan.getEndCalendar());
  }
}
