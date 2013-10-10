package er.extensions.eof;

import java.util.LinkedList;
import java.util.List;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.erxtest.Application;
import er.extensions.ERXExtensions;

public class ERXThrashTest {
  static List<ThrashTest> useScheme(String scheme) throws Throwable {
    // System.out.println("useScheme:: start, scheme = \""+scheme+"\"");

    List<ThrashTest> tests = new LinkedList<ThrashTest>();

    String[] parts = scheme.split(":");
    // System.out.println("useScheme:: parts # "+parts.length);

    String testClassName = parts[1];
    String parameter = parts[2];
    String plan = parts[3];

    System.out.println("testClassName = \"" + testClassName + "\"");
    System.out.println("parameter = \"" + parameter + "\"");
    System.out.println("plan = \"" + plan + "\"");

    Class<? extends ThrashTest> testClass = Class.forName(ERXThrashTest.class.getName() + "$" + testClassName).asSubclass(ThrashTest.class);

    String[] plans = plan.split(",");
    for (int planNum = 0; planNum < plans.length; planNum++) {
      String subPlan = plans[planNum];
      // System.out.println("subPlan # "+idx+" = \""+subPlan+"\"");

      String[] planParts = subPlan.split("-");
      if (planParts.length != 2) {
        throw new IllegalArgumentException("Cannot figure out part # " + planNum + " (\"" + planParts[planNum] + "\") in scheme: \"" + scheme + "\"");
      }

      int delay = Integer.parseInt(planParts[0]);
      int count = Integer.parseInt(planParts[1]);

      for (int testNum = 0; testNum < count; testNum++) {
        ThrashTest test = testClass.newInstance();
        test.setDelay(delay);
        test.setParam(parameter);
        tests.add(test);
      }
    }

    return tests;
  }

  public static boolean _useWonder = true;
  
  public static void main(String[] arg) throws Throwable {
    ERXExtensions.initApp(Application.class, arg);

    // System.out.println("ERXThrashTest:: Hear me roar!");

    // Enumeration props = System.getProperties().propertyNames();
    // while (props.hasMoreElements()) { System.out.println("props = "+props.nextElement()); }

    //List<ThrashTest> tests = useScheme(System.getProperty("thrash.scheme", "scheme:SimpleInsertTest::0-100,2-100,5-200,10-100,12-100,15-200,20-100,22-100,25-200"));
    long startTime = System.currentTimeMillis();
    //List<ThrashTest> tests = useScheme(System.getProperty("thrash.scheme", "scheme:SimpleFetchTest::0-100,2-100"));
    List<ThrashTest> tests = useScheme(System.getProperty("thrash.scheme", "scheme:SimpleInsertTest::0-100,2-100"));

    List<Thread> threads = new LinkedList<Thread>();
    for (ThrashTest test : tests) {
      Thread thread = new Thread(test);
      thread.start();
      threads.add(thread);
    }

    for (Thread thread : threads) {
      thread.join();
    }
    System.out.println("ERXThrashTest.main: " + (System.currentTimeMillis() - startTime) + " total time");
  }

  public abstract static class ThrashTest implements Runnable {
    private String _param;
    private int _delay;
    private long _startTime;
    private long _endTime;

    public void setDelay(int delay) {
      _delay = delay;
    }

    public void setParam(String param) {
      _param = param;
    }

    public String param() {
      return _param;
    }

    public EOEditingContext editingContext() {
      EOEditingContext ec = _useWonder ? ERXEC.newEditingContext() : new EOEditingContext();
      return ec;
    }

    protected void start() {
      _startTime = System.currentTimeMillis();
    }

    protected void end() {
      _endTime = System.currentTimeMillis();
    }

    protected void print(String title, String str) {
      System.out.println(this + ": " + title + ": " + str);
    }

    public void run() {
      if (_delay > 0) {
        try {
          Thread.sleep(_delay * 1000L);
        }
        catch (java.lang.InterruptedException ie) {
        }
      }

      start();
      _run();
      if (_endTime == 0) {
        end();
      }

      print("TIME", "duration = " + (_endTime - _startTime));
    }

    protected abstract void _run();
  }

  public static class SimpleFetchTest extends ThrashTest {
    @Override
    protected void _run() {
      EOEditingContext ec = editingContext();
      ec.lock();
      NSArray rows = EOUtilities.objectsForEntityNamed(ec, "Employee");
      ec.unlock();
      print("RESULT", "row count = " + rows.count());
    }
  }

  public static class SimpleInsertTest extends ThrashTest {
    @Override
    protected void _run() {
      EOEditingContext ec = editingContext();

      ec.lock();
      EOEnterpriseObject company = EOUtilities.createAndInsertInstance(ec, "Company");
      company.takeValueForKey("SomeBody.com", "name");
      ec.saveChanges();
      ec.unlock();

      start();

      String lastName = Thread.currentThread().getName() + System.currentTimeMillis() + "Person";

      int iters = 10;

      for (int idx = 0; idx < iters; idx++) {
        ec.lock();
        EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(ec, "Employee");
        eo.addObjectToBothSidesOfRelationshipWithKey(company, "company");
        eo.takeValueForKey(Boolean.TRUE, "manager");
        eo.takeValueForKey("Bob" + idx, "name");
        eo.takeValueForKey(lastName, "state");
        ec.unlock();
      }
      ec.lock();
      ec.saveChanges();
      ec.unlock();

      ec.lock();
      NSArray rowsInserted = EOUtilities.objectsMatchingKeyAndValue(ec, "Employee", "state", lastName);
      boolean insertOk = (rowsInserted.count() == iters);

      for (int idx = 0; idx < iters; idx++) {
        ec.deleteObject((EOEnterpriseObject) rowsInserted.get(idx));
      }
      ec.saveChanges();
      ec.unlock();

      ec.lock();
      NSArray rowsDeleted = EOUtilities.objectsMatchingKeyAndValue(ec, "Employee", "state", lastName);
      ec.unlock();

      boolean deleteOk = (rowsDeleted.count() == 0);

      ec.lock();
      ec.deleteObject(company);
      ec.saveChanges();
      ec.unlock();

      end();

      print("RESULT", ((insertOk && deleteOk) ? "OK" : "FAILED"));
    }
  }
}
