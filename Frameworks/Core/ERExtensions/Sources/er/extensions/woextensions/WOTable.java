/*
 * Copyright (c) 2000 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_LICENSE_HEADER_START@
 * 
 * Portions Copyright (c) 2000 Apple Computer, Inc.  All Rights
 * Reserved.  This file contains Original Code and/or Modifications of
 * Original Code as defined in and that are subject to the Apple Public
 * Source License Version 1.1 (the "License").  You may not use this file
 * except in compliance with the License.  Please obtain a copy of the
 * License at http://www.apple.com/publicsource and read it before using
 * this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON- INFRINGEMENT.  Please see the
 * License for the specific language governing rights and limitations
 * under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */

//package com.webobjects.woextensions;

//import com.webobjects.appserver.*;
//import com.webobjects.foundation.*;
package er.extensions.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXConstant;

/**
 * (Back port from WO 5 WOExtensions)
 */
@Deprecated
public class WOTable extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public NSArray _list;
    public int _maxColumns;
    public int currentRow;
    public int currentCol;
    public int _rowCount;
    public int _colCount;
    public int currentItemIndex;

    public WOTable(WOContext aContext)  {
        super(aContext);
        _resetInternalCaches();
    }

    @Override
    public boolean isStateless() {
        return true;
    }

    public NSArray list()  {
        if (_list==null) {
            _list = (NSArray)valueForBinding("list");
        }
        return _list;
    }

    public int maxColumns()  {
        if (_maxColumns == -1) {
            Object maxStr = valueForBinding("maxColumns");
            if (maxStr != null) {
                try {
                    _maxColumns = Integer.parseInt(maxStr.toString());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("WOTable - problem parsing int from maxColumns binding "+e);
                }
            }
            if (_maxColumns <= 0)
                _maxColumns=1;
       }
        return _maxColumns;
    }


    public int rowCount()  {
        if (_rowCount == -1) {
            _rowCount=0;
            NSArray aList = list();
            if (aList!=null) {
                int aMaxColCount = maxColumns();
                int aListCount = aList.count();
                int aRemainder = 0;
                if (aMaxColCount!=0)  {
                    _rowCount = aListCount / aMaxColCount;
                    aRemainder = aListCount % aMaxColCount;
                }
                if (aRemainder!=0) {
                    _rowCount++;
                }
            }
        }
        return _rowCount;
    }

    public int colCount()  {
        if (_colCount == -1) {
            int aMaxColumns = maxColumns();
            NSArray aList = list();
            if (currentRow < (rowCount() - 1)) {
                _colCount = aMaxColumns;
            } else {
                if (aMaxColumns!=0)
                    _colCount = aList.count() % aMaxColumns;
                if (_colCount == 0) {
                    _colCount = aMaxColumns;
                }
            }
        }
        return _colCount;
    }

    public void setCurrentRow(int newValue) {
        currentRow=newValue;
        _colCount=-1;
        currentCol=-1;
    }


    public void pushItem()  {
        NSArray aList = list();
        int index = currentCol+maxColumns()*currentRow;
        Object item = index < aList.count() ? aList.objectAtIndex(index) : null;
        setValueForBinding(item, "item");
        setValueForBinding(ERXConstant.integerForInt(currentRow), "row");
        setValueForBinding(ERXConstant.integerForInt(currentCol), "col");
        setValueForBinding(ERXConstant.integerForInt(index), "index");
        currentItemIndex++;
    }

    public void setCurrentCol(int newValue){
        currentCol=newValue;
        pushItem();
    }

    protected void _resetInternalCaches() {
        _list=null;
        _rowCount=-1;
        _colCount=-1;
        currentCol=-1;
        currentRow=-1;
        currentItemIndex = 0;
        _maxColumns = -1;
    }

    @Override
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext)  {
        _resetInternalCaches();
        super.takeValuesFromRequest(aRequest, aContext);
    }

    @Override
    public void reset() {
        _resetInternalCaches();
    }
}