package com.webobjects.foundation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Reimplementation with a fix for StackOverflowError in WO 5.4.3. 
 * http://lists.apple.com/archives/webobjects-dev/2009/Nov/msg00459.html
 * 
 * @author dfrolov
 * 
 */
public class _NSWeakMutableArray extends _NSWeakMutableCollection implements Serializable {
	private static final ObjectStreamField serialPersistentFields[];
	static {
		serialPersistentFields = (new ObjectStreamField[] { new ObjectStreamField("objects", ((Object) (_NSUtilities._NoObjectArray)).getClass()) });
	}
	public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.foundation._NSWeakMutableArray");

	static final long serialVersionUID = -2060217327L;

	@SuppressWarnings("unused")
	private static final String SerializationValuesFieldKey = "objects";

	private NSMutableArray array;
	
	public _NSWeakMutableArray() {
		this(16);
	}

	public _NSWeakMutableArray(int capacity) {
		array = new NSMutableArray(capacity);
	}

	@Override
	public NSArray allObjects() {
		processQueue();
		NSMutableArray list = new NSMutableArray(array.count());
		Iterator iterator = array.iterator();
		do {
			if (!iterator.hasNext())
				break;
			Object object = ((WeakReference) iterator.next()).get();
			if (object != null)
				list.add(object);
		} while (true);
		return list.immutableClone();
	}

	@Override
	public int count() {
		return array.count();
	}

	public int indexOfObject(Object anObject) {
		processQueue();
		int c = array.count();
		for (int i = 0; i < c; i++)
			if (anObject == ((WeakReference) array.objectAtIndex(i)).get())
				return i;

		return -1;
	}

	@Override
	public Object[] objects() {
		return allObjects().objects();
	}

	@Override
	public Enumeration objectEnumerator() {
		return new _NSWeakMutableCollection._NSWeakMutableCollectionEnumerator(array.objectEnumerator());
	}

	@Override
	public Enumeration referenceEnumerator() {
		return array.objectEnumerator();
	}

	@Override
	public void addObject(Object object) {
		processQueue();
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an _NSWeakMutableArray");
		}
		array.addObject(new _NSWeakMutableCollection._NSWeakMutableCollectionReference(object, queue));
	}

	@Override
	public void addReference(WeakReference object) {
		processQueue();
		array.addObject(object);
	}

	@Override
	public void removeObject(Object object) {
		processQueue();
		if (object == null || array.count() == 0) {
			return;
		}
		array.removeObject(new _NSWeakMutableCollection._NSWeakMutableCollectionReference(object, queue));
	}

	@Override
	public void removeReference(Object object) {
		processQueue();
		array.removeObject(object);
	}

	@Override
	protected void __removeReference(Reference object) {
		array.removeObject(object);
	}

	@Override
	public void removeAllObjects() {
		array.removeAllObjects();
	}

	@Override
	public String toString() {
		processQueue();
		StringBuilder buffer = new StringBuilder(128);
		buffer.append("( ");

		Iterator iterator = array.iterator();
		do {
			if (!iterator.hasNext())
				break;
			Object object = ((WeakReference) iterator.next()).get();
			if (object == null)
				buffer.append("gc'd");
			else
				buffer.append(object.toString());
			if (iterator.hasNext())
				buffer.append(",\n");
		} while (true);
		buffer.append(" )");
		return buffer.toString();
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		Object values[] = allObjects().objects();
		fields.put("objects", values);
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();

		Object values[] = (Object[]) fields.get("objects", _NSUtilities._NoObjectArray);
		values = values != null ? values : _NSUtilities._NoObjectArray;
		int c = values.length;
		for (int i = 0; i < c; i++)
			addObject(values[i]);
	}

}