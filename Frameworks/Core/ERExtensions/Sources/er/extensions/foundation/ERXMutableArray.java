package er.extensions.foundation;

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
import com.webobjects.foundation.NSComparator.ComparisonException;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;

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
public class ERXMutableArray<E> extends NSMutableArray<E> implements List<E> {
	public static final long serialVersionUID = -6581075256974648875L;

	public ERXMutableArray() {
		super();
	}

	public ERXMutableArray(Collection<? extends E> c) {
		super((E[])c.toArray());
	}

	public ERXMutableArray(NSArray<? extends E> array) {
		super(array);
	}

	public ERXMutableArray(int i) {
		super(i);
	}

	public ERXMutableArray(E obj) {
		super(obj);
	}

	public ERXMutableArray(E aobj[]) {
		super(aobj);
	}

	public ERXMutableArray(E objects[], NSRange range) {
		super(objects, range);
	}

	public ERXMutableArray(Vector<? extends E> vector, NSRange range, boolean flag) {
		super(vector, range, flag);
	}

	public static NSData toBlob(NSArray<?> d) {
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bout)) {
			oos.writeObject(d);
			NSData sp = new NSData(bout.toByteArray());
			return sp;
		} catch (IOException e) {
			// shouldn't ever happen, as we only write to memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public static NSData toBlob(NSMutableArray<?> d) {
		return toBlob((NSArray<?>) d);
	}
	
	@SuppressWarnings("unchecked")
	public static NSArray fromBlob(NSData d) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes()); ObjectInputStream ois = new ERXMappingObjectStream(bis)) {
			NSArray<?> dd = (NSArray<?>) ois.readObject();
			return dd;
		} catch (IOException e) {
			// shouldn't ever happen, as we only read from memory
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (ClassNotFoundException e) {
			// might happen, but it doesn't help us much to know it
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static NSArray fromPropertyList(String arrayAsString) {
		NSArray<?> a = (NSArray<?>) NSPropertyListSerialization.propertyListFromString(arrayAsString);
		return new ERXMutableArray<Object>(a);
	}

	public static String toPropertyList(NSArray<?> array) {
		return NSPropertyListSerialization.stringFromPropertyList(array);
	}

	public NSData toBlob() {
		return toBlob(this);
	}

	public String toPropertyList() {
		return toPropertyList(this);
	}

	@Override
	public ERXMutableArray<E> mutableClone() {
		return new ERXMutableArray<E>(this);
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
	public static class ThreadSafeArray<V> extends ERXMutableArray<V> {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		public ThreadSafeArray(NSArray<? extends V> array) {
			super(array);
		}

		@Override
		public synchronized void _moveObjectAtIndexToIndex(int sourceIndex, int destIndex) {
			super._moveObjectAtIndexToIndex(sourceIndex, destIndex);
		}

		@Override
		public synchronized void addObject(Object object) {
			super.addObject((V) object);
		}

		@Override
		public synchronized void addObjects(Object... objects) {
			super.addObjects((V[])objects);
		}

		@Override
		public synchronized void addObjectsFromArray(NSArray<? extends V> otherArray) {
			super.addObjectsFromArray(otherArray);
		}

		@Override
		public synchronized Object clone() {
			return new ThreadSafeArray<V>(this);
		}

		@Override
		public synchronized NSArray<V> immutableClone() {
			return super.immutableClone();
		}

		@Override
		public synchronized void insertObjectAtIndex(V object, int index) {
			super.insertObjectAtIndex(object, index);
		}

		@Override
		public synchronized void removeAllObjects() {
			super.removeAllObjects();
		}

		@Override
		public synchronized boolean removeIdenticalObject(Object object, NSRange range) {
			return super.removeIdenticalObject(object, range);
		}

		@Override
		public synchronized boolean removeIdenticalObject(Object object) {
			return super.removeIdenticalObject(object);
		}

		@Override
		public synchronized V removeLastObject() {
			return super.removeLastObject();
		}

		@Override
		public synchronized boolean removeObject(Object object, NSRange range) {
			return super.removeObject(object, range);
		}

		@Override
		public synchronized boolean removeObject(Object object) {
			return super.removeObject(object);
		}

		@Override
		public synchronized V removeObjectAtIndex(int index) {
			return super.removeObjectAtIndex(index);
		}

		@Override
		public synchronized void removeObjects(Object... objects) {
			super.removeObjects(objects);
		}

		@Override
		public synchronized void removeObjectsInArray(NSArray<?> otherArray) {
			super.removeObjectsInArray(otherArray);
		}

		@Override
		public synchronized void removeObjectsInRange(NSRange range) {
			super.removeObjectsInRange(range);
		}

		@Override
		public synchronized V replaceObjectAtIndex(V object, int index) {
			return super.replaceObjectAtIndex(object, index);
		}

		@Override
		public synchronized void replaceObjectsInRange(NSRange range, NSArray otherArray, NSRange otherRange) {
			super.replaceObjectsInRange(range, otherArray, otherRange);
		}

		@Override
		public synchronized void setArray(NSArray<? extends V> otherArray) {
			super.setArray(otherArray);
		}

		@Override
		public synchronized void sortUsingComparator(NSComparator comparator) throws ComparisonException {
			super.sortUsingComparator(comparator);
		}

		@Override
		public synchronized int _shallowHashCode() {
			return super._shallowHashCode();
		}

		@Override
		public synchronized NSArray<V> arrayByAddingObject(V object) {
			return super.arrayByAddingObject(object);
		}

		@Override
		public synchronized NSArray<V> arrayByAddingObjectsFromArray(NSArray<? extends V> otherArray) {
			return super.arrayByAddingObjectsFromArray(otherArray);
		}

		@Override
		public synchronized ArrayList<V> arrayList() {
			V[] objects = objectsNoCopy();
			ArrayList<V> list = new ArrayList<V>(objects.length);
			for(int i = 0; i < objects.length; i++) {
				list.add(objects[i]);
			}
			return list;
		}

		@Override
		@SuppressWarnings("unchecked")
		public synchronized Class classForCoder() {
			return super.classForCoder();
		}

		@Override
		public synchronized String componentsJoinedByString(String separator) {
			return super.componentsJoinedByString(separator);
		}

		@Override
		public synchronized boolean containsObject(Object object) {
			return super.containsObject(object);
		}

		@Override
		public synchronized int count() {
			return super.count();
		}

		@Override
		public synchronized void encodeWithCoder(NSCoder coder) {
			super.encodeWithCoder(coder);
		}

		@Override
		public synchronized boolean equals(Object object) {
			return super.equals(object);
		}

		@Override
		public synchronized V firstObjectCommonWithArray(NSArray<?> otherArray) {
			return super.firstObjectCommonWithArray(otherArray);
		}

		@Override
		public synchronized int hashCode() {
			return super.hashCode();
		}

		@Override
		public synchronized int indexOfIdenticalObject(Object object) {
			return super.indexOfIdenticalObject(object);
		}

		@Override
		public synchronized int indexOfIdenticalObject(Object object, NSRange range) {
			return super.indexOfIdenticalObject(object, range);
		}

		@Override
		public synchronized int indexOfObject(Object object) {
			return super.indexOfObject(object);
		}

		@Override
		public synchronized int indexOfObject(Object object, NSRange range) {
			return super.indexOfObject(object, range);
		}

		@Override
		public synchronized boolean isEqualToArray(NSArray otherArray) {
			return super.isEqualToArray(otherArray);
		}

		@Override
		public synchronized V lastObject() {
			return super.lastObject();
		}

		@Override
		public synchronized void makeObjectsPerformSelector(NSSelector selector, Object... parameters) {
			super.makeObjectsPerformSelector(selector, parameters);
		}

		@Override
		public synchronized V objectAtIndex(int index) {
			return super.objectAtIndex(index);
		}

		@Override
		public synchronized Enumeration<V> objectEnumerator() {
			return super.objectEnumerator();
		}

		@Override
		public synchronized V[] objects() {
			return (V[])super.objects();
		}

		@Override
		public synchronized Object[] objects(NSRange range) {
			return super.objects(range);
		}

		@Override
		protected synchronized V[] objectsNoCopy() {
			return (V[]) super.objectsNoCopy();
		}

		@Override
		public synchronized Enumeration<V> reverseObjectEnumerator() {
			return super.reverseObjectEnumerator();
		}

		@Override
		public synchronized NSArray<V> sortedArrayUsingComparator(NSComparator comparator) throws ComparisonException {
			return super.sortedArrayUsingComparator(comparator);
		}

		@Override
		public synchronized NSArray<V> subarrayWithRange(NSRange range) {
			return super.subarrayWithRange(range);
		}

		@Override
		public synchronized void takeValueForKey(Object value, String key) {
			super.takeValueForKey(value, key);
		}

		@Override
		public synchronized void takeValueForKeyPath(Object value, String keyPath) {
			super.takeValueForKeyPath(value, keyPath);
		}

		@Override
		public synchronized String toString() {
			return super.toString();
		}

		@Override
		public synchronized Object valueForKey(String key) {
			return super.valueForKey(key);
		}

		@Override
		public synchronized Object valueForKeyPath(String keyPath) {
			return super.valueForKeyPath(keyPath);
		}

		@Override
		public synchronized Vector<V> vector() {
			return super.vector();
		}
	}
	
	public static <T> NSMutableArray<T> synchronizedArray() {
		return new ThreadSafeArray<T>(new ERXMutableArray<T>());
	}
	
	public static <T> NSArray<T> synchronizedArray(NSArray<T> array) {
		if(!(array instanceof NSMutableArray)) {
			return array;
		}
		return new ThreadSafeArray(array);
	}

	public static <T> NSMutableArray<T> synchronizedArray(NSMutableArray<T> array) {
		return new ThreadSafeArray<T>(array);
	}

}
