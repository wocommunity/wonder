package er.extensions;

import java.io.*;
import java.util.*;

import com.webobjects.foundation.*;
import com.webobjects.foundation.NSComparator.*;

/**
 * Custom subclass of NSMutableArray. Implements {@see java.util.List} and can
 * be used as a EOF custom value class because it can automatically en- and
 * decode an NSMutableArray as blob into a database. NOTE: As the List
 * implementation is based on the NSMutableArray implementation, care must be
 * taken when subclassing; it is best if you use only List-methods when
 * extending List-methods and NSArray methods in other cases. Otherwise you will
 * most likely get into stack overflows. NOTE: List allows for NULL values,
 * NSMutableArray does not. Therefore you can't use NULL objects. The
 * ERPrototype name is <code>mutableArray</code>
 */
public class ERXMutableArray extends NSMutableArray implements List {
	public static final long serialVersionUID = -6581075256974648875L;

	public ERXMutableArray() {
		super();
	}

	public ERXMutableArray(Collection c) {
		super(c.toArray());
	}

	public ERXMutableArray(NSArray array) {
		super(array);
	}

	public ERXMutableArray(int i) {
		super(i);
	}

	public ERXMutableArray(Object obj) {
		super(obj);
	}

	public ERXMutableArray(Object aobj[]) {
		super(aobj);
	}

	public ERXMutableArray(Object objects[], NSRange range) {
		super(objects, range);
	}

	public ERXMutableArray(Vector vector, NSRange range, boolean flag) {
		super(vector, range, flag);
	}

