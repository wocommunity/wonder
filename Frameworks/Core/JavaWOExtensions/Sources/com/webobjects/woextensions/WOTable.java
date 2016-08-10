/*
 * WOTable.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;

public class WOTable extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected NSArray _list;
    protected int _maxColumns;
    public int currentRow;
    public int currentCol;
    protected int _rowCount;
    protected int _colCount;
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
            _list = (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("list",this);
            if (_list == null) {
                _list = NSArray.EmptyArray;
            }
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
            NSArray aList = list();
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

    public void setCurrentRow(Number newValue) {
        if (newValue!=null) {
            currentRow=newValue.intValue();
            _colCount=-1;
            currentCol=-1;
        }
    }


    public void pushItem()  {
        NSArray aList = list();
        int index = currentCol+maxColumns()*currentRow;
        Object item = index < aList.count() ? aList.objectAtIndex(index) : null;
        setValueForBinding(item, "item");
        if (canSetValueForBinding("row")) {
            setValueForBinding(Integer.valueOf(currentRow), "row");
        }
        if (canSetValueForBinding("col")) {
            setValueForBinding(Integer.valueOf(currentCol), "col");
        }
        if (canSetValueForBinding("index")) {
            setValueForBinding(Integer.valueOf(index), "index");
        }
        currentItemIndex++;
    }

    public void setCurrentCol(Number newValue){
        currentCol=newValue.intValue();
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
