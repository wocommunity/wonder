package er.chronic;

import java.util.Calendar;

import junit.framework.TestCase;
import er.chronic.tags.Pointer;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class ParseSpanTest extends TestCase {

  private static ThreadLocal<Calendar> TIME_2006_08_16_14_00_00_TS = new ThreadLocal<Calendar>() {
	  public Calendar initialValue() {
		  return Time.construct(2006, 8, 16, 14, 0, 0, 0);
	  }
  };
  public static Calendar TIME_2006_08_16_14_00_00() { return TIME_2006_08_16_14_00_00_TS.get(); }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void assertBeginEquals(Calendar ec, Span span) {
    assertEquals(ec, (span == null) ? null : span.getBeginCalendar());
  }

  protected void assertEndEquals(Calendar ec, Span span) {
    assertEquals(ec, (span == null) ? null : span.getEndCalendar());
  }

  public void test_year_span() {
    Span t = parse_now("2008");
    assertBeginEquals(Time.construct(2008, 1, 1, 0, 0, 0), t);
    assertEndEquals(Time.construct(2009, 1, 1, 0, 0, 0), t);
  }

  public void test_month_span() {
    Span t = parse_now("May 2008");
    assertBeginEquals(Time.construct(2008, 5, 1, 0, 0, 0), t);
    assertEndEquals(Time.construct(2008, 6, 1, 0, 0, 0), t);
  }

  public Span parse_now(String string) {
    return parse_now(string, new Options());
  }

  public Span parse_now(String string, Options options) {
    options.setNow(TIME_2006_08_16_14_00_00());
    options.setCompatibilityMode(true);
    options.setGuess(false); // we want a range rather than a point time
    options.setContext(Pointer.PointerType.NONE);
    return Chronic.parse(string, options);
  }

}
