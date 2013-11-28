package com.webobjects.foundation;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Vector;
/**
 * <span class="en">
 * Bugfix reimplementation of NSMutableArray. To be able to use this class, the framework this class resides in must be
 * before JavaFoundation.framework in your classpath. <br />
 * It fixes a lot of issues:
 * <ul>
 * <li>implements the correct Collection methods, so you can <code>anArray.add(anObject)</code>
 * <li>has several performance improvements that make EOF faster by several orders of magnitude when you have large record sets 
 * <li>fixes a bug when the actual objects were handed out replaceObjectAtIndex()
 * <li>fixes a bug when the iterator method wasn't firing a fault in <code>_EOCheapCopyMutableArray</code>
 * </ul>
 * 
 * Once these issues are resolved in a WO distribution, this class will go away and the Apple 
 * supplied will will be used again without changes in code on your side. <br />
 * 
 * @param <E> type of array contents
 * </span>
 * 
 * <span class="ja">
 * NSMutableArray のバッグフィックス再実装。
 * このクラスを使用する為には現フレームワークのクラスパスが JavaFoundation.framework の前にある必要があります。<br>
 * 
 * 次の問題を対応しています:
 * <ul>
 * <li>正しいコレクション・メソッドの実装：<code>anArray.add(anObject)</code> が可能
 * <li>大きなレコード・セットの場合でのスピード改良で EOF が早くなるのです
 * <li>実際のオブジェクトが replaceObjectAtIndex() の外で処理されるバッグフィックス
 * <li><code>_EOCheapCopyMutableArray</code> でのフォルトはトリーガされない問題のバッグフィックス
 * </ul>
 * 
 * @param <E> type of array contents
 * </span>
 * 
 * @author ak
 */
public class NSMutableArray <E> extends NSArray<E> implements RandomAccess {

  static final long serialVersionUID = -3909373569895711876L;
  
	public static final Class _CLASS = _NSUtilitiesExtra._classWithFullySpecifiedNamePrime("com.webobjects.foundation.NSMutableArray");
    

    public static final Object ERX_MARKER = "Wonder";

    protected transient int modCount = 0;
	protected transient int _capacity;
	protected transient Object[] _objectsCache;
	protected transient int _count;

    public NSMutableArray() {
    }
    
    public NSMutableArray(Collection<? extends E> collection) {
    	super(collection);
    }

    public NSMutableArray(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be less than 0");
        }
        
