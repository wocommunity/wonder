
package er.extensions;

import er.extensions.ERExtensionsTest;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/** Tests of the public API of the ERXExtensions framework.
 *
 * @author ray@ganymede.org, Ray Kiddy
 */
public class ERExtensionsTest extends TestSuite {

    public static TestSuite suite;

    public static Test suite() {
        suite = new TestSuite("Tests for ERExtensions");
		//$JUnit-BEGIN$
        suite.addTestSuite(com.webobjects.eoaccess.ERXEntityTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSArrayTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSDictionaryTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSKeyValueCodingTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSMutableArrayTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSMutableDictionaryTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSMutableSetTest.class);
        suite.addTestSuite(com.webobjects.foundation.NSSetTest.class);
        suite.addTestSuite(er.extensions.eof.ERXECTest.class);
        suite.addTestSuite(er.extensions.eof.ERXEOAccessUtilitiesTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXArrayUtilitiesTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXMutableArrayTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXThreadStorageTest.class);
        suite.addTestSuite(er.extensions.foundation.ERXUtilitiesTest.class);
        suite.addTestSuite(er.extensions.jdbc.MicrosoftSQLHelperTest.class);
		//$JUnit-END$
        return suite;
    }

    /**
     * Return true if the two objects provided are logically equivalent, as seen through the
     * object's public API. This relies on the fact that the EOAccess methods are fairly
     * regular about how they use their toString methods. This gets most of what we need for
     * testing. The safeEquals() method below is going to be more correct, but can be used
     * when it is more fully tested. (author: kiddyr@sourceforge.net)
     */
    public static boolean equalsForEOAccessObjects(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 != null && obj2 != null) {
            return obj1.toString().equals(obj2.toString());
        } else
            return false;
    }

    public static NSArray<String> testMethodsForClassName(String className) {

        Class klass = null;
        try {
            klass = Class.forName(className);
        } catch (java.lang.ClassNotFoundException cnfe) {
            // TODO - proper logging should go here.
            System.err.println("ERROR: class not found for name \""+className+"\"");
            return new NSArray<String>();
        }

        java.lang.reflect.Method[] methods = klass.getMethods();
        NSMutableArray<String> target = new NSMutableArray<String>();

        for (int idx = 0; idx < methods.length; idx++) {
            if (methods[idx].getName().startsWith("test")) {
                target.add(methods[idx].getName());
            }
        }
        return target.immutableClone();
    }
}
