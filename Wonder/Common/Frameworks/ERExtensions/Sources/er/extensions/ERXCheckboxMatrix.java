/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;

// ported from WebScript - Corrected nil context problem.
public class ERXCheckboxMatrix extends WOComponent {

    public ERXCheckboxMatrix(WOContext aContext) {
        super(aContext);
    }

    protected Object currentItem;
    protected int index;
    protected String wrapperElementID;
    protected NSArray _selections;

    public boolean isStateless() { return true; }

    public void reset() {
        invalidateCaches();
    }

    public Object currentItem() {
        setValueForBinding(currentItem, "item");
        return currentItem;
    }
    public void setCurrentItem(Object anItem) {
        currentItem = anItem;
        setValueForBinding(currentItem, "item");
    }

    public NSArray selections() {
        if (_selections==null) {
            _selections = (NSArray)valueForBinding("selections");
        }
        return _selections;
    }

    public void setSelections(NSArray aFormValuesArray) {
        // ** This is where we accept the formValues.  Kind of weird.
        NSMutableArray aSelectionsArray = new NSMutableArray();
        if (aFormValuesArray != null && aFormValuesArray.count() > 0) {
            Enumeration anIndexEnumerator = aFormValuesArray.objectEnumerator();
            NSArray anItemList = (NSArray)valueForBinding("list");
            int anItemCount = anItemList.count();
            while (anIndexEnumerator.hasMoreElements()) {
                int anIndex = Integer.parseInt((String)anIndexEnumerator.nextElement());
                if (anIndex < anItemCount) {
                    Object anObject = anItemList.objectAtIndex(anIndex);
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
        return selections() != null && selections().containsObject(currentItem) ? "checked" : null;
    }

    public void invalidateCaches() {
        _selections=null;
        currentItem=null;
        index=0;
        wrapperElementID=null;
    }

    public void sleep() {
        invalidateCaches();
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        // ** By setting these to nil, we allow the dictionary to change after the action and before the next cycle of this component (if the component is 	on a page which is recycled)
        invalidateCaches();
        super.appendToResponse(aResponse, aContext);
    }

    public NSArray maybeSortedList() {
        if (hasBinding("sortKey")) {
            NSMutableArray sortedList = new NSMutableArray((NSArray)valueForBinding("list"));
            ERXUtilities.sortEOsUsingSingleKey(sortedList, (String)valueForBinding("sortKey"));
            return sortedList;
        } else
            return (NSArray)valueForBinding("list");
    }

    boolean isListEmpty() {
        NSArray anItemList = (NSArray)valueForBinding("list");
        return (anItemList == null || anItemList.count() == 0);
    }
}
