
package er.extensions.eof;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXTestUtilities;

public class ERXThrashTest {

    static Method loadMethod;

    public static String modelName() {
    	return System.getProperty("wonder.test.thrash.adaptor", "Memory")+"BusinessModel";
    }
    
    static ArrayList<Thread> useScheme(String scheme) {
        // System.out.println("useScheme:: start, scheme = \""+scheme+"\"");

        ArrayList<Thread> threads = new ArrayList<Thread>();

        String[] parts = scheme.split(":");
        // System.out.println("useScheme:: parts # "+parts.length);

        String testClassName = parts[1];
        String parameter = parts[2];
        String plan = parts[3];

        System.out.println("testClassName = \""+testClassName+"\"");
        System.out.println("parameter = \""+parameter+"\"");
        System.out.println("plan = \""+plan+"\"");
 
        Class testClass = null;

        try {
            testClass = Class.forName("er.extensions.eof.ERXThrashTest$"+testClassName);
        } catch (java.lang.ClassNotFoundException cnfe) { throw new IllegalArgumentException(cnfe.getMessage(), cnfe); }
        // System.out.println("useScheme:: testClass = \""+testClass+"\"");

        try {
            loadMethod = testClass.getMethod("load");
        } catch (java.lang.NoSuchMethodException nsme) { throw new IllegalArgumentException(nsme.getMessage(), nsme); }

        Constructor[] cons = testClass.getConstructors();
        // System.out.println("useScheme:: cons count = "+cons.length);

        Constructor con = cons[0];
        // System.out.println("useScheme:: con = "+con);

        String[] plans = plan.split(",");
        for (int idx = 0; idx < plans.length; idx++) {
            String subPlan = plans[idx];
            // System.out.println("subPlan # "+idx+" = \""+subPlan+"\"");

            String[] planParts = subPlan.split("-");

            if (planParts.length != 2)
                throw new IllegalArgumentException("Cannot figure out part # "+idx+" (\""+planParts[idx]+"\") in scheme: \""+scheme+"\"");

            Integer delay = null;
            int count = -1;

            try {
                delay = new Integer(planParts[0]);
                count = (new Integer(planParts[1])).intValue();
            } catch (java.lang.NumberFormatException nfe) {
                throw new IllegalArgumentException("Cannot figure out part # "+idx+" (\""+planParts[idx]+"\") in scheme: \""+scheme+"\"");
            }
 
            try {
                for (int jdx = 0; jdx < count; jdx++) { threads.add((Thread)con.newInstance(delay, parameter)); }
            } catch (java.lang.InstantiationException ie) { throw new IllegalArgumentException(ie.getMessage(), ie); }
              catch (java.lang.IllegalAccessException iae) { throw new IllegalArgumentException(iae.getMessage(), iae); }
              catch (java.lang.reflect.InvocationTargetException ite) { throw new IllegalArgumentException(ite.getMessage(), ite); }
        }

        return threads;
    }

    public static void main(String[] arg) {

        // System.out.println("ERXThrashTest:: Hear me roar!");

        ArrayList<Thread> threads = null;

        // Enumeration props = System.getProperties().propertyNames();
        // while (props.hasMoreElements()) { System.out.println("props = "+props.nextElement()); }

        threads = useScheme(System.getProperty("thrash.scheme", "scheme:SimpleInsertTest::0-100,2-100,5-200,10-100,12-100,15-200,20-100,22-100,25-200"));

        if (threads == null || threads.size() == 0) { System.exit(0); }

        long startTicks = System.currentTimeMillis();

        try {
            loadMethod.invoke(threads.get(0));
        } catch (java.lang.IllegalAccessException iae) { throw new IllegalArgumentException(iae.getMessage(), iae); }
          catch (java.lang.reflect.InvocationTargetException ite) { throw new IllegalArgumentException(ite.getMessage(), ite); }

        Iterator<Thread> starting = threads.iterator();

        while (starting.hasNext()) { starting.next().start(); }

        boolean done = false;
        while (!done) {
/*
            for (int idx = 0; idx < 3; idx++) {
                System.out.println("   thread["+idx+"]: state: "+(threads.get(idx).isAlive() ? "ALIVE" : "DEAD")+", name: \""+threads.get(idx).getName()+"\"");
            }
*/
            try {
                Thread.sleep(1000L);

                Iterator<Thread> checking = threads.iterator();

                boolean allDead = true;
                while (checking.hasNext() && allDead) {
                    if (checking.next().isAlive()) allDead = false;
                }
       
                if (allDead) done = true;
            } catch (java.lang.InterruptedException ie) { done = true; }
        }
    }

    public static class SimpleFetchTest extends Thread {

        private int delay;
        private String param;

        public SimpleFetchTest(int aDelay, String parameter) {
            delay = aDelay;
            param = parameter;
        }

