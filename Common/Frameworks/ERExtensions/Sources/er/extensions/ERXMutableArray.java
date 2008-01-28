package er.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSComparator.ComparisonException;

/**
 * Custom subclass of NSMutableArray. Implements {@link java.util.List} and can
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
