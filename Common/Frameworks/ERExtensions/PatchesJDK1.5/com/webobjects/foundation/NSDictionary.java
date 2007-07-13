package com.webobjects.foundation;

import java.io.*;
import java.util.*;

/**
 * NSDictionary reimplementation to support JDK 1.5 templates. Use with
 * 
 * <pre>
 * NSDictionary&lt;String, String&gt; env = new NSDictionary&lt;String, String&gt;(System.getenv(), true);
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
public class NSDictionary<K, V> implements Cloneable, Serializable, NSCoding, NSKeyValueCoding, NSKeyValueCodingAdditions, _NSFoundationCollection, Map<K, V> {
	public class _JavaNSDictionaryMapEntry<P, Q> implements java.util.Map.Entry<P, Q> {

		public P getKey() {
			return _entryKey;
		}

		public Q getValue() {
			return _entryValue;
		}

		public Q setValue(Q value) {
			throw new UnsupportedOperationException("setValue is not a supported operation in _JavaNSDictionaryMapEntry");
		}

		public boolean equals(Object o) {
			return _entryKey == null && ((java.util.Map.Entry) o).getKey() == null && getKey().equals(((java.util.Map.Entry) o).getKey()) && getValue().equals(((java.util.Map.Entry) o).getValue());
		}

		public int hashCode() {
			return NSDictionary._NSDictionaryMapEntryHashCode;
		}

		Q _entryValue;
		P _entryKey;

		public _JavaNSDictionaryMapEntry(P key, Q value) {
			super();
			_entryKey = key;
			_entryValue = value;
		}
	}

	private void _copyImmutableDictionary(NSDictionary<K, V> otherDictionary) {
		_capacity = otherDictionary._capacity;
		_count = otherDictionary._count;
		_hashtableBuckets = otherDictionary._hashtableBuckets;
		_hashCache = otherDictionary._hashCache;
		_objects = otherDictionary._objects;
		_objectsCache = otherDictionary._objectsCache;
		_flags = otherDictionary._flags;
		_keys = otherDictionary._keys;
		_keysCache = otherDictionary._keysCache;
		_deletionLimit = otherDictionary._deletionLimit;
	}

	void _copyMutableDictionary(NSDictionary<K, V> otherDictionary) {
		_capacity = otherDictionary._capacity;
		_count = otherDictionary._count;
		_hashtableBuckets = otherDictionary._hashtableBuckets;
		_hashCache = otherDictionary._hashCache;
		_objects = _NSCollectionPrimitives.copyArray(otherDictionary._objects);
		_objectsCache = null;
		_flags = _NSCollectionPrimitives.copyArray(otherDictionary._flags);
		_keys = _NSCollectionPrimitives.copyArray(otherDictionary._keys);
		_keysCache = null;
		_deletionLimit = otherDictionary._deletionLimit;
	}

	protected void _initializeDictionary() {
		_capacity = _count = 0;
		_objects = _objectsCache = null;
		_flags = null;
		_keys = _keysCache = null;
		_hashtableBuckets = _NSCollectionPrimitives.hashTableBucketsForCapacity(_capacity);
		_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(_hashtableBuckets);
		_keySetCache = null;
		_entrySetCache = null;
	}

	protected void _ensureCapacity(int capacity) {
		int currentCapacity = _capacity;
		if (capacity > currentCapacity) {
			int newCapacity = _NSCollectionPrimitives.hashTableCapacityForCapacity(capacity);
			if (newCapacity != currentCapacity) {
				int oldSize = _hashtableBuckets;
				int newSize = _NSCollectionPrimitives.hashTableBucketsForCapacity(newCapacity);
				_hashtableBuckets = newSize;
				if (newSize == 0) {
					_objects = null;
					_keys = null;
					_flags = null;
				}
				else {
					Object oldObjects[] = _objects;
					Object oldKeys[] = _keys;
					byte oldFlags[] = _flags;
					Object newObjects[] = new Object[newSize];
					Object newKeys[] = new Object[newSize];
					byte newFlags[] = new byte[newSize];
					for (int i = 0; i < oldSize; i++) {
						if ((oldFlags[i] & 0xffffffc0) == -128) {
							_NSCollectionPrimitives.addValueInHashTable(oldKeys[i], oldObjects[i], newKeys, newObjects, newFlags);
						}
					}

					_objects = newObjects;
					_keys = newKeys;
					_flags = newFlags;
				}
				_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(newSize);
				_capacity = newCapacity;
			}
		}
	}

	protected void _clearDeletionsAndCollisions() {
		int size = _hashtableBuckets;
		if (_count == 0) {
			_flags = new byte[size];
		}
		else {
			Object oldObjects[] = _objects;
			Object oldKeys[] = _keys;
			byte oldFlags[] = _flags;
			Object newObjects[] = new Object[size];
			Object newKeys[] = new Object[size];
			byte newFlags[] = new byte[size];
			for (int i = 0; i < size; i++) {
				if ((oldFlags[i] & 0xffffffc0) == -128) {
					_NSCollectionPrimitives.addValueInHashTable(oldKeys[i], oldObjects[i], newKeys, newObjects, newFlags);
				}
			}

		}
		_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(size);
	}

	public NSDictionary() {
		_initializeDictionary();
	}

	public NSDictionary(V object, K key) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null object into an  " + getClass().getName() + ".");
		}
		if (key == null) {
			throw new IllegalArgumentException("Attempt to insert null key into an  " + getClass().getName() + ".");
		}
		_initializeDictionary();
		_ensureCapacity(1);
		if (_NSCollectionPrimitives.addValueInHashTable(key, object, _keys, _objects, _flags)) {
			_count++;
		}
	}

	private void initFromKeyValues(Object objects[], Object keys[], boolean checkForNull) {
		if (objects != null && keys != null) {
			if (objects.length != keys.length) {
				throw new IllegalArgumentException("Attempt to create an " + getClass().getName() + " with a different number of objects and keys.");
			}
			if (checkForNull) {
				for (int i = 0; i < objects.length; i++) {
					if (objects[i] == null) {
						throw new IllegalArgumentException("Attempt to insert null object into an  " + getClass().getName() + ".");
					}
					if (keys[i] == null) {
						throw new IllegalArgumentException("Attempt to insert null key into an  " + getClass().getName() + ".");
					}
				}

			}
			_initializeDictionary();
			_ensureCapacity(objects.length);
			for (int i = 0; i < objects.length; i++) {
				if (_NSCollectionPrimitives.addValueInHashTable(keys[i], objects[i], _keys, _objects, _flags)) {
					_count++;
				}
			}

		}
		else if (objects == null && keys == null) {
			_initializeDictionary();
		}
		else {
			throw new IllegalArgumentException("Both objects and keys cannot be null");
		}
	}

	private NSDictionary(V objects[], K keys[], boolean checkForNull) {
		initFromKeyValues(objects, keys, checkForNull);
	}

	public NSDictionary(V objects[], K keys[]) {
		this(objects, keys, true);
	}

	public NSDictionary(NSArray<V> objects, NSArray<K> keys) {
		this(objects == null ? null : objects.objectsNoCopy(), keys == null ? null : keys.objectsNoCopy(), false);
	}

	public NSDictionary(NSDictionary<K, V> otherDictionary) {
		if (otherDictionary.getClass() == _CLASS) {
			_copyImmutableDictionary(otherDictionary);
		}
		else {
			_copyMutableDictionary(otherDictionary);
		}
	}

	public NSDictionary(Map<K, V> map, boolean ignoreNull) {
		_initializeDictionary();
		if (map != null) {
			_ensureCapacity(map.size());
			Set keySet = map.keySet();
			Iterator it = keySet.iterator();
			do {
				if (!it.hasNext()) {
					break;
				}
				Object key = it.next();
				Object object = map.get(key);
				if (key == null) {
					if (!ignoreNull) {
						throw new IllegalArgumentException("Attempt to insert null key into an  " + getClass().getName() + ".");
					}
				}
				else if (object == null) {
					if (!ignoreNull) {
						throw new IllegalArgumentException("Attempt to insert null value into an  " + getClass().getName() + ".");
					}
				}
				else if (_NSCollectionPrimitives.addValueInHashTable(key, object, _keys, _objects, _flags)) {
					_count++;
				}
			}
			while (true);
		}
	}

	public NSDictionary(Dictionary<K, V> dictionary, boolean ignoreNull) {
		_initializeDictionary();
		if (dictionary != null) {
			_ensureCapacity(dictionary.size());
			Enumeration enumeration = dictionary.keys();
			do {
				if (!enumeration.hasMoreElements()) {
					break;
				}
				Object key = enumeration.nextElement();
				Object object = dictionary.get(key);
				if (key == null) {
					if (!ignoreNull) {
						throw new IllegalArgumentException("Attempt to insert null key into an  " + getClass().getName() + ".");
					}
				}
				else if (object == null) {
					if (!ignoreNull) {
						throw new IllegalArgumentException("Attempt to insert null value into an  " + getClass().getName() + ".");
					}
				}
				else if (_NSCollectionPrimitives.addValueInHashTable(key, object, _keys, _objects, _flags)) {
					_count++;
				}
			}
			while (true);
		}
	}

	protected K[] keysNoCopy() {
		if (_keysCache == null) {
			_keysCache = _count != 0 ? _NSCollectionPrimitives.keysInHashTable(_keys, _objects, _flags, _capacity, _hashtableBuckets) : _NSCollectionPrimitives.EmptyArray;
		}
		return (K[]) _keysCache;
	}

	protected V[] objectsNoCopy() {
		if (_objectsCache == null) {
			_objectsCache = _count != 0 ? _NSCollectionPrimitives.valuesInHashTable(_keys, _objects, _flags, _capacity, _hashtableBuckets) : _NSCollectionPrimitives.EmptyArray;
		}
		return (V[]) _objectsCache;
	}

	public int count() {
		return _count;
	}

	public V objectForKey(K key) {
		return _count != 0 && key != null ? (V) _NSCollectionPrimitives.findValueInHashTable(key, _keys, _objects, _flags) : null;
	}

	public Hashtable<K, V> hashtable() {
		Object keys[] = keysNoCopy();
		int c = keys.length;
		Hashtable hashtable = new Hashtable(c <= 0 ? 1 : c);
		for (int i = 0; i < c; i++) {
			hashtable.put(keys[i], objectForKey((K) keys[i]));
		}

		return hashtable;
	}

	public HashMap<K, V> hashMap() {
		Object keys[] = keysNoCopy();
		int c = keys.length;
		HashMap map = new HashMap(c <= 0 ? 1 : c);
		for (int i = 0; i < c; i++) {
			map.put(keys[i], objectForKey((K) keys[i]));
		}

		return map;
	}

	public NSArray<K> allKeysForObject(V object) {
		if (object != null) {
			Object keys[] = keysNoCopy();
			NSMutableArray array = new NSMutableArray(keys.length);
			for (int i = 0; i < keys.length; i++) {
				Object compareObject = objectForKey((K) keys[i]);
				if (object == compareObject || object.equals(compareObject)) {
					array.addObject(keys[i]);
				}
			}

			return array;
		}
		else {
			return NSArray.EmptyArray;
		}
	}

	public NSArray<V> objectsForKeys(NSArray<K> keys, V notFoundMarker) {
		if (keys != null) {
			Object keysArray[] = keys.objectsNoCopy();
			NSMutableArray array = new NSMutableArray(keysArray.length);
			for (int i = 0; i < keysArray.length; i++) {
				Object object = objectForKey((K) keysArray[i]);
				if (object != null) {
					array.addObject(object);
					continue;
				}
				if (notFoundMarker != null) {
					array.addObject(notFoundMarker);
				}
			}

			return array;
		}
		else {
			return NSArray.EmptyArray;
		}
	}

	private boolean _equalsDictionary(NSDictionary<K, V> otherDictionary) {
		int count = count();
		if (count != otherDictionary.count()) {
			return false;
		}
		Object keys[] = keysNoCopy();
		for (int i = 0; i < count; i++) {
			Object value = objectForKey((K) keys[i]);
			Object otherValue = otherDictionary.objectForKey((K) keys[i]);
			if (otherValue == null || !value.equals(otherValue)) {
				return false;
			}
		}

		return true;
	}

	public boolean isEqualToDictionary(NSDictionary<K, V> otherDictionary) {
		if (otherDictionary == null) {
			return false;
		}
		if (otherDictionary == this) {
			return true;
		}
		else {
			return _equalsDictionary(otherDictionary);
		}
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof NSDictionary) {
			return _equalsDictionary((NSDictionary) object);
		}
		else {
			return false;
		}
	}

	public NSArray<K> allKeys() {
		return new NSArray(keysNoCopy());
	}

	public NSArray<V> allValues() {
		return new NSArray(objectsNoCopy());
	}

	public Enumeration<K> keyEnumerator() {
		return new _NSCollectionEnumerator(_keys, _flags, _count);
	}

	public Enumeration<V> objectEnumerator() {
		return new _NSCollectionEnumerator(_objects, _flags, _count);
	}

	public Object valueForKey(String key) {
		Object value = objectForKey((K) key);
		if (value == null && key != null) {
			if (key.equals("allValues")) {
				return allValues();
			}
			if (key.equals("allKeys")) {
				return allKeys();
			}
			if (key.equals("count")) {
				return _NSUtilities.IntegerForInt(count());
			}
		}
		return value;
	}

	public void takeValueForKey(Object value, String key) {
		throw new IllegalStateException(getClass().getName() + " is immutable.");
	}

	public Object valueForKeyPath(String keyPath) {
		Object flattenedKeyPresent = objectForKey((K) keyPath);
		if (flattenedKeyPresent != null) {
			return flattenedKeyPresent;
		}
		else {
			return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
		}
	}

	public void takeValueForKeyPath(Object value, String keyPath) {
		NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
	}

	public Class classForCoder() {
		return _CLASS;
	}

	public static Object decodeObject(NSCoder coder) {
		int count = coder.decodeInt();
		Object keys[] = new Object[count];
		Object objects[] = new Object[count];
		for (int i = 0; i < count; i++) {
			keys[i] = coder.decodeObject();
			objects[i] = coder.decodeObject();
		}

		return new NSDictionary(objects, keys);
	}

	public void encodeWithCoder(NSCoder coder) {
		int count = count();
		coder.encodeInt(count);
		if (count > 0) {
			Object keys[] = keysNoCopy();
			for (int i = 0; i < keys.length; i++) {
				coder.encodeObject(keys[i]);
				coder.encodeObject(objectForKey((K) keys[i]));
			}

		}
	}

	public int _shallowHashCode() {
		return _NSDictionaryClassHashCode;
	}

	public int hashCode() {
		return _NSDictionaryClassHashCode ^ count();
	}

	public Object clone() {
		return this;
	}

	public NSDictionary<K, V> immutableClone() {
		return this;
	}

	public NSMutableDictionary<K, V> mutableClone() {
		return new NSMutableDictionary<K, V>(this);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(128);
		buffer.append("{");
		Object keys[] = keysNoCopy();
		for (int i = 0; i < keys.length; i++) {
			Object key = keys[i];
			Object object = objectForKey((K) key);
			buffer.append(key.toString());
			buffer.append(" = ");
			if (object instanceof String) {
				buffer.append('"');
				buffer.append((String) object);
				buffer.append('"');
			}
			else if (object instanceof Boolean) {
				buffer.append(((Boolean) object).booleanValue() ? "true" : "false");
			}
			else {
				buffer.append(object.toString());
			}
			buffer.append("; ");
		}

		buffer.append("}");
		return new String(buffer);
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		Object keys[] = keysNoCopy();
		int c = keys.length;
		Object values[] = new Object[c];
		for (int i = 0; i < c; i++) {
			values[i] = objectForKey((K) keys[i]);
		}

		fields.put("keys", ((keys)));
		fields.put("objects", ((values)));
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();
		Object keys[] = (Object[]) fields.get("keys", ((_NSUtilities._NoObjectArray)));
		Object values[] = (Object[]) fields.get("objects", ((_NSUtilities._NoObjectArray)));
		keys = keys != null ? keys : _NSUtilities._NoObjectArray;
		values = values != null ? values : _NSUtilities._NoObjectArray;
		initFromKeyValues(values, keys, true);
	}

	private Object readResolve() throws ObjectStreamException {
		if (getClass() == _CLASS && count() == 0) {
			return EmptyDictionary;
		}
		else {
			return this;
		}
	}

	public int size() {
		return _count;
	}

	public boolean isEmpty() {
		return _count <= 0;
	}

	public boolean containsKey(Object key) {
		if (key != null) {
			Object keys[] = keysNoCopy();
			for (int i = 0; i < keys.length; i++) {
				if (keys[i].equals(key)) {
					return true;
				}
			}

		}
		return false;
	}

	public boolean containsValue(Object value) {
		if (value != null || _count != 0) {
			Object values[] = _NSCollectionPrimitives.valuesInHashTable(_keys, _objects, _flags, _capacity, _hashtableBuckets);
			for (int i = 0; i < values.length; i++) {
				if (values[i].equals(value)) {
					return true;
				}
			}

		}
		return false;
	}

	public V get(Object key) {
		return objectForKey((K) key);
	}

	public V put(K key, V value) {
		throw new UnsupportedOperationException("put is not a supported operation in com.webobjects.foundation.NSDictionary");
	}

	public V remove(Object key) {
		throw new UnsupportedOperationException("remove is not a supported operation in com.webobjects.foundation.NSDictionary");
	}

	public void putAll(Map t) {
		throw new UnsupportedOperationException("putAll is not a supported operation in com.webobjects.foundation.NSDictionary");
	}

	public void clear() {
		throw new UnsupportedOperationException("putAll is not a supported operation in com.webobjects.foundation.NSDictionary");
	}

	public Set keySet() {
		if (_keySetCache == null) {
			Object currKeys[] = keysNoCopy();
			if (currKeys != null && currKeys.length > 0) {
				_keySetCache = new NSSet(currKeys);
			}
			else {
				_keySetCache = NSSet.EmptySet;
			}
		}
		return _keySetCache;
	}

	public Collection values() {
		return allValues();
	}

	public Set entrySet() {
		if (_entrySetCache == null) {
			return _initMapEntrySet();
		}
		else {
			return _entrySetCache;
		}
	}

	private Set _initMapEntrySet() {
		Object keys[] = keysNoCopy();
		_JavaNSDictionaryMapEntry<K, V> set[] = new _JavaNSDictionaryMapEntry[keys.length];
		for (int i = 0; i < keys.length; i++) {
			Object key = keys[i];
			Object object = valueForKey((String) key);
			_JavaNSDictionaryMapEntry<K, V> current = new _JavaNSDictionaryMapEntry(key, object);
			set[i] = current;
		}

		return new NSSet<_JavaNSDictionaryMapEntry<K, V>>(set);
	}

	public static final Class _CLASS;
	public static final Class _MAP_ENTRY_CLASS;
	public static final NSDictionary EmptyDictionary = new NSDictionary();
	static final long serialVersionUID = 2886170486405617806L;
	private static final Class _objectArrayClass;
	protected transient int _capacity;
	protected transient int _hashtableBuckets;
	protected transient int _count;
	protected Object _objects[];
	protected transient Object _objectsCache[];
	protected transient byte _flags[];
	protected Object _keys[];
	protected transient Object _keysCache[];
	protected transient int _hashCache;
	protected transient int _deletionLimit;
	protected static int _NSDictionaryClassHashCode;
	protected static int _NSDictionaryMapEntryHashCode;
	protected NSSet _keySetCache;
	protected NSSet _entrySetCache;
	private static final ObjectStreamField serialPersistentFields[];

	static {
		_CLASS = _NSUtilitiesExtra._classWithFullySpecifiedNamePrime("com.webobjects.foundation.NSDictionary");
		_MAP_ENTRY_CLASS = _NSUtilitiesExtra._classWithFullySpecifiedNamePrime("com.webobjects.foundation.NSDictionary$_JavaNSDictionaryMapEntry");
		_objectArrayClass = ((Object) (new Object[0])).getClass();
		_NSDictionaryClassHashCode = _CLASS.hashCode();
		_NSDictionaryMapEntryHashCode = _MAP_ENTRY_CLASS.hashCode();
		serialPersistentFields = (new ObjectStreamField[] { new ObjectStreamField("keys", _objectArrayClass), new ObjectStreamField("objects", _objectArrayClass) });
	}
}
