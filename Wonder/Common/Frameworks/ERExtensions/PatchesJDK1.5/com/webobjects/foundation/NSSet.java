package com.webobjects.foundation;

import java.io.*;
import java.util.*;

/**
 * NSSet reimplementation to support JDK 1.5 templates. Use with
 * 
 * <pre>
 * NSSet&lt;T&gt; setA = new NSSet&lt;T&gt;(NSArray &lt; T &gt; listA);
 * NSSet&lt;T&gt; setB = new NSSet&lt;T&gt;(NSArray &lt; T &gt; listB);
 * logger.debug(&quot;intersection contains &quot; + setA.setByIntersectingSet(setB));
 * </pre>
 * 
 * @param &lt;T&gt;
 *            type of set contents
 */
@SuppressWarnings("unchecked")
public class NSSet<T> implements Cloneable, Serializable, NSCoding, _NSFoundationCollection, Set<T> {

	public static final Class _CLASS;

	protected static int _NSSetClassHashCode;

	public static final NSSet EmptySet = new NSSet();

	private static final ObjectStreamField serialPersistentFields[];

	static final long serialVersionUID = 0x508ecb88L;

	static {
		_CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.foundation.NSSet");
		_NSSetClassHashCode = _CLASS.hashCode();
		serialPersistentFields = (new ObjectStreamField[] { new ObjectStreamField("objects", ((Object) (_NSUtilities._NoObjectArray)).getClass()) });
	}

	public static Object decodeObject(NSCoder coder) {
		return new NSSet(coder.decodeObjects());
	}

	protected transient int _capacity;

	protected transient int _count;

	protected transient int _deletionLimit;

	protected transient byte _flags[];

	protected transient int _hashCache;

	protected transient int _hashtableBuckets;

	protected Object _objects[];

	protected transient Object _objectsCache[];

	public NSSet() {
		_initializeSet();
	}

	public NSSet(NSArray<T> objects) {
		this(objects == null ? null : objects.objectsNoCopy(), false);
	}

	public NSSet(NSSet<T> otherSet) {
		this(otherSet == null ? null : otherSet.objectsNoCopy(), false);
	}

	public NSSet(Set<T> set, boolean ignoreNull) {
		if (set == null) {
			throw new IllegalArgumentException("Set cannot be null");
		}
		else {
			T aSet[] = (T[]) set.toArray();
			initFromObjects(aSet, ignoreNull);
			return;
		}
	}

