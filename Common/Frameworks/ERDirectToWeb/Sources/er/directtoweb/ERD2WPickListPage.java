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

// FIXME: Need a formal protocol for cancel vs. selection.
public class ERD2WPickListPage extends ERD2WListPage implements ERDBranchInterface {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WPickListPage.class);

    /** holds the selected objects */
    public NSMutableArray selectedObjects = new NSMutableArray();
    
    /**
     * IE sometimes won't submit the form if it only has checkboxes, we
     * bind this to a WOHiddenField.
     */
    public String dummy;

    /** Caches if we are in single selection mode. */
    public Boolean _singleSelection;

    /**
     * Public constructor.
     * @param context current context
     */
    public ERD2WPickListPage(WOContext context) { super(context); }    

    /**
     * Determines if the cancel button should be shown.
     * @return if we have a nextPage set
     */
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

    public WOComponent printerFriendlyVersion() {
        D2WListPage result=(D2WListPage)ERDirectToWeb.printerFriendlyPageForD2WContext(d2wContext(),session());
        result.setDataSource(dataSource());
        result.displayGroup().setSortOrderings(displayGroup().sortOrderings());
        result.displayGroup().setNumberOfObjectsPerBatch(displayGroup().allObjects().count());
        result.displayGroup().updateDisplayedObjects();
        return result;
    }

    public boolean singleSelection() {
        if (_singleSelection==null) {
            _singleSelection = ERXValueUtilities.booleanValue(d2wContext().valueForKey("singleSelection")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _singleSelection.booleanValue();
    }

    public String selectionWidgetName() {
        return singleSelection() ? "WORadioButton" : "WOCheckBox";
    }

    //---------------- Branch Delegate Support --------------------//
    /** holds the chosen branch */
    protected NSDictionary _branch;

    /**
        * Cover method for getting the choosen branch.
     * @return user choosen branch.
     */
    public NSDictionary branch() { return _branch; }

    /**
        * Sets the user choosen branch.
     * @param branch choosen by user.
     */
    public void setBranch(NSDictionary branch) { _branch = branch; }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    // ENHANCEME: Should be localized
    public String branchName() { return (String)branch().valueForKey("branchName"); }

    /**
     * Calculates the branch choices for the current
     * poage. This method is just a cover for calling
     * the method <code>branchChoicesForContext</code>
     * on the current {@link ERDBranchDelegate ERDBranchDelegate}.
     * @return array of branch choices
     */
    public NSArray branchChoices() {
        NSArray branchChoices = null;
        if (nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface) {
            branchChoices = ((ERDBranchDelegateInterface)nextPageDelegate()).branchChoicesForContext(d2wContext());
        } else {
            log.error("Attempting to call branchChoices on a page with a delegate: " + nextPageDelegate() + " that doesn't support the ERDBranchDelegateInterface!");
        }
        return branchChoices;
    }

    /**
     * Determines if this message page should display branch choices.
     * @return if the current delegate supports branch choices.
     */
    public boolean hasBranchChoices() {
        return nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface;
    }
    //---------------- End Branch Delegate Support --------------------//
        
}
