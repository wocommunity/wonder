
package er.extensions;

import java.net.URL;
import java.util.Enumeration;

import er.extensions.ERExtensionsTest;
import er.extensions.eof.ERXEOAccessUtilities;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;

import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationFactory;

import com.webobjects.eocontrol.EOEditingContext;

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
        suite.addTestSuite(com.webobjects.foundation.NSTimestampTest.class);
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

    public static NSArray<String> availableAdaptorNames() {
        String mysqlURL = System.getProperties().getProperty("wonder.test.MySQL.url");
        if (mysqlURL != null && !mysqlURL.startsWith("$")) return new NSArray<String>("MySQL");
        return new NSArray<String>("Memory");
    }

    public static boolean dbExistsForAdaptor(String name) {
        if (name.equals("Memory")) return true;
        String url = System.getProperties().getProperty("wonder.test."+name+".url");
        String usr = System.getProperties().getProperty("wonder.test."+name+".user");
        String pwd = System.getProperties().getProperty("wonder.test."+name+".pwd");
        return (url != null && url.length() > 0 && usr != null && usr.length() > 0 && pwd != null && pwd.length() > 0);
    }

    public static NSDictionary connectionDict(String name) {

        if (name == null || name.equals("Memory")) return NSDictionary.EmptyDictionary;

        String url = System.getProperties().getProperty("wonder.test."+name+".url");
        String usr = System.getProperties().getProperty("wonder.test."+name+".user");
        String pwd = System.getProperties().getProperty("wonder.test."+name+".pwd");

        //System.out.println("connectionDict:: url = \""+url+"\", usr = \""+usr+"\", pwd = \""+pwd+"\"");

        NSArray keys = new NSArray(new Object[] { "URL", "username", "password" } );
        NSArray vals = new NSArray(new Object[] { url, usr, pwd } );
        return new NSDictionary(vals, keys);
    }

    public static void loadData(EOEditingContext ec, EOModel model, Test test, String plistName) {

        EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
        EOSchemaSynchronizationFactory factory = adaptor.schemaSynchronizationFactory();

        NSMutableArray<NSArray<EOEntity>> entities = new NSMutableArray<NSArray<EOEntity>>();
        entities.add(new NSArray<EOEntity>(model.entityNamed("Company")));
        entities.add(new NSArray<EOEntity>(model.entityNamed("Employee")));

        NSArray<EOSQLExpression> deletes = factory.dropTableStatementsForEntityGroups(entities);
        NSArray<EOSQLExpression> creates = factory.createTableStatementsForEntityGroups(entities);

        EOAdaptorContext context = adaptor.createAdaptorContext();
        EOAdaptorChannel channel = context.createAdaptorChannel();
        channel.openChannel();

        for (int idx = 0; idx < deletes.count(); idx++) {
            EOSQLExpression expr = deletes.get(idx);
            try {
            channel.evaluateExpression(expr);
            } catch (com.webobjects.jdbcadaptor.JDBCAdaptorException jdbce) { /* swallow this exception. I could look for "error code: 1050". Robust? */ }
        }
        for (int idx = 0; idx < creates.count(); idx++) {
            EOSQLExpression expr = creates.get(idx);
            channel.evaluateExpression(expr);
        }

        URL plistURL = test.getClass().getResource("/AjaxExample.plist");
        if (plistURL == null) {
            try {
                plistURL = new java.net.URL("file://"+System.getProperty("build.root")+"/ERExtensions.framework/TestResources/AjaxExample.xml");
            } catch (java.net.MalformedURLException mue) { System.out.println("mue: "+mue); }
        }

        NSDictionary data = NSPropertyListSerialization.dictionaryWithPathURL(plistURL);

        Enumeration<String> keys = data.allKeys().objectEnumerator();
        while (keys.hasMoreElements()) {
            String entityName = keys.nextElement();
            ERXEOAccessUtilities.insertRows(ec, entityName, (NSArray<NSDictionary>)data.objectForKey(entityName));
        }
        ec.saveChanges();
    }
}
