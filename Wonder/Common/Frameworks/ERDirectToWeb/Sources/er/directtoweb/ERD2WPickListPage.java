/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

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

    public WOComponent selectAll() {
        selectedObjects.removeAllObjects();
        NSArray list = displayGroup().qualifier() == null ? displayGroup().allObjects() : EOQualifier.filteredArrayWithQualifier(displayGroup().allObjects(),
                displayGroup().qualifier());
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