        _ensureCapacity(capacity);
    }

    public NSMutableArray(E object) {
        super(object);
    }

    public NSMutableArray(E[] objects) {
        super(objects);
    }

    public NSMutableArray(E object, E... objects) {
        super(object, objects);
    }
    
    public NSMutableArray(E[] objects, NSRange range) {
        super(objects, range);
    }

    public NSMutableArray(NSArray<? extends E> otherArray) {
        super(otherArray);
    }

    public NSMutableArray(Vector<? extends E> vector, NSRange range, boolean ignoreNull) {
        super(vector, range, ignoreNull);
    }

    public NSMutableArray(List<? extends E> list, NSRange range, boolean ignoreNull) {
      super(list, range, ignoreNull);
    }

	@Override
	protected void _initializeWithCapacity(int capacity) {
		_capacity = capacity;
		_objectsCache = null;
		super._initializeWithCapacity(capacity);
	}
	
	protected void _ensureCapacity(int capacity) {
		if (capacity > _capacity) {
			if (capacity == 0) {
				_setObjects(null);
			} else {
				if (capacity < 4) {
					capacity = 4;
				} else {
					int testCapacity = 2 * _capacity;
					if (testCapacity > capacity) {
						capacity = testCapacity;
					}
				}
				Object[] objs = _objects();
				objs = objs != null ? _NSCollectionPrimitives.copyArray(objs, capacity) : new Object[capacity];
				_setObjects(objs);
			}
			_capacity = capacity;
		}
	}

    public void ensureCapacity(int capacity) {
    	_ensureCapacity(capacity);
    }
    
    public void trimToSize() {
    	// no op
    }
    
    @Override
    protected void _setCount(int count) {
    	_count = count;
    }
        
    public void setArray(NSArray<? extends E> otherArray) {
        if (otherArray != this) {
            if (otherArray == null) {
            	_setCount(0);
            } else {
                Object[] objects = otherArray.objectsNoCopy();
                _ensureCapacity(objects.length);
                int count = count();
                Object[] objs = _objects();
                if (objects.length > 0)
                    System.arraycopy(objects, 0, objs, 0, objects.length);
                for (int i = objects.length; i < count; i++)
                	objs[i] = null;
                _setCount(objects.length);
            }
            clearCache();
        }
    }
    
    @Override
 	protected Object[] objectsNoCopy() {
		if (_objectsCache == null) {
			int count = count();
			if (count == 0) {
				_objectsCache = _NSCollectionPrimitives.EmptyArray;
			}
			else if (_count == _capacity) {
				_objectsCache = _objects();
			}
			else {
				_objectsCache = _NSCollectionPrimitives.copyArray(_objects(), count);
			}
		}
		return _objectsCache;
 	}

    @Override
    public int count() {
    	return _count;
    }
    
    public void addObject(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Attempt to insert null into an " + getClass().getName() + ".");
        }
        int count = count();
        _ensureCapacity(count+1);
        _objects()[count] = object;
        _setCount(count+1);
        clearCache();
    }

    public void addObjects(E... objects) {
        if (objects != null && objects.length > 0) {
            for (int i = 0; i < objects.length; i++)
                if (objects[i] == null)
                    throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
            int count = count();
            _ensureCapacity(count + objects.length);
            System.arraycopy(objects, 0, _objects(), count, objects.length);
            _setCount(count + objects.length);
            clearCache();
        }
    }

    /**
     * @deprecated use {@link #replaceObjectAtIndex(Object, int)}
     */
    @Deprecated
    public void replaceObjectAtIndex(int index, E object) {
        replaceObjectAtIndex(object, index);
    }

    public void insertObjectAtIndex(E object, int index) {
        if (object == null)
            throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
        int count = count();
        if (index >= 0 && index <= count) {
            _ensureCapacity(count + 1);
            Object[] objs = _objects();
            if (index < count)
                System.arraycopy(objs, index, objs, index + 1, count - index);
            objs[index] = object;
            _setCount(count+1);
            clearCache();
            return;
        }
        
        throw new IndexOutOfBoundsException("Index (" + index + ") out of bounds [0, " + (count - 1) + "]");
    }

    public E removeObjectAtIndex(int index) {
        int count = count();
        if (index >= 0 && index < count) {
            count--;
            Object[] objs = _objects();
            Object result = objs[index];
            if (index < count)
                System.arraycopy(objs, index + 1, objs, index, count - index);
            objs[count] = null;
            _setCount(count);
            clearCache();
            return (E) result;
        }
        if (count == 0)
            throw new IndexOutOfBoundsException("Array is empty");

        throw new IndexOutOfBoundsException("Index (" + index + ") out of bounds [0, " + (count - 1) + "]");
    }

    public void removeAllObjects() {
        if (count() > 0) {
			_setObjects(new Object[_capacity]);
            _setCount(0);
            clearCache();
        }
    }

    public void sortUsingComparator(NSComparator comparator) throws NSComparator.ComparisonException {
        if (comparator == null)
            throw new IllegalArgumentException("Comparator not specified");
        int count = count();
        if (count < 2) {
            return;
        }

        _NSCollectionPrimitives.K2SortArray(_objects(), count, comparator);
        clearCache();
    }

    public void addObjectsFromArray(NSArray<? extends E> otherArray) {
        if (otherArray != null)
            addObjects((E[])otherArray.objectsNoCopy());
    }

    public void replaceObjectsInRange(NSRange range, NSArray<? extends E> otherArray, NSRange otherRange) {
        if (range == null || otherRange == null) {
            throw new IllegalArgumentException("Both ranges cannot be null");
        }
        if (otherArray == null) {
            throw new IllegalArgumentException("Other array cannot be null");
        }
        int rangeLength = range.length();
        int rangeLocation = range.location();
        int otherRangeLength = otherRange.length();
        int otherRangeLocation = otherRange.location();
        for (; 0 < rangeLength && 0 < otherRangeLength; otherRangeLength--) {
            replaceObjectAtIndex(otherArray.objectAtIndex(otherRangeLocation), rangeLocation);
            rangeLocation++;
            rangeLength--;
            otherRangeLocation++;
        }

        for (; 0 < otherRangeLength; otherRangeLength--) {
            insertObjectAtIndex(otherArray.objectAtIndex(otherRangeLocation), rangeLocation);
            rangeLocation++;
            otherRangeLocation++;
        }

        for (; 0 < rangeLength; rangeLength--) {
            removeObjectAtIndex(rangeLocation);
        }

    }

    public E removeLastObject() {
        if (count() == 0) {
            return null;
        }
            
        return removeObjectAtIndex(count() - 1);
    }

    private boolean _removeObject(Object object, int index, int length, boolean identical) {
        boolean wasRemoved = false;
        if (object == null) {
            throw new IllegalArgumentException("Attempt to remove null from an  " + getClass().getName() + ".");
        }
        if (count() > 0) {
            Object[] objects = objectsNoCopy();
            int maxIndex = (index + length) - 1;
            if (identical) {
                for (int i = maxIndex; i >= index; i--)
                    if (objects[i] == object) {
                        removeObjectAtIndex(i);
                        wasRemoved = true;
                    }

            } else if (!identical) {
                for (int i = maxIndex; i >= index; i--)
                    if (objects[i] == object || object.equals(objects[i])) {
                        removeObjectAtIndex(i);
                        wasRemoved = true;
                    }

            }
        }
        return wasRemoved;
    }

    public boolean removeObject(Object object) {
        return _removeObject(object, 0, count(), false);
    }

    public boolean removeObject(Object object, NSRange range) {
        boolean wasRemoved = false;
        if (range != null) {
            int count = count();
            int rangeLocation = range.location();
            int rangeLength = range.length();
            if (rangeLocation + rangeLength > count || rangeLocation >= count)
                throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength
                        + "] out of bounds [0, " + (count - 1) + "]");
            wasRemoved = _removeObject(object, rangeLocation, rangeLength, false);
        }
        return wasRemoved;
    }

    public boolean removeIdenticalObject(Object object) {
        return _removeObject(object, 0, count(), true);
    }

    public boolean removeIdenticalObject(Object object, NSRange range) {
        if (range != null) {
            int count = count();
            int rangeLocation = range.location();
            int rangeLength = range.length();
            if (rangeLocation + rangeLength > count || rangeLocation >= count) {
                throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength
                        + "] out of bounds [0, " + (count - 1) + "]");
            }
            return _removeObject(object, rangeLocation, rangeLength, true);
        }
        return false;
    }

    public void removeObjectsInArray(NSArray<?> otherArray) {
        if (otherArray != null) {
            removeObjects(otherArray.objectsNoCopy());
        }
    }

    public void removeObjectsInRange(NSRange range) {
        if (range != null) {
            int count = count();
            int rangeLocation = range.location();
            int rangeLength = range.length();
            if (rangeLocation + rangeLength > count || rangeLocation >= count)
                throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength
                        + "] out of bounds [0, " + (count - 1) + "]");
            while (rangeLength-- > 0)
                removeObjectAtIndex(rangeLocation);
        }
    }

    @Override
    public Object clone() {
        return new NSMutableArray<E>(this);
    }

    @Override
    public NSArray<E> immutableClone() {
        return new NSArray<E>(this);
    }

    @Override
    public NSMutableArray<E> mutableClone() {
        return (NSMutableArray<E>) clone();
    }

    public void _moveObjectAtIndexToIndex(int sourceIndex, int destIndex) {
        if (sourceIndex == destIndex)
            return;
        int count = count();
        if (sourceIndex < 0 || sourceIndex >= count || destIndex < 0 || destIndex >= count)
			throw new IllegalArgumentException("Either source(" + sourceIndex + ") or destination(" + destIndex + ") is illegal.");
        Object objs[] = _objects();
        Object temp = objs[sourceIndex];
		int boundary;
		int index;
		int direction;
        if (sourceIndex < destIndex) {
            index = destIndex;
            boundary = sourceIndex;
            direction = 1;
        } else {
            index = sourceIndex;
            boundary = destIndex;
            direction = -1;
        }
        for (; index != boundary; index += direction)
        	objs[index] = objs[index + direction];

        objs[destIndex] = temp;
        _objectsCache = null;
    }
    
    // AK: Bugfixes and enhancements from here on
    /**
     * Clears out the object cache and tell us to recompute the hash.
     *
     */
    private void clearCache() {
        _objectsCache = null;
        _setMustRecomputeHash(true);
    }

    /**
     * Clears the objectsNoCopy too. It's wrong not to clear it.
     * 
     * @param object the replacement object
     * @param index index of object to replace
     * @return object that has been replaced
     */
    public E replaceObjectAtIndex(E object, int index) {
        if (object == null) {
            throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
        }
        int count = count();
        if (index >= 0 && index < count) {
            Object[] objs = _objects();
            Object result = objs[index];
            objs[index] = object;
            clearCache();
            return (E) result;
        }
        
        throw new IllegalArgumentException("Index (" + index + ") out of bounds [0, " + (count - 1) + "]");
    }

    /**
     * Much faster implementation of the remove method for larger arrays.
     * 
     * @param otherObjects objects to remove
     */
    public void removeObjects(Object... otherObjects) {
        if (otherObjects != null) {
            int count = count();
            if(count > 0) {
                int otherCount = otherObjects.length;
                if(count * otherCount > 100) {
                    if(count > 0) {
                        NSMutableSet<Object> table = new NSMutableSet<Object>(otherCount);
                        for (int i = 0; i < otherCount; i++) {
                            Object o = otherObjects[i];
                            if(o != null) {
                                table.addObject(o);
                            }
                        }
                        int offset = 0;
                        Object[] objs = _objects();
                        for(int i = 0; i < count; i++) {
                            Object o = objs[i];
                            objs[i] = null;
                            if (!table.containsObject(o)) {
                            	objs[offset++] = o;
                            }
                        }
                        _setCount(offset);
                        clearCache();
                    }
                } else {
                    for (int i = 0; i < otherObjects.length; i++)
                        removeObject(otherObjects[i]);
                }
            }
        }
    }

    /**
     * Bugfix for the broken implementation in NSArray.
     */
    @Override
    public <T> T[] toArray(T[] array) {
    	int i = size();
    	if (array.length < i) {
    		array = (T[]) Array.newInstance(array.getClass().getComponentType(), i);
    	}
    	Object[] result = array;
    	for (int j = 0; j < i; j++) {
    		result[j] = objectAtIndex(j);
    	}

    	if (array.length > i) {
    		array[i] = null;
    	}
    	return array;
    }

    //AK: from here on only java.util.List stuff

    @Override
    public E set(int index, E element) {
    	E old = objectAtIndex(index);
    	if(element != old) {
    		replaceObjectAtIndex(element, index);
    	}
    	return old;
    }

    @Override
    public void add(int index, E element) {
        insertObjectAtIndex(element, index);
    }

    @Override
    public boolean add(E element) {
        addObject(element);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        addObjects((E[]) collection.toArray());
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        boolean modified = false;
        if(collection == this) {
        	collection = ((NSMutableArray<? extends E>)collection).immutableClone();
        }
        Iterator<? extends E> e = collection.iterator();
        while (e.hasNext()) {
            add(index++, e.next());
            modified = true;
        }
        return modified;
    }

    @Override
    public E remove(int index) {
        return removeObjectAtIndex(index);
    }

    @Override
    public boolean remove(Object o) {
    	boolean modified = false;
    	int index = indexOf(o);
    	if (index != NotFound) {
    		removeObjectAtIndex(index);
    		modified = true;
    	}
    	return modified;
    }

    @Override
    public void clear() {
        removeAllObjects();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<?> e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        int count = count();
        removeObjects(collection.toArray());
        return count != count();
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException("Index: " + index);

        return new ListItr(index);
    }

    private class Itr implements Iterator<E> {
        int cursor = 0;

        int lastRet = NotFound;

        int expectedModCount = modCount;

        protected Itr() { }
        
        public boolean hasNext() {
            return cursor != size();
        }

        public E next() {
            try {
                E next = get(cursor);
                checkForComodification();
                lastRet = cursor++;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastRet == NotFound)
                throw new IllegalStateException();
            checkForComodification();

            try {
                NSMutableArray.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = NotFound;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public E previous() {
            try {
                int i = cursor - 1;
                E previous = get(i);
                checkForComodification();
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public void set(Object o) {
            if (lastRet == NotFound)
                throw new IllegalStateException();
            checkForComodification();

            try {
                NSMutableArray.this.set(lastRet, (E)o);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(Object o) {
            checkForComodification();

            try {
                NSMutableArray.this.add(cursor++, (E)o);
                lastRet = NotFound;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @SuppressWarnings("cast")
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess ? new RandomAccessSubList<E>(this,
                fromIndex, toIndex) : new SubList<E>(this, fromIndex, toIndex));
    }

    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<?> it = listIterator(fromIndex);
        for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
            it.next();
            it.remove();
        }
    }
}

class SubList<E> extends NSMutableArray<E> {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    NSMutableArray<E> l;
    int offset;
    int size;
    int expectedModCount;

    SubList(NSMutableArray<E> list, int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
        l = list;
        offset = fromIndex;
        size = toIndex - fromIndex;
        expectedModCount = l.modCount;
    }

    @Override
    public E set(int index, E element) {
        rangeCheck(index);
        checkForComodification();
        return l.set(index + offset, element);
    }

    @Override
    public E get(int index) {
        rangeCheck(index);
        checkForComodification();
        return l.get(index + offset);
    }

    @Override
    public int size() {
        checkForComodification();
        return size;
    }

    @Override
    public int count() {
        return size();
    }

    @Override
    public boolean add(E element) {
    	add(size(), element);
    	return true;
    }

    @Override
    public void add(int index, E element) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();
        checkForComodification();
        l.add(index + offset, element);
        expectedModCount = l.modCount;
        size++;
        modCount++;
    }

    @Override
    public boolean remove(Object o) {
    	Iterator<E> e = iterator();
    	while (e.hasNext()) {
    		if (o.equals(e.next())) {
    			e.remove();
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    public E remove(int index) {
        rangeCheck(index);
        checkForComodification();
        E result = l.remove(index + offset);
        expectedModCount = l.modCount;
        size--;
        modCount++;
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex + offset, toIndex + offset);
        expectedModCount = l.modCount;
        size -= (toIndex - fromIndex);
        modCount++;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
        int cSize = c.size();
        if (cSize == 0)
            return false;

        checkForComodification();
        l.addAll(offset + index, c);
        expectedModCount = l.modCount;
        size += cSize;
        modCount++;
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }


    @Override
    public ListIterator<E> listIterator(final int index) {
        checkForComodification();
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);

        return new ListIterator<E>() {
            private ListIterator<E> i = l.listIterator(index + offset);

            public boolean hasNext() {
                return nextIndex() < size;
            }

            public E next() {
                if (hasNext()) {
                    return i.next();
                }
                throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public E previous() {
                if (hasPrevious()) {
                    return i.previous();
                }
                throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                expectedModCount = l.modCount;
                size--;
                modCount++;
            }

            public void set(E o) {
                i.set(o);
            }

            public void add(E o) {
                i.add(o);
                expectedModCount = l.modCount;
                size++;
                modCount++;
            }
        };
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList<E>(this, fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        int count = count();
        Object[] objects = new Object[count];
        if (count > 0) {
            System.arraycopy(l.objectsNoCopy(), offset, objects, 0, count);
        }
        return objects;
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ",Size: "
                    + size);
    }

    private void checkForComodification() {
        if (l.modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }

    @Override
    public String toString() {
        int count = count();
        if (count == 0) {
            return "()";
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append('(');
        for (int i = 0; i < count; i++) {
            Object object = l.get(i + offset);
            if (i > 0) {
                sb.append(", ");
            }
            if (object instanceof String) {
                sb.append('"');
                sb.append(object);
                sb.append('"');
            } else {
                sb.append(object == this ? "THIS" : object);
            }
        }
        sb.append(')');
        return sb.toString();
    }
}

class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
    /**
     * Do I need to update serialVersionUID?
     * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
     * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
     */
    private static final long serialVersionUID = 1L;

    RandomAccessSubList(NSMutableArray<E> list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList<E>(this, fromIndex, toIndex);
    }
}
