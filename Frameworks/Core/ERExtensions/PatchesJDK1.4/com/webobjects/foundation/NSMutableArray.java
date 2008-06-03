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
 * Bugfix reimplementation of NSMutableArray. To be able to use this class, the framework this class resides in must be
 * before JavaFoundation.framework in your classpath. <br />
 * It fixes a lot of issues:
 * <ul>
 * <li>implements the correct Collection methods, so you can <code>anArray.add(anObject)</code>
 * <li>has several performance improvements that make EOF faster by several orders of magnitude when you have large record sets 
 * <li>fixes a bug when the actual objects were handed out replaceObjectAtIndex()
 * <li>fixes a bug when the iterator method wasn't firing a fault in <code>_EOCheapCopyMutableArray</code>
 * </ul>
 * Once these issues are resolved in a WO distribution, this class will go away and the Apple 
 * supplied will will be used again without changes in code on your side. <br />
 * @author ak
 */
public class NSMutableArray extends NSArray {

	public static final Class _CLASS = _NSUtilitiesExtra._classWithFullySpecifiedNamePrime("com.webobjects.foundation.NSMutableArray");
    
    static final long serialVersionUID = -3909373569895711876L;

    public static final Object ERX_MARKER = "Wonder";
    
    public NSMutableArray() {
    }

