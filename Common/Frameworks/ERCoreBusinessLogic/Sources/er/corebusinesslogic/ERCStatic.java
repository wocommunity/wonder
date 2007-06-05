// ERCStatic.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.EOEnterpriseObjectClazz;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXProperties;

public class ERCStatic extends _ERCStatic {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERCStatic.class);
        
    // Class methods go here
    
    public static class ERCStaticClazz extends _ERCStaticClazz {

        private NSMutableDictionary _staticsPerKey = new NSMutableDictionary();

        public ERCStatic objectMatchingKey(EOEditingContext ec, String key) {
            return objectMatchingKey(ec, key, false);
        }
        
        public NSArray preferencesWithKey(EOEditingContext ec, String key) {
        	return objectsForPreferences(ec, key);
        }
        
        public ERCStatic objectMatchingKey(EOEditingContext ec, String key, boolean noCache) {
            // If noCache is true we always go to the database
            Object result = noCache ? null : _staticsPerKey.objectForKey(key);
            if (result == null) {
                NSArray arr = preferencesWithKey(ec, key);
                if (arr.count() > 1)
                    throw new IllegalStateException("Found " + arr.count() + " rows for key " + key);
                result = arr.count() == 1 ? arr.objectAtIndex(0) : NSKeyValueCoding.NullValue;
                if (result instanceof EOEnterpriseObject) {
                    privateEditingContext().lock();
                    try {
                        _staticsPerKey.setObjectForKey(ERXEOControlUtilities.localInstanceOfObject(privateEditingContext(),
                                                                                                   (ERCStatic)result),
                                                       key);                        
                    } finally {
                        privateEditingContext().unlock();
                    }
                }
                result = result == NSKeyValueCoding.NullValue ? null : result;
            } else if (result instanceof EOEnterpriseObject) {
                privateEditingContext().lock();
                try {
                    result = ERXEOControlUtilities.localInstanceOfObject(ec, (ERCStatic)result);
                } finally {
                    privateEditingContext().unlock();
                }                
            } else if (result.equals(NSKeyValueCoding.NullValue)) {
                result = null;
            }
            return (ERCStatic)result;
        }

        public void invalidateCache() { _staticsPerKey.removeAllObjects(); }

        private EOEditingContext _privateEditingContext;
        private EOEditingContext privateEditingContext() {
            if (_privateEditingContext == null) {
                if (ERXProperties.booleanForKeyWithDefault("er.corebusinesslogic.ERCStatic.UseSeparateChannel", true)) {
                    _privateEditingContext = ERXEC.newEditingContext(new EOObjectStoreCoordinator());
                    _privateEditingContext.lock();
                    try {
                        _privateEditingContext.setSharedEditingContext(null);
                    } finally {
                        _privateEditingContext.unlock();
                    }
                } else {
                    _privateEditingContext = ERXEC.newEditingContext();
                }                
            }
            return _privateEditingContext;
        }

        public String staticStoredValueForKey(EOEditingContext ec, String key) {
            return staticStoredValueForKey(ec, key, false);
        }
        
        public String staticStoredValueForKey(EOEditingContext ec, String key, boolean noCache) {
            ERCStatic entry = ERCStatic.clazz.objectMatchingKey(ec, key, noCache);
            return entry != null ? entry.value() : null;
        }

        public int staticStoredIntValueForKey(EOEditingContext ec, String key) {
            return staticStoredIntValueForKey(ec, key, false);
        }
        
        public int staticStoredIntValueForKey(EOEditingContext ec, String key, boolean noCache) {
            int result = -1;
            String s = staticStoredValueForKey(ec, key, noCache);
            if (s != null) {
                try {
                    result = Integer.parseInt(s);
                } catch (NumberFormatException e) {}
            }
            return result;
        }

        public String staticStoredValueForKey(String key, boolean noCache) {
            String value = null;
            privateEditingContext().lock();
            try {
                value = staticStoredValueForKey(privateEditingContext(), key, noCache);
            } finally {
                privateEditingContext().unlock();
            }
            return value;
        }

        public String staticStoredValueForKey(String key) {
            return staticStoredValueForKey(key, false);
        }
        
        public int staticStoredIntValueForKey(String key) {
            return staticStoredIntValueForKey(key, false);
        }

        public int staticStoredIntValueForKey(String key, boolean noCache) {
            int value = 0;
            privateEditingContext().lock();
            try {
                value = staticStoredIntValueForKey(privateEditingContext(), key, noCache);
            } finally {
                privateEditingContext().unlock();
            }
            return value;
        }        
        
        public void takeStaticStoredValueForKey(String value,
                                                       String key) {
            privateEditingContext().lock();
            try {
                takeStaticStoredValueForKey(privateEditingContext(), value, key);
                // Clear out the stacks.
                privateEditingContext().saveChanges();
                privateEditingContext().revert();
            } finally {
                privateEditingContext().unlock();
            }
        }

        public void takeStaticStoredValueForKey(EOEditingContext editingContext,
                                                       String value,
                                                       String key) {
            ERCStatic entry = ERCStatic.clazz.objectMatchingKey(editingContext,key);
            if (entry==null) {
                entry=(ERCStatic)ERXEOControlUtilities.createAndInsertObject(editingContext, "ERCStatic");
                entry.setKey(key);
            }
            entry.setValue(value);
        }
    }

    public static ERCStaticClazz clazz = new ERCStaticClazz();

    public String toString() {
        return entityName()+": "+key()+"="+value();
    }

    public String userPresentableDescription() {
        return toString();
    }    
}
