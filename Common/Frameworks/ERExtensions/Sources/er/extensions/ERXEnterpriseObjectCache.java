package er.extensions;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

/**
 * Caches one entity by a given key. Listens to
 * EOEditingContextDidSaveChanges notifications to track changes.
 * Typically you'd have an "identifier" property and you'd fetch values by:<code><pre>
 * ERXEnterpriseObjectCache&lt;HelpText&gt; helpTextCache = new ERXEnterpriseObjectCache&lt;HelpText&gt;("HelpText", "pageConfiguration");
 * ...
 * HelpText helpText = helpTextCache.objectForKey(ec, "ListHelpText");
 * </pre></code>
 * You can supply a timeout after which the cache is to get cleared and all the objects refetched. Note
 * that this implementation only caches the global IDs, not the actual data. 
 * @author ak inspired by a class from Dominik Westner
 */
public class ERXEnterpriseObjectCache<T extends EOEnterpriseObject> {
    private String _entityName;
    private String _keyPath;
    private Map _cache;
    private long _timeout;
    private long _fetchTime;
    protected static final EOGlobalID NO_GID_MARKER= new EOTemporaryGlobalID();
    
    
    public static String ClearCachesNotification = "ERXEnterpriseObjectCache.ClearCaches";
    
    /**
     * Creates the cache for the given entity name and the given keypath. No
     * timeout value is used.
     * @param entityName
     * @param keyPath
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath) {
       this(entityName, keyPath, 0L);
    }
    
    /**
     * Creates the cache for the given entity name and the given keypath. No
     * timeout value is used.
     * @param entityName
     * @param keyPath
     */
    public ERXEnterpriseObjectCache(Class c, String keyPath) {
       this(entityNameForClass(c), keyPath);
    }
    
    private static String entityNameForClass(Class c) {
    	EOEditingContext ec = ERXEC.newEditingContext();
    	ec.lock();
    	try {
    		EOEntity entity = EOUtilities.entityForClass(ec, c);
    		if(entity != null) {
    			return entity.name();
    		}
    		return null;
    	} finally {
    		ec.unlock();
    	}
    }
    