	public static NSData toBlob(NSArray d) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bout);
			oos.writeObject(d);
			oos.close();
			NSData sp = new NSData(bout.toByteArray());
			return sp;
		} catch (IOException e) {
			// shouldn't ever happen, as we only write to memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public static NSData toBlob(NSMutableArray d) {
		return toBlob((NSArray)d);
	}
	
	public static NSArray fromBlob(NSData d) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes());
			ObjectInputStream ois = new ObjectInputStream(bis);
			NSArray dd = (NSArray) ois.readObject();
			ois.close();
			return dd;
		} catch (IOException e) {
			// shouldn't ever happen, as we only read from memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (ClassNotFoundException e) {
			// might happen, but it doesn't help us much to know it
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public static NSArray fromPropertyList(String arrayAsString) {
		NSArray a = (NSArray) NSPropertyListSerialization.propertyListFromString(arrayAsString);
		return new ERXMutableArray(a);
	}

	public static String toPropertyList(NSArray array) {
		return NSPropertyListSerialization.stringFromPropertyList(array);
	}

	public NSData toBlob() {
		return toBlob(this);
	}

	public String toPropertyList() {
		return toPropertyList(this);
	}

	public NSMutableArray mutableClone() {
		return new ERXMutableArray((NSArray) this);
	}

	/* overriden from the java.util.List interface */
	public boolean remove(Object o) {
		Iterator e = iterator();
		if (o == null) {
			while (e.hasNext()) {
				if (e.next() == null) {
					e.remove();
					return true;
				}
			}
		} else {
			while (e.hasNext()) {
				if (o.equals(e.next())) {
					e.remove();
					return true;
				}
			}
		}
		return false;
	}

	/* overriden from the java.util.List interface */
	public boolean containsAll(Collection c) {
		Iterator e = c.iterator();
		while (e.hasNext())
			if (!contains(e.next()))
				return false;

		return true;
	}

	/* overriden from the java.util.List interface */
	public boolean addAll(Collection c) {
		boolean modified = false;
		Iterator e = c.iterator();
		while (e.hasNext()) {
			if (add(e.next()))
				modified = true;
		}
		return modified;
	}

	/* overriden from the java.util.List interface */
	public boolean removeAll(Collection c) {
		boolean modified = false;
		Iterator e = iterator();
		while (e.hasNext()) {
			if (c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		return modified;
	}

	/* overriden from the java.util.List interface */
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

	/* overriden from the java.util.List interface */
	public void trimToSize() {
		modCount++;
		// NO-OP
	}

	/* overriden from the java.util.List interface */
	public void ensureCapacity(int minCapacity) {
		modCount++;
		// NO-OP
	}

	/* overriden from the java.util.List interface */
	public int size() {
		return count();
	}

	/* overriden from the java.util.List interface */
	public boolean isEmpty() {
		return count() == 0;
	}

	/* overriden from the java.util.List interface */
	public boolean contains(Object elem) {
		return containsObject(elem);
	}

	/* overriden from the java.util.List interface */
	public int indexOf(Object elem) {
		return indexOfObject(elem);
	}

	/* overriden from the java.util.List interface */
	public int lastIndexOf(Object elem) {
		Object[] elementData = objectsNoCopy();
		int size = count();
		if (elem == null) {
			for (int i = size - 1; i >= 0; i--)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (elem.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	/* overriden from the java.util.List interface */
	public Object[] toArray() {
		return objects();
	}

	/* overriden from the java.util.List interface */
	public Object[] toArray(Object a[]) {
		Object[] elementData = objectsNoCopy();
		int size = count();
		if (a.length < size)
			a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass()
					.getComponentType(), size);

		System.arraycopy(elementData, 0, a, 0, size);

		if (a.length > size)
			a[size] = null;

		return a;
	}

	/* overriden from the java.util.List interface */
	public Object get(int index) {
		return objectAtIndex(index);
	}

	/* overriden from the java.util.List interface */
	public Object set(int index, Object element) {
		Object oldValue = objectAtIndex(index);
		replaceObjectAtIndex(element, index);
		return oldValue;
	}

	/* overriden from the java.util.List interface */
	public boolean add(Object o) {
		addObject(o);
		return true;
	}

	/* overriden from the java.util.List interface */
	public void add(int index, Object element) {
		insertObjectAtIndex(element, index);
	}

	/* overriden from the java.util.List interface */
	public Object remove(int index) {
		Object oldValue = objectAtIndex(index);
		removeObjectAtIndex(index);
		return oldValue;
	}

	/* overriden from the java.util.List interface */
	public void clear() {
		removeAllObjects();
	}
	/* overriden from the java.util.List interface */
	public boolean addAll(int index, Collection c) {
		boolean modified = false;
		Iterator e = c.iterator();
		while (e.hasNext()) {
			add(index++, e.next());
			modified = true;
		}
		return modified;
	}

	/* overriden from the java.util.List interface */
	public Iterator iterator() {
		return new Itr();
	}

	/* overriden from the java.util.List interface */
	public ListIterator listIterator() {
		return listIterator(0);
	}
	/* overriden from the java.util.List interface */
	public ListIterator listIterator(final int index) {
		if (index < 0 || index > size())
			throw new IndexOutOfBoundsException("Index: " + index);

		return new ListItr(index);
	}

	private class Itr implements Iterator {
		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		int cursor = 0;

		/**
		 * Index of element returned by most recent call to next or previous.
		 * Reset to -1 if this element is deleted by a call to remove.
		 */
		int lastRet = -1;

		/**
		 * The modCount value that the iterator believes that the backing List
		 * should have. If this expectation is violated, the iterator has
		 * detected concurrent modification.
		 */
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
				ERXMutableArray.this.remove(lastRet);
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
				ERXMutableArray.this.set(lastRet, o);
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}

		public void add(Object o) {
			checkForComodification();

			try {
				ERXMutableArray.this.add(cursor++, o);
				lastRet = -1;
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}
	}
	/* overriden from the java.util.List interface */
	public List subList(int fromIndex, int toIndex) {
		return (this instanceof RandomAccess ? new RandomAccessSubList(this,
				fromIndex, toIndex) : new SubList(this, fromIndex, toIndex));
	}
	/* overriden from the java.util.List interface */
	protected void removeRange(int fromIndex, int toIndex) {
		ListIterator it = listIterator(fromIndex);
		for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
			it.next();
			it.remove();
		}
	}
	/* overriden from the java.util.List interface */
	protected transient int modCount = 0;

	public String[] toStringArray() {
		return ERXArrayUtilities.toStringArray(this);
	}

	/**
	 * Simple thread safe wrapper. May or may not be correct, but it doesn't
	 * matter as you will never, *ever* call this directly, but call <code>
	 * ERXMutableArray.synchronizedArray();
	 * </code> instead and we will fix all the bugs in due time.
	 * @author ak
	 * 
	 */
	public static class ThreadSafeArray extends ERXMutableArray {

		public ThreadSafeArray(NSArray array) {
			super((NSMutableArray) array);
		}

		public synchronized void _moveObjectAtIndexToIndex(int sourceIndex, int destIndex) {
			super._moveObjectAtIndexToIndex(sourceIndex, destIndex);
		}

		public synchronized void addObject(Object object) {
			super.addObject(object);
		}

		public synchronized void addObjects(Object[] objects) {
			super.addObjects(objects);
		}

		public synchronized void addObjectsFromArray(NSArray otherArray) {
			super.addObjectsFromArray(otherArray);
		}

		public synchronized Object clone() {
			return super.clone();
		}

		public synchronized NSArray immutableClone() {
			return super.immutableClone();
		}

		public synchronized void insertObjectAtIndex(Object object, int index) {
			super.insertObjectAtIndex(object, index);
		}

		public synchronized void removeAllObjects() {
			super.removeAllObjects();
		}

		public synchronized boolean removeIdenticalObject(Object object, NSRange range) {
			return super.removeIdenticalObject(object, range);
		}

		public synchronized boolean removeIdenticalObject(Object object) {
			return super.removeIdenticalObject(object);
		}

		public synchronized Object removeLastObject() {
			return super.removeLastObject();
		}

		public synchronized boolean removeObject(Object object, NSRange range) {
			return super.removeObject(object, range);
		}

		public synchronized boolean removeObject(Object object) {
			return super.removeObject(object);
		}

		public synchronized Object removeObjectAtIndex(int index) {
			return super.removeObjectAtIndex(index);
		}

		public synchronized void removeObjects(Object[] objects) {
			super.removeObjects(objects);
		}

		public synchronized void removeObjectsInArray(NSArray otherArray) {
			super.removeObjectsInArray(otherArray);
		}

		public synchronized void removeObjectsInRange(NSRange range) {
			super.removeObjectsInRange(range);
		}

		public synchronized Object replaceObjectAtIndex(Object object, int index) {
			return super.replaceObjectAtIndex(object, index);
		}

		public synchronized void replaceObjectsInRange(NSRange range, NSArray otherArray, NSRange otherRange) {
			super.replaceObjectsInRange(range, otherArray, otherRange);
		}

		public synchronized void setArray(NSArray otherArray) {
			super.setArray(otherArray);
		}

		public synchronized void sortUsingComparator(NSComparator comparator) throws ComparisonException {
			super.sortUsingComparator(comparator);
		}

		protected synchronized void _ensureCapacity(int capacity) {
			super._ensureCapacity(capacity);
		}

		protected synchronized void _initializeWithCapacity(int capacity) {
			super._initializeWithCapacity(capacity);
		}

		protected synchronized boolean _mustRecomputeHash() {
			return super._mustRecomputeHash();
		}

		protected synchronized void _setMustRecomputeHash(boolean change) {
			super._setMustRecomputeHash(change);
		}

		public synchronized int _shallowHashCode() {
			return super._shallowHashCode();
		}

		public synchronized NSArray arrayByAddingObject(Object object) {
			return super.arrayByAddingObject(object);
		}

		public synchronized NSArray arrayByAddingObjectsFromArray(NSArray otherArray) {
			return super.arrayByAddingObjectsFromArray(otherArray);
		}

		public synchronized ArrayList arrayList() {
			Object objects[] = objectsNoCopy();
			ArrayList list = new ArrayList(objects.length);
			for(int i = 0; i < objects.length; i++) {
				list.add(objects[i]);
			}
			return list;
		}

		public synchronized Class classForCoder() {
			return super.classForCoder();
		}

		public synchronized String componentsJoinedByString(String separator) {
			return super.componentsJoinedByString(separator);
		}

		public synchronized boolean containsObject(Object object) {
			return super.containsObject(object);
		}

		public synchronized int count() {
			return super.count();
		}

		public synchronized void encodeWithCoder(NSCoder coder) {
			super.encodeWithCoder(coder);
		}

		public synchronized boolean equals(Object object) {
			return super.equals(object);
		}

		public synchronized Object firstObjectCommonWithArray(NSArray otherArray) {
			return super.firstObjectCommonWithArray(otherArray);
		}

		public synchronized int hashCode() {
			return super.hashCode();
		}

		public synchronized int indexOfIdenticalObject(Object object) {
			return super.indexOfIdenticalObject(object);
		}

		public synchronized int indexOfIdenticalObject(Object object, NSRange range) {
			return super.indexOfIdenticalObject(object, range);
		}

		public synchronized int indexOfObject(Object object) {
			return super.indexOfObject(object);
		}

		public synchronized int indexOfObject(Object object, NSRange range) {
			return super.indexOfObject(object, range);
		}

		public synchronized boolean isEqualToArray(NSArray otherArray) {
			return super.isEqualToArray(otherArray);
		}

		public synchronized Object lastObject() {
			return super.lastObject();
		}

		public synchronized void makeObjectsPerformSelector(NSSelector selector, Object[] parameters) {
			super.makeObjectsPerformSelector(selector, parameters);
		}

		public synchronized Object objectAtIndex(int index) {
			return super.objectAtIndex(index);
		}

		public synchronized Enumeration objectEnumerator() {
			return super.objectEnumerator();
		}

		public synchronized Object[] objects() {
			return super.objects();
		}

		public synchronized Object[] objects(NSRange range) {
			return super.objects(range);
		}

		protected synchronized Object[] objectsNoCopy() {
			return super.objectsNoCopy();
		}

		public synchronized Enumeration reverseObjectEnumerator() {
			return super.reverseObjectEnumerator();
		}

		public synchronized NSArray sortedArrayUsingComparator(NSComparator comparator) throws ComparisonException {
			return super.sortedArrayUsingComparator(comparator);
		}

		public synchronized NSArray subarrayWithRange(NSRange range) {
			return super.subarrayWithRange(range);
		}

		public synchronized void takeValueForKey(Object value, String key) {
			super.takeValueForKey(value, key);
		}

		public synchronized void takeValueForKeyPath(Object value, String keyPath) {
			super.takeValueForKeyPath(value, keyPath);
		}

		public synchronized String toString() {
			return super.toString();
		}

		public synchronized Object valueForKey(String key) {
			return super.valueForKey(key);
		}

		public synchronized Object valueForKeyPath(String keyPath) {
			return super.valueForKeyPath(keyPath);
		}

		public synchronized Vector vector() {
			return super.vector();
		}
	}
	
	public static NSArray synchronizedArray() {
		return new ThreadSafeArray(new ERXMutableArray());
	}
	
	public static NSArray synchronizedArray(NSArray array) {
		if(!(array instanceof NSMutableArray)) {
			return array;
		}
		return new ThreadSafeArray((NSMutableArray)array);
	}

}

class SubList extends ERXMutableArray {
	private ERXMutableArray l;

	private int offset;

	private int size;

	private int expectedModCount;

	SubList(ERXMutableArray list, int fromIndex, int toIndex) {
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
	RandomAccessSubList(ERXMutableArray list, int fromIndex, int toIndex) {
		super(list, fromIndex, toIndex);
	}

	public List subList(int fromIndex, int toIndex) {
		return new RandomAccessSubList(this, fromIndex, toIndex);
	}
}