/*
 * WOTable.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import java.util.*;
import com.webobjects.foundation.*;

public class WOTable extends WOComponent {
    protected NSArray _list;
    protected int _maxColumns;
    public int currentRow;
    public int currentCol;
    protected int _rowCount;
    protected int _colCount;

    public WOTable(WOContext aContext)  {
        super(aContext);
        _resetInternalCaches();
    }

    public boolean isStateless() {
        return true;
    }

    public NSArray list()  {
        if (_list==null) {
            _list = (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("list",this);
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
        if (index < aList.count()) {
            Object item = aList.objectAtIndex(index);
            setValueForBinding(item, "item");
            setValueForBinding(new Integer(currentRow), "row");
            setValueForBinding(new Integer(currentCol), "col");
            setValueForBinding(new Integer(index), "index");
        }
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
        _maxColumns = -1;
    }

    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext)  {
        _resetInternalCaches();
        super.takeValuesFromRequest(aRequest, aContext);
    }

    public void reset() {
        _resetInternalCaches();
    }
}