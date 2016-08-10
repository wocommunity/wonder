/*
 * _ClassNameComparator.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSSelector;

/**
 * <span class="ja">
 *  このクラスは二つの EOEvent を比較する為に使用します。
 *  woextensions の private クラス
 * </span>
 */
public class _ClassNameComparator extends NSComparator {
    protected boolean _compareAscending;

    public _ClassNameComparator(NSSelector comparator) {
        super();
        _compareAscending = (comparator == EOSortOrdering.CompareAscending) ;
    }

    @Override
    public int compare(Object c1, Object c2) throws NSComparator.ComparisonException {
        if (!(c1 instanceof Class) || !(c2 instanceof Class) || (c1 == null) || (c2 == null))
            throw new NSComparator.ComparisonException("<"+getClass().getName()+" Unable to compare classes. Either one of the arguments is not a Class or is null. Comparison was made with " + c1 + " and " + c2 + "." );

        Class class1, class2;
        class1 = (Class)c1;
        class2 = (Class)c2;
        
        int result = class1.getName().compareTo(class2.getName());
        if (result == 0) {
            return result;
        }
        if (!_compareAscending) {
            result = 0 - result;
        }
        return result > 0 ? 1 : -1;
    }
}
