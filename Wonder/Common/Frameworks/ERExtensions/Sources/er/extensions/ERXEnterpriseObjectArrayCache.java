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
 * Caches objects of one entity by a given key. Listens to
 * EOEditingContextDidSaveChanges notifications to track changes.
 * Typically you'd fetch values by:<code><pre>
 * ERXEnterpriseObjectArrayCache&lt;HelpText&gt; helpTextCache = new ERXEnterpriseObjectArrayCache&lt;HelpText&gt;("HelpText") {
 *    protected void handleUnsuccessfullQueryForKey(Object key) {
 *       NSArray helpTexts = ... fetch from somewhere
 *       setObjectsForKey(helpTexts, key);
 *   }
 * };
 * ...
 * NSArray&lt;HelpText&gt; helpTexts = helpTextCache.objectsForKey(ec, "AllTexts");
 * ...
 * </pre></code>
 * You can supply a timeout after which the cache is to get cleared and all the objects refetched. Note
 * that this implementation only caches the global IDs, not the actual data. 
 * @author ak
 */
public class ERXEnterpriseObjectArrayCache<T extends EOEnterpriseObject> {
    private String _entityName;
    private Map<Object, NSArray<EOGlobalID>> _cache;
    private long _timeout;
    private long _fetchTime;
    protected static final NSArray NOT_FOUND_MARKER= new NSArray();
    
    /**
     * Creates the cache for the given entity name and the given keypath. No
     * timeout value is used.
     * @param entityName
     */
    public ERXEnterpriseObjectArrayCache(String entityName) {
       this(entityName, 0L);
    }
    
    /**
     * Creates the cache for the given entity name and the given keypath. No
     * timeout value is used.
     * @param entityName
     */
    public ERXEnterpriseObjectArrayCache(Class c) {
       this(entityNameForClass(c));
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
    public ERXEnterpriseObjectArrayCache(String entityName, long timeout) {
        _entityName = entityName;
        _timeout = timeout;
        NSSelector selector = ERXSelectorUtilities.notificationSelector("editingContextDidSaveChanges");
        NSNotificationCenter.defaultCenter().addObserver(this, selector, 
                EOEditingContext.EditingContextDidSaveChangesNotification, null);
    }
    
    /**
     * Helper to check if an array of EOs contains the handled entity. 
     * @param eos
     * @return
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
    
    protected String entityName() {
        return _entityName;
    }

    /**
     * Returns the backing cache. If the cache is to old, it is cleared first.
     * @return
     */
    protected synchronized Map cache() {
        long now = System.currentTimeMillis();
        if(_timeout > 0L && (now - _timeout) > _fetchTime) {
            reset();
        }
        if(_cache == null) {
            _cache = Collections.synchronizedMap(new HashMap()); 
            _fetchTime = System.currentTimeMillis();
        }
        return _cache;
    }

    /**
     * Add a list of objects to the cache with the given key. The object
     * can be null.
     * @param eos array of objects
     */
    public void setObjectsForKey(NSArray<T> eos, Object key) {
        NSArray<EOGlobalID> gids = NOT_FOUND_MARKER;
        if(eos != null) {
            gids = ERXEOControlUtilities.globalIDsForObjects(eos);
        }
        cache().put(key, gids);
    }
    
    /**
     * Retrieves a list of EOs that matches the given key or null if no match 
     * is in the cache.
     * @param ec editing context to get the objects into
     * @param key key value under which the objects are registered 
     * @return
     */
    public NSArray<T> objectsForKey(EOEditingContext ec, Object key) {
        Map<String, NSArray<EOGlobalID>> cache = cache();
        NSArray<EOGlobalID> gids = cache.get(key);
        if(gids == NOT_FOUND_MARKER) {
            return null;
        } else if(gids == null) {
            handleUnsuccessfullQueryForKey(key);
            gids = cache.get(key);
            if(gids == NOT_FOUND_MARKER) {
                return null;
            } else if(gids == null) {
               return null;
            }
        }
        NSArray<T> eos = (NSArray<T>) ERXEOControlUtilities.faultsForGlobalIDs(ec, gids);
        return eos;
    }
  
    /**
     * Called when a query hasn't found an entry in the cache. This
     * implementation puts a not-found marker in the cache so
     * the next query will return null. You could override this
     * method to create an EO with sensible default values and
     * call {@link #setObjectsForKey(NSArray, Object)} on it.
     * @param key
     */
    protected void handleUnsuccessfullQueryForKey(Object key) {
        cache().put(key, NOT_FOUND_MARKER);
    }

    /**
     * Resets the cache by clearing the internal map. When the next value 
     * is accessed, the objects are refetched.
     */
    public synchronized void reset() {
        _cache = null;
    }
    
}