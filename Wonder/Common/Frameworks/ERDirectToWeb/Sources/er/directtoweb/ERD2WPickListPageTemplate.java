/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

import er.extensions.*;
import java.util.*;


public class ERD2WPickListPageTemplate extends ERD2WListPage implements ERDBranchInterface {

     public ERD2WPickListPageTemplate(WOContext context) {super(context);}
    /*
     FIXME:
     1/ we are missing a formal protocol for selectedObject
     2/ this component uses a hidden trick:
         - clicking on the select button uses next page delegate
         - the cancel button uses the next page

    */

    public NSDictionary aBranch;
    public NSMutableArray selectedObjects = new NSMutableArray();
    protected String dummy;

    public boolean hasForm() {
        Object hasForm = d2wContext().valueForKey("hasForm");
        return hasForm == null ? true : ((Integer)hasForm).intValue() != 0;
    }

    public boolean showCancel() { return nextPage()!=null; }
    
    public boolean checked() { return selectedObjects.containsObject(object()); }
    public void setChecked(boolean newChecked) {
        if (newChecked) {
            if (!selectedObjects.containsObject(object()))
                selectedObjects.addObject(object());
        } else
            selectedObjects.removeObject(object());
    }
    
    public WOComponent selectAll() {
        selectedObjects.removeAllObjects();
        NSArray list=displayGroup().qualifier()==null ? displayGroup().allObjects() :
            EOQualifier.filteredArrayWithQualifier(displayGroup().allObjects(), displayGroup().qualifier());
        for (Enumeration e=list.objectEnumerator();e.hasMoreElements();) { selectedObjects.addObject(e.nextElement()); }
        return null;
    }
    public WOComponent unselectAll() {
        selectedObjects.removeAllObjects();
        return null;
    }
    
    public boolean isEntityInspectable() {
        Integer isEntityInspectable=(Integer)d2wContext().valueForKey("isEntityInspectable");
        return isEntityReadOnly() && (isEntityInspectable!=null && isEntityInspectable.intValue()!=0);
    }

    public String branchName() { return (String)aBranch.valueForKey("branchName"); }

    public WOComponent printerFriendlyVersion() {
        D2WListPage result=(D2WListPage)ERDirectToWeb.printerFriendlyPageForD2WContext(d2wContext(),session());
        result.setDataSource(dataSource());
        result.displayGroup().setSortOrderings(displayGroup().sortOrderings());
        result.displayGroup().setNumberOfObjectsPerBatch(displayGroup().allObjects().count());
        result.displayGroup().updateDisplayedObjects();
        return result;
    }


    public Boolean _singleSelection;
    public boolean singleSelection() {
        if (_singleSelection==null) {
            _singleSelection=ERXUtilities.booleanValue(d2wContext().valueForKey("singleSelection")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _singleSelection.booleanValue();
    }
    
    public String selectionWidgetName() {
        return singleSelection() ? "WORadioButton" : "WOCheckBox";
    }
    
}
