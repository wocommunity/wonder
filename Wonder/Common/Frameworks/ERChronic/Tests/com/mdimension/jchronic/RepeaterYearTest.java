package com.mdimension.jchronic;

import java.util.Calendar;

import junit.framework.TestCase;

import com.mdimension.jchronic.repeaters.RepeaterYear;
import com.mdimension.jchronic.tags.Pointer;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;

public class RepeaterYearTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testNextFuture() {
    RepeaterYear years = new RepeaterYear();
    years.setStart(_now);

    Span nextYear = years.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2007, 1, 1), nextYear.getBeginCalendar());
    assertEquals(Time.construct(2008, 1, 1), nextYear.getEndCalendar());

    Span nextNextYear = years.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2008, 1, 1), nextNextYear.getBeginCalendar());
    assertEquals(Time.construct(2009, 1, 1), nextNextYear.getEndCalendar());
  }

  public void testNextPast() {
    RepeaterYear years = new RepeaterYear();
    years.setStart(_now);
    Span lastYear = years.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2005, 1, 1), lastYear.getBeginCalendar());
    assertEquals(Time.construct(2006, 1, 1), lastYear.getEndCalendar());

    Span lastLastYear = years.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2004, 1, 1), lastLastYear.getBeginCalendar());
    assertEquals(Time.construct(2005, 1, 1), lastLastYear.getEndCalendar());
  }

  public void testThis() {
    RepeaterYear years = new RepeaterYear();
    years.setStart(_now);

    Span thisYear;
    thisYear = years.thisSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 17), thisYear.getBeginCalendar());
    assertEquals(Time.construct(2007, 1, 1), thisYear.getEndCalendar());

    thisYear = years.thisSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 1, 1), thisYear.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16), thisYear.getEndCalendar());
  }

  public void testOffset() {
      Span span = new Span(_now, Calendar.SECOND, 1);

      Span offsetSpan;
      offsetSpan = new RepeaterYear().getOffset(span, 3, Pointer.PointerType.FUTURE);

      assertEquals(Time.construct(2009, 8, 16, 14), offsetSpan.getBeginCalendar());
      assertEquals(Time.construct(2009, 8, 16, 14, 0, 1), offsetSpan.getEndCalendar());

      offsetSpan = new RepeaterYear().getOffset(span, 10, Pointer.PointerType.PAST);

      assertEquals(Time.construct(1996, 8, 16, 14), offsetSpan.getBeginCalendar());
      assertEquals(Time.construct(1996, 8, 16, 14, 0, 1), offsetSpan.getEndCalendar());
  }
}
