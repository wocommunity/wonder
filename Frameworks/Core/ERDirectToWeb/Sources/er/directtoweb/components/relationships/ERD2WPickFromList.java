/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.components.ERDCustomQueryComponent;

/**
 * Custom query component that let's the user select from an arbitrary list.
 */
public class ERD2WPickFromList extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WPickFromList(WOContext context) { super(context); }
    // lets you pick from either an arbitrary list or a pool of shared EOs
    
    public Object item; 

    // can't be stateless!
    @Override
    public boolean isStateless() { return false; }
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    private String _sharedEOsEntityName;
    public String sharedEOsEntityName() {
        if (_sharedEOsEntityName==null)
            _sharedEOsEntityName=(String)valueForBinding("sharedEOsEntityName");
        return _sharedEOsEntityName;
    }
    private String _keyWhenRelationship;
    public String keyWhenRelationship () {
        if (_keyWhenRelationship ==null)
            _keyWhenRelationship =(String)valueForBinding("keyWhenRelationship");
        return _keyWhenRelationship;
    }

    public NSArray list() {
        NSArray result=null;
        if (sharedEOsEntityName()!=null) {
            result=EOUtilities.objectsForEntityNamed(EOSharedEditingContext.defaultSharedEditingContext(),
                                                     sharedEOsEntityName());
        } else {
            result=(NSArray)valueForBinding("list");
        }
        return result;
    }

    public String displayString() {
        String result=null;
        if (item!=null) {
            if (item instanceof String) result=(String)item;
            else if (item instanceof EOEnterpriseObject)
                result=((EOEnterpriseObject)item).valueForKeyPath(keyWhenRelationship()).toString();
            else result=item.toString();
        }
        return result;
    }
}
