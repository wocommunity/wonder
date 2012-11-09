package com.webobjects.eocontrol;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation._NSUtilities;

/**
 * Reimplementation which fixes a bug in NSArray, where the iterator methods won't trigger the fault.
 * 
 * @author ak
 */
public class _EOCheapCopyArray extends NSArray implements EOFaulting {

	public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.eocontrol._EOCheapCopyArray");

	static final long serialVersionUID = -1712494429L;

	private transient EOFaultHandler _faultHandler;

	public _EOCheapCopyArray() {
	}

	public _EOCheapCopyArray(NSArray otherArray) {
		super(otherArray);
	}

	public void _setArray(NSArray otherArray) {
		willRead();
		int count = otherArray.count();
		_objects = null;
		if (count > 0) {
			_objects = new Object[count];
			System.arraycopy(((otherArray.objects())), 0, ((_objects)), 0, count);
		}
		_setMustRecomputeHash(true);
	}

	public void clearFault() {
		_faultHandler = null;
	}

	@Override
	public int count() {
		willRead();
		return super.count();
	}

	public EOFaultHandler faultHandler() {
		return _faultHandler;
	}

	public boolean isFault() {
		return _faultHandler != null;
	}

	@Override
	public NSMutableArray mutableClone() {
		if (_faultHandler != null) {
			return _faultHandler._mutableCloneForArray(this);
		}
		return super.mutableClone();
	}
	
	@Override
    public NSArray immutableClone() {
        if (_faultHandler != null) {
            return _faultHandler._immutableCloneForArray(this);
        }

        return new _EOCheapCopyArray(this);
    }
        
	@Override
	public Object get(int index) {
		willRead();
		return super.get(index);
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
	public int hashCode() {
		// willRead();
		return super.hashCode();
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
	
	@Override
	protected Object[] objectsNoCopy() {
		willRead();
		return super.objectsNoCopy();
	}

	@Override
	public Enumeration reverseObjectEnumerator() {
		willRead();
		return super.reverseObjectEnumerator();
	}
	
	@Override
	public int lastIndexOf(Object element) {
		willRead();
		return super.lastIndexOf(element);
	}
    
	@Override
	public String toString() {
		if (isFault()) {
			return getClass().getName() + "["
					+ Integer.toHexString(System.identityHashCode(this)) + "]";
		}
		return super.toString();
	}

	public void turnIntoFault(EOFaultHandler handler) {
		_faultHandler = handler;
		_initializeWithCapacity(0);
	}

	public void willRead() {
		if (_faultHandler != null) {
			EOFaultHandler localHandler = _faultHandler;
			localHandler.completeInitializationOfObject(this);
			if (_faultHandler != null) {
				return;
			}
			_setMustRecomputeHash(true);
		}
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		willRead();
		s.defaultWriteObject();
	}
}
