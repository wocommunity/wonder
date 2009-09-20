package er.chronic;

import java.util.Calendar;

import junit.framework.TestCase;
import er.chronic.repeaters.RepeaterMonthName;
import er.chronic.tags.Pointer;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterMonthNameTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testNext() {
    RepeaterMonthName mays = new RepeaterMonthName(RepeaterMonthName.MonthName.MAY);
    mays.setStart(_now);
    
    Span nextMay = mays.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2007, 5), nextMay.getBeginCalendar());
    assertEquals(Time.construct(2007, 6), nextMay.getEndCalendar());

    Span nextNextMay = mays.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2008, 5), nextNextMay.getBeginCalendar());
    assertEquals(Time.construct(2008, 6), nextNextMay.getEndCalendar());

    RepeaterMonthName decembers = new RepeaterMonthName(RepeaterMonthName.MonthName.DECEMBER);
    decembers.setStart(_now);
    
    Span nextDecember = decembers.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2006, 12), nextDecember.getBeginCalendar());
    assertEquals(Time.construct(2007, 1), nextDecember.getEndCalendar());

    mays = new RepeaterMonthName(RepeaterMonthName.MonthName.MAY);
    mays.setStart(_now);
    
    assertEquals(Time.construct(2006, 5), mays.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
    assertEquals(Time.construct(2005, 5), mays.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
  }

  public void testThis() {
    RepeaterMonthName octobers = new RepeaterMonthName(RepeaterMonthName.MonthName.MAY);
    octobers.setStart(_now);
    
    Span nextMay = octobers.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2007, 5), nextMay.getBeginCalendar());
    assertEquals(Time.construct(2007, 6), nextMay.getEndCalendar());

    Span nextNextMay = octobers.nextSpan(Pointer.PointerType.FUTURE);
    assertEquals(Time.construct(2008, 5), nextNextMay.getBeginCalendar());
    assertEquals(Time.construct(2008, 6), nextNextMay.getEndCalendar());
  }
}
