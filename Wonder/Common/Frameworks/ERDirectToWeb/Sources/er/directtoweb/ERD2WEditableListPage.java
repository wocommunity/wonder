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

public class ERD2WEditableListPage extends ERD2WListPage implements ERXExceptionHolder {

    public ERD2WEditableListPage(WOContext context) {super(context);}

    public int colspanForNavBar() {
        return 2*displayPropertyKeys().count()+2;
    }

    /*
     FIXME:
     1/ we are missing a formal protocol for selectedObject
     2/ this component uses a hidden trick:
     - clicking on the select button uses next page delegate
     - the cancel button uses the next page

     */
    protected NSMutableDictionary errorMessages=new NSMutableDictionary();
    protected String errorMessage;
    private NSMutableDictionary _errorMessagesDictionaries;
    
    protected NSMutableDictionary errorMessagesDictionaries(){
        if(_errorMessagesDictionaries == null){
            _errorMessagesDictionaries = new NSMutableDictionary();
        }
        return _errorMessagesDictionaries;
    }

    public NSMutableDictionary currentErrorDictionary() {
        Object key = d2wContext().valueForKeyPath("object.hashCode");
        if (errorMessagesDictionaries().objectForKey(key) == null) {
            errorMessagesDictionaries().setObjectForKey(new NSMutableDictionary(), key);
        }
        return (NSMutableDictionary)errorMessagesDictionaries().objectForKey(key);
    }
    
    public String dummy;

    public boolean showCancel() {
        return nextPage()!=null;
    }

    public boolean isEntityInspectable() {
        return isEntityReadOnly() && ERXValueUtilities.booleanValue(d2wContext().valueForKey("isEntityInspectable"));
    }

    public void setObject(EOEnterpriseObject eo) {
        super.setObject(eo);
        d2wContext().takeValueForKey(eo,"object");
    }

    public EOEditingContext editingContext() {
        return displayGroup().dataSource().editingContext();
    }

    public WOComponent backAction() {
        return super.backAction();
    }

    public WOComponent saveAction() {
        WOComponent result = null;
        try {
            if(!isListEmpty()) {
                editingContext().saveChanges();
            }
            result = backAction();
        } catch(NSValidation.ValidationException ex) {
            errorMessage = ex.toString();
            validationFailedWithException(ex, ex.object(), "saveChangesExceptionKey");
        }
        return result;
    }

    public WOComponent cancel(){
        clearValidationFailed();
        if(!isListEmpty()) {
            editingContext().revert();
        }
        return backAction();
    }
    
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        ERXValidation.validationFailedWithException(e, value, keyPath, currentErrorDictionary(), propertyKey(), ERXLocalizer.currentLocalizer(), d2wContext().entity(), shouldSetFailedValidationValue());
    }
    
    public void clearValidationFailed(){
        for(Enumeration e = errorMessagesDictionaries().objectEnumerator(); e.hasMoreElements();){
            ((NSMutableDictionary)e.nextElement()).removeAllObjects();
        }
    }

    public WOComponent update() {
        try {
            if(!isListEmpty()) {
                editingContext().saveChanges();
            }
        } catch(NSValidation.ValidationException EOVe) {
            errorMessage = EOVe.toString();
            editingContext().revert();
        }
        return null;
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        // Need to make sure that we have a clean slate, every time
        clearValidationFailed();
        _errorMessagesDictionaries = null;
        super.takeValuesFromRequest(r, c);
    }

    public String saveLabel() {
        String templateKey = (String)d2wContext().valueForKey("saveLabelTemplateKey");
        String displayName = (String)d2wContext().valueForKey("displayNameForEntity");
        int count = displayGroup().allObjects().count();
        if(templateKey == null)
            templateKey = "ERDEditList.saveLabel";

        String saveLabel = ERXLocalizer.currentLocalizer().plurifiedStringWithTemplateForKey(templateKey, displayName, count, d2wContext());
        return saveLabel;
    }
}