    /**
     * Creates the cache for the given entity, keypath and timeout value in milliseconds.
     * @param entityName
     * @param keyPath
     * @param timeout
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath, long timeout) {
        _entityName = entityName;
        _keyPath = keyPath;
        _timeout = timeout;
        registerForNotifications();
    }

	protected void registerForNotifications() {
		NSSelector selector = ERXSelectorUtilities.notificationSelector("editingContextDidSaveChanges");
        NSNotificationCenter.defaultCenter().addObserver(this, selector, 
                EOEditingContext.EditingContextDidSaveChangesNotification, null);
        selector = ERXSelectorUtilities.notificationSelector("clearCaches");
        NSNotificationCenter.defaultCenter().addObserver(this, selector, 
                ERXEnterpriseObjectCache.ClearCachesNotification, null);
	}
    
    /**
     * Helper to check if an array of EOs contains the handled entity. 
     * @param eos
     */
    private boolean hadRelevantChanges(NSDictionary dict, String key) {
        NSArray eos = (NSArray) dict.objectForKey(key);
        for (Enumeration enumeration = eos.objectEnumerator(); enumeration.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject) enumeration.nextElement();
            if(eo.entityName().equals(entityName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Handler for the editingContextDidSaveChanges notification. Calls reset if
     * and object of the given entity were changed.
     * @param n
     */
    public void editingContextDidSaveChanges(NSNotification n) {
        EOEditingContext ec = (EOEditingContext) n.object();
        if(ec.parentObjectStore() instanceof EOObjectStoreCoordinator) {
            if(!hadRelevantChanges(n.userInfo(), EOEditingContext.InsertedKey)) {
                if(!hadRelevantChanges(n.userInfo(), EOEditingContext.UpdatedKey)) {
                    if(!hadRelevantChanges(n.userInfo(), EOEditingContext.DeletedKey)) {
                        return;
                    }
                }
            }
            reset();
        }
    }
    
    /**
     * Handler for the clearCaches notification. Calls reset if
     * n.object is the entity name.
     * @param n
     */
    public void clearCaches(NSNotification n) {
    	if(n.object() == null || entityName().equals(n.object())) {
    		reset();
    	}
    }
    
    protected String entityName() {
        return _entityName;
    }
    
    /**
     * The key path which should get used for the key of the cache.
     */
    protected String keyPath() {
        return _keyPath;
    }

    /**
     * Returns the backing cache. If the cache is to old, it is cleared first.
     */
    protected synchronized Map cache() {
        long now = System.currentTimeMillis();
        if(_timeout > 0L && (now - _timeout) > _fetchTime) {
            reset();
        }
        if(_cache == null) {
            _cache = Collections.synchronizedMap(new HashMap()); 
            EOEditingContext ec = ERXEC.newEditingContext();
            ec.lock();
            try {
                NSArray objects = initialObjects(ec);
                for (Enumeration enumeration = objects.objectEnumerator(); enumeration.hasMoreElements();) {
                    T eo = (T) enumeration.nextElement();
                    addObject(eo);
                }
            } finally {
                ec.unlock();
            }
            _fetchTime = System.currentTimeMillis();
        }
        return _cache;
    }

    /**
     * Returns the objects to cache initially.
     * @param ec
     */
    protected NSArray<T> initialObjects(EOEditingContext ec) {
        NSArray objects = EOUtilities.objectsForEntityNamed(ec, entityName());
        return objects;
    }

    /**
     * Add an object to the cache. Subclasses may want to override this if the object 
     * to reside under more than one entry (eg, title and identifier).
     * @param eo
     */
    public void addObject(T eo) {
        Object key = eo.valueForKeyPath(keyPath());
        addObjectForKey(eo, key);
    }

    /**
     * Add an object to the cache with the given key. The object
     * can be null.
     * @param eo
     */
    public void addObjectForKey(T eo, Object key) {
        EOGlobalID gid = NO_GID_MARKER;
        if(eo != null) {
            gid = eo.editingContext().globalIDForObject(eo);
        }
        cache().put(key, gid);
    }
    
    /**
     * Retrieves an EO that matches the given key or null if no match 
     * is in the cache.
     * @param ec editing context to get the object into
     * @param key key value under which the object is registered 
     */
    public T objectForKey(EOEditingContext ec, Object key) {
        Map cache = cache();
        EOGlobalID gid = (EOGlobalID) cache.get(key);
        if(gid == NO_GID_MARKER) {
            return null;
        } else if(gid == null) {
            handleUnsuccessfullQueryForKey(key);
            gid = (EOGlobalID) cache.get(key);
            if(gid == NO_GID_MARKER) {
                return null;
            } else if(gid == null) {
               return null;
            }
        }
        T eo = (T) ec.faultForGlobalID(gid, ec);
        return eo;
    }
    
    /**
     * Retrieves an EO that matches the given key or null if no match 
     * is in the cache.
     * @param ec editing context to get the object into
     * @param key key value under which the object is registered 
     */
    public T objectsForKey(EOEditingContext ec, Object key) {
        Map cache = cache();
        EOGlobalID gid = (EOGlobalID) cache.get(key);
        if(gid == NO_GID_MARKER) {
            return null;
        } else if(gid == null) {
            handleUnsuccessfullQueryForKey(key);
            gid = (EOGlobalID) cache.get(key);
            if(gid == NO_GID_MARKER) {
                return null;
            } else if(gid == null) {
               return null;
            }
        }
        T eo = (T) ec.faultForGlobalID(gid, ec);
        return eo;
    }
  
    /**
     * Called when a query hasn't found an entry in the cache. This
     * implementation puts a not-found marker in the cache so
     * the next query will return null. You could override this
     * method to create an EO with sensible default values and
     * call {@link #addObject(EOEnterpriseObject)} on it.
     * @param key
     */
    protected void handleUnsuccessfullQueryForKey(Object key) {
        cache().put(key, NO_GID_MARKER);
    }

    /**
     * Resets the cache by clearing the internal map. When the next value 
     * is accessed, the objects are refetched.
     */
    public synchronized void reset() {
        _cache = null;
    }
    
}
