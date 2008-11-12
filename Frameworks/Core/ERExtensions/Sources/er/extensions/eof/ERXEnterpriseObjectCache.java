package er.extensions.eof;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXExpiringCache;
import er.extensions.foundation.ERXSelectorUtilities;

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
 * @param <T> 
 */
public class ERXEnterpriseObjectCache<T extends EOEnterpriseObject> {
    public static String ClearCachesNotification = "ERXEnterpriseObjectCache.ClearCaches";
    protected static final EOGlobalID NO_GID_MARKER= new EOTemporaryGlobalID();
    
    private String _entityName;
    private String _keyPath;
    private EOQualifier _qualifier;
    private ERXExpiringCache<Object, EORecord<T>> _cache;
    private long _timeout;
    private long _fetchTime;
    private boolean _fetchInitialValues;
    
    private boolean _reuseEditingContext;
    private boolean _retainObjects;
    private ERXEC _editingContext;
    
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
     * @param c 
     * @param keyPath 
     */
    public ERXEnterpriseObjectCache(Class c, String keyPath) {
       this(entityNameForClass(c), keyPath);
    }
    
    private static String entityNameForClass(Class c) {
        ERXEC ec = (ERXEC)ERXEC.newEditingContext();
        ec.setCoalesceAutoLocks(false);
    	ec.lock();
    	try {
    		EOEntity entity = EOUtilities.entityForClass(ec, c);
    		if(entity != null) {
    			return entity.name();
    		}
    		return null;
    	} finally {
    		ec.unlock();
    		ec.dispose();
    	}
    }
    
