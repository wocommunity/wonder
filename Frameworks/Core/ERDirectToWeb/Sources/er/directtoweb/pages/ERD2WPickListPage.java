/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSet;

import er.directtoweb.delegates.ERDBranchDelegate;
import er.directtoweb.interfaces.ERDPickPageInterface;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Allows the selection of one or more objects from a set of EOs. Can also be used directly as a 
 * EditRelationshipPage for editing to-one and to-many relationships.
 * @d2wKey showActions
 * @d2wKey singleSelection
 * @d2wKey restrictedChoiceKey
 * @d2wKey restrictingFetchSpecification
 */
// FIXME: Need a formal protocol for cancel vs. selection.
public class ERD2WPickListPage extends ERD2WListPage implements ERDPickPageInterface, EditRelationshipPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WPickListPage.class);

    /**
     * IE sometimes won't submit the form if it only has checkboxes, we bind
     * this to a WOHiddenField.
     */
    public String dummy;

    /** Caches if we are in single selection mode. */
    public Boolean _singleSelection;

    /** These are set when we are in edit-relationship mode. */
    public EOEnterpriseObject _masterObject;
    public String _relationshipKey;

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
    @Override
    public boolean showCancel() {
        return ((!(nextPageDelegate() instanceof ERDBranchDelegate)) && nextPage() != null) || cancelPage() !=null;
    }

    public boolean checked() {
        return selectedObjects().containsObject(object());
    }

    public void setChecked(boolean newChecked) {
        NSMutableArray selectedObjects = selectedObjects().mutableClone();
        if (newChecked) {
            if (!selectedObjects.containsObject(object())) {
                selectedObjects.addObject(object());
            }
        } else {
            selectedObjects.removeObject(object());
        }
        setSelectedObjects(selectedObjects);
    }

    @Override
    public NSArray selectedObjects() {
        return super.selectedObjects();
    }

    @Override
    public void setSelectedObjects(NSArray selectedObjects) {
        super.setSelectedObjects(selectedObjects);
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

    @Override
    public WOComponent backAction() {
        if(_masterObject != null) {
            EOEditingContext ec = _masterObject.editingContext();
            ec.lock();
            try {
                _masterObject.takeValueForKey(singleSelection() ? selectedObjects().lastObject() : selectedObjects(), _relationshipKey);
                ec.saveChanges();
            } finally {
                ec.unlock();
            }
        }
        return super.backAction();
    }

    public void setChoices(NSArray choices) {
        displayGroup().setObjectArray(choices);
    }

    // Called by ERD2WListPage before the display group is updated
    @Override
    protected void willUpdate() {
        super.willUpdate();
    }
    
    // Called by ERD2WListPage after the display group is updated
    @Override
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
    
    public boolean showSelectionActions() {
        return filteredObjects().count() > 0 && ERXValueUtilities.booleanValue(d2wContext().valueForKey("showActions"));
    }
    
    /**
     * The display group's objects, filtered by the display group qualifier (if any)
     */
    public NSArray filteredObjects() {
        if (displayGroup() instanceof ERXDisplayGroup) {
            ERXDisplayGroup dg = (ERXDisplayGroup) displayGroup();
            return dg.filteredObjects();
        }
        return displayGroup().qualifier() == null ? displayGroup().allObjects() : EOQualifier.filteredArrayWithQualifier(displayGroup().allObjects(), displayGroup().qualifier());
    }
    
    public WOComponent selectAll() {
        NSMutableArray selectedObjects = new NSMutableArray();
        NSArray list = filteredObjects();
        for (Enumeration e = list.objectEnumerator(); e.hasMoreElements();) {
            selectedObjects.addObject(e.nextElement());
        }
        setSelectedObjects(selectedObjects);
        return context().page();
    }
    
    public WOComponent selectAllOnPage() {
        NSMutableArray selectedObjects = selectedObjects().mutableClone();
        NSArray list = displayGroup().displayedObjects();
        for (Enumeration e = list.objectEnumerator(); e.hasMoreElements();) {
            selectedObjects.addObject(e.nextElement());
        }
        setSelectedObjects(selectedObjects);
        return context().page();
    }

    public WOComponent unselectAll() {
        NSMutableArray selectedObjects = new NSMutableArray();
        setSelectedObjects(selectedObjects);
        return context().page();
    }
    
    public WOComponent unselectAllOnPage() {
        NSArray selectedObjects = ERXArrayUtilities.arrayMinusArray(selectedObjects(), displayGroup().displayedObjects());
        setSelectedObjects(selectedObjects);
        return context().page();
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
    
    public void setMasterObjectAndRelationshipKey(EOEnterpriseObject eo, String relationshipName) {
        EOEditingContext ec = ERXEC.newEditingContext(eo.editingContext(), false);
        setEditingContext(ec);
        _masterObject = EOUtilities.localInstanceOfObject(ec, eo);
        _relationshipKey = relationshipName;
        setObject(_masterObject);
        _singleSelection = _masterObject.classDescription().toManyRelationshipKeys().containsObject(_relationshipKey) ? Boolean.FALSE : Boolean.TRUE;
        d2wContext().takeValueForKey(_singleSelection, "singleSelection");
         
        EODataSource ds;
        String restrictedChoiceKey = (String)d2wContext().valueForKeyPath("restrictedChoiceKey");
        if(restrictedChoiceKey != null && restrictedChoiceKey.length() > 0) {
            Object choices = d2wContext().valueForKeyPath(restrictedChoiceKey);
            ds = ERXEOControlUtilities.dataSourceForArray((NSArray)choices);
        } else {
            ds = new EODatabaseDataSource(ec, d2wContext().entity().name(), (String)d2wContext().valueForKeyPath("restrictingFetchSpecification"));
        }
        setDataSource(ds);
        
        Object relationshipValue = _masterObject.valueForKey(relationshipName);
        NSArray objects;
        if(relationshipValue instanceof NSArray) {
            objects = (NSArray)relationshipValue;
        } else if(relationshipValue != null) {
            objects = new NSArray(relationshipValue);
        } else {
            objects = NSArray.EmptyArray;
        }
        setSelectedObjects(objects);
     }
}
