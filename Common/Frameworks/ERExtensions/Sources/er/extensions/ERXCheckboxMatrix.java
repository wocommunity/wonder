/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSet;

// ported from WebScript - Corrected nil context problem.
/**
 * Works around a webscript bug.<br />
 * 
 * @binding list
 * @binding item
 * @binding selections
 * @binding maxColumns
 * @binding goingVertically" defaults="Boolean
 * @binding contentCellOtherTagString
 * @binding sortKey
 * @binding width
 * @binding cellAlign
 * @binding cellVAlign
 * @binding cellpadding
 * @binding cellspacing
 * @binding cellWidth
 * @binding relationshipName
 * @binding relationshipOwner
 * @binding tableOtherTagString
 */

public class ERXCheckboxMatrix extends ERXNonSynchronizingComponent {
    /** logging support */
    public final static Logger log = Logger.getLogger(ERXCheckboxMatrix.class);

    
    private static final Integer DEFAULT_PADDING = new Integer(0);
    private static final Integer DEFAULT_SPACING = new Integer(0);

    public ERXCheckboxMatrix(WOContext aContext) {
        super(aContext);
    }

    protected NSArray _selections;
    protected Object currentItem;
    public int index;
    public String wrapperElementID;

    public boolean isStateless() { return true; }

    public void reset() {
        invalidateCaches();
    }

    public String onClick(boolean onOff) {
        return "ERXCheckboxMatrix.checkAll(this.form, '" + wrapperElementID + "'," + (onOff ? "true" : "false") + ")";
    }

    public String selectOnClick() {
        return onClick(true);
    }

    public String deselectOnClick() {
        return onClick(false);
    }
    
    public Object currentItem() {
        setValueForBinding(currentItem, "item");
        return currentItem;
    }
    public void setCurrentItem(Object anItem) {
        currentItem = anItem;
        setValueForBinding(currentItem, "item");
    }

    public EOEnterpriseObject relationshipOwner() {
        return (EOEnterpriseObject)valueForBinding("relationshipOwner");
    }

    public String relationshipName() {
        Object o = valueForBinding("relationshipName");
        return o == null ? null : o.toString();
    }
    
    public NSArray selections() {
        if (_selections==null) {
            _selections = (NSArray)valueForBinding("selections");
        }
        return _selections;
    }

    public void setSelections(Vector v) throws IllegalAccessException, InvocationTargetException {
        NSRange r = new NSRange(0, v.size());
        setSelections(new NSArray(v, r, true));
    }

    public void takeValueForKey(Object value, String key)
    {
        try {
            super.takeValueForKey(value, key);
        } catch (java.lang.IllegalArgumentException e) {
            if (value instanceof Vector) {
                //convert the vector
                NSRange r = new NSRange(0, ((Vector)value).size());
                NSMutableArray a = new NSMutableArray((Vector)value, r, true);
                super.takeValueForKey(a, key);
                NSLog.out.appendln("done");
            } else {
                throw e;
            }
        }
    }
    
    public void setSelections(NSArray aFormValuesArray) throws IllegalAccessException, InvocationTargetException {
        if(aFormValuesArray!=null){
            // ** This is where we accept the formValues.  Kind of weird.
            NSMutableArray aSelectionsArray = new NSMutableArray();
            Enumeration anIndexEnumerator = aFormValuesArray.objectEnumerator();
            NSArray anItemList = (NSArray)valueForBinding("list");
            int anItemCount = anItemList.count();
            while (anIndexEnumerator.hasMoreElements()) {
                int anIndex = Integer.parseInt((String)anIndexEnumerator.nextElement());
                if (anIndex != -1 && anIndex < anItemCount) {
                    Object anObject = anItemList.objectAtIndex(anIndex);
                    aSelectionsArray.addObject(anObject);
                } else {
                    // ** serious problem here. Raise an exception?
                }
            }
            // dt: this can be used with a subset as array for the checkboxes.
            if (relationshipName() != null && relationshipName().length() > 0 && relationshipOwner() != null) {
                NSSet objectsToRemove = new NSSet(_selections).setBySubtractingSet(new NSSet(aSelectionsArray));
                NSSet objectsToAdd = new NSSet(aSelectionsArray).setBySubtractingSet(new NSSet(_selections));
                EOEnterpriseObject owner = relationshipOwner();
                String relname = relationshipName();
                for (Enumeration e = objectsToRemove.objectEnumerator(); e.hasMoreElements();) {
                    EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                    owner.removeObjectFromBothSidesOfRelationshipWithKey(eo, relname);
                }
                for (Enumeration e = objectsToAdd.objectEnumerator(); e.hasMoreElements();) {
                    EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                    owner.addObjectToBothSidesOfRelationshipWithKey(eo, relname);
                }
                
            } else {
                setValueForBinding(aSelectionsArray, "selections");
            }
            _selections = null;
        }
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
            String sortKey = (String)valueForBinding("sortKey");
            if(sortKey != null && sortKey.length() > 0) {
                NSMutableArray sortedList = new NSMutableArray((NSArray)valueForBinding("list"));
                ERXArrayUtilities.sortArrayWithKey(sortedList,sortKey);
                return sortedList;
            }
        }
        return (NSArray)valueForBinding("list");
    }

    public boolean isListEmpty() {
        NSArray anItemList = (NSArray)valueForBinding("list");
        return (anItemList == null || anItemList.count() == 0);
    }

    public Object cellpadding() {
        Object v = valueForBinding("cellpadding");

        if(v != null) {
            return v;
        } else {
            return DEFAULT_PADDING;
        }
    }

    public Object cellspacing() {
        Object v = valueForBinding("cellspacing");

        if(v != null) {
            return v;
        } else {
            return DEFAULT_SPACING;
        }
    }

}
