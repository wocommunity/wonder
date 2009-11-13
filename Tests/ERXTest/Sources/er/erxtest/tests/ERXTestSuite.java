package er.erxtest.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import er.erxtest.Application;
import er.extensions.ERXExtensions;

public class ERXTestSuite {
  public static void initialize() {
    ERXExtensions.initApp(Application.class, new String[0]);
    // just provided so TestCase can touch this class to get the static block
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
//    suite.addTestSuite(ERXECLockingTestCase.class);
//    suite.addTestSuite(ERXEnterpriseObjectCacheTestCase.class);
//    suite.addTestSuite(ERXExpiringCacheTestCase.class);
//    suite.addTestSuite(ERXGenericRecordUpdateInverseRelationshipsTest.class);
    suite.addTestSuite(ERXObjectStoreCoordinatorSynchronizerTestCase.class);
    return suite;
  }
}
