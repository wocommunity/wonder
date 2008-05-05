// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   _NSJavaSetIterator.java

/*
 * TODO: Remove when we move to WO 5.3.x. or greater. -TC
 */

package com.webobjects.foundation;

import java.util.Iterator;

public class _NSJavaSetIterator
    implements Iterator
{

    public _NSJavaSetIterator(Object objects[])
    {
        index = 0;
        _objects = objects;
    }

    public boolean hasNext()
    {
        return index < _objects.length;
    }

    public Object next()
    {
        index++;
        return _objects[index - 1];
    }

    public void remove()
    {
        throw new UnsupportedOperationException("remove is not a supported operation in com.webobjects.foundation._NSJavaSetIterator");
    }

    int index;
    Object _objects[];
}
