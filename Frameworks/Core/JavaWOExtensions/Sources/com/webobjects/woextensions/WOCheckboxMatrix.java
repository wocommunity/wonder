/*
 * WOCheckboxMatrix.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

// ** This uses a technique of having the WORepetition compute the form values for us.  This is a bit strange to have the Repetition having form values.  It may well be clearer to simply use takeValuesFromRequest... in here and not use this trick.  The ability to ask an element for its elementID seems logical and useful (as we use it for the umbrealla name here).  Of course, we could have this on the component just as easily, and this may be clearer.  However, if there is a repetition with a repetition in it, then the component's elementID isn't enough.

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class WOCheckboxMatrix extends WOComponent {

    public Object currentItem;
    public int index;
    public String wrapperElementID;
    protected NSArray _selections = null;

    public WOCheckboxMatrix(WOContext aContext) {
        super(aContext);
    }
    
    public boolean isStateless() {
        return true;
    }
    
    public void setCurrentItem(Object anItem) {
        currentItem = anItem;
        setValueForBinding(currentItem, "item");
    }

    public NSArray selections() {
        if (_selections == null) {
            _selections = (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("selections",this);
            if (_selections == null) {
                _selections = NSArray.EmptyArray;
            }
        }
        return _selections;
    }

    public void setSelections(NSArray aFormValuesArray) {
        // ** This is where we accept the formValues.  Kind of weird.
        NSMutableArray aSelectionsArray = new NSMutableArray();
        if (aFormValuesArray != null) {
            Number anIndex = null;
            Enumeration anIndexEnumerator = aFormValuesArray.objectEnumerator();
            NSArray anItemList = (NSArray)_WOJExtensionsUtil.valueForBindingOrNull("list",this);
            if (anItemList == null) {
                anItemList = NSArray.EmptyArray;
            }
            int anItemCount = anItemList.count();
            while (anIndexEnumerator.hasMoreElements()) {
                anIndex = new Integer((String)anIndexEnumerator.nextElement());
                int i = anIndex.intValue();
                if (i < anItemCount) {
                    Object anObject = anItemList.objectAtIndex(i);
                    aSelectionsArray.addObject(anObject);
                } else {
                    // ** serious problem here. Raise an exception?
                }
            }
        }
        setValueForBinding(aSelectionsArray, "selections");
        _selections = null;
    }

    public String isCurrentItemChecked() {
        if ((selections() != null) && selections().containsObject(currentItem)) {
            return "checked";
        }
        return null;
    }


    protected void _invalidateCaches() {
        _selections=null;
        currentItem = null;
    }

    public void reset()  {
        _invalidateCaches();
    }

    public Object nullValue() {
    	return null;
    }
}
