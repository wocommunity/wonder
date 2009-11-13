package er.extensions.foundation;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ERXThreadStorageTest extends TestCase {

    // public static final java.lang.String KEYS_ADDED_IN_CURRENT_THREAD_KEY;
    // public static final java.lang.String WAS_CLONED_MARKER;

    public void testConstructor() {
        Assert.assertNotNull(new ERXThreadStorage());
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
        // public static java.util.Map map();
    }

    public void testReset() {
        // public static void reset();
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
