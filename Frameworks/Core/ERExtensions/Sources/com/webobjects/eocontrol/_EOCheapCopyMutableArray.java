/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
package com.webobjects.eocontrol;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

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

	public Object clone() {
		if (_faultHandler != null)
			return _faultHandler._mutableCloneForArray(this);
		else
			return new _EOCheapCopyMutableArray(this);
	}

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

	protected Object[] objectsNoCopy() {
		willRead();
		return super.objectsNoCopy();
	}

	public int count() {
		willRead();
		return super.count();
	}

	public Object objectAtIndex(int index) {
		willRead();
		return super.objectAtIndex(index);
	}

	public Enumeration objectEnumerator() {
		willRead();
		return super.objectEnumerator();
	}

	public Enumeration reverseObjectEnumerator() {
		willRead();
		return super.reverseObjectEnumerator();
	}

	public void setArray(NSArray otherArray) {
		willRead();
		super.setArray(otherArray);
		_immutableCopy = null;
	}

	public void addObject(Object object) {
		willRead();
		super.addObject(object);
		_immutableCopy = null;
	}

	public void addObjects(Object objects[]) {
		willRead();
		super.addObjects(objects);
		_immutableCopy = null;
	}

	public Object replaceObjectAtIndex(Object object, int index) {
		willRead();
		Object result = super.replaceObjectAtIndex(object, index);
		_immutableCopy = null;
		return result;
	}

	public void insertObjectAtIndex(Object object, int index) {
		willRead();
		super.insertObjectAtIndex(object, index);
		_immutableCopy = null;
	}

	public Object removeObjectAtIndex(int index) {
		willRead();
		Object result = super.removeObjectAtIndex(index);
		_immutableCopy = null;
		return result;
	}

	public void removeAllObjects() {
		willRead();
		super.removeAllObjects();
		_immutableCopy = null;
	}

	public void sortUsingComparator(NSComparator comparator) throws com.webobjects.foundation.NSComparator.ComparisonException {
		willRead();
		super.sortUsingComparator(comparator);
		_immutableCopy = null;
	}

	public String toString() {
		if (isFault())
			return getClass().getName() + "[" + Integer.toHexString(System.identityHashCode(this)) + "]";
		else
			return super.toString();
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		willRead();
		s.defaultWriteObject();
	}

}
