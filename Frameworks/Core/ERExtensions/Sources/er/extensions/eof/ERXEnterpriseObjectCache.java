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
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXExtensions;
import er.extensions.foundation.ERXExpiringCache;
import er.extensions.foundation.ERXSelectorUtilities;

/**
 * Caches instances of one entity by a given key(path). Typically you'd have an "identifier" property 
 * and you'd fetch values by:<pre><code>
 * ERXEnterpriseObjectCache&lt;HelpText&gt; helpTextCache = new ERXEnterpriseObjectCache&lt;HelpText&gt;("HelpText", "pageConfiguration");
 * ...
 * HelpText helpText = helpTextCache.objectForKey(ec, "ListHelpText");
 * </code></pre>
 * 
 * You can supply a timeout after which individual objects (or all objects if fetchInitialValues
 * is <code>true</code>) get cleared and re-fetched. This implementation can cache either only the global IDs, 
 * or the global ID and a copy of the actual object. Caching the actual object ensures that the snapshot stays around
 * and thus prevent additional trips to the database.
 * 
 * Listens to EOEditingContextDidSaveChanges notifications to track changes to objects in the cache and ClearCachesNotification 
 * for messages to purge the cache.
 * @author ak inspired by a class from Dominik Westner
 * @param <T> the type of EOEnterpriseObject in this cache
 */
public class ERXEnterpriseObjectCache<T extends EOEnterpriseObject> {
	
	/** Other code can send this notification if it needs to have this cache discard all of the
	 * objects that it has. The object in the notification is the name of the EOEntity to discard cache for.
	 */
    public static String ClearCachesNotification = "ERXEnterpriseObjectCache.ClearCaches";
    
    protected static final EOGlobalID NO_GID_MARKER = new EOTemporaryGlobalID();
    
    /** Name of the EOEntity to cache. */
    private String _entityName;
    
    /** Key path to data uniquely identifying an instance of this entity. */
    private String _keyPath;
    
    /** EOQualifier restricting which instances are stored in this cache */
    private EOQualifier _qualifier;
    
    /** Actual cache implementation. */
    private ERXExpiringCache<Object, EORecord<T>> _cache;
    
    /** Time to live in milliseconds for an object in this cache. */
    private long _timeout;
    
    /** The time when the objects in this cache were fetched. Only used if _fetchInitialValues is <code>true</code>. */
    private long _fetchTime;
    
    /** <code>true</code> if this cache should be populated when created, <code>false</code> for lazy population. */
    private boolean _fetchInitialValues;
    
    /** If <code>true</code>, just a single editing context instance is used for this cache instance. */
    private boolean _reuseEditingContext;

    /** The single editing context instance is used for this cache instance if <code>_reuseEditingContext</code> is <code>true</code>. */
    private ERXEC _editingContext;

    /** If <code>true</code>, this cache retains an instance of each object so that the snapshot does not expire. */
    private boolean _retainObjects;
    
    /** If <code>true</code>, the entire cache contents are discarded when any object in it changes. Probably not what you want.
     * @see #editingContextDidSaveChanges(NSNotification)
     */
    private boolean _resetOnChange;
    
    /** If <code>true</code>, object that have not been saved yet are found by the cache. */
    private boolean _returnUnsavedObjects;
    
    /** If <code>false</code>, the cache is not allowed to fetch values as migrations may not have been processed yet.
     * @see ERXExtensions#didFinishInitialization()
     * @see #setApplicationDidFinishInitialization(boolean)
     */
    private static boolean _applicationDidFinishInitialization;
    