    /**
     * Creates the cache for the given entity, keypath and timeout value in milliseconds.
     * @param entityName
     * @param keyPath
     * @param qualifier
     * @param timeout
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath, long timeout) {
    	this(entityName, keyPath, null, timeout);
    }
    
    /**
     * Creates the cache for the given entity, keypath and timeout value in milliseconds.
     * @param entityName
     * @param keyPath
     * @param qualifier
     * @param timeout
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath, EOQualifier qualifier, long timeout) {
        _entityName = entityName;
        _keyPath = keyPath;
        _timeout = timeout;
        _qualifier = qualifier;
        _fetchInitialValues = true; // MS: for backwards compatibility
        start();
    }

	public void start() {
		NSSelector selector = ERXSelectorUtilities.notificationSelector("editingContextDidSaveChanges");
        NSNotificationCenter.defaultCenter().addObserver(this, selector, 
                EOEditingContext.EditingContextDidSaveChangesNotification, null);
        selector = ERXSelectorUtilities.notificationSelector("clearCaches");
        NSNotificationCenter.defaultCenter().addObserver(this, selector, 
                ERXEnterpriseObjectCache.ClearCachesNotification, null);
        
        if (_timeout > 0) {
        	_cache.startBackgroundExpiration();
        }
	}
	
	public void stop() {
		NSNotificationCenter.defaultCenter().removeObserver(this, EOEditingContext.EditingContextDidSaveChangesNotification, null);
		NSNotificationCenter.defaultCenter().removeObserver(this, ERXEnterpriseObjectCache.ClearCachesNotification, null);
    	_cache.stopBackgroundExpiration();
	}
    
	protected ERXEC editingContext() {
		ERXEC editingContext;
		if (_reuseEditingContext) {
			synchronized (this) {
				if (_editingContext == null) {
					_editingContext = (ERXEC)ERXEC.newEditingContext();
		            _editingContext.setCoalesceAutoLocks(false);
				}
			}
			editingContext = _editingContext;
		}
		else {
			editingContext = (ERXEC)ERXEC.newEditingContext();
            editingContext.setCoalesceAutoLocks(false);
		}
		return editingContext;
	}
	
    /**
     * Helper to check if an array of EOs contains the handled entity. 
     * @param dict 
     * @param key 
     * @param eos
     * @return 
     */
    private NSArray<T> relevantChanges(NSDictionary dict, String key) {
    	NSMutableArray<T> releventEOs = null;
        NSArray<EOEnterpriseObject> eos = (NSArray<EOEnterpriseObject>) dict.objectForKey(key);
        for (Enumeration enumeration = eos.objectEnumerator(); enumeration.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject) enumeration.nextElement();
            if(eo.entityName().equals(entityName())) {
            	if (releventEOs == null) {
            		releventEOs = new NSMutableArray();
            	}
            	releventEOs.addObject((T)eo);
            }
        }
        return releventEOs;
    }
    
    /**
     * Handler for the editingContextDidSaveChanges notification. Calls reset if
     * and object of the given entity were changed.
     * @param n
     */
    public void editingContextDidSaveChanges(NSNotification n) {
        EOEditingContext ec = (EOEditingContext) n.object();
        if(ec.parentObjectStore() instanceof EOObjectStoreCoordinator) {
        	NSArray<T> releventsInsertedEOs = relevantChanges(n.userInfo(), EOEditingContext.InsertedKey);
        	NSArray<T> releventsUpdatedEOs = relevantChanges(n.userInfo(), EOEditingContext.UpdatedKey);
        	NSArray<T> releventsDeletedEOs = relevantChanges(n.userInfo(), EOEditingContext.DeletedKey);
        	ERXExpiringCache<Object, EORecord<T>> cache = cache();
        	synchronized (cache) { 
	        	if (releventsInsertedEOs != null) {
	        		for (T eo : releventsInsertedEOs) {
	        			addObject(eo);
	        		}
	        	}
	        	if (releventsUpdatedEOs != null) {
	        		for (T eo : releventsUpdatedEOs) {
	        			updateObject(eo);
	        		}
	        	}
	        	if (releventsDeletedEOs != null) {
	        		for (T eo : releventsDeletedEOs) {
	        			removeObject(eo);
	        		}
	        	}
        	}
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
     * @return 
     */
    protected String keyPath() {
        return _keyPath;
    }

    /**
     * Returns the backing cache. If the cache is to old, it is cleared first.
     * @return 
     */
    protected synchronized ERXExpiringCache<Object, EORecord<T>> cache() {
        long now = System.currentTimeMillis();
        if(_fetchInitialValues && _timeout > 0L && (now - _timeout) > _fetchTime) {
            reset();
        }
        if(_cache == null) {
        	if (_fetchInitialValues) {
                _cache = new ERXExpiringCache<Object, EORecord<T>>(ERXExpiringCache.NO_TIMEOUT);
        	}
        	else {
                _cache = new ERXExpiringCache<Object, EORecord<T>>(_timeout);
                if (_timeout > 0) {
                	_cache.startBackgroundExpiration();
                }
        	}
            if (_fetchInitialValues) {
	            ERXEC ec = editingContext();
	            ec.setCoalesceAutoLocks(false);
	            ec.lock();
	            try {
	                NSArray objects = initialObjects(ec);
	                for (Enumeration enumeration = objects.objectEnumerator(); enumeration.hasMoreElements();) {
	                    T eo = (T) enumeration.nextElement();
	                    addObject(eo);
	                }
	            } finally {
	                ec.unlock();
	                ec.dispose();
	            }
            }
            _fetchTime = System.currentTimeMillis();
        }
        return _cache;
    }
    
    protected EORecord<T> createRecord(EOGlobalID gid, T eo) {
    	EORecord<T> record;
    	if (_retainObjects) {
    		EOEditingContext editingContext = editingContext();
    		T localEO = ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    		record = new EORecord<T>(gid, localEO);
    	}
    	else {
    		record = new EORecord<T>(gid, null);
    	}
    	return record;
    }

    /**
     * Returns the objects to cache initially.
     * @param ec
     * @return 
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
     * @param key 
     */
    public void addObjectForKey(T eo, Object key) {
    	if (_qualifier == null || _qualifier.evaluateWithObject(eo)) {
	        EOGlobalID gid = NO_GID_MARKER;
	        if(eo != null) {
	            gid = eo.editingContext().globalIDForObject(eo);
	        }
	        cache().setObjectForKey(createRecord(gid, eo), key);
    	}
    }

    public void removeObject(T eo) {
        Object key = eo.valueForKeyPath(keyPath());
        removeObjectForKey(eo, key);
    }

    public void removeObjectForKey(T eo, Object key) {
        cache().setObjectForKey(createRecord(NO_GID_MARKER, null), key);
    }

    public void updateObject(T eo) {
        Object key = eo.valueForKeyPath(keyPath());
        updateObjectForKey(eo, key);
    }

    public void updateObjectForKey(T eo, Object key) {
        EOGlobalID gid = NO_GID_MARKER;
        if(eo != null) {
            gid = eo.editingContext().globalIDForObject(eo);
        }
        ERXExpiringCache<Object, EORecord<T>> cache = cache();
        synchronized (cache) {
        	Object previousKey = null;
        	for (Object entryKey : cache.allKeys()) {
        		EORecord<T> entryValue = cache.objectForKey(entryKey);
        		if (entryValue != null && entryValue.gid.equals(gid)) {
        			previousKey = entryKey;
        			break;
        		}
        	}
        	if (previousKey != null) {
        		if (!previousKey.equals(key)) {
	        		removeObjectForKey(eo, previousKey);
	            	addObjectForKey(eo, key);
	        	}
	        	else if (_qualifier != null && !_qualifier.evaluateWithObject(eo)) {
	        		removeObjectForKey(eo, previousKey);
	        	}
	        	else {
	            	// leave it alone
	        	}
        	}
        	else {
        		addObjectForKey(eo, key);
        	}
        }
    }
    
    /**
     * Retrieves an EO that matches the given key or null if no match 
     * is in the cache.
     * @param ec editing context to get the object into
     * @param key key value under which the object is registered 
     * @return 
     */
    public T objectForKey(EOEditingContext ec, Object key) {
    	return objectForKey(ec, key, true);
    }
    
    /**
     * Retrieves an EO that matches the given key or null if no match 
     * is in the cache.
     * @param ec editing context to get the object into
     * @param key key value under which the object is registered 
     * @param handleUnsuccessfulQueryForKey if false, a cache miss returns null rather than fetching
     * @return 
     */
    public T objectForKey(EOEditingContext ec, Object key, boolean handleUnsuccessfulQueryForKey) {
        ERXExpiringCache<Object, EORecord<T>> cache = cache();
        EORecord<T> record = cache.objectForKey(key);
        if (record == null) {
        	if (handleUnsuccessfulQueryForKey) {
	            handleUnsuccessfullQueryForKey(key);
	            record = cache.objectForKey(key);
	            if (record == null) {
	            	return null;
	            }
	            else if (record.gid == NO_GID_MARKER) {
	               return null;
	            }
        	}
        	else {
        		return null;
        	}
        }
        else if (record.gid == NO_GID_MARKER) {
            return null;
        }
        T eo = record.eo;
        if (eo == null) {
        	eo = (T) ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(ec, record.gid);
        }
        else {
        	eo = ERXEOControlUtilities.localInstanceOfObject(ec, eo);
        }
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
    	if (!_fetchInitialValues) {
            ERXEC ec = editingContext();
        	ec.lock();
        	try {
    			EOQualifier qualifier;
    			if (_qualifier == null) {
    				qualifier = ERXQ.is(_keyPath, key);
    			}
    			else {
    				qualifier = ERXQ.is(_keyPath, key).and(_qualifier);
    			}
    			ERXFetchSpecification fetchSpec = new ERXFetchSpecification(_entityName, qualifier, null);
    			NSArray<T> objects = ec.objectsWithFetchSpecification(fetchSpec);
    			if (objects.count() == 0) {
    				cache().setObjectForKey(createRecord(NO_GID_MARKER, null), key);
    			}
    			else if (objects.count() == 1) {
        			T eo = objects.objectAtIndex(0);
        			addObject(eo);
    			}
    			else {
    				throw new EOUtilities.MoreThanOneException("There was more than one " + _entityName + " matching the qualifier '" + qualifier + "'.");
    			}
        	}
        	finally {
        		ec.unlock();
        		if (!_reuseEditingContext) {
        			ec.dispose();
        		}
        	}
    	}
    	else {
    		cache().setObjectForKey(createRecord(NO_GID_MARKER, null), key);
    	}
    }

    /**
     * Resets the cache by clearing the internal map. When the next value 
     * is accessed, the objects are refetched.
     */
    public synchronized void reset() {
    	if (_cache != null) {
    		_cache.removeAllObjects();
    	}
    }
    
    /**
     * Sets whether or not the initial values should be fetched into
     * this cache or whether or should lazy load.
     * 
     * @param fetchInitialValues if true, the initial values are fetched into the cache
     */
    public void setFetchInitialValues(boolean fetchInitialValues) {
		_fetchInitialValues = fetchInitialValues;
	}
    
    /**
     * Sets whether or not the editing context for this cache is reused for multiple requests.
     * 
     * @param reuseEditingContext whether or not the editing context for this cache is reused for multiple requests
     */
    public void setReuseEditingContext(boolean reuseEditingContext) {
		if (_retainObjects && !reuseEditingContext) {
			throw new IllegalArgumentException("If retainObjects is true, reuseEditingContext cannot be false.");
		}
		_reuseEditingContext = reuseEditingContext;
	}
    
    /**
     * Sets whether or not the cached EO's themselves are retained versus just their GID's.  If set,
     * this implicitly sets reuseEditingContext to true.
     *   
     * @param retainObjects if true, the EO's are retained
     */
    public void setRetainObjects(boolean retainObjects) {
		_retainObjects = retainObjects;
		setReuseEditingContext(true);
	}
    
    private static class EORecord<T> {
    	public EOGlobalID gid;
    	public T eo;
    	
    	public EORecord(EOGlobalID gid, T eo) {
    		this.gid = gid;
    		this.eo = eo;
    	}
    }
}
