package com.webobjects.eocontrol;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation._NSUtilities;

/**
 * Reimp of the supplied class which will have some optimizations.
 */
public class _EOCheapCopyMutableArray extends NSMutableArray implements EOFaulting {

	public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.eocontrol._EOCheapCopyMutableArray");
	
	static final long serialVersionUID = -2000885307L;
	private transient EOFaultHandler _faultHandler;
	private transient _EOCheapCopyArray _immutableCopy;

	public _EOCheapCopyMutableArray() {
	}

	public _EOCheapCopyMutableArray(NSArray otherArray) {
		super(otherArray);
	}

	public _EOCheapCopyMutableArray(EOFaultHandler handler) {
		_faultHandler = handler;
	}

	public void willRead() {
		if (_faultHandler != null) {
			EOFaultHandler localHandler = _faultHandler;
			localHandler.completeInitializationOfObject(this);
			if (_faultHandler != null)
				return;
			_setMustRecomputeHash(true);
		}
	}

	public boolean isFault() {
		return _faultHandler != null;
	}

	public void clearFault() {
		_faultHandler = null;
	}

	public void turnIntoFault(EOFaultHandler handler) {
		_faultHandler = handler;
		_initializeWithCapacity(0);
	}

	public EOFaultHandler faultHandler() {
		return _faultHandler;
	}

	@Override
	public Object clone() {
		if (_faultHandler != null)
			return _faultHandler._mutableCloneForArray(this);
		return new _EOCheapCopyMutableArray(this);
	}

	@Override
	public NSMutableArray mutableClone() {
		if (_faultHandler != null) {
			return _faultHandler._mutableCloneForArray(this);
		}
		return new _EOCheapCopyMutableArray(this);
	}
	
	@Override
	public NSArray immutableClone() {
		if (_faultHandler != null)
			return _faultHandler._immutableCloneForArray(this);
		if (_immutableCopy == null)
			_immutableCopy = new _EOCheapCopyArray(this);
		return _immutableCopy;
	}

	public void _setCopy(_EOCheapCopyArray copy) {
		_immutableCopy = copy;
	}

	@Override
	protected Object[] objectsNoCopy() {
		willRead();
		return super.objectsNoCopy();
	}

	@Override
	public int count() {
		willRead();
		return super.count();
	}

	@Override
	public Object objectAtIndex(int index) {
		willRead();
		return super.objectAtIndex(index);
	}

	@Override
	public Enumeration objectEnumerator() {
		willRead();
		return super.objectEnumerator();
	}

	@Override
	public Enumeration reverseObjectEnumerator() {
		willRead();
		return super.reverseObjectEnumerator();
	}

	@Override
	public void setArray(NSArray otherArray) {
		willRead();
		super.setArray(otherArray);
		_immutableCopy = null;
	}

	@Override
	public void addObject(Object object) {
		willRead();
		super.addObject(object);
		_immutableCopy = null;
	}

	@Override
	public void addObjects(Object... objects) {
		willRead();
		super.addObjects(objects);
		_immutableCopy = null;
	}

	@Override
	public Object replaceObjectAtIndex(Object object, int index) {
		willRead();
		Object result = super.replaceObjectAtIndex(object, index);
		_immutableCopy = null;
		return result;
	}

	@Override
	public void insertObjectAtIndex(Object object, int index) {
		willRead();
		super.insertObjectAtIndex(object, index);
		_immutableCopy = null;
	}

	@Override
	public Object removeObjectAtIndex(int index) {
		willRead();
		Object result = super.removeObjectAtIndex(index);
		_immutableCopy = null;
		return result;
	}

	@Override
	public void removeAllObjects() {
		willRead();
		super.removeAllObjects();
		_immutableCopy = null;
	}

	@Override
	public void sortUsingComparator(NSComparator comparator) throws com.webobjects.foundation.NSComparator.ComparisonException {
		willRead();
		super.sortUsingComparator(comparator);
		_immutableCopy = null;
	}

	@Override
	public String toString() {
		if (isFault())
			return getClass().getName() + "[" + Integer.toHexString(System.identityHashCode(this)) + "]";
		return super.toString();
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		willRead();
		s.defaultWriteObject();
	}

	@Override
    public Iterator iterator() {
    	willRead();
    	return super.iterator();
    }

	@Override
	public ListIterator listIterator() {
		willRead();
		return super.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		willRead();
		return super.listIterator(index);
	}
	
}
