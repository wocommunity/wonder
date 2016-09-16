package er.extensions.foundation;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Cache that expires its entries based on time or version changes. Version can
 * be any object that represents the current state of a cached value. When
 * retrieving the value, you can retrieve it with a version key. If the version
 * key used in the retrieval does not match the original version key of the
 * object in the cache, then the cache will invalidate the value for that key
 * and return null. An example version key might be the count of an array, if
 * the count changes, you want to invalidate the cached object.
 * 
 * Note that on a time-expiring cache, if you do not use the reaper with
 * startBackgroundExpiration(), or manually call removeStaleEntries(), unexpired
 * entries will remain in the cache for the lifetime of the cache.
 * 
 * @author ak
 * @author mschrag
 */
public class ERXExpiringCache<K, V> {
	public static class Entry<V> {
		private long _expiration;
		private Object _versionKey;
		private V _object;
		private boolean _stale;

		public Entry(V o, long expiration, Object version) {
			_expiration = expiration;
			_versionKey = version;
			_object = o;
		}

		protected boolean isStale(long currentTime, Object currentVersionKey) {
			if (!_stale) {
				if (_expiration != ERXExpiringCache.NO_TIMEOUT && currentTime != ERXExpiringCache.NO_TIMEOUT && _expiration < currentTime) {
					_stale = true;
				}
				else if (_versionKey == ERXExpiringCache.NO_VERSION || currentVersionKey == ERXExpiringCache.NO_VERSION) {
					_stale = false;
				}
				else if (_object == null) {
					_stale = currentVersionKey != null;
				}
				else {
					_stale = !_versionKey.equals(currentVersionKey);
				}
			}
			return _stale;
		}

		public V object() {
			return _object;
		}

		@Override
		public String toString() {
			return super.toString() + " { " + "expiration = " + (_expiration == ERXExpiringCache.NO_TIMEOUT ? "NO_TIMEOUT" : new java.util.Date(_expiration)) + ", version = " + (_versionKey == ERXExpiringCache.NO_VERSION ? "NO_VERSION" : _versionKey) + ", object = " + _object + " }";
		}
	}

	/**
	 * Designates that no timeout was specified.
	 */
	public static final long NO_TIMEOUT = 0L;

	/**
	 * Designates that no explicit version was specified.
	 */
	public static final Object NO_VERSION = new Object();

	/**
	 * The reaper for ERXExpiringCaches.
	 */
	private static ERXExpiringCache.GrimReaper _reaper;

	private NSMutableDictionary<K, ERXExpiringCache.Entry<V>> _backingDictionary;
	private long _expiryTime;
	private long _cleanupPause;
	private long _lastCleanupTime;

	/**
	 * Constructs an ERXExpiringCache with a 60 second expiration.
	 */
	public ERXExpiringCache() {
		this(60);
	}

	/**
	 * Constructs an ERXExpiringCache with a cleanup time that matches
	 * expiryTimeInSeconds.
	 * 
	 * @param expiryTimeInSeconds
	 *            the lifetime in seconds of an object in the cache or
	 *            NO_TIMEOUT
	 */
	public ERXExpiringCache(long expiryTimeInSeconds) {
		this(expiryTimeInSeconds, expiryTimeInSeconds);
	}

	/**
	 * @param expiryTimeInSeconds
	 *            the lifetime in seconds of an object in the cache or
	 *            NO_TIMEOUT
	 * @param cleanupPauseInSeconds
	 *            the number of seconds to pause between cleanups
	 */
	public ERXExpiringCache(long expiryTimeInSeconds, long cleanupPauseInSeconds) {
		_expiryTime = expiryTimeInSeconds * 1000L;
		_cleanupPause = cleanupPauseInSeconds * 1000L;
		if (_cleanupPause == 0) {
			_cleanupPause = 60 * 1000L;
		}
		_lastCleanupTime = 0L;
		_backingDictionary = new NSMutableDictionary<K, Entry<V>>();
	}

	/**
	 * Removes all the objects in this cache.
	 */
	public synchronized void removeAllObjects() {
		for (Iterator<K> iterator = _backingDictionary.allKeys().iterator(); iterator.hasNext();) {
			K key = iterator.next();
			removeEntryForKey(entryForKey(key), key);
		}
	}

	private long expiryTime() {
		return _expiryTime;
	}

