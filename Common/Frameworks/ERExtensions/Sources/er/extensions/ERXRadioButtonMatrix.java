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

public class ERXRadioButtonMatrix extends WOComponent {

    public ERXRadioButtonMatrix(WOContext aContext) {
        super(aContext);
    }

    protected Object currentItem;
    protected Object _selection;
    protected Number index;
    protected String uniqueID;

    public boolean isStateless() { return true; }

    public void reset() { invalidateCaches(); }

    public void setCurrentItem(Object aValue) {
        currentItem = aValue;
        setValueForBinding(aValue, "item");
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


    public void invalidateCaches() {
        _selection=null;
        uniqueID=null;
        currentItem=null;
        index=null;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        // it is essential to call uniqueID so that the elemenent ID is set for the whole set of radio buttons
        reset();
        uniqueID();
        super.appendToResponse(aResponse, aContext);
    }

    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
        reset();
        setSelection((String)aRequest.formValueForKey(uniqueID()));
        super.takeValuesFromRequest(aRequest, aContext);
    }

    public String uniqueID() {
        if (uniqueID==null) {
            uniqueID = context().elementID();
        }
        return uniqueID;
    }    
}
