
package er.extensions;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests of the public API of the ERXExtensions framework.
 *
 * @author ray@ganymede.org, Ray Kiddy
 */
public class ERExtensionsTest extends TestSuite {

    public static Test suite() { 
        TestSuite suite = new TestSuite("Tests for ERExtensions"); 
		//$JUnit-BEGIN$
        suite.addTestSuite(com.webobjects.foundation.NSArrayTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSDictionaryTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSKeyValueCodingTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSMutableArrayTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSMutableDictionaryTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSMutableSetTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSSetTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXArrayUtilitiesTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXMutableArrayTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXThreadStorageTest.class);
        suite.addTestSuite(er.extensions.statistics.ERXMetricsTest.class);
		//$JUnit-END$
        return suite; 
    }
}
