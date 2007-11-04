package er.chronic;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ChronicTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite("Chronic");
    //$JUnit-BEGIN$
    suite.addTestSuite(RepeaterMonthNameTest.class);
    suite.addTestSuite(RepeaterYearTest.class);
    suite.addTestSuite(RepeaterDayNameTest.class);
    suite.addTestSuite(ParserTest.class);
    suite.addTestSuite(RepeaterTimeTest.class);
    suite.addTestSuite(TokenTestCase.class);
    suite.addTestSuite(RepeaterMonthTest.class);
    suite.addTestSuite(RepeaterWeekTest.class);
    suite.addTestSuite(ChronicTestCase.class);
    suite.addTestSuite(RepeaterHourTest.class);
    suite.addTestSuite(RepeaterFortnightTest.class);
    suite.addTestSuite(SpanTestCase.class);
    suite.addTestSuite(HandlerTestCase.class);
    suite.addTestSuite(RepeaterWeekendTest.class);
    suite.addTestSuite(NumerizerTestCase.class);
    //$JUnit-END$
    return suite;
  }

}
