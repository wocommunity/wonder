
package er.extensions;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests of the public API of the ERXExtensions framework.
 *
 * @author ray@ganymede.org, Ray Kiddy
 */
public class ERExtensionsTest extends TestSuite {

    static public Test suite() { 
        TestSuite suite = new TestSuite(); 

        suite.addTestSuite(com.webobjects.foundation.NSArrayTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSMutableArrayTest.class);

        suite.addTestSuite(er.extensions.foundation.ERXArrayUtilitiesTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXMutableArrayTest.class);

        suite.addTestSuite(er.extensions.statistics.ERXMetricsTest.class);

        return suite; 
    }
}
