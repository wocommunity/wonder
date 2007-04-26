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
import com.webobjects.directtoweb.ConfirmPageInterface;

// FIXME: Need a formal protocol for cancel vs. selection.
public class ERD2WPickListPage extends ERD2WListPage implements ERDPickPageInterface {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WPickListPage.class);

    /** holds the selected objects */
    protected NSMutableArray selectedObjects = new NSMutableArray();

    /**
     * IE sometimes won't submit the form if it only has checkboxes, we bind
     * this to a WOHiddenField.
     */
    public String dummy;

    /** Caches if we are in single selection mode. */
    public Boolean _singleSelection;

    /**
     * Public constructor.
     * 
     * @param context
     *            current context
     */
    public ERD2WPickListPage(WOContext context) {
        super(context);
    }

    /**
     * Determines if the cancel button should be shown.
     * 
     * @return if we have a nextPage set
     */
    public boolean showCancel() {
        return nextPage() != null;
    }

    public boolean checked() {
        return selectedObjects.containsObject(object());
    }

    public void setChecked(boolean newChecked) {
        if (newChecked) {
            if (!selectedObjects.containsObject(object())) selectedObjects.addObject(object());
        } else
            selectedObjects.removeObject(object());
    }

    //public WOComponent cancelPage()
    //public void setCancelPage(WOComponent cp);

    public NSArray selectedObjects() {
        return selectedObjects;
    }

    public void setSelectedObjects(NSArray selectedObjects) {
        this.selectedObjects = selectedObjects.mutableClone();
    }

    // FIXME: Not using cancel page at the moment. Only matters if not using a
    // branch delegate.
    private WOComponent _cancelPage;

    public WOComponent cancelPage() {
        return _cancelPage;
    }

    public void setCancelPage(WOComponent cp) {
        _cancelPage = cp;
    }

    // FIXME: Not sure if this makes sense to
    public void setChoices(NSArray choices) {
    }

    // Called by ERD2WListPage before the display group is updated
    protected void willUpdate() {
        super.willUpdate();
    }
    
    // Called by ERD2WListPage after the display group is updated
    protected void didUpdate() {
        super.didUpdate();
        // update our selection, so that we don't have any objects selected that are not visible on any page
        if ( selectedObjects().count() > 0 && filteredObjects().count() > 0 ) {
            // once intersectingElements() is more efficient, we can use that
            //int preIntersectCount = selectedObjects().count();
            //NSArray newSelection = ERXArrayUtilities.intersectingElements(selectedObjects(), filteredObjects());            
            NSSet selectedSet = new NSSet (selectedObjects());
            NSSet filteredSet = new NSSet (filteredObjects());
            int preIntersectCount = selectedSet.count();
            NSSet newSelection = selectedSet.setByIntersectingSet(filteredSet);
            if (newSelection.count() != preIntersectCount) {
                setSelectedObjects(newSelection.allObjects());
            }
        }
    }
    
    /**
     * The display group's objects, filtered by the display group qualifier (if any)
     */
    public NSArray filteredObjects() {
        return displayGroup().qualifier() == null ? displayGroup().allObjects() : EOQualifier.filteredArrayWithQualifier(displayGroup().allObjects(), displayGroup().qualifier());
    }
   
    public WOComponent  selectAll() {
	return _doActionWithPossibleConfirmation(filteredObjects().count(), "_finishSelectAll");
    }
 
    protected WOComponent _doActionWithPossibleConfirmation(int selectAllCount, String actionName) {
	int selectAllWarnThreshold=ERXValueUtilities.intValue(d2wContext().valueForKey("confirmSelectAllThreshold"));

        if (selectAllWarnThreshold>0 && selectAllCount>selectAllWarnThreshold) {
            ConfirmPageInterface confirmPage = (ConfirmPageInterface)D2W.factory().pageForConfigurationNamed("ConfirmSelectAll", session());

            // delegate
            ConfirmApprovalDelegate delegate = new ConfirmApprovalDelegate();
            delegate.nextPage = context().page();
            delegate.pickPage = this;
            delegate.actionName = actionName;
 
            confirmPage.setConfirmDelegate(delegate);
            confirmPage.setCancelDelegate(new ERDPageDelegate(context().page()));
            confirmPage.setMessage("Are you sure you want to select all "+selectAllCount+" records");

            return (WOComponent) confirmPage;
        }

        return (WOComponent) valueForKey(actionName);
    }

    public static class ConfirmApprovalDelegate implements NextPageDelegate {
        public WOComponent nextPage;
        ERD2WPickListPage pickPage;
        public String actionName;

        public WOComponent nextPage(WOComponent sender){
            pickPage.valueForKey(actionName);
    
            return nextPage;
        }
    }

    public WOComponent _finishSelectAll() {
        selectedObjects.removeAllObjects();

        NSArray list = filteredObjects();
        for (Enumeration e = list.objectEnumerator(); e.hasMoreElements();) {
            selectedObjects.addObject(e.nextElement());
        }
        return null;
    }
    
    public WOComponent selectAllOnPage() {
        return _doActionWithPossibleConfirmation(displayGroup().displayedObjects().count(), "_finishSelectAllOnPage");
    }

    public WOComponent _finishSelectAllOnPage() {
        selectedObjects.removeAllObjects();
        NSArray list = displayGroup().displayedObjects();
        for (Enumeration e = list.objectEnumerator(); e.hasMoreElements();) {
            selectedObjects.addObject(e.nextElement());
        }
        return null;
    }

    public WOComponent unselectAll() {
        selectedObjects.removeAllObjects();
        return null;
    }

    public boolean singleSelection() {
        if (_singleSelection == null) {
            _singleSelection = ERXValueUtilities.booleanValue(d2wContext().valueForKey("singleSelection")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _singleSelection.booleanValue();
    }

    public String selectionWidgetName() {
        return singleSelection() ? "WORadioButton" : "WOCheckBox";
    }
}
