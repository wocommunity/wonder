// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   _NSJavaArrayListIterator.java

/*
 * TODO: Remove when we move to WO 5.3.x. or greater. -TC
 */

package com.webobjects.foundation;

import java.util.ListIterator;
import java.util.NoSuchElementException;

// Referenced classes of package com.webobjects.foundation:
//            _NSUtilities

public class _NSJavaArrayListIterator
    implements ListIterator
{

    public _NSJavaArrayListIterator(Object array[], int count)
    {
        this(array, count, 0);
    }

    public _NSJavaArrayListIterator(Object array[], int count, int index)
    {
        _array = array;
        if(_array != null)
        {
            if(index < 0 || index > array.length)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + array.length);
            _limit = count;
            _nextIndex = index;
        } else
        {
            _limit = 0;
            _nextIndex = 0;
        }
    }

    public void add(Object element)
    {
        throw new UnsupportedOperationException("Add is not supported by com.webobjects.foundation.NSArray ListIterators");
    }

    public void set(Object element)
    {
        throw new UnsupportedOperationException("Set is not supported by com.webobjects.foundation.NSArray ListIterators");
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Remove is not supported by com.webobjects.foundation.NSArray ListIterators");
    }

    public int previousIndex()
    {
        return _nextIndex - 1;
    }

    public int nextIndex()
    {
        return _nextIndex;
    }

    public Object previous()
    {
        if(_nextIndex == 0)
        {
            throw new NoSuchElementException("Iteration does not have a previous element");
        } else
        {
            _lastReturned = _nextIndex - 1;
            _nextIndex--;
            return _array[_lastReturned];
        }
    }

    public boolean hasPrevious()
    {
        return _nextIndex != 0;
    }

    public Object next()
    {
        if(_nextIndex == _limit)
        {
            throw new NoSuchElementException("Iteration does not have a next element");
        } else
        {
            _lastReturned = _nextIndex;
            _nextIndex++;
            return _array[_lastReturned];
        }
    }

    public boolean hasNext()
    {
        return _nextIndex != _limit;
    }

    public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.foundation._NSJavaArrayListIterator");
    private Object _array[];
    private int _limit;
    private int _nextIndex;
    private int _lastReturned;

}
