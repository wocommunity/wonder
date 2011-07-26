package er.chronic;

import java.util.Calendar;

import junit.framework.TestCase;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class SpanTestCase extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testSpanWidth() {
    Span span = new Span(Time.construct(2006, 8, 16, 0), Time.construct(2006, 8, 17, 0));
    assertEquals(60 * 60 * 24, span.getWidth());
  }

  public void testSpanMath() {
    Span span = new Span(1, 2);
    assertEquals(2, span.add(1).getBegin());
    assertEquals(3, span.add(1).getEnd());
    assertEquals(0, span.subtract(1).getBegin());
    assertEquals(1, span.subtract(1).getEnd());
  }
}
