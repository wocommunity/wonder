package er.extensions;

import java.util.Enumeration;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Cache that expires its entries based on time or version changes. Version can
 * be any object that represents the current state of a cached value. When
 * retrieving the value, you can retrieve it with a version key. If the version
 * key used in the retrieval does not match the original version key of the 
 * object in the cache, then the cache will invalidate the value for that key
 * and return null.  An example version key might be the count of an array, if 
 * the count changes, you want to invalidate the cached object.
 * 
 * @author ak
 * @author mschrag
 */
// FIXME: the last entry stays in the cache if it is not requested.
public class ERXExpiringCache<K, V> {
	private static class Entry<V> {
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

		protected V object() {
			return _object;
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
	public void removeAllObjects() {
		_backingDictionary.removeAllObjects();
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
	 * @param object the object to set
	 * @param key the lookup key
	 * @param currentVersionKey the version of the object right now
	 */
	public synchronized void setObjectForKeyWithVersion(V object, K key, Object currentVersionKey) {
		removeStaleEntries();
		long expirationTime;
		if (_expiryTime == ERXExpiringCache.NO_TIMEOUT) {
			expirationTime = ERXExpiringCache.NO_TIMEOUT;
		}
		else {
			expirationTime = System.currentTimeMillis() + _expiryTime;
		}
		Entry<V> entry = new Entry<V>(object, expirationTime, currentVersionKey);
		_backingDictionary.setObjectForKey(entry, key);
	}

	/**
	 * Returns the value of the given key with an unspecified version.
	 * 
	 * @param key the key to lookup with
	 * @return the value in the cache or null
	 */
	public synchronized V objectForKey(K key) {
		return objectForKeyWithVersion(key, ERXExpiringCache.NO_VERSION);
	}

	/**
	 * Returns the value of the given key passing in the current version
	 * of the cache value.  If the version key passed in does not
	 * match the version key in the cache, the cache will invalidate
	 * that key.
	 * 
	 * @param key the key to lookup with
	 * @param currentVersionKey the current version of this key
	 * @return the value in the cache or null
	 */
	public synchronized V objectForKeyWithVersion(K key, Object currentVersionKey) {
		Entry<V> entry = _backingDictionary.objectForKey(key);
		V value = null;
		if (entry != null) {
			if (entry.isStale(System.currentTimeMillis(), currentVersionKey)) {
				_backingDictionary.removeObjectForKey(key);
			}
			else {
				value = entry.object();
			}
		}
		return value;
	}

	/**
	 * Returns whether or not the object for the given key is a stale cache entry.
	 * 
	 * @param key the key to lookup
	 * @return true if the value is stale
	 */
	public synchronized boolean isStale(Object key) {
		return isStaleWithVersion(key, ERXExpiringCache.NO_VERSION);
	}

	/**
	 * Returns whether or not the object for the given key is a stale cache entry
	 * given the context of the current version of the key.
	 * 
	 * @param key the key to lookup
	 * @param currentVersionKey the current version of this key
	 * @return true if the value is stale
	 */
	public synchronized boolean isStaleWithVersion(Object key, Object currentVersionKey) {
		Entry<V> entry = _backingDictionary.objectForKey(key);
		boolean isStale = false;
		if (entry != null) {
			isStale = entry.isStale(System.currentTimeMillis(), currentVersionKey);
		}
		return isStale;
	}

	/**
	 * Removes the object for the given key.
	 * 
	 * @param key the key to remove
	 * @return the removed object
	 */
	public synchronized V removeObjectForKey(K key) {
		removeStaleEntries();
		Entry<V> entry = _backingDictionary.removeObjectForKey(key);
		V value = null;
		if (entry != null) {
			value = entry.object();
		}
		return value;
	}

	/**
	 * Removes all stale entries.
	 */
	private void removeStaleEntries() {
		long now = System.currentTimeMillis();
		if ((_lastCleanupTime + _cleanupPause) < now) {
			_lastCleanupTime = System.currentTimeMillis();
			for (Enumeration<K> keyEnum = _backingDictionary.keyEnumerator(); keyEnum.hasMoreElements();) {
				K key = keyEnum.nextElement();
				Entry<V> entry = _backingDictionary.objectForKey(key);
				// ak: add 10 seconds as a safety margin
				// we need this because the entry could be requested
				// when we just checked and noticed it is ok
				if (entry.isStale(now + 10L * 1000, ERXExpiringCache.NO_VERSION)) {
					_backingDictionary.removeObjectForKey(key);
				}
			}
		}
	}
}
