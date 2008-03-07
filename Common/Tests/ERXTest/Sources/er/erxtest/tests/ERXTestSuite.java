package er.erxtest.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ERXTestSuite {
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ERXExpiringCacheTestCase.class);
    suite.addTestSuite(ERXGenericRecordUpdateInverseRelationshipsTest.class);
    suite.addTestSuite(ERXObjectStoreCoordinatorSynchronizerTestCase.class);
    return suite;
  }
}
