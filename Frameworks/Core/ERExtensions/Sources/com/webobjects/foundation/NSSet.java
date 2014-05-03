package com.webobjects.foundation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <span class="en">
 * NSSet reimplementation to support JDK 1.5 templates. Use with
 * </span>
 * 
 * <span class="ja">
 * JDK 1.5 テンプレートをサポートする為の再実装。使用は
 * </span>
 * 
 * <pre>
 * NSSet<E> setA = new NSSet<E>(NSArray<E> listA);
 * NSSet<E> setB = new NSSet<E>(NSArray<E> listB);
 * logger.debug("intersection contains " + setA.setByIntersectingSet(setB));
 * </pre>
 * 
 * @param <E> - type of set contents
 */
public class NSSet<E> implements Cloneable, Serializable, NSCoding, _NSFoundationCollection, Set<E> {
  
  static final long serialVersionUID = -8833684352747517048L;

	public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.foundation.NSSet");

	protected static int _NSSetClassHashCode = _CLASS.hashCode();

	public static final NSSet EmptySet = new NSSet();

	private static final String SerializationValuesFieldKey = "objects";
	private static final ObjectStreamField[] serialPersistentFields = (new ObjectStreamField[] { new ObjectStreamField(SerializationValuesFieldKey, ((Object) (_NSUtilities._NoObjectArray)).getClass()) });

	public static Object decodeObject(NSCoder coder) {
		return new NSSet<Object>(coder.decodeObjects());
	}

	protected transient int _capacity;

	protected transient int _count;

	protected transient int _deletionLimit;

	protected transient byte[] _flags;

	protected transient int _hashCache;

	protected transient int _hashtableBuckets;

	protected Object[] _objects;

	protected transient Object[] _objectsCache;

	public NSSet() {
		_initializeSet();
	}

	public NSSet(Collection<? extends E> collection) {
		Object[] objects = collection.toArray();
		initFromObjects(objects, true);
	}
	
	public NSSet(NSArray<? extends E> objects) {
		this(objects == null ? null : (E[])objects.objectsNoCopy(), false);
	}

	public NSSet(NSSet<? extends E> otherSet) {
		this(otherSet == null ? null : (E[])otherSet.objectsNoCopy(), false);
	}

	public NSSet(Set<? extends E> set, boolean ignoreNull) {
		if (set == null) {
			throw new IllegalArgumentException("Set cannot be null");
		}

		if (!ignoreNull && set.contains(null)) {
			throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
		}
		
		Object[] aSet = set.toArray();
		initFromObjects(aSet, !ignoreNull);
	}

