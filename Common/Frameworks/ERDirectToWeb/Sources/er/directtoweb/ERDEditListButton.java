/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXConstant;
import er.extensions.ERXExceptionHolder;
import er.extensions.ERXLocalizer;


/**
 * Nice component used for editing a toMany relationship by taking the user to another page to pick which objects belong in the relationship.<br />
 * 
 * @binding choiceDisplayKey
 * @binding choicePageName
 * @binding choices
 * @binding editButtonName
 * @binding extraBindings
 * @binding key
 * @binding numberOfColumns
 * @binding object
 */

public class ERDEditListButton extends ERDCustomEditComponent {

    public ERDEditListButton(WOContext context) {super(context);}
    
    public EOEnterpriseObject item;
    protected NextPageDelegate _nextPageDelegate;

    public boolean synchronizesVariablesWithBindings() { return false; }

    
    public NSArray list() {
        return (NSArray)objectKeyPathValue();
    }
    
    public String displayStringForItem() {
        return item!=null && item.valueForKey(choiceDisplayKey())!= null ? item.valueForKey(choiceDisplayKey()).toString() : "";
    }

    private String _editButtonName;
    public String editButtonName(){
        if (_editButtonName == null) {
            Object editButtonName = valueForBinding("editButtonName");
            if(editButtonName == null) {
                editButtonName = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERDEditListButton.editButtonName", this);
            } else {
                _editButtonName = (String)editButtonName;
            }
        }
        return _editButtonName;
    }
    
    /** @TypeInfo Object */
    private String _choices;
    public String choices(){
        if (_choices==null)
            _choices=(String)valueForBinding("choices");
        return _choices;
    }

    private String _explaination;
    public String explaination(){
        if (_explaination==null)
            _explaination=(String)valueForBinding("explaination");
        return _explaination;
    }

    private String _choiceDisplayKey;
    public String choiceDisplayKey(){
        if (_choiceDisplayKey==null)
            _choiceDisplayKey=(String)valueForBinding("choiceDisplayKey");
        return _choiceDisplayKey;
    }
    
    private String choicePageName;
    public String choicePageName(){
        if (choicePageName==null)            
            choicePageName=(String)valueForBinding("choicePageName");
        return choicePageName;
    }
    
    private Number _numberOfColumns;
    private static final Number THREE=ERXConstant.integerForInt(3);
    public Number numberOfColumns(){
        if (_numberOfColumns==null)
            _numberOfColumns = valueForBinding("numberOfColumns") != null ? (Number)valueForBinding("numberOfColumns") : THREE;
        return _numberOfColumns;
    }
    
    public WOComponent editList() {
        if (parent() instanceof ERXExceptionHolder)
            ((ERXExceptionHolder)parent()).clearValidationFailed();
        WOComponent result = pageWithName(choicePageName());
        result.takeValueForKey(object(), "object");
        result.takeValueForKey(key(), "key");
        result.takeValueForKey(choices(), "choices");
        result.takeValueForKey(numberOfColumns(), "numberOfColumns");
        result.takeValueForKey(choiceDisplayKey(), "choiceDisplayKey");
        result.takeValueForKey(context().page(), "nextPage");
        result.takeValueForKey(_nextPageDelegate, "nextPageDelegate");
        if (explaination() != null)
            result.takeValueForKey(explaination(), "explaination");
        return result;
    }
}
