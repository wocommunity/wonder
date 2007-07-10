package com.webobjects.foundation;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;

/**
 * NSMutableDictionary reimplementation to support JDK 1.5 templates and the
 * proper collection methods. Use with
 * 
 * <pre>
 * NSMutableDictionary&lt;String, String&gt; env = new NSMutableDictionary&lt;String, String&gt;(System.getenv(), true);
 * 
 * for (String key : env)
 * 	logger.debug(env.valueForKey(key));
 * </pre>
 * 
 * @param &lt;K&gt;
 *            type of key contents
 * @param &lt;V&gt;
 *            type of value contents
 */
@SuppressWarnings("unchecked")
public class NSMutableDictionary<K, V> extends NSDictionary<K, V> {

	public NSMutableDictionary() {
	}

	public NSMutableDictionary(int capacity) {
		this();
		if (capacity < 0) {
			throw new IllegalArgumentException(getClass().getName() + ": Capacity cannot be less than 0");
		}
		else {
			_ensureCapacity(capacity);
			return;
		}
	}

	public NSMutableDictionary(V object, K key) {
		super(object, key);
	}

	public NSMutableDictionary(V objects[], K keys[]) {
		super(objects, keys);
	}

	public NSMutableDictionary(NSArray<V> objects, NSArray<K> keys) {
		super(objects, keys);
	}

	public NSMutableDictionary(NSDictionary<K, V> otherDictionary) {
		_copyMutableDictionary(otherDictionary);
	}

	public NSMutableDictionary(Dictionary<K, V> dictionary, boolean ignoreNull) {
		super(dictionary, ignoreNull);
	}

	public void setObjectForKey(V object, K key) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null object into an " + getClass().getName() + ".");
		}
		if (key == null) {
			throw new IllegalArgumentException("Attempt to insert null key into an " + getClass().getName() + ".");
		}
		int capacity = _capacity;
		int count = _count;
		if (++count > capacity) {
			_ensureCapacity(count);
		}
		if (_NSCollectionPrimitives.addValueInHashTable(key, object, _keys, _objects, _flags)) {
			_count = count;
			_keysCache = null;
		}
		_objectsCache = null;
	}

	public V removeObjectForKey(K key) {
		Object result = null;
		if (key == null) {
			throw new IllegalArgumentException("Attempt to remove null key from an " + getClass().getName() + ".");
		}
		if (_count != 0) {
			result = _NSCollectionPrimitives.removeValueInHashTable(key, _keys, _objects, _flags);
			if (result != null) {
				_count--;
				_deletionLimit--;
				if (_count == 0 || _deletionLimit == 0) {
					_clearDeletionsAndCollisions();
				}
				_objectsCache = null;
				_keysCache = null;
			}
		}
		return (V) result;
	}

	public void removeAllObjects() {
		if (_count != 0) {
			_objects = new Object[_hashtableBuckets];
			_keys = new Object[_hashtableBuckets];
			_flags = new byte[_hashtableBuckets];
			_count = 0;
			_objectsCache = null;
			_keysCache = null;
			_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(_hashtableBuckets);
		}
	}

	public void setDictionary(NSDictionary<K, V> otherDictionary) {
		if (otherDictionary != this) {
			removeAllObjects();
			if (otherDictionary != null) {
				addEntriesFromDictionary(otherDictionary);
			}
		}
	}

	public void addEntriesFromDictionary(NSDictionary<K, V> otherDictionary) {
		if (otherDictionary != null) {
			Object keys[] = otherDictionary.keysNoCopy();
			for (int i = 0; i < keys.length; i++) {
				setObjectForKey(otherDictionary.objectForKey((K) keys[i]), (K) keys[i]);
			}

		}
	}

	public void removeObjectsForKeys(NSArray<K> keys) {
		if (keys != null) {
			Object keysArray[] = keys.objectsNoCopy();
			for (int i = 0; i < keysArray.length; i++) {
				removeObjectForKey((K) keysArray[i]);
			}

		}
	}

	public void takeValueForKey(Object value, String key) {
		if (value != null) {
			setObjectForKey((V) value, (K) key);
		}
		else {
			removeObjectForKey((K) key);
		}
	}

	public Object clone() {
		return new NSMutableDictionary<K, V>(this);
	}

	public NSDictionary<K, V> immutableClone() {
		return new NSDictionary<K, V>(this);
	}

	public NSMutableDictionary<K, V> mutableClone() {
		return (NSMutableDictionary<K, V>) clone();
	}

	public static final Class _CLASS = _NSUtilitiesExtra._classWithFullySpecifiedNamePrime("com.webobjects.foundation.NSMutableDictionary");
	static final long serialVersionUID = 0x1d6146f8L;

	/**
	 * Associate the <tt>value</tt> with <tt>key</tt> in this map. If the
	 * map previously contained a mapping for <tt>key</tt>, the old value
	 * will be replaced by <tt>value</tt>.
	 * 
	 * @param key
	 *            key to associate value with
	 * @param value
	 *            vaue to be associated with key
	 * 
	 * @return previous value associated with specified key, or null if there
	 *         was no mapping for key.
	 */
	public V put(K key, V value) {
		V temp = objectForKey(key);
		setObjectForKey(value, key);

		return temp;
	}

	/**
	 * Remove mapping for <tt>key</tt> from this map if it is present.
	 * 
	 * @param key
	 *            key whose mapping is to be removed from the map.
	 * 
	 * @return previous value associated with key, or null if there was no
	 *         mapping for key.
	 */
	public V remove(Object key) {
		V temp = objectForKey((K) key);
		removeObjectForKey((K) key);

		return temp;
	}

	/**
	 * Copy all mappings from <tt>m</tt> to this map.
	 * 
	 * @param m
	 *            Mappings to be stored in this map.
	 */
	public void putAll(Map m) {
		addEntriesFromDictionary(new NSDictionary(m, true));
	}

	/**
	 * Remove all mappings from this map.
	 */
	public void clear() {
		removeAllObjects();
	}

	/**
	 * Return a collection view of the values contained in this map.
	 */
	public Collection values() {
		return allValues();
	}

}
