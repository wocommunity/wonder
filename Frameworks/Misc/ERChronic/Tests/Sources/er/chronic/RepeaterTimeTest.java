package er.chronic;

import java.util.Calendar;

import junit.framework.TestCase;
import er.chronic.repeaters.RepeaterTime;
import er.chronic.tags.Pointer;
import er.chronic.utils.Time;

public class RepeaterTimeTest extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testNextFuture() {
    RepeaterTime t;

    t = new RepeaterTime("4:00");
    t.setStart(_now);

    assertEquals(Time.construct(2006, 8, 16, 16), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 17, 4), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());

    t = new RepeaterTime("13:00");
    t.setStart(_now);

    assertEquals(Time.construct(2006, 8, 17, 13), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 18, 13), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());

    t = new RepeaterTime("0400");
    t.setStart(_now);

    assertEquals(Time.construct(2006, 8, 17, 4), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 18, 4), t.nextSpan(Pointer.PointerType.FUTURE).getBeginCalendar());
  }

  public void testNextPast() {
    RepeaterTime t;
    t = new RepeaterTime("4:00");
    t.setStart(_now);

    assertEquals(Time.construct(2006, 8, 16, 4), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 15, 16), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());

    t = new RepeaterTime("13:00");
    t.setStart(_now);

    assertEquals(Time.construct(2006, 8, 16, 13), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
    assertEquals(Time.construct(2006, 8, 15, 13), t.nextSpan(Pointer.PointerType.PAST).getBeginCalendar());
  }

  public void testType() {
    RepeaterTime t1;
    t1 = new RepeaterTime("4");
    assertEquals(14400, t1.getType().intValue());

    t1 = new RepeaterTime("14");
    assertEquals(50400, t1.getType().intValue());

    t1 = new RepeaterTime("4:00");
    assertEquals(14400, t1.getType().intValue());

    t1 = new RepeaterTime("4:30");
    assertEquals(16200, t1.getType().intValue());

    t1 = new RepeaterTime("1400");
    assertEquals(50400, t1.getType().intValue());

    t1 = new RepeaterTime("0400");
    assertEquals(14400, t1.getType().intValue());

    t1 = new RepeaterTime("04");
    assertEquals(14400, t1.getType().intValue());

    t1 = new RepeaterTime("400");
    assertEquals(14400, t1.getType().intValue());
  }
}
