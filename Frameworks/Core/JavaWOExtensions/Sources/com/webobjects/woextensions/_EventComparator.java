/*
 * _EventComparator.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.eocontrol.EOEvent;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSSelector;

/**
 * <span class="ja">
 *  このクラスは二つの EOEvent を比較する為に使用します。
 *  woextensions の private クラス
 * </span>
 */
public class _EventComparator extends NSComparator {
    protected boolean _compareAscending;
    protected WOEventDisplayPage _controller;

    public _EventComparator(NSSelector comparator, WOEventDisplayPage ctrl) {
        super();
        _compareAscending = (comparator == EOSortOrdering.CompareAscending) ;
        _controller = ctrl;
    }

    @Override
    public int compare(Object e1, Object e2) throws NSComparator.ComparisonException {
        if (!(e1 instanceof EOEvent) || !(e2 instanceof EOEvent) || (e1 == null) || (e2 == null))
            throw new NSComparator.ComparisonException("<"+getClass().getName()+" Unable to compare EOEvents. Either one of the arguments is not a EOEvent or is null. Comparison was made with " + e1 + " and " + e2 + "." );

        int result = ((EOEvent)e1)._compareDuration((EOEvent)e2);
        return _compareAscending ? result : 0 - result;  
    }
}
