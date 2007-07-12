package er.extensions;

import java.util.Enumeration;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Cache that expires its entries, 60 seconds is the default.
 * @author ak
 *
 */
//FIXME: the last entry stays in the cache if it is not requested.
public class ERXExpiringCache extends NSMutableDictionary {
	
	private static class Entry {
		private long _timestamp;
		private Object _object;

		public Entry(Object o) {
			_timestamp = System.currentTimeMillis();
			_object = o;
		}
		
		private boolean isStale(long expiryTime) {
			return _timestamp + expiryTime < System.currentTimeMillis();
		}
		
		private Object object() {
			return _object;
		}
	}

	private long _expiryTime;
	private long _lastPrune;
	
	public ERXExpiringCache() {
		this(60);
	}
	
	/**
	 * Time in seconds when an entry expires.
	 */
	public ERXExpiringCache(long expiryTime) {
		_expiryTime = expiryTime * 1000L;
		_lastPrune = 0L;
	}
	
	private long expiryTime() {
		return _expiryTime;
	}

	public synchronized void setObjectForKey(Object object, Object key) {
		removeStaleEntries();
		Entry entry = new Entry(object);
		super.setObjectForKey(entry, key);
	}

	public synchronized Object objectForKey(Object key) {
		Entry entry = (Entry) super.objectForKey(key);
		if(entry != null) {
			return entry.object();
		}
		return null;
	}

	public synchronized boolean isStale(Object key) {
		Entry entry = (Entry) super.objectForKey(key);
		if(entry != null) {
			return entry.isStale(expiryTime());
		}
		return true;
	}
	
	private void removeStaleEntries() {
		long current = System.currentTimeMillis();
		if((_lastPrune + expiryTime()) < current) {
			_lastPrune = System.currentTimeMillis();
			for (Enumeration iter = keyEnumerator(); iter.hasMoreElements();) {
				Object key = (Object) iter.nextElement();
				Entry entry  = (Entry) super.objectForKey(key);
				// ak: add 10 seconds as a safety margin
				// we need this because the entry could be requested
				// when we just checked and noticed it is ok
				if( entry.isStale(expiryTime() + 10L * 1000)) {
					super.removeObjectForKey(key);
				}
			}
		}
	}

	public synchronized Object removeObjectForKey(Object key) {
		removeStaleEntries();
		return super.removeObjectForKey(key);
	}
}
