package er.extensions;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Cache that expires its entries, 60 seconds is the default.
 * @author ak
 *
 */
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
		
		private Object object(long expiryTime) {
			if(isStale(expiryTime)) {
				_object = null;
			}
			return _object;
		}
	}

	private long _expiryTime;
	
	public ERXExpiringCache() {
		this(60 * 100L);
	}
	
	public ERXExpiringCache(long expiryTime) {
		_expiryTime = expiryTime;
	}
	
	private long expiryTime() {
		return _expiryTime;
	}

	public void setObjectForKey(Object object, Object key) {
		Entry entry = new Entry(object);
		super.setObjectForKey(entry, key);
	}
	
	public Object objectForKey(Object key) {
		Entry entry = (Entry) super.objectForKey(key);
		Object o = entry.object(expiryTime());
		if(o == null) {
			removeObjectForKey(key);
		}
		return o;
	}
}
