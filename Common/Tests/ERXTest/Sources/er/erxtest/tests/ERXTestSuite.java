package er.erxtest.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import er.erxtest.Application;
import er.extensions.ERXExtensions;

public class ERXTestSuite {
  static {
    ERXExtensions.initApp(Application.class, new String[0]);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ERXExpiringCacheTestCase.class);
    suite.addTestSuite(ERXGenericRecordUpdateInverseRelationshipsTest.class);
    suite.addTestSuite(ERXObjectStoreCoordinatorSynchronizerTestCase.class);
    return suite;
  }
}
