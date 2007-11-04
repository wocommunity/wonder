package com.mdimension.jchronic;

import java.util.Calendar;

import junit.framework.TestCase;

import com.mdimension.jchronic.repeaters.RepeaterFortnight;
import com.mdimension.jchronic.repeaters.RepeaterWeek;
import com.mdimension.jchronic.tags.Pointer;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;

public class RepeaterFortnightTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testNextFuture() {
    RepeaterFortnight fortnights = new RepeaterFortnight();
    fortnights.setStart(_now);

    Span nextFortnight = fortnights.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 20), nextFortnight.getBeginCalendar());
    assertEquals(Time.construct(2006, 9, 3), nextFortnight.getEndCalendar());

    Span nextNextFortnight = fortnights.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 9, 3), nextNextFortnight.getBeginCalendar());
    assertEquals(Time.construct(2006, 9, 17), nextNextFortnight.getEndCalendar());
  }

  public void testNextPast() {
    RepeaterFortnight fortnights = new RepeaterFortnight();
    fortnights.setStart(_now);
    Span lastFortnight = fortnights.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 7, 30), lastFortnight.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 13), lastFortnight.getEndCalendar());

    Span lastLastFortnight = fortnights.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 7, 16), lastLastFortnight.getBeginCalendar());
    assertEquals(Time.construct(2006, 7, 30), lastLastFortnight.getEndCalendar());
  }

  public void testThisFuture() {
    RepeaterFortnight fortnights = new RepeaterFortnight();
    fortnights.setStart(_now);

    Span thisFortnight = fortnights.thisSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 16, 15), thisFortnight.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 27), thisFortnight.getEndCalendar());
  }

  public void testThisPast() {
    RepeaterFortnight fortnights = new RepeaterFortnight();
    fortnights.setStart(_now);

    Span thisFortnight = fortnights.thisSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 13, 0), thisFortnight.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 14), thisFortnight.getEndCalendar());
  }

  public void testOffset() {
      Span span = new Span(_now, Calendar.SECOND, 1);

      Span offsetSpan = new RepeaterWeek().getOffset(span, 3, Pointer.PointerType.FUTURE);

      assertEquals(Time.construct(2006, 9, 6, 14), offsetSpan.getBeginCalendar());
      assertEquals(Time.construct(2006, 9, 6, 14, 0, 1), offsetSpan.getEndCalendar());
  }
}