    /**
     * Creates the cache for the given entity name and the given keypath. No
     * timeout value is used.
     * @param entityName name of the EOEntity to cache
     * @param keyPath key path to data uniquely identifying an instance of this entity
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath) {
       this(entityName, keyPath, 0L);
    }
    
    /**
     * Creates the cache for the entity implemented by the passed class and the given keypath. No
     * timeout value is used.
     * @param c Class used to identify which EOEntity this cache is for
     * @param keyPath key path to data uniquely identifying an instance of this entity
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
     * @param entityName name of the EOEntity to cache
     * @param keyPath key path to data uniquely identifying an instance of this entity
     * @param timeout time to live in milliseconds for an object in this cache
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath, long timeout) {
    	this(entityName, keyPath, null, timeout);
    }
    
    /**
     * Creates the cache for the given entity, keypath and timeout value in milliseconds. Only objects
     * that match qualifier are stored in the cache. Note that _resetOnChange (and _fetchInitialValues) are
     * both <code>true</code> after this constructor. You will almost certainly want to call
     * <code>setResetOnChange(false);</code>.
 	 *
 	 * @see #setResetOnChange(boolean)
 	 * @see #setFetchInitialValues(boolean)
 	 * 
     * @param entityName name of the EOEntity to cache
     * @param keyPath key path to data uniquely identifying an instance of this entity
     * @param qualifier EOQualifier restricting which instances are stored in this cache
     * @param timeout time to live in milliseconds for an object in this cache
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath, EOQualifier qualifier, long timeout) {
        _entityName = entityName;
        _keyPath = keyPath;
        _timeout = timeout;
        _qualifier = qualifier;
        _resetOnChange = true; // MS: for backwards compatibility
        _fetchInitialValues = true; // MS: for backwards compatibility
        start();
    }

    /**
     * Creates the cache for the given entity, keypath and timeout value in milliseconds. Only objects
     * that match qualifier are stored in the cache.
 	 *
 	 * @see #setResetOnChange(boolean)
 	 * @see #setFetchInitialValues(boolean)
 	 * @see #setRetainObjects(boolean)
 	 * 
     * @param entityName name of the EOEntity to cache
     * @param keyPath key path to data uniquely identifying an instance of this entity
     * @param qualifier EOQualifier restricting which instances are stored in this cache
     * @param timeout time to live in milliseconds for an object in this cache
     * @param shouldRetainObjects true if this cache should retain the cached objects, false to keep only the GID
     * @param shouldFetchInitialValues true if the cache should be fully populated on first access
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath, EOQualifier qualifier, long timeout, boolean shouldRetainObjects, boolean shouldFetchInitialValues) {
    	this(entityName, keyPath, qualifier, timeout, shouldRetainObjects, shouldFetchInitialValues, false);
    }

    /**
     * Creates the cache for the given entity, keypath and timeout value in milliseconds. Only objects
     * that match qualifier are stored in the cache.
 	 *
 	 * @see #setResetOnChange(boolean)
 	 * @see #setFetchInitialValues(boolean)
 	 * @see #setRetainObjects(boolean)
 	 * 
     * @param entityName name of the EOEntity to cache
     * @param keyPath key path to data uniquely identifying an instance of this entity
     * @param qualifier EOQualifier restricting which instances are stored in this cache
     * @param timeout time to live in milliseconds for an object in this cache
     * @param shouldRetainObjects true if this cache should retain the cached objects, false to keep only the GID
     * @param shouldFetchInitialValues true if the cache should be fully populated on first access
     * @param shouldReturnUnsavedObjects true if unsaved matching objects should be returned, see {@link #unsavedMatchingObject(EOEditingContext, Object)}
     */
    public ERXEnterpriseObjectCache(String entityName, String keyPath, EOQualifier qualifier, long timeout, 
    		                        boolean shouldRetainObjects, boolean shouldFetchInitialValues, boolean shouldReturnUnsavedObjects) {
        _entityName = entityName;
        _keyPath = keyPath;
        _timeout = timeout;
        _qualifier = qualifier;
        _returnUnsavedObjects = shouldReturnUnsavedObjects;
        setRetainObjects(shouldRetainObjects);
        setResetOnChange(false);
        setFetchInitialValues(shouldFetchInitialValues);
        start();
    }
    