	/**
	 * Sets the object for the specified key in this cache with no version
	 * specified.
	 * 
	 * @param object
	 *            the value to set
	 * @param key
	 *            the lookup key
	 */
	public synchronized void setObjectForKey(V object, K key) {
		setObjectForKeyWithVersion(object, key, ERXExpiringCache.NO_VERSION);
	}

	/**
	 * Sets the object for the specified key and current version key.
	 * 
	 * @param object
	 *            the object to set
	 * @param key
	 *            the lookup key
	 * @param currentVersionKey
	 *            the version of the object right now
	 */
	public synchronized void setObjectForKeyWithVersion(V object, K key, Object currentVersionKey, long expirationTime) {
		removeStaleEntries();
		if (expirationTime != ERXExpiringCache.NO_TIMEOUT) {
			expirationTime = System.currentTimeMillis() + expirationTime;
		}
		Entry<V> entry = new Entry<>(object, expirationTime, currentVersionKey);
		setEntryForKey(entry, key);
	}

	/**
	 * Sets the object for the specified key and current version key.
	 * 
	 * @param object
	 *            the object to set
	 * @param key
	 *            the lookup key
	 * @param currentVersionKey
	 *            the version of the object right now
	 */
	public synchronized void setObjectForKeyWithVersion(V object, K key, Object currentVersionKey) {
		setObjectForKeyWithVersion(object, key, currentVersionKey, _expiryTime);
	}

	/**
	 * Returns the value of the given key with an unspecified version.
	 * 
	 * @param key
	 *            the key to lookup with
	 * @return the value in the cache or null
	 */
	public synchronized V objectForKey(K key) {
		return objectForKeyWithVersion(key, ERXExpiringCache.NO_VERSION);
	}

	/**
	 * Returns the value of the given key passing in the current version of the
	 * cache value. If the version key passed in does not match the version key
	 * in the cache, the cache will invalidate that key.
	 * 
	 * @param key
	 *            the key to lookup with
	 * @param currentVersionKey
	 *            the current version of this key
	 * @return the value in the cache or null
	 */
	public synchronized V objectForKeyWithVersion(K key, Object currentVersionKey) {
		Entry<V> entry = entryForKey(key);
		V value = null;
		if (entry != null) {
			if (entry.isStale(System.currentTimeMillis(), currentVersionKey)) {
				removeEntryForKey(entry, key);
			}
			else {
				value = entry.object();
			}
		}
		return value;
	}

	/**
	 * Returns whether or not the object for the given key is a stale cache
	 * entry.
	 * 
	 * @param key
	 *            the key to lookup
	 * @return true if the value is stale
	 */
	public synchronized boolean isStale(K key) {
		return isStaleWithVersion(key, ERXExpiringCache.NO_VERSION);
	}

	/**
	 * Returns whether or not the object for the given key is a stale cache
	 * entry given the context of the current version of the key.
	 * 
	 * @param key
	 *            the key to lookup
	 * @param currentVersionKey
	 *            the current version of this key
	 * @return true if the value is stale
	 */
	public synchronized boolean isStaleWithVersion(K key, Object currentVersionKey) {
		Entry<V> entry = entryForKey(key);
		boolean isStale = true;
		if (entry != null) {
			isStale = entry.isStale(System.currentTimeMillis(), currentVersionKey);
		}
		return isStale;
	}

	/**
	 * Removes the object for the given key.
	 * 
	 * @param key
	 *            the key to remove
	 * @return the removed object
	 */
	public synchronized V removeObjectForKey(K key) {
		removeStaleEntries();
		Entry<V> entry = entryForKey(key);
		V value = null;
		if (entry != null) {
			removeEntryForKey(entry, key);
			value = entry.object();
		}
		return value;
	}

	/**
	 * Removes all stale entries.
	 */
	public synchronized void removeStaleEntries() {
		if (_backingDictionary.count() > 0) {
			long now = System.currentTimeMillis();
			if ((_lastCleanupTime + _cleanupPause) < now) {
				_lastCleanupTime = System.currentTimeMillis();
				for (Enumeration<K> keyEnum = _backingDictionary.keyEnumerator(); keyEnum.hasMoreElements();) {
					K key = keyEnum.nextElement();
					Entry<V> entry = entryForKey(key);
					// (AR): It's wrong to add 10 seconds, subtracting 10 makes objects
					// live longer but this really isn't necessary. It appears
					// no "fudge factor" is needed.
					if (entry.isStale(now, ERXExpiringCache.NO_VERSION)) {
						removeEntryForKey(entry, key);
					}
				}
			}
		}
	}
	
