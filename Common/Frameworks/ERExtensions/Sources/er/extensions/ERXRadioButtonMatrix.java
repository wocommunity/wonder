/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

/**
 * Radio button list with lots of more options.<br />
 * 
 * @binding list
 * @binding uniqueID
 * @binding item
 * @binding selection
 * @binding maxColumns
 * @binding cellpadding
 * @binding cellspacing
 * @binding width
 * @binding cellAlign
 * @binding cellVAlign
 * @binding cellWidth
 * @binding tableOtherTagString
 */

public class ERXRadioButtonMatrix extends ERXStatelessComponent {

    private static final Integer DEFAULT_PADDING = ERXConstant.ZeroInteger;
    private static final Integer DEFAULT_SPACING = ERXConstant.ZeroInteger;

    public ERXRadioButtonMatrix(WOContext aContext) {
        super(aContext);
    }

    protected Object currentItem;
    protected Object _selection;
    protected Number index;
    protected Object uniqueID;

    public void reset() {
        invalidateCaches();
    }

    public Object currentItem() {
        return currentItem;
    }

    public void setCurrentItem(Object aValue) {
        currentItem = aValue;
        setValueForBinding(aValue, "item");
    }

    public Number index() {
        return index;
    }

    public void setIndex(Number newIndex) {
        index = newIndex;
    }

    public Object selection() {
        if (_selection == null) {
            // ** only pull this one time
            _selection = valueForBinding("selection");
        }

        return _selection;
    }

    public void setSelection(String anIndex) {
        if (anIndex != null) {
            // ** push the selection to the parent
            NSArray anItemList = (NSArray)valueForBinding("list");
            Object aSelectedObject = anItemList.objectAtIndex(Integer.parseInt(anIndex));
            setValueForBinding(aSelectedObject, "selection");
            // ** and force it to be pulled if there's a next time.
        }

        _selection = null;
    }

    public String isCurrentItemSelected() {
        if (selection()!=null && selection().equals(currentItem)) {
            return "checked";
        }

        return "";
    }

    public void awake() {
        super.awake();
        uniqueID = valueForBinding("uniqueID");
        if(uniqueID == null) {
            uniqueID = context().elementID();
        }
    }

    public void invalidateCaches() {
        _selection=null;
        currentItem=null;
        index=null;
        uniqueID=null;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        super.appendToResponse(aResponse, aContext);
    }

    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
        setSelection(aRequest.stringFormValueForKey(uniqueID()));
        super.takeValuesFromRequest(aRequest, aContext);
    }
    
    public String uniqueID() {
        return uniqueID.toString();
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
