package er.erxtest.tests;

import junit.framework.TestCase;

public class ERXTestCase extends TestCase {
  static {
    ERXTestSuite.initialize();
  }
  
  public ERXTestCase() {
    super();
  }
  
  public ERXTestCase(String name) {
    super(name);
  }
}