	protected synchronized void removeEntryForKey(Entry<V> entry, K key) {
		_backingDictionary.removeObjectForKey(key);
	}
	
	protected synchronized void setEntryForKey(Entry<V> entry, K key) {
		_backingDictionary.setObjectForKey(entry, key);
	}

	protected synchronized Entry<V> entryForKey(K key) {
		return _backingDictionary.objectForKey( key);
	}
	@Override
	public String toString() {
		return super.toString() + " " + _backingDictionary;
	}

	/**
	 * Adds this cache to the background thread that reaps time-expired entries
	 * from expiring caches. If this cache is not a time-expiration cache, this
	 * will throw an IllegalArgumentException.
	 */
	public void startBackgroundExpiration() {
		if (_expiryTime == ERXExpiringCache.NO_TIMEOUT) {
			throw new IllegalArgumentException("This ERXExpiringCache does not have an expiration time.");
		}
		ERXExpiringCache.reaper().addCache(this);
	}

	/**
	 * Stops the background reaper for this cache.
	 */
	public synchronized void stopBackgroundExpiration() {
		ERXExpiringCache.reaper().stop(this);
	}

	/**
	 * Returns the repear for all ERXExpringCaches.
	 * 
	 * @return the repear for all ERXExpringCaches
	 */
	protected static synchronized ERXExpiringCache.GrimReaper reaper() {
		if (_reaper == null) {
			_reaper = new GrimReaper(ERXProperties.intForKeyWithDefault("er.extensions.ERXExpiringCache.reaperFrequency", 5000));
		}
		return ERXExpiringCache._reaper;
	}

	/**
	 * The reaper runnable for ERXExpiringCache.
	 * 
	 * @author mschrag
	 */
	protected static class GrimReaper implements Runnable {
		private List<WeakReference<ERXExpiringCache>> _caches;
		private long _reapFrequencyInMillis;
		private boolean _stopped;

		public GrimReaper(long reapFrequencyInMillis) {
			_caches = new LinkedList<WeakReference<ERXExpiringCache>>();
			_reapFrequencyInMillis = reapFrequencyInMillis;
			_stopped = true;
		}

		public void addCache(ERXExpiringCache cache) {
			synchronized (_caches) {
				_caches.add(new WeakReference<>(cache));
				if (_stopped) {
					start();
				}
			}

		}

		public void start() {
			synchronized (_caches) {
				if (_stopped) {
					_stopped = false;
					Thread reaperThread = new Thread(this);
					reaperThread.start();
				}
			}
		}

		public void stop() {
			synchronized (_caches) {
				_stopped = true;
			}
		}

		public void stop(ERXExpiringCache cache) {
			synchronized (_caches) {
				Iterator<WeakReference<ERXExpiringCache>> cacheIter = _caches.iterator();
				while (cacheIter.hasNext()) {
					WeakReference<ERXExpiringCache> cacheRef = cacheIter.next();
					ERXExpiringCache reapingCache = cacheRef.get();
					if (reapingCache == cache) {
						cacheIter.remove();
						break;
					}
				}
			}
		}

		public void run() {
			boolean stopped = false;
			do {
				try {
					Thread.sleep(_reapFrequencyInMillis);
				}
				catch (InterruptedException e) {
					// IGNORE
				}
				synchronized (_caches) {
					Iterator<WeakReference<ERXExpiringCache>> cacheIter = _caches.iterator();
					while (cacheIter.hasNext()) {
						WeakReference<ERXExpiringCache> cacheRef = cacheIter.next();
						ERXExpiringCache cache = cacheRef.get();
						if (cache == null) {
							cacheIter.remove();
						}
						else {
							cache.removeStaleEntries();
						}
					}
					if (_caches.size() == 0) {
						_stopped = true;
						stopped = true;
					}
				}
			}
			while (!stopped);
		}
	}

	/**
	 * Returns all keys.
	 */
	public synchronized NSArray<K> allKeys() {
		NSMutableArray<K> result = new NSMutableArray<>(_backingDictionary.count());
		for (K key : _backingDictionary.allKeys()) {
			result.addObject(key);
		}
		return result;
	}
}
