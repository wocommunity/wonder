package er.chronic;

import java.util.Calendar;

import junit.framework.TestCase;
import er.chronic.repeaters.RepeaterHour;
import er.chronic.tags.Pointer;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterHourTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testNextFuture() {
    RepeaterHour hours = new RepeaterHour();
    hours.setStart(_now);

    Span nextHour = hours.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 16, 15), nextHour.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 16), nextHour.getEndCalendar());

    Span nextNextHour = hours.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 16, 16), nextNextHour.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 17), nextNextHour.getEndCalendar());
  }

  public void testNextPast() {
    RepeaterHour hours = new RepeaterHour();
    hours.setStart(_now);
    Span lastHour = hours.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 16, 13), lastHour.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 14), lastHour.getEndCalendar());

    Span lastLastHour = hours.nextSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 16, 12), lastLastHour.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 13), lastLastHour.getEndCalendar());
  }

  public void testThis() {
    _now = Time.construct(2006, 8, 16, 14, 30);

    RepeaterHour hours = new RepeaterHour();
    hours.setStart(_now);

    Span thisHour;
    thisHour = hours.thisSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 8, 16, 14, 31), thisHour.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 15), thisHour.getEndCalendar());

    thisHour = hours.thisSpan(Pointer.PointerType.PAST);
    assertEquals(Time.construct(2006, 8, 16, 14), thisHour.getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 16, 14, 30), thisHour.getEndCalendar());
  }

  public void testOffset() {
      Span span = new Span(_now, Calendar.SECOND, 1);

      Span offsetSpan;
      offsetSpan = new RepeaterHour().getOffset(span, 3, Pointer.PointerType.FUTURE);

      assertEquals(Time.construct(2006, 8, 16, 17), offsetSpan.getBeginCalendar());
      assertEquals(Time.construct(2006, 8, 16, 17, 0, 1), offsetSpan.getEndCalendar());

      offsetSpan = new RepeaterHour().getOffset(span, 24, Pointer.PointerType.PAST);

      assertEquals(Time.construct(2006, 8, 15, 14), offsetSpan.getBeginCalendar());
      assertEquals(Time.construct(2006, 8, 15, 14, 0, 1), offsetSpan.getEndCalendar());
  }
}
