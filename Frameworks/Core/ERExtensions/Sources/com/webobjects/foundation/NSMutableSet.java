package com.webobjects.foundation;

import java.util.Collection;
import java.util.Set;

/**
 * NSSet reimplementation to support JDK 1.5 templates. Use with
 * 
 * <pre>
 * NSMutableSet&lt;E&gt; set = new NSMutableSet&lt;E&gt;();
 * set.put(new E())
 * 
 * for (E t : set)
 *     logger.debug(t);
 * </pre>
 * 
 * @param <E>
 *            type of set contents
 */

//TODO iterator.remove() throws unimplemented

public class NSMutableSet<E> extends NSSet<E> {
	public NSMutableSet() {
	}

	public NSMutableSet(int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException("Capacity cannot be less than 0");
		}
		
		_ensureCapacity(capacity);
	}
	
	public NSMutableSet(Collection<? extends E> collection) {
		super(collection);
	}

	public NSMutableSet(E object) {
		super(object);
	}

	public NSMutableSet(E[] objects) {
		super(objects);
	}

	public NSMutableSet(E object, E... objects) {
		super(object, objects);
	}
	
	public NSMutableSet(NSArray<? extends E> objects) {
		super(objects);
	}

	public NSMutableSet(NSSet<? extends E> otherSet) {
		super(otherSet);
	}
	
	public NSMutableSet(Set<? extends E> set, boolean ignoreNull) {
		super(set, ignoreNull);
	}

	public void addObject(E object) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
		}
		if (count() == capacity()) {
			_ensureCapacity(count() + 1);
		}
		if (_NSCollectionPrimitives.addValueToSet(object, _objects, _flags)) {
			_setCount(count() + 1);
			_objectsCache = null;
		}
	}
	
	public void addObjects(E... objects) {
		if (objects != null && objects.length > 0) {
			if (count() + objects.length > capacity()) {
				_ensureCapacity(count() + objects.length);
			}
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] == null)
                    throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
                if (_NSCollectionPrimitives.addValueToSet(objects[i], _objects, _flags)) {
                	_setCount(count() + 1);
                }
            }
		}
	}

	public E removeObject(Object object) {
		Object result = null;
		if (object != null && count() != 0) {
			result = _NSCollectionPrimitives.removeValueInHashTable(object, _objects, _objects, _flags);
			if (result != null) {
				_setCount(count() - 1);
				_deletionLimit--;
				if (count() == 0 || _deletionLimit == 0) {
					_clearDeletionsAndCollisions();
				}
				_objectsCache = null;
			}
		}
		return (E) result;
	}

	public void removeAllObjects() {
		if (count() != 0) {
			_objects = new Object[_hashtableBuckets];
			_flags = new byte[_hashtableBuckets];
			_setCount(0);
			_objectsCache = null;
			_deletionLimit = _NSCollectionPrimitives.deletionLimitForTableBuckets(_hashtableBuckets);
		}
	}

	public void setSet(NSSet<? extends E> otherSet) {
		if (otherSet != this) {
			removeAllObjects();
			if (otherSet != null) {
				E[] objects = (E[])otherSet.objectsNoCopy();
				for (int i = 0; i < objects.length; i++) {
					addObject(objects[i]);
				}

			}
		}
	}

	public void addObjectsFromArray(NSArray<? extends E> array) {
		if (array != null) {
			E[] objects = (E[])array.objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				addObject(objects[i]);
			}

		}
	}

	public void intersectSet(NSSet<?> otherSet) {
		if (otherSet != this) {
			if (otherSet == null || otherSet.count() == 0) {
				removeAllObjects();
				return;
			}
			E[] objects = (E[])objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				if (otherSet.member(objects[i]) == null) {
					removeObject(objects[i]);
				}
			}

		}
	}

	public void subtractSet(NSSet<?> otherSet) {
		if (otherSet == null || otherSet.count() == 0) {
			return;
		}
		if (otherSet == this) {
			removeAllObjects();
			return;
		}
		Object[] objects = otherSet.objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			if (member(objects[i]) != null) {
				removeObject(objects[i]);
			}
		}

	}

	public void unionSet(NSSet<? extends E> otherSet) {
		if (otherSet == null || otherSet.count() == 0 || otherSet == this) {
			return;
		}
		E[] objects = (E[])otherSet.objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			addObject(objects[i]);
		}

	}

	@Override
	public Object clone() {
		return new NSMutableSet<E>(this);
	}

	@Override
	public NSSet<E> immutableClone() {
		return new NSSet<E>(this);
	}

	@Override
	public NSMutableSet<E> mutableClone() {
		return (NSMutableSet<E>) clone();
	}

	@SuppressWarnings({ "hiding", "unchecked" })
	public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.foundation.NSMutableSet");
	static final long serialVersionUID = -6054074706096120227L;

	@Override
	public boolean add(E o) {
		if (contains(o)) {
			return false;
		}

		addObject(o);

		return true;
	}

	@Override
	public boolean remove(Object o) {
		return removeObject(o) != null ? true : false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean updated = false;
		for (E t : c) {
			if (!contains(t)) {
				add(t);
				updated = true;
			}
		}

		return updated;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		NSMutableSet<Object> s = new NSMutableSet<Object>();
		boolean updated = false;
		for (Object o : c) {
			s.add(o);
			if (!contains(o)) {
				updated = true;
			}

		}
		intersectSet(s);

		return updated;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		NSMutableSet<Object> s = new NSMutableSet<Object>();
		boolean updated = false;
		for (Object o : c) {
			s.add(o);
			if (!contains(o)) {
				updated = true;
			}
		}
		subtractSet(s);

		return updated;
	}

	@Override
	public void clear() {
		removeAllObjects();
	}

}
