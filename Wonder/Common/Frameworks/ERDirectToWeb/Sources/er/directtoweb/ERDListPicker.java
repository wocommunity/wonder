/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * Used to pick a string from a list.  Should use ERPickTypePageTemplate with D2W instead.<br />
 * 
 * @binding list
 * @binding listComponentName
 * @binding pickComponentName
 * @binding object
 * @binding key
 */

public class ERDListPicker extends ERDCustomEditComponent {

    public ERDListPicker(WOContext context) { super(context); }
    
    public String iFrameName() { return "someName"; }
    
    public EOEnterpriseObject item;

    private String _pickComponentName; // can either be filled by binding or by the method below
    public String pickComponentName() {
        if (_pickComponentName==null && extraBindings!=null)
            _pickComponentName=(String)((NSDictionary)extraBindings).objectForKey("pickComponentName");
        return _pickComponentName;
    }
    private String _listComponentName; // can either be filled by binding or by the method below
    public String listComponentName () {
        if (_listComponentName ==null && extraBindings!=null)
            _listComponentName =(String)((NSDictionary)extraBindings).objectForKey("listComponentName");
        return _listComponentName;
    }

    private WOComponent _listComponent;
    public NSArray list;
    public WOComponent listComponent() {
        if (_listComponent==null) {
            _listComponent=pageWithName(listComponentName());
            _listComponent.takeValueForKey(object(),"object");
            _listComponent.takeValueForKey(key(),"key");
        }
        return _listComponent;
    }

    public WOComponent add() {
        NSArray existingList=(NSArray)objectKeyPathValue();
        if (!existingList.containsObject(item)&&item!=null)
            ((EOEnterpriseObject)object()).addObjectToBothSidesOfRelationshipWithKey(item,key());        
        return listComponent();
    }
}