	public NSSet(T object) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
		}
		_initializeSet();
		_ensureCapacity(1);
		if (_NSCollectionPrimitives.addValueToSet(object, _objects, _flags)) {
			_count++;
		}
	}

	public NSSet(T objects[]) {
		this(objects, true);
	}

	private NSSet(T objects[], boolean checkForNull) {
		initFromObjects(objects, checkForNull);
	}

	public T[] _allObjects() {
		int count = count();
		Object objects[] = new Object[count];
		if (count > 0) {
			System.arraycopy(((objectsNoCopy())), 0, ((objects)), 0, count);
		}
		return (T[]) objects;
	}

	protected void _clearDeletionsAndCollisions() {
		int size = _hashtableBuckets;
		if (_count == 0) {
			_flags = new byte[size];
		}
		else {
			Object oldObjects[] = _objects;
			byte oldFlags[] = _flags;
			_objects = new Object[size];
			_flags = new byte[size];
			for (int i = 0; i < size; i++) {
				if ((oldFlags[i] & 0xffffffc0) == -128) {
					_NSCollectionPrimitives.addValueToSet(oldObjects[i], _objects, _flags);
				}
			}

		}
		_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(size);
	}

	protected void _ensureCapacity(int capacity) {
		int currentCapacity = _capacity;
		if (capacity > currentCapacity) {
			int newCapacity = _NSCollectionPrimitives.hashTableCapacityForCapacity(capacity);
			if (newCapacity != currentCapacity) {
				int oldSize = _hashtableBuckets;
				_capacity = newCapacity;
				_hashtableBuckets = _NSCollectionPrimitives.hashTableBucketsForCapacity(newCapacity);
				int newSize = _hashtableBuckets;
				if (newSize == 0) {
					_objects = null;
					_flags = null;
				}
				else {
					Object oldObjects[] = _objects;
					byte oldFlags[] = _flags;
					_objects = new Object[newSize];
					_flags = new byte[newSize];
					for (int i = 0; i < oldSize; i++) {
						if ((oldFlags[i] & 0xffffffc0) == -128) {
							_NSCollectionPrimitives.addValueToSet(oldObjects[i], _objects, _flags);
						}
					}

				}
				_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(newSize);
			}
		}
	}

	private boolean _equalsSet(NSSet<T> otherSet) {
		int count = count();
		if (count != otherSet.count()) {
			return false;
		}
		T objects[] = objectsNoCopy();
		for (int i = 0; i < count; i++) {
			if (otherSet.member(objects[i]) == null) {
				return false;
			}
		}

		return true;
	}

	protected void _initializeSet() {
		_capacity = _count = 0;
		_objects = _objectsCache = null;
		_flags = null;
		_hashtableBuckets = _NSCollectionPrimitives.hashTableBucketsForCapacity(_capacity);
		_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(_hashtableBuckets);
	}

	public int _shallowHashCode() {
		return _NSSetClassHashCode;
	}

	public boolean add(T o) {
		throw new UnsupportedOperationException("add is not a supported operation in com.webobjects.foundation.NSSet");
	}

	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("addAll is not a supported operation in com.webobjects.foundation.NSSet");
	}

	public NSArray<T> allObjects() {
		return new NSArray<T>(objectsNoCopy());
	}

	public T anyObject() {
		return count() <= 0 ? null : objectsNoCopy()[0];
	}

	public Class classForCoder() {
		return _CLASS;
	}

	public void clear() {
		throw new UnsupportedOperationException("clear is not a supported operation in com.webobjects.foundation.NSSet");
	}

	public Object clone() {
		return this;
	}

	public boolean contains(Object o) {
		return containsObject(o);
	}

	public boolean containsAll(Collection<?> c) {
		if (c == null) {
			throw new NullPointerException("Collection passed into containsAll() cannot be null");
		}
		T objects[] = (T[]) c.toArray();
		if (objects.length > 0) {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] == null) {
					return false;
				}
				if (member(objects[i]) == null) {
					return false;
				}
			}

		}
		return true;
	}

	public boolean containsObject(Object object) {
		return object == null ? false : member(object) != null;
	}

	public int count() {
		return _count;
	}

	public void encodeWithCoder(NSCoder coder) {
		coder.encodeObjects(objectsNoCopy());
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof NSSet) {
			return _equalsSet((NSSet) object);
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		return _NSSetClassHashCode ^ count();
	}

	public HashSet<T> hashSet() {
		T objects[] = objectsNoCopy();
		HashSet<T> set = new HashSet<T>(objects.length);
		for (int i = 0; i < objects.length; i++) {
			set.add(objects[i]);
		}

		return set;
	}

	public NSSet<T> immutableClone() {
		return this;
	}

	private void initFromObjects(T objects[], boolean checkForNull) {
		if (checkForNull) {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] == null) {
					throw new IllegalArgumentException("Attempt to insert null object into an  " + getClass().getName() + ".");
				}
			}

		}
		_initializeSet();
		_ensureCapacity(objects.length);
		for (int i = 0; i < objects.length; i++) {
			if (_NSCollectionPrimitives.addValueToSet(objects[i], _objects, _flags)) {
				_count++;
			}
		}

	}

	public boolean intersectsSet(NSSet<T> otherSet) {
		if (count() != 0 && otherSet != null && otherSet.count() != 0) {
			T objects[] = objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				if (otherSet.member(objects[i]) != null) {
					return true;
				}
			}

		}
		return false;
	}

	public boolean isEmpty() {
		return _count == 0;
	}

	public boolean isEqualToSet(NSSet<T> otherSet) {
		if (otherSet == null) {
			return false;
		}
		if (otherSet == this) {
			return true;
		}
		else {
			return _equalsSet(otherSet);
		}
	}

	public boolean isSubsetOfSet(NSSet<T> otherSet) {
		int count = count();
		if (otherSet == null || otherSet.count() < count) {
			return false;
		}
		if (count == 0) {
			return true;
		}
		T objects[] = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			if (otherSet.member(objects[i]) == null) {
				return false;
			}
		}

		return true;
	}

	public Iterator<T> iterator() {
		return new _NSJavaSetIterator(objectsNoCopy());
	}

	public T member(Object object) {
		return _count != 0 && object != null ? (T) _NSCollectionPrimitives.findValueInHashTable(object, _objects, _objects, _flags) : null;
	}

	public NSMutableSet<T> mutableClone() {
		return new NSMutableSet<T>(this);
	}

	public Enumeration<T> objectEnumerator() {
		return new _NSCollectionEnumerator(_objects, _flags, _count);
	}

	protected T[] objectsNoCopy() {
		if (_objectsCache == null) {
			_objectsCache = _count != 0 ? _NSCollectionPrimitives.valuesInHashTable(_objects, _objects, _flags, _capacity, _hashtableBuckets) : _NSCollectionPrimitives.EmptyArray;
		}
		return (T[]) _objectsCache;
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();
		Object keys[] = (Object[]) fields.get("objects", ((_NSUtilities._NoObjectArray)));
		keys = keys != null ? keys : _NSUtilities._NoObjectArray;
		initFromObjects((T[]) keys, true);
	}
	private Object readResolve() throws ObjectStreamException {
		if (getClass() == _CLASS && count() == 0) {
			return EmptySet;
		}
		else {
			return this;
		}
	}
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("remove is not a supported operation in com.webobjects.foundation.NSSet");
	}
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("removeAll is not a supported operation in com.webobjects.foundation.NSSet");
	}
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("retainAll is not a supported operation in com.webobjects.foundation.NSSet");
	}
	public NSSet<T> setByIntersectingSet(NSSet<T> otherSet) {
		NSMutableSet<T> set = new NSMutableSet<T>(this);
		set.intersectSet(otherSet);
		return set;
	}
	public NSSet<T> setBySubtractingSet(NSSet otherSet) {
		NSMutableSet<T> set = new NSMutableSet<T>(this);
		set.subtractSet(otherSet);
		return set;
	}
	public NSSet<T> setByUnioningSet(NSSet<T> otherSet) {
		NSMutableSet<T> set = new NSMutableSet<T>(this);
		set.unionSet(otherSet);
		return set;
	}
	public int size() {
		return _count;
	}
	public T[] toArray() {
		return objectsNoCopy();
	}
	public Object[] toArray(Object objects[]) {
		if (objects == null) {
			throw new NullPointerException("Cannot pass null as parameter");
		}
		Object currObjects[] = objectsNoCopy();
		if (objects.length < currObjects.length) {
			objects = (T[]) java.lang.reflect.Array.newInstance(objects.getClass().getComponentType(), _objects.length);
		}
		for (int i = 0; i < _objects.length; i++) {
			objects[i] = (T) _objects[i];
		}
		return objects;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(128);
		buffer.append("(");
		Object objects[] = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (i > 0) {
				buffer.append(", ");
			}
			if (object instanceof String) {
				buffer.append('"');
				buffer.append((String) object);
				buffer.append('"');
				continue;
			}
			if (object instanceof Boolean) {
				buffer.append(((Boolean) object).booleanValue() ? "true" : "false");
			}
			else {
				buffer.append(object.toString());
			}
		}

		buffer.append(")");
		return new String(buffer);
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		fields.put("objects", ((_allObjects())));
		s.writeFields();
	}
}