    /**
     * Call this to re-start cache updating after stop() is called. This is automatically called from the
     * constructor so unless you call stop(), there is no need to ever call this method.
	 * @see #stop()
     */
	public void start() {
		// Catch this to update the cache when an object is changed
        NSSelector selector = ERXSelectorUtilities.notificationSelector("editingContextDidSaveChanges");
        NSNotificationCenter.defaultCenter().addObserver(this, selector, 
                EOEditingContext.EditingContextDidSaveChangesNotification, null);
        
        // Catch this for custom notifications that the cache should be discarded
        selector = ERXSelectorUtilities.notificationSelector("clearCaches");
        NSNotificationCenter.defaultCenter().addObserver(this, selector, 
                ERXEnterpriseObjectCache.ClearCachesNotification, null);
        
        if (_timeout > 0 && _cache != null) {
        	_cache.startBackgroundExpiration();
        }
	}
	
    /**
     * Call this to stop cache updating.
	 * @see #start()
     */
	public void stop() {
		NSNotificationCenter.defaultCenter().removeObserver(this, EOEditingContext.EditingContextDidSaveChangesNotification, null);
		NSNotificationCenter.defaultCenter().removeObserver(this, ERXEnterpriseObjectCache.ClearCachesNotification, null);
    	_cache.stopBackgroundExpiration();
	}

	/**
	 * Called from {@link ERXExtensions#finishInitialization()} to enable fetches. This is to ensure that
	 * migrations have run prior first fetch from this class.
	 * 
	 * @param didFinish indicator if application did finish initialization phase
	 */
	public static void setApplicationDidFinishInitialization(boolean didFinish) {
		_applicationDidFinishInitialization = didFinish;
	}