    public NSMutableArray(int capacity) {
        this();
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be less than 0");
        } else {
            _ensureCapacity(capacity);
            return;
        }
    }

    public NSMutableArray(Object object) {
        super(object);
    }

    public NSMutableArray(Object objects[]) {
        super(objects);
    }

    public NSMutableArray(Object objects[], NSRange range) {
        super(objects, range);
    }

    public NSMutableArray(NSArray otherArray) {
        super(otherArray);
    }

    public NSMutableArray(Vector vector, NSRange range, boolean ignoreNull) {
        super(vector, range, ignoreNull);
    }

    public void setArray(NSArray otherArray) {
        if (otherArray != this) {
            if (otherArray == null) {
                _count = 0;
            } else {
                Object objects[] = otherArray.objectsNoCopy();
                _ensureCapacity(objects.length);
                if (objects.length > 0)
                    System.arraycopy(objects, 0, _objects, 0, objects.length);
                for (int i = objects.length; i < _count; i++)
                    _objects[i] = null;

                _count = objects.length;
            }
            clearCache();
        }
    }

    public void addObject(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Attempt to insert null into an " + getClass().getName() + ".");
        } else {
            _ensureCapacity(_count + 1);
            _objects[_count++] = object;
            clearCache();
            return;
        }
    }

    public void addObjects(Object objects[]) {
        if (objects != null && objects.length > 0) {
            for (int i = 0; i < objects.length; i++)
                if (objects[i] == null)
                    throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");

            _ensureCapacity(_count + objects.length);
            System.arraycopy(objects, 0, _objects, _count, objects.length);
            _count += objects.length;
            clearCache();
        }
    }

    /**
     * @deprecated Method replaceObjectAtIndex is deprecated
     */

    public void replaceObjectAtIndex(int index, Object object) {
        replaceObjectAtIndex(object, index);
    }

    public void insertObjectAtIndex(Object object, int index) {
        if (object == null)
            throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
        if (index >= 0 && index <= _count) {
            _ensureCapacity(_count + 1);
            if (index < _count)
                System.arraycopy(_objects, index, _objects, index + 1, _count - index);
            _objects[index] = object;
            _count++;
            clearCache();
            return;
        } else {
            throw new IllegalArgumentException("Index (" + index + ") out of bounds [0, " + (_count - 1) + "]");
        }
    }

    public Object removeObjectAtIndex(int index) {
        if (index >= 0 && index < _count) {
            _count--;
            Object result = _objects[index];
            if (index < _count)
                System.arraycopy(_objects, index + 1, _objects, index, _count - index);
            _objects[_count] = null;
            clearCache();
            return result;
        }
        if (_count == 0)
            throw new IllegalArgumentException("Array is empty");
        else
            throw new IllegalArgumentException("Index (" + index + ") out of bounds [0, " + (_count - 1) + "]");
    }

    public void removeAllObjects() {
        if (_count > 0) {
            _objects = new Object[_capacity];
            _count = 0;
            clearCache();
        }
    }

    public void sortUsingComparator(NSComparator comparator) throws NSComparator.ComparisonException {
        if (comparator == null)
            throw new IllegalArgumentException("Comparator not specified");
        if (_count < 2) {
            return;
        } else {
            _NSCollectionPrimitives.K2SortArray(_objects, _count, comparator);
            clearCache();
            return;
        }
    }

    public void addObjectsFromArray(NSArray otherArray) {
        if (otherArray != null)
            addObjects(otherArray.objectsNoCopy());
    }

    public void replaceObjectsInRange(NSRange range, NSArray otherArray, NSRange otherRange) {
        if (range == null || otherRange == null)
            throw new IllegalArgumentException("Both ranges cannot be null");
        if (otherArray == null)
            throw new IllegalArgumentException("Other array cannot be null");
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

        for (; 0 < rangeLength; rangeLength--)
            removeObjectAtIndex(rangeLocation);

    }

    public Object removeLastObject() {
        if (count() == 0)
            return null;
        else
            return removeObjectAtIndex(count() - 1);
    }

    private boolean _removeObject(Object object, int index, int length, boolean identical) {
        boolean wasRemoved = false;
        if (object == null)
            throw new IllegalArgumentException("Attempt to remove null from an  " + getClass().getName() + ".");
        if (count() > 0) {
            Object objects[] = objectsNoCopy();
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
                        + "] out of bounds [0, " + (_count - 1) + "]");
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
            if (rangeLocation + rangeLength > count || rangeLocation >= count)
                throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength
                        + "] out of bounds [0, " + (_count - 1) + "]");
            else
                return _removeObject(object, rangeLocation, rangeLength, true);
        } else {
            return false;
        }
    }

    public void removeObjectsInArray(NSArray otherArray) {
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
                        + "] out of bounds [0, " + (_count - 1) + "]");
            while (rangeLength-- > 0)
                removeObjectAtIndex(rangeLocation);
        }
    }

    public Object clone() {
        return new NSMutableArray(this);
    }

    public NSArray immutableClone() {
        return new NSArray(this);
    }

    public NSMutableArray mutableClone() {
        return (NSMutableArray) clone();
    }

    public void _moveObjectAtIndexToIndex(int sourceIndex, int destIndex) {
        if (sourceIndex == destIndex)
            return;
        if (sourceIndex < 0 || sourceIndex >= _count || destIndex < 0 || destIndex >= _count)
            throw new IllegalArgumentException("Either source(" + sourceIndex + ") or destination(" + destIndex
                    + ") is illegal.");
        Object temp = _objects[sourceIndex];
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
            _objects[index] = _objects[index + direction];

        _objects[destIndex] = temp;
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
     */

    public Object replaceObjectAtIndex(Object object, int index) {
        if (object == null)
            throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
        if (index >= 0 && index < _count) {
            Object result = _objects[index];
            _objects[index] = object;
            clearCache();
            return result;
        } else {
            throw new IllegalArgumentException("Index (" + index + ") out of bounds [0, " + (_count - 1) + "]");
        }
    }

    /**
     * Much faster implementation of the remove method for larger arrays.
     */

    public void removeObjects(Object otherObjects[]) {
        if (otherObjects != null) {
            int count = count();
            if(count > 0) {
                int otherCount = otherObjects.length;
                if(count * otherCount > 100) {
                    if(count > 0) {
                        NSMutableSet table = new NSMutableSet(otherCount);
                        for (int i = 0; i < otherCount; i++) {
                            Object o = otherObjects[i];
                            if(o != null) {
                                table.addObject(o);
                            }
                        }
                        int offset = 0;
                        for(int i = 0; i < count; i++) {
                            Object o = _objects[i];
                            _objects[i] = null;
                            if (!table.containsObject(o)) {
                                _objects[offset] = o;
                                offset = offset + 1;
                            }
                        }
                        _count = offset;
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
    public Object[] toArray(Object array[]) {
    	int i = size();
    	if (array.length < i) {
    		array = (Object[]) Array.newInstance(((Object) (array)).getClass().getComponentType(), i);
    	}
    	Object result[] = array;
    	for (int j = 0; j < i; j++) {
    		result[j] = objectAtIndex(j);
    	}

    	if (array.length > i) {
    		array[i] = null;
    	}
    	return array;
    }

    //AK: from here on only java.util.List stuff

    public Object set(int index, Object element) {
    	Object old = objectAtIndex(index);
    	if(element != old) {
    		replaceObjectAtIndex(element, index);
    	}
    	return old;
    }

    public void add(int index, Object element) {
        insertObjectAtIndex(element, index);
    }

    public boolean add(Object element) {
        addObject(element);
        return true;
    }

    public boolean addAll(Collection collection) {
        addObjects(collection.toArray());
        return true;
    }

    public boolean addAll(int index, Collection collection) {
        boolean modified = false;
        Iterator e = collection.iterator();
        while (e.hasNext()) {
            add(index++, e.next());
            modified = true;
        }
        return modified;
    }

    public Object remove(int index) {
        Object result = removeObjectAtIndex(index);
        return result;
    }

    public boolean remove(Object o) {
        boolean present = removeObject(o);
        return present;
    }

    public void clear() {
        removeAllObjects();
    }

    public boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    public boolean removeAll(Collection collection) {
        int count = count();
        removeObjects(collection.toArray());
        return count != count();
    }

    public Iterator iterator() {
        return new Itr();
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(final int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException("Index: " + index);

        return new ListItr(index);
    }

    private class Itr implements Iterator {
        int cursor = 0;

        int lastRet = -1;

        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != size();
        }

        public Object next() {
            try {
                Object next = get(cursor);
                checkForComodification();
                lastRet = cursor++;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastRet == -1)
                throw new IllegalStateException();
            checkForComodification();

            try {
                NSMutableArray.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
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

    private class ListItr extends Itr implements ListIterator {
        ListItr(int index) {
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public Object previous() {
            try {
                int i = cursor - 1;
                Object previous = get(i);
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
            if (lastRet == -1)
                throw new IllegalStateException();
            checkForComodification();

            try {
                NSMutableArray.this.set(lastRet, o);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(Object o) {
            checkForComodification();

            try {
                NSMutableArray.this.add(cursor++, o);
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public List subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess ? new RandomAccessSubList(this,
                fromIndex, toIndex) : new SubList(this, fromIndex, toIndex));
    }

    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator it = listIterator(fromIndex);
        for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
            it.next();
            it.remove();
        }
    }

    protected transient int modCount = 0;


}

class SubList extends NSMutableArray {
    private NSMutableArray l;

    private int offset;

    private int size;

    private int expectedModCount;

    SubList(NSMutableArray list, int fromIndex, int toIndex) {
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

    public Object set(int index, Object element) {
        rangeCheck(index);
        checkForComodification();
        return l.set(index + offset, element);
    }

    public Object get(int index) {
        rangeCheck(index);
        checkForComodification();
        return l.get(index + offset);
    }

    public int size() {
        checkForComodification();
        return size;
    }

    public void add(int index, Object element) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();
        checkForComodification();
        l.add(index + offset, element);
        expectedModCount = l.modCount;
        size++;
        modCount++;
    }

    public Object remove(int index) {
        rangeCheck(index);
        checkForComodification();
        Object result = l.remove(index + offset);
        expectedModCount = l.modCount;
        size--;
        modCount++;
        return result;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex + offset, toIndex + offset);
        expectedModCount = l.modCount;
        size -= (toIndex - fromIndex);
        modCount++;
    }

    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    public boolean addAll(int index, Collection c) {
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

    public Iterator iterator() {
        return listIterator();
    }


    public ListIterator listIterator(final int index) {
        checkForComodification();
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);

        return new ListIterator() {
            private ListIterator i = l.listIterator(index + offset);

            public boolean hasNext() {
                return nextIndex() < size;
            }

            public Object next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public Object previous() {
                if (hasPrevious())
                    return i.previous();
                else
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

            public void set(Object o) {
                i.set(o);
            }

            public void add(Object o) {
                i.add(o);
                expectedModCount = l.modCount;
                size++;
                modCount++;
            }
        };
    }

    public List subList(int fromIndex, int toIndex) {
        return new SubList(this, fromIndex, toIndex);
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

}

class RandomAccessSubList extends SubList implements RandomAccess {
    RandomAccessSubList(NSMutableArray list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    public List subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList(this, fromIndex, toIndex);
    }
}
