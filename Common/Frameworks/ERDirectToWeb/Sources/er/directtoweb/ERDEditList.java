/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXConstant;
import er.extensions.ERXEOControlUtilities;

/**
 * Description forthcoming.<br />
 * 
 */
//DELETEME This looks like a pick page?

public class ERDEditList extends ERDCustomEditComponent {

    public ERDEditList(WOContext context) { super(context); }

    public final static Logger log = Logger.getLogger(ERDEditList.class);
    
    public String choices;
    public String choiceDisplayKey;
    public String choicesSortKey;
    public Number numberOfColumns;
    public WOComponent nextPage;
    public NextPageDelegate nextPageDelegate;
    // working around checkbox matrix bug
    public String dummy;
    public String errorMessage;
    public EOEnterpriseObject item;
    
    private NSArray _list;
    /** @TypeInfo com.webobjects.eocontrol.EOEnterpriseObject */    
    public NSArray list() {
        if (_list==null) {
            EOEditingContext objectContext = object().editingContext();
            NSMutableArray nonSortedLocalList=new NSMutableArray();
            if (choices == null)
                log.warn("Choices was null for the list.  For now this means that an empty list will appear.");
            // FIXME: Might want to just fetch all of the eos if a choice isn't specified.
            NSArray nonLocallist = choices == null ? ERXConstant.EmptyArray : (NSArray)valueForKeyPath(choices);
            if (nonLocallist!=null) {
                for (Enumeration e=nonLocallist.objectEnumerator(); e.hasMoreElements();) {
                    nonSortedLocalList.addObject(EOUtilities.localInstanceOfObject(objectContext, (EOEnterpriseObject) e.nextElement()));
                }	
                NSArray sortArray=new NSArray(new EOSortOrdering(choicesSortKey(),
                                                                 EOSortOrdering.CompareAscending));
                _list=EOSortOrdering.sortedArrayUsingKeyOrderArray(nonSortedLocalList, sortArray);
            }
        }
        return _list;
    }

    public String choicesSortKey() {
        if (choicesSortKey==null)
            choicesSortKey=choiceDisplayKey;
        return choicesSortKey;
    }

    public WOComponent submit() {
        NSArray existingListArray = (NSArray) objectKeyPathValue();
        errorMessage=null;
        String key=key();
        String subKey;
        EOEnterpriseObject subObject;
        if (key.indexOf('.') > 0) {
            String firstSubKey = key.substring(0, key.lastIndexOf('.'));
            subKey = key.substring(key.lastIndexOf('.') + 1);
            subObject = (EOEnterpriseObject)object().valueForKeyPath(firstSubKey);
        } else {
            subObject = object();
            subKey = key;
        }
        if (log.isDebugEnabled()) {
            log.debug("Original Array: "+existingListArray);
            log.debug("subObject: "+ subObject);
            log.debug("subKey: "+ subKey);
        }
        
        for (Enumeration e= existingListArray.objectEnumerator(); e.hasMoreElements();){
            EOEnterpriseObject anItem = (EOEnterpriseObject)e.nextElement();
            if(!selections().containsObject(anItem)){                
                subObject.removeObjectFromBothSidesOfRelationshipWithKey(anItem, subKey);
                if (log.isDebugEnabled())
                    log.debug("removing: "+ anItem);
            }
        }
        for (Enumeration e=selections().objectEnumerator(); e.hasMoreElements();){
            EOEnterpriseObject anItem = (EOEnterpriseObject)e.nextElement();
            if(!existingListArray.containsObject(anItem)){
                subObject.addObjectToBothSidesOfRelationshipWithKey(anItem, subKey);
                if (log.isDebugEnabled())
                    log.debug("adding: "+ anItem);
            }
        }

        // we save directly if the object is not new
        if (subObject.editingContext().hasChanges() && !ERXEOControlUtilities.isNewObject(subObject)) {
            try {
                if (log.isDebugEnabled())
                    log.debug("saving changes..");
                subObject.validateForSave();
                subObject.editingContext().saveChanges();
                if (log.isDebugEnabled())
                    log.debug("changes saved.");
            } catch (NSValidation.ValidationException e) {
                errorMessage = " Could not save your changes: "+e.getMessage()+" ";
            }
        }

        return errorMessage==null ? nextPage() : null;
    }

    public WOComponent nextPage() {
        return nextPageDelegate!=null ? nextPageDelegate.nextPage(this) : nextPage;
    }

    public WOComponent cancel() {
        // only revert if it's not a new EO, since otherwise we wipe out the whole thing!
    if (!ERXEOControlUtilities.isNewObject(object())) object().editingContext().revert();
        return nextPage();
    }
    
    public String listLabel(){
        return (String)item.valueForKey(choiceDisplayKey);
    }

    private NSArray _selections;
    public NSArray selections() {
        if (_selections==null) {
            _selections=(NSArray) objectKeyPathValue();
        }
        return _selections;
    }
}
