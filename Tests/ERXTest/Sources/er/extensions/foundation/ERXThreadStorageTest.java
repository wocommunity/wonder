package er.extensions.foundation;

import java.util.Map;

import junit.framework.Assert;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.erxtest.ERXTestCase;
import er.erxtest.model.Company;
import er.extensions.eof.ERXEC;

public class ERXThreadStorageTest extends ERXTestCase {

    // public static final java.lang.String KEYS_ADDED_IN_CURRENT_THREAD_KEY;
    // public static final java.lang.String WAS_CLONED_MARKER;

    public void testConstructor() {
        RunnableStorageTester t1 = new RunnableStorageTester(7, "testConstructor", null, null);
        RunnableStorageTester t2 = new RunnableStorageTester(13, "testConstructor", null, null);
        RunnableStorageTester t3 = new RunnableStorageTester(23, "testConstructor", null, null);
        t1.start(); t2.start(); t3.start();
    }

    private class RunnableStorageTester extends Thread {

        private int pause;
        private String test;
        private NSArray<EOEnterpriseObject> eos;
        private NSArray<String> keys;

        protected RunnableStorageTester(int pauseInterval,
                                      String testName,
                                      NSArray<EOEnterpriseObject> objects,
                                      NSArray<String> keyStrings) {
            pause = pauseInterval;
            test = testName;
            eos = objects;
            keys = keyStrings;
        }

        @Override
        public void run() {

            Map map1 = null, map2 = null, map3 = null;

            EOEditingContext ec = null;

            Company c1 = null, c2 = null;

            //System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" start");

            try {
                sleep(pause);
            } catch(java.lang.InterruptedException ie) { System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" interrupted1"); }
            //System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" awake1");

            if ("testConstructor".equals(test)) {
                Assert.assertNotNull(new ERXThreadStorage());
            }

            if ("testMap".equals(test)) {
                map1 = ERXThreadStorage.map();
                Assert.assertNotNull(map1);
            }
            
            if ("testReset".equals(test)) {
                ec = ERXEC.newEditingContext();
                c1 = Company.createCompany(ec, "Acme");
                ec.saveChanges();
                ERXThreadStorage.takeValueForKey(c1, "Acme");
                Assert.assertEquals(1, ERXThreadStorage.map().size());
                ERXThreadStorage.reset();
                Assert.assertEquals(0, ERXThreadStorage.map().size());
            }

            try {
                sleep(pause);
            } catch(java.lang.InterruptedException ie) { System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" interrupted2"); }
            //System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" awake2");

            if ("testConstructor".equals(test)) {
                Assert.assertNotNull(new ERXThreadStorage());
            }

            if ("testMap".equals(test)) {
                map2 = ERXThreadStorage.map();
                Assert.assertNotNull(map2);
            }

            if ("testReset".equals(test)) {
                ERXThreadStorage.takeValueForKey(c1, "Acme");
                Assert.assertEquals(1, ERXThreadStorage.map().size());
                ERXThreadStorage.reset();
                Assert.assertEquals(0, ERXThreadStorage.map().size());
            }

            try {
                sleep(pause);
            } catch(java.lang.InterruptedException ie) { System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" interrupted3"); }
            //System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" awake3");

            if ("testConstructor".equals(test)) {
                Assert.assertNotNull(new ERXThreadStorage());
            }

            if ("testMap".equals(test)) {
                map3 = ERXThreadStorage.map();
                Assert.assertNotNull(map3);
            }

            if ("testReset".equals(test)) {
                Assert.assertEquals(0, ERXThreadStorage.map().size());
            }
        }
    }

    public void testTakeValueForKey() {
        // public static void takeValueForKey(java.lang.Object, java.lang.String);
    }

    public void testRemoveValueForKey() {
        // public static java.lang.Object removeValueForKey(java.lang.String);
    }

    public void testValueForKeyPath() {
        // public static java.lang.Object valueForKeyPath(java.lang.String);
    }

    public void testValueForKey() {
        // public static java.lang.Object valueForKey(java.lang.String);
    }

    public void testValueForKeyInEditingContext() {
        // public static java.lang.Object valueForKey(com.webobjects.eocontrol.EOEditingContext, java.lang.String);
    }

    public void testMap() {
        RunnableStorageTester t1 = new RunnableStorageTester(7, "testMap", null, null);
        RunnableStorageTester t2 = new RunnableStorageTester(13, "testMap", null, null);
        RunnableStorageTester t3 = new RunnableStorageTester(23, "testMap", null, null);
        t1.start(); t2.start(); t3.start();
    }

    public void testReset() {
        RunnableStorageTester t1 = new RunnableStorageTester(7, "testReset", null, null);
        RunnableStorageTester t2 = new RunnableStorageTester(13, "testReset", null, null);
        RunnableStorageTester t3 = new RunnableStorageTester(23, "testReset", null, null);
        t1.start(); t2.start(); t3.start();
    }

    public void testWasInheritedFromParentThread() {
        // public static boolean wasInheritedFromParentThread();
    }

    public void testSetProblematicTypes() {
        // public static void setProblematicTypes(com.webobjects.foundation.NSSet);
    }

    public void testProblematicTypes() {
        // public static java.util.Set problematicTypes();
    }

    public void testSetProblematicKeys() {
        // public static void setProblematicKeys(java.util.Set);
    }

    public void testProblematicKeys() {
        // public static java.util.Set problematicKeys();
    }
}
