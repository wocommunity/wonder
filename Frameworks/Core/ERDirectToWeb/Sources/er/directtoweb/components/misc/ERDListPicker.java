/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * Used to pick a string from a list.  Should use ERPickTypePageTemplate with D2W instead.
 * 
 * @binding list
 * @binding listComponentName
 * @binding pickComponentName
 * @binding object
 * @binding key
 */

public class ERDListPicker extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
            object().addObjectToBothSidesOfRelationshipWithKey(item,key());        
        return listComponent();
    }
}
