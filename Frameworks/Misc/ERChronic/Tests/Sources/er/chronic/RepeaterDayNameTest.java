package er.chronic;

import java.util.Calendar;

import junit.framework.TestCase;
import er.chronic.repeaters.RepeaterDayName;
import er.chronic.tags.Pointer;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

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