        public void load() {

            EOModelGroup.setDefaultGroup(new EOModelGroup());

            URL modelUrl = ERXTestUtilities.resourcePathURL("/"+modelName() + ".eomodeld", getClass());

            EOModelGroup.defaultGroup().addModel(new EOModel(modelUrl));

            EOModel model = EOModelGroup.defaultGroup().modelNamed(modelName());
            NSDictionary connDict = new NSDictionary(
                    new NSArray(new Object[] { System.getProperty("wonder.test.thrash.user"),
                                               System.getProperty("wonder.test.thrash.pwd"), 
                                               System.getProperty("wonder.test.thrash.url") }),
                    new NSArray(new String[] { "username", "password", "URL" }));

            model.setConnectionDictionary(connDict);
        }

        public void run() {

            if (delay > 0) {
                try {
                    Thread.sleep(delay * 1000L);
                } catch (java.lang.InterruptedException ie) { }
            }

            EOEditingContext ec = new EOEditingContext();

            long startTicks = System.currentTimeMillis();

            NSArray rows = EOUtilities.objectsForEntityNamed(ec, "Company");

            long endTicks = System.currentTimeMillis();

            System.out.println(this+": RESULT: row count = "+rows.count());
            System.out.println(this+": START: start = "+startTicks);
            System.out.println(this+": TIME: duration = "+(endTicks - startTicks));

            // System.out.println(this+": done");
        }
    }

    public static class SimpleInsertTest extends Thread {

        private int delay;
        private String param;

        private Constructor ecConstructor;

        public SimpleInsertTest(int aDelay, String parameter) {
            delay = aDelay;
            param = parameter;

            Class ecClass = null;

            try {
                if (param == null || param.equals(""))
                    ecClass = Class.forName("com.webobjects.eocontrol.EOEditingContext");
                else
                    ecClass = Class.forName(param);
            } catch (java.lang.ClassNotFoundException cnfe) { System.exit(1); }

            try {
                ecConstructor = ecClass.getConstructor();
            } catch (java.lang.NoSuchMethodException nsme) { System.exit(1); }
        }

        public void load() {

            EOModelGroup.setDefaultGroup(new EOModelGroup());

            URL modelUrl = ERXTestUtilities.resourcePathURL("/" + modelName() + ".eomodeld", getClass());

            EOModelGroup.defaultGroup().addModel(new EOModel(modelUrl));

            EOModel model = EOModelGroup.defaultGroup().modelNamed(modelName());
            NSDictionary connDict = new NSDictionary(
                    new NSArray(new Object[] { System.getProperty("wonder.test.thrash.user", "none"),
                                               System.getProperty("wonder.test.thrash.pwd", "none"), 
                                               System.getProperty("wonder.test.thrash.url", "none") }),
                    new NSArray(new String[] { "username", "password", "URL" }));

            model.setConnectionDictionary(connDict);
        }

        public void run() {

            if (delay > 0) {
                try {
                    Thread.sleep(delay * 1000L);
                } catch (java.lang.InterruptedException ie) { }
            }

            EOEditingContext ec = null;

            try {
            ec = (EOEditingContext)ecConstructor.newInstance();
            } catch (java.lang.InstantiationException ie) { }
              catch (java.lang.IllegalAccessException iae) { }
              catch (java.lang.reflect.InvocationTargetException ite) { }

            ec.lock();
            EOEnterpriseObject company = EOUtilities.createAndInsertInstance(ec, "Company");
            company.takeValueForKey("SomeBody.com", "name");
            ec.saveChanges();
            ec.unlock();

            long startTicks = System.currentTimeMillis();

            String lastName = Thread.currentThread().getName()+System.currentTimeMillis()+"Person";

            int iters = 10;

            for (int idx = 0; idx < iters; idx++) {
                ec.lock();
                EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(ec, "Employee");
                eo.addObjectToBothSidesOfRelationshipWithKey(company, "company");
                eo.takeValueForKey("Bob"+idx, "firstName");
                eo.takeValueForKey(lastName, "lastName");
                ec.unlock();
            }
            ec.lock();
            ec.saveChanges();
            ec.unlock();

            ec.lock();
            NSArray rowsInserted = EOUtilities.objectsMatchingKeyAndValue(ec, "Employee", "lastName", lastName);
            boolean insertOk = (rowsInserted.count() == iters);

            for (int idx = 0; idx < iters; idx++) { ec.deleteObject((EOEnterpriseObject)rowsInserted.get(idx)); }
            ec.saveChanges();
            ec.unlock();

            ec.lock();
            NSArray rowsDeleted = EOUtilities.objectsMatchingKeyAndValue(ec, "Employee", "lastName", lastName);
            ec.unlock();

            boolean deleteOk = (rowsDeleted.count() == 0);

            ec.lock();
            ec.deleteObject(company);
            ec.saveChanges();
            ec.unlock();

            long endTicks = System.currentTimeMillis();

            System.out.println(this+": RESULT: "+((insertOk && deleteOk) ? "OK" : "FAILED"));
            System.out.println(this+": START: start = "+startTicks);
            System.out.println(this+": TIME: duration = "+(endTicks - startTicks));

            // System.out.println(this+": done");
        }
    }
}