	public NSSet(E object) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
		}
		_initializeSet();
		_ensureCapacity(1);
		if (_NSCollectionPrimitives.addValueToSet(object, _objects, _flags)) {
			_setCount(count() + 1);
		}
	}

	public NSSet(E[] objects) {
		this(objects, true);
	}
	
	public NSSet(E object, E... objects) {
		this(objects, true);
		_ensureCapacity(count() + 1);
		if (_NSCollectionPrimitives.addValueToSet(object, _objects, _flags)) {
			_setCount(count() + 1);
		}
	}

	private NSSet(E[] objects, boolean checkForNull) {
		initFromObjects(objects, checkForNull);
	}

	public Object[] _allObjects() {
		int count = count();
		Object[] objects = new Object[count];
		if (count > 0) {
			System.arraycopy(objectsNoCopy(), 0, objects, 0, count);
		}
		return objects;
	}

	protected void _clearDeletionsAndCollisions() {
		int size = _hashtableBuckets;
		if (count() == 0) {
			_flags = new byte[size];
		} else {
			Object[] oldObjects = _objects;
			byte[] oldFlags = _flags;
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
		int currentCapacity = capacity();
		if (capacity > currentCapacity) {
			int newCapacity = _NSCollectionPrimitives.hashTableCapacityForCapacity(capacity);
			if (newCapacity != currentCapacity) {
				int oldSize = _hashtableBuckets;
				_setCapacity(newCapacity);
				_hashtableBuckets = _NSCollectionPrimitives.hashTableBucketsForCapacity(newCapacity);
				int newSize = _hashtableBuckets;
				if (newSize == 0) {
					_objects = null;
					_flags = null;
				} else {
					Object[] oldObjects = _objects;
					byte[] oldFlags = _flags;
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

	private boolean _equalsSet(NSSet<?> otherSet) {
		int count = count();
		if (count != otherSet.count()) {
			return false;
		}
		Object[] objects = objectsNoCopy();
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

	public boolean add(E o) {
		throw new UnsupportedOperationException("add is not a supported operation in com.webobjects.foundation.NSSet");
	}

	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("addAll is not a supported operation in com.webobjects.foundation.NSSet");
	}

	public NSArray<E> allObjects() {
		return new NSArray<E>((E[])objectsNoCopy());
	}

	public E anyObject() {
		return count() <= 0 ? null : (E)objectsNoCopy()[0];
	}

	public Class classForCoder() {
		return _CLASS;
	}

	public void clear() {
		throw new UnsupportedOperationException("clear is not a supported operation in com.webobjects.foundation.NSSet");
	}

	@Override
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
		Object[] objects = c.toArray();
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
	
	protected void _setCount(int count) {
    	_count = count;
    }
	
	protected int capacity() {
		return _capacity;
	}
	
	protected void _setCapacity(int capacity) {
		_capacity = capacity;
	}

	public void encodeWithCoder(NSCoder coder) {
		coder.encodeObjects(objectsNoCopy());
	}
	
	@SuppressWarnings("cast")
	public static <T> NSSet<T> emptySet() {
		return (NSSet<T>) EmptySet;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof NSSet) {
			return _equalsSet((NSSet<?>) object);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return _NSSetClassHashCode ^ count();
	}

	public HashSet<E> hashSet() {
		E[] objects = (E[])objectsNoCopy();
		HashSet<E> set = new HashSet<E>(objects.length);
		for (int i = 0; i < objects.length; i++) {
			set.add(objects[i]);
		}

		return set;
	}

	public NSSet<E> immutableClone() {
		return this;
	}

	private void initFromObjects(Object[] objects, boolean checkForNull) {
		_initializeSet();
		_ensureCapacity(objects.length);
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] == null) {
				if (checkForNull)
					throw new IllegalArgumentException("Attempt to insert null object into an  " + getClass().getName() + ".");
			} else {
				if (_NSCollectionPrimitives.addValueToSet(objects[i], _objects, _flags)) {
					_setCount(count() + 1);
				}
			}
		}

	}

	public boolean intersectsSet(NSSet<?> otherSet) {
		if (count() != 0 && otherSet != null && otherSet.count() != 0) {
			Object[] objects = objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				if (otherSet.member(objects[i]) != null) {
					return true;
				}
			}

		}
		return false;
	}

	public boolean isEmpty() {
		return count() == 0;
	}

	public boolean isEqualToSet(NSSet<?> otherSet) {
		if (otherSet == null) {
			return false;
		}
		if (otherSet == this) {
			return true;
		}

		return _equalsSet(otherSet);
	}

	public boolean isSubsetOfSet(NSSet<?> otherSet) {
		int count = count();
		if (otherSet == null || otherSet.count() < count) {
			return false;
		}
		if (count == 0) {
			return true;
		}
		Object[] objects = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			if (otherSet.member(objects[i]) == null) {
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public Iterator<E> iterator() {
		return new _NSJavaSetIterator(objectsNoCopy());
	}

	public E member(Object object) {
		return count() != 0 && object != null ? (E) _NSCollectionPrimitives.findValueInHashTable(object, _objects, _objects, _flags) : null;
	}

	public NSMutableSet<E> mutableClone() {
		return new NSMutableSet<E>(this);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<E> objectEnumerator() {
		return new _NSCollectionEnumerator(_objects, _flags, count());
	}

	protected Object[] objectsNoCopy() {
		if (_objectsCache == null) {
			_objectsCache = count() != 0 ? _NSCollectionPrimitives.valuesInHashTable(_objects, _objects, _flags, capacity(), _hashtableBuckets) : _NSCollectionPrimitives.EmptyArray;
		}
		return _objectsCache;
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();
		Object[] keys = (Object[]) fields.get(SerializationValuesFieldKey, ((_NSUtilities._NoObjectArray)));
		keys = keys != null ? keys : _NSUtilities._NoObjectArray;
		initFromObjects(keys, true);
	}
	private Object readResolve() throws ObjectStreamException {
		if (getClass() == _CLASS && count() == 0) {
			return EmptySet;
		}

		return this;
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
	public NSSet<E> setByIntersectingSet(NSSet<?> otherSet) {
		NSMutableSet<E> set = new NSMutableSet<E>(this);
		set.intersectSet(otherSet);
		return set;
	}
	public NSSet<E> setBySubtractingSet(NSSet<?> otherSet) {
		NSMutableSet<E> set = new NSMutableSet<E>(this);
		set.subtractSet(otherSet);
		return set;
	}
	public NSSet<E> setByUnioningSet(NSSet<? extends E> otherSet) {
		NSMutableSet<E> set = new NSMutableSet<E>(this);
		set.unionSet(otherSet);
		return set;
	}
	public int size() {
		return count();
	}
	public Object[] toArray() {
		Object[] currObjects = objectsNoCopy();
		Object[] objects = new Object[currObjects.length];
		if (currObjects.length > 0) {
			System.arraycopy(currObjects, 0, objects, 0, currObjects.length);
		}
		return objects;
	}
	public <T> T[] toArray(T[] objects) {
		if (objects == null) {
			throw new NullPointerException("Cannot pass null as parameter");
		}
		Object[] currObjects = objectsNoCopy();
		if (objects.length < currObjects.length) {
			objects = (T[]) java.lang.reflect.Array.newInstance(objects.getClass().getComponentType(), currObjects.length);
		}
		System.arraycopy(currObjects, 0, objects, 0, currObjects.length);
		return objects;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(128);
		buffer.append('(');
		Object[] objects = objectsNoCopy();
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

		buffer.append(')');
		return buffer.toString();
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		fields.put(SerializationValuesFieldKey, ((_allObjects())));
		s.writeFields();
	}
}
