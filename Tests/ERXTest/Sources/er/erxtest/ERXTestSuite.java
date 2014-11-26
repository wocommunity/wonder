package er.erxtest;

import junit.framework.Test;
import junit.framework.TestSuite;
import er.extensions.ERXExtensions;
import er.extensions.foundation.ERXStringUtilitiesTest;

public class ERXTestSuite {

  public static final String ERXTEST_MODEL = "ERXTest";

  public static void initialize() {
    ERXExtensions.initApp(Application.class, new String[0]);
    // just provided so TestCase can touch this class to get the static block
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite(com.webobjects.foundation.NSArrayTest.class);
    suite.addTestSuite(com.webobjects.foundation.NSDictionaryTest.class);
    suite.addTestSuite(com.webobjects.foundation.NSKeyValueCodingTest.class);
    suite.addTestSuite(com.webobjects.foundation.NSMutableArrayTest.class);
    suite.addTestSuite(com.webobjects.foundation.NSMutableDictionaryTest.class);
    suite.addTestSuite(com.webobjects.foundation.NSMutableSetTest.class);
    suite.addTestSuite(com.webobjects.foundation.NSSetTest.class);
    suite.addTestSuite(com.webobjects.foundation.NSTimestampTest.class);
    
    suite.addTestSuite(com.webobjects.eoaccess.ERXEntityTest.class);

    suite.addTestSuite(com.webobjects.appserver.WORequestTest.class);
    suite.addTestSuite(com.webobjects.appserver.WOTimerTest.class);

    suite.addTestSuite(er.extensions.eof.ERXECTest.class);
    suite.addTestSuite(er.extensions.eof.ERXEOAccessUtilitiesTest.class);
    suite.addTestSuite(er.extensions.eof.ERXEOControlUtilitiesTest.class);    
    suite.addTestSuite(er.extensions.eof.ERXKeyGlobalIDTest.class);
    suite.addTestSuite(er.extensions.eof.ERXKeyTest.class);

    suite.addTestSuite(er.extensions.eof.qualifiers.ERXToManyQualifierTest.class);
    suite.addTestSuite(er.extensions.eof.qualifiers.ERXQTest.class);

    suite.addTestSuite(er.extensions.foundation.ERXArrayUtilitiesTest.class);
    suite.addTestSuite(er.extensions.foundation.ERXMutableArrayTest.class);

    suite.addTest(ERXStringUtilitiesTest.suite());

    suite.addTestSuite(er.extensions.foundation.ERXThreadStorageTest.class);
    suite.addTestSuite(er.extensions.foundation.ERXUtilitiesTest.class);
    suite.addTestSuite(er.extensions.foundation.ERXValueUtilitiesTest.class);
    suite.addTestSuite(er.extensions.foundation.ERXFileUtilitiesTest.class);

    suite.addTestSuite(er.extensions.formatters.ERXOrdinalFormatterTests.class);
    suite.addTestSuite(er.extensions.formatters.ERXOrdinalDateFormatterTests.class);
    
    suite.addTestSuite(er.javamail.ERMailUtilsTest.class);

    suite.addTestSuite(er.extensions.jdbc.MicrosoftSQLHelperTest.class);

    suite.addTestSuite(er.extensions.net.ERXEmailValidatorTest.class);

    if (ERXTestCase.adaptorName().equals("Memory")) {
        suite.addTestSuite(er.memoryadaptor.ERMemoryAdaptorTest.class);
    }

    suite.addTestSuite(er.extensions.appserver.ERXApplicationTest.class);
    suite.addTestSuite(er.extensions.appserver.ERXRequestTest.class);
    
    suite.addTestSuite(er.directtoweb.ERD2WModelTest.class);

    suite.addTestSuite(er.erxtest.tests.ERXECLockingTestCase.class);

    if (ERXTestCase.adaptorName().equals("Memory")) {
        // XXX Having problems making this work with MySQL. Until it works.... -rrk 2012/07/14
        suite.addTestSuite(er.erxtest.tests.ERXEnterpriseObjectCacheTestCase.class);
    }

    suite.addTestSuite(er.erxtest.tests.ERXGenericRecordUpdateInverseRelationshipsTest.class);
    //suite.addTestSuite(er.erxtest.tests.ERXObjectStoreCoordinatorSynchronizerTestCase.class);

    // TODO - How long is this supposed to take to run? Is it hanging?
    //
    //suite.addTestSuite(ERXExpiringCacheTestCase.class);

    suite.addTestSuite(er.extensions.excel.EGSimpleWorkbookHelperTest.class);

    suite.addTestSuite(er.chronic.RepeaterMonthNameTest.class);
    suite.addTestSuite(er.chronic.RepeaterYearTest.class);
    suite.addTestSuite(er.chronic.RepeaterDayNameTest.class);

    suite.addTest((new er.chronic.ParserTest()).suite());

    suite.addTestSuite(er.chronic.ParseSpanTest.class);
    suite.addTestSuite(er.chronic.RepeaterTimeTest.class);
    suite.addTestSuite(er.chronic.TokenTest.class);
    suite.addTestSuite(er.chronic.RepeaterMonthTest.class);
    suite.addTestSuite(er.chronic.RepeaterWeekTest.class);
    suite.addTestSuite(er.chronic.ChronicTest.class);
    suite.addTestSuite(er.chronic.RepeaterHourTest.class);
    suite.addTestSuite(er.chronic.RepeaterFortnightTest.class);
    suite.addTestSuite(er.chronic.SpanTest.class);
    suite.addTestSuite(er.chronic.HandlerTest.class);
    suite.addTestSuite(er.chronic.RepeaterWeekendTest.class);
    suite.addTestSuite(er.chronic.NumerizerTest.class);

    suite.addTestSuite(er.extensions.crypting.TestBCrypt.class);

    return suite;
  }
}