	/**
	 * Returns the editing context that holds object that are in this cache. If _reuseEditingContext is false, 
	 * a new editing context instance is returned each time. The returned editing context is autolocking.
	 * 
	 * @return the editing context that holds object that are in this cache
	 */
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
     * Helper to check a dictionary of objects from an EOF notification and return any that are for the
     * entity that we are caching.
     * 
     * @param dict dictionary of key to {@literal NSArray<EOEnterpriseObject>}
     * @param key key into dict indicating which list to process
     * @return objects from the list that are of the entity we are caching, or an empty array if there are no matches
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
        return releventEOs != null ? releventEOs : NSArray.EmptyArray;
    }
    
    /**
     * Handler for the editingContextDidSaveChanges notification. If <code>_resetOnChange</code> is <code>true</code>, this
     * calls reset() to discard the entire cache contents if an object of the given entity has been changed.
     * If <code>_resetOnChange</code> is <code>false</code>, this updates the cache to reflect the added/changed/removed
     * objects.
     * 
     * @see EOEditingContext#ObjectsChangedInEditingContextNotification
     * @see #reset()
     * 
     * @param n NSNotification with EOEditingContext as the object and a dictionary of changes in the userInfo
     */
    public void editingContextDidSaveChanges(NSNotification n) {
        EOEditingContext ec = (EOEditingContext) n.object();
        // Only look at changes from editing contexts in the same EOF stack; ignore changes from child editing contexts
        if(_applicationDidFinishInitialization && ec.parentObjectStore().equals(editingContext().rootObjectStore())) {
        	NSArray<T> releventsInsertedEOs = relevantChanges(n.userInfo(), EOEditingContext.InsertedKey);
        	NSArray<T> releventsUpdatedEOs = relevantChanges(n.userInfo(), EOEditingContext.UpdatedKey);
        	NSArray<T> releventsDeletedEOs = relevantChanges(n.userInfo(), EOEditingContext.DeletedKey);
        	if (_resetOnChange) {
        		if (releventsInsertedEOs.count() > 0 || releventsUpdatedEOs.count() > 0 || releventsDeletedEOs.count() > 0) {
        			reset();
        		}
        	}
        	else {
	        	ERXExpiringCache<Object, EORecord<T>> cache = cache();
	        	synchronized (cache) { 
	        		for (T eo : releventsInsertedEOs) {
	        			addObject(eo);
	        		}
	        		for (T eo : releventsUpdatedEOs) {
	        			updateObject(eo);
	        		}
	        		for (T eo : releventsDeletedEOs) {
	        			removeObject(eo);
	        		}
	        	}
        	}
        }
    }
    
    /**
     * Handler for the clearCaches notification. Calls reset if n.object is the name of the entity we are caching.
     * Other code can send this notification if it needs to have this cache discard all of the objects.
     * 
     * @see #ClearCachesNotification
     * 
     * @param n NSNotification with an entity name
     */
    public void clearCaches(NSNotification n) {
    	if(n.object() == null || entityName().equals(n.object())) {
    		reset();
    	}
    }

    /**
     * @return the name of the EOEntity this cache is for
     */
    protected String entityName() {
        return _entityName;
    }
    
    /**
     * @return Key path to data uniquely identifying an instance of the entity in this cache
     */
    protected String keyPath() {
        return _keyPath;
    }

    /**
     * Returns the backing cache. If the cache is to old, it is cleared first. The cache is created if needed,
     * and the contents populated if <code>_fetchInitialValues</code>.
     * @return the backing cache
     */
    protected synchronized ERXExpiringCache<Object, EORecord<T>> cache() {
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
        	
            preLoadCacheIfNeeded();
        }
        
        // If initial values are fetched, the entire cache expires at the same time
        long now = System.currentTimeMillis();
        if(_fetchInitialValues && _timeout > 0L && (now - _timeout) > _fetchTime) {
            reset();
        }
        
        return _cache;
    }
    
    /**
     * Created an EORecord instance representing eo using its EOGlobalID. If <code>_retainObjects</code>, 
     * this will also include an instance of the EO to ensure that the snapshot is retained.
     *
     * @param gid EOGlobalID of eo
     * @param eo the EO to make an EORecord for
     * @return EORecord instance representing eo
     */
    protected EORecord<T> createRecord(EOGlobalID gid, T eo) {
    	EORecord<T> record;
    	if (_retainObjects) {
    		EOEditingContext editingContext = editingContext();
    		T localEO = ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    		if (localEO != null && localEO.isFault()) {
    			localEO.willRead();
    		}
    		record = new EORecord<T>(gid, localEO);
    	}
    	else {
    		record = new EORecord<T>(gid, null);
    	}
    	return record;
    }
    
    /**
     * Loads all relevant objects into the cache if set to fetch initial values.
     */
    protected void preLoadCacheIfNeeded() {
        if (_fetchInitialValues) {
            _fetchTime = System.currentTimeMillis();
            ERXEC ec = editingContext();
            ec.setCoalesceAutoLocks(false);
            // The other methods are synchronized on cache and then lock the EC. If we do it backwards, 
            // we can get a deadly embrace.
    		synchronized (cache()) {
	            ec.lock();	// Prevents lock churn
	            try {
	        		ERXFetchSpecification fetchSpec = new ERXFetchSpecification(entityName(), qualifier(), null);
	        		fetchSpec.setRefreshesRefetchedObjects(true);
	        		fetchSpec.setIsDeep(true);
	        		NSArray objects = ec.objectsWithFetchSpecification(fetchSpec);
	                for (Enumeration enumeration = objects.objectEnumerator(); enumeration.hasMoreElements();) {
	                    T eo = (T) enumeration.nextElement();
	                    addObject(eo);
	                }
	            } finally {
	                ec.unlock();
	                if ( ! _reuseEditingContext) {
	                	ec.dispose();
	                }
	            }
    		}
        }
    }

    /**
     * Add an object to the cache using <code>eo.valueForKeyPath(keyPath())</code> as the key. 
     * @see #addObjectForKey(EOEnterpriseObject, Object)
     * @param eo the object to add to the cache
     */
    public void addObject(T eo) {
        Object key = eo.valueForKeyPath(keyPath());
        if (key == null) {
        	key = NSKeyValueCoding.NullValue;
        }
        addObjectForKey(eo, key);
    }

    /**
     * Add an object to the cache with the given key if it matches the qualifier, or 
     * if there is no qualifier. The object can be null, in which case a place holder 
     * is added.
     * @param eo eo the object to add to the cache
     * @param key the key to add the object under
     */
    public void addObjectForKey(T eo, Object key) {
    	if (qualifier() == null || qualifier().evaluateWithObject(eo)) {
	        EOGlobalID gid = NO_GID_MARKER;
	        if(eo != null) {
	            gid = eo.editingContext().globalIDForObject(eo);
	        }
	        cache().setObjectForKey(createRecord(gid, eo), key);
    	}
    }

    /**
     * Removes an object from the cache using <code>eo.valueForKeyPath(keyPath())</code> as the key. 
     * @see #removeObjectForKey(EOEnterpriseObject, Object)
     * @param eo the object to remove from the cache
     */
    public void removeObject(T eo) {
        Object key = eo.valueForKeyPath(keyPath());
        if (key == null) {
        	key = NSKeyValueCoding.NullValue;
        }
        removeObjectForKey(eo, key);
    }

    /**
     * Removes the object associated with key from the cache.
     *
     * @param eo eo the object to remove from the cache (ignored)
     * @param key the key to remove the object for
     */
    public void removeObjectForKey(T eo, Object key) {
        cache().setObjectForKey(createRecord(NO_GID_MARKER, null), key);
    }
    
    /**
     * Updates an object in the cache (adding if not present) using 
     * <code>eo.valueForKeyPath(keyPath())</code> as the key.
     * @see #updateObjectForKey(EOEnterpriseObject, Object)
     * @param eo the object to update in the cache
     */
    public void updateObject(T eo) {
        Object key = eo.valueForKeyPath(keyPath());
        if (key == null) {
        	key = NSKeyValueCoding.NullValue;
        }
        updateObjectForKey(eo, key);
    }

    /**
     * Updates an object in the cache (adding if not present) with the given key if it 
     * matches the qualifier, or if there is no qualifier. The object can be null, in which 
     * case is it removed from the cache. If <code>qualifier()</code> is not null, the object
     * is removed from the cache if it does not match the qualifier.
     * @param eo eo the object to update in the cache
     * @param key the key of the object to update
     */
    public void updateObjectForKey(T eo, Object key) {
        EOGlobalID gid = NO_GID_MARKER;
        if(eo != null) {
            gid = eo.editingContext().globalIDForObject(eo);
        }
        ERXExpiringCache<Object, EORecord<T>> cache = cache();
        synchronized (cache) {
        	Object previousKey = key;
        	T previousObject = objectForKey(editingContext(), key, false);
        	
        	// If the object does not exist under key, or a different object exists under that key,
            // the key value may have been changed. Search the entire cache for the object by GID
        	if (previousObject == null || ! previousObject.editingContext().globalIDForObject(previousObject).equals(gid)) {
            	previousKey = null;
            	for (Object entryKey : cache.allKeys()) {
            		EORecord<T> entryValue = cache.objectForKey(entryKey);
            		if (entryValue != null && entryValue.gid.equals(gid)) {
            			previousKey = entryKey;
            			break;
            		}
            	}
        	}
        	if (previousKey != null) {
        		if (!previousKey.equals(key)) {
	        		removeObjectForKey(eo, previousKey);
	            	addObjectForKey(eo, key);
	        	}
	        	else if (qualifier() != null && !qualifier().evaluateWithObject(eo)) {
	        		removeObjectForKey(eo, previousKey);
	        	}
	        	else {
	            	// leave it alone, the key value has not changed and EOF will take care of the rest
	        	}
        	}
        	else {
        		addObjectForKey(eo, key);
        	}
        }
    }
    
    /**
     * Retrieves an EO that matches the given key. If there is no match in the
     * cache, it attempts to fetch the missing objects. Null is returned if no matching
     * object can be fetched. 
     * @param ec editing context to get the object into
     * @param key key value under which the object is registered 
     * @return the matching object
     */
    public T objectForKey(EOEditingContext ec, Object key) {
    	return objectForKey(ec, key, true);
    }
    
    /**
     * Retrieves an EO that matches the given key. If there is no match in the
     * cache, and <code>_returnUnsavedObjects</code> is <code>true</code>,
     * it attempts to find and return an unsaved object. If there is still no match
     * and <code>handleUnsuccessfulQueryForKey</code> is <code>true</code>,
     * it attempts to fetch the missing objects. Null is returned if 
     * <code>handleUnsuccessfulQueryForKey</code> is <code>false</code> or no matching
     * object can be fetched. 
     * @param ec editing context to get the object into
     * @param key key value under which the object is registered 
     * @param handleUnsuccessfulQueryForKey if false, a cache miss returns null rather than fetching
     * @return the matching object
     */
    public T objectForKey(EOEditingContext ec, Object key, boolean handleUnsuccessfulQueryForKey) {
        ERXExpiringCache<Object, EORecord<T>> cache = cache();
        EORecord<T> record = cache.objectForKey(key);
        if (record == null) {
        	if (handleUnsuccessfulQueryForKey) {
            	if (_returnUnsavedObjects) {
            		T unsavedMatchingObject = unsavedMatchingObject(ec, key);
            		if (unsavedMatchingObject != null) {
            			return unsavedMatchingObject;
            		}
            	}	            
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
     * Looks in ec for an newly inserted (unsaved) EO that matches the given key. ONLY ec is examined.
	 * Null is returned if no matching
     * 
     * @param ec editing context to search for unsaved, matching objects
     * @param key key value to identify the unsaved object
     * @return the matching object or null if not found
     */
    public T unsavedMatchingObject(EOEditingContext ec, Object key) {
    	NSArray matchingObjects = EOQualifier.filteredArrayWithQualifier(ec.insertedObjects(), ERXQ.equals("entityName", _entityName));
    	matchingObjects = EOQualifier.filteredArrayWithQualifier(matchingObjects, fetchObjectsQualifier(key));

    	if (matchingObjects.count() > 1) {
			throw new EOUtilities.MoreThanOneException("There was more than one " + _entityName + " with the key '" + key + "'.");
    	}
    	return matchingObjects.count() == 1 ? (T)matchingObjects.lastObject() : null;
    }
    	
    /**
     * Returns a list of all the objects currently in the cache and not yet expired.
     * @param ec editing context to get the objects into
     * @return all objects currently in the cache and unexpired
     */
    public NSArray<T> allObjects(EOEditingContext ec) {
    	return allObjects(ec, null);
    }
    
    /**
     * Returns a list of all the objects currently in the cache and not yet expired which match additionalQualifier.
     * @param ec editing context to get the objects into
     * @param additionalQualifier qualifier to restrict which objects are returned from the cache
     * @return all objects currently in the cache and unexpired
     */
    public NSArray<T> allObjects(EOEditingContext ec, EOQualifier additionalQualifier) {
		additionalQualifier = ERXEOControlUtilities.localInstancesInQualifier(ec, additionalQualifier);
    	ERXExpiringCache<Object, EORecord<T>> cache = cache();
    	NSArray allKeys = cache.allKeys();
    	NSMutableArray<T> allObjects = new NSMutableArray<>(allKeys.count());

    	for (Object entryKey : allKeys) {
    		T object = objectForKey(ec, entryKey, false);
    		if (object != null && (additionalQualifier == null || additionalQualifier.evaluateWithObject(object))) {
        		allObjects.addObject(object);
    		}
    	}
    	return allObjects;
    }
    
    /**
     * Called when a query hasn't found an entry in the cache. This
     * implementation puts a not-found marker in the cache so
     * the next query will return null. If <code>_fetchInitialValues</code> 
     * is <code>false</code>, it will attempt to fetch the missing object and 
     * adds it to the cache if it is found.
     * call {@link #addObject(EOEnterpriseObject)} on it.
     * @param key the key of the object that was not found in the cache
     */
    protected void handleUnsuccessfullQueryForKey(Object key) {
    	if (!_fetchInitialValues) {
    		ERXExpiringCache cache = cache();
            // The other methods are synchronized on cache and then lock the EC. If we do it backwards, 
            // we can get a deadly embrace.
    		synchronized (cache) {
                ERXEC editingContext = editingContext();
            	editingContext.lock();
            	try {
            		NSArray<T> objects = fetchObjectsForKey(editingContext, key);
        			if (objects.count() == 0) {
        				cache.setObjectForKey(createRecord(NO_GID_MARKER, null), key);
        			}
        			else if (objects.count() == 1) {
            			T eo = objects.objectAtIndex(0);
            			addObject(eo);
        			}
        			else {
        				throw new EOUtilities.MoreThanOneException("There was more than one " + _entityName + " with the key '" + key + "'.");
        			}
            	}
            	finally {
            		editingContext.unlock();
            		if (!_reuseEditingContext) {
            			editingContext.dispose();
            		}
            	}
			}
    	}
    	else {
    		cache().setObjectForKey(createRecord(NO_GID_MARKER, null), key);
    	}
    }
    
    /**
     * Actually performs a fetch for the given key. Override this method to implement
     * custom fetch rules.
     * 
     * @param editingContext the editing context to fetch in
     * @param key the key to fetch with
     * @return the fetch objects
     */
    protected NSArray<T> fetchObjectsForKey(EOEditingContext editingContext, Object key) {
		EOQualifier qualifier = fetchObjectsQualifier(key);
		ERXFetchSpecification fetchSpec = new ERXFetchSpecification(_entityName, qualifier, null);
		fetchSpec.setRefreshesRefetchedObjects(true);
		fetchSpec.setIsDeep(true);
		NSArray<T> objects = editingContext.objectsWithFetchSpecification(fetchSpec);
		return objects;
    }
    
    /**
     * Returns the additional qualifier for this cache. 
     * @return the additional qualifier for this cache
     */
    public EOQualifier qualifier() {
    	return _qualifier;
    }
    
    /**
     * Returns the qualifier to use during for fetching: the value for keyPath matches key
     * AND qualifier() (if not null).
     * @param key the key to fetch
     * @return the qualifier to use
     */
    protected EOQualifier fetchObjectsQualifier(Object key) {
		EOQualifier qualifier;
		if (qualifier() == null) {
			qualifier = ERXQ.is(_keyPath, key);
		}
		else {
			qualifier = ERXQ.is(_keyPath, key).and(qualifier());
		}
		return qualifier;
    }

    /**
     * Resets the cache by clearing the internal map. The values are refreshed right away if 
     * <code>_fetchInitialValues</code> is <code>true</code>, otherwise they are re-loaded on demand.
     * 
     * @see #preLoadCacheIfNeeded()
     */
    public synchronized void reset() {
    	if (_cache != null) {
    		_cache.removeAllObjects();
    	    preLoadCacheIfNeeded();
    	}
    }
    
    /**
     * Sets whether or not the initial values should be fetched into
     * this cache or whether it should lazy load. If turned off, resetOnChange
     * will also be turned off.
     * 
     * @param fetchInitialValues if true, the initial values are fetched into the cache
     */
    public void setFetchInitialValues(boolean fetchInitialValues) {
    	_fetchInitialValues = fetchInitialValues;
    	if (!fetchInitialValues) {
    		setResetOnChange(false);
    	}
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
     * Sets whether or not the cached EO's themselves are retained versus just their GID's. If set,
     * this implicitly sets reuseEditingContext to true.
     *   
     * @param retainObjects if true, the EO's are retained
     */
    public void setRetainObjects(boolean retainObjects) {
    	if (retainObjects && ! ERXEC.defaultAutomaticLockUnlock()) {
    		throw new RuntimeException("ERXEnterpriseObjectCache requires automatic locking when objects are retained. " + 
    				"Set er.extensions.ERXEC.defaultAutomaticLockUnlock or " +
    				"er.extensions.ERXEC.safeLocking in your Properties file");
    	}
		_retainObjects = retainObjects;
		setReuseEditingContext(retainObjects);
	}
    
    /**
     * Sets whether or not the cache is cleared when any change occurs. This requires fetching initial values (and will
     * be turned on if you set this)
     * 
     * @param resetOnChange if true, the cache will clear on changes; if false, the cache will update on changes
     */
    public void setResetOnChange(boolean resetOnChange) {
		_resetOnChange = resetOnChange;
		if (_resetOnChange) {
			setFetchInitialValues(true);
		}
	}
    
    private static class EORecord<T> {
    	public EOGlobalID gid;
    	public T eo;
    	
    	public EORecord(@SuppressWarnings("hiding") EOGlobalID gid, @SuppressWarnings("hiding") T eo) {
    		this.gid = gid;
    		this.eo = eo;
    	}
    }
}
