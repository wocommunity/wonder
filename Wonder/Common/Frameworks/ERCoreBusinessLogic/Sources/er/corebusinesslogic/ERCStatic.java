// ERCStatic.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class ERCStatic extends _ERCStatic {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCStatic.class);
        
    // Class methods go here
    
    public static class ERCStaticClazz extends _ERCStaticClazz {

        private NSMutableDictionary _staticsPerKey = new NSMutableDictionary();

        public ERCStatic objectMatchingKey(EOEditingContext ec, String key) {
            return objectMatchingKey(ec, key, false);
        }
        
        public ERCStatic objectMatchingKey(EOEditingContext ec, String key, boolean noCache) {
            // If noCache is true we always go to the database
            Object result = noCache ? null : _staticsPerKey.objectForKey(key);
            if (result == null) {
                NSArray arr = preferencesWithKey(ec, key);
                if (arr.count() > 1)
                    throw new IllegalStateException("Found " + arr.count() + " rows for key " + key);
                result = arr.count() == 1 ? arr.objectAtIndex(0) : NSKeyValueCoding.NullValue;
                _staticsPerKey.setObjectForKey(result, key);
                result = result == NSKeyValueCoding.NullValue ? null : result;
            }
            result = result != null ? ERXUtilities.localInstanceOfObject(ec, (ERCStatic)result) : null;
            return (ERCStatic)result;
        }

        public void invalidateCache() { _staticsPerKey.removeAllObjects(); }

        // the STATIC table acts as a dictionary
        private final static EOEditingContext _ec;

        static {
            _ec = ERXEC.newEditingContext(new EOObjectStoreCoordinator());
            _ec.setSharedEditingContext(null);
        }

        public static String staticStoredValueForKey(EOEditingContext ec, String key) {
            return staticStoredValueForKey(ec, key, false);
        }
        
        public static String staticStoredValueForKey(EOEditingContext ec, String key, boolean noCache) {
            ERCStatic entry = ERCStatic.staticClazz().objectMatchingKey(ec, key, noCache);
            return entry != null ? entry.value() : null;
        }

        public static int staticStoredIntValueForKey(EOEditingContext ec, String key) {
            return staticStoredIntValueForKey(ec, key, false);
        }
        
        public static int staticStoredIntValueForKey(EOEditingContext ec, String key, boolean noCache) {
            int result = -1;
            String s = staticStoredValueForKey(ec, key, noCache);
            if (s != null) {
                try {
                    result = Integer.parseInt(s);
                } catch (NumberFormatException e) {}
            }
            return result;
        }

        public static String staticStoredValueForKey(String key, boolean noCache) {
            String value = null;
            try {
                _ec.lock();
                value = staticStoredValueForKey(_ec, key, noCache);
            } finally {
                _ec.unlock();
            }
            return value;
        }

        public static String staticStoredValueForKey(String key) {
            return staticStoredValueForKey(key, false);
        }
        
        public static int staticStoredIntValueForKey(String key) {
            return staticStoredIntValueForKey(key, false);
        }

        public static int staticStoredIntValueForKey(String key, boolean noCache) {
            int value = 0;
            try {
                _ec.lock();
                value = staticStoredIntValueForKey(_ec, key, noCache);
            } finally {
                _ec.unlock();
            }
            return value;
        }        
        
        public static void takeStaticStoredValueForKey(String value,
                                                       String key) {
            try {
                _ec.lock();
                takeStaticStoredValueForKey(_ec, value, key);
                // Clear out the stacks.
                _ec.revert();
            } finally {
                _ec.unlock();
            }
        }

        public static void takeStaticStoredValueForKey(EOEditingContext editingContext,
                                                       String value,
                                                       String key) {
            ERCStatic entry = ERCStatic.staticClazz().objectMatchingKey(editingContext,key);
            if (entry==null) {
                entry=(ERCStatic)ERXUtilities.createEO("ERCStatic", editingContext);
                entry.setKey(key);
            }
            entry.setValue(value);
        }
    }

    public static ERCStaticClazz staticClazz() {
        return (ERCStaticClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCStatic");
    }

    public String toString() {
        return entityName()+": "+key()+"="+value();
    }

    public String userPresentableDescription() {
        return toString();
    }    
}
