/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSValidation;
import er.directtoweb.components.ERDCustomEditComponent;

public class ComponentPicker extends ERDCustomEditComponent {

    public ComponentPicker(WOContext c) {
        super(c);
    }
    
    public boolean synchronizesVariablesWithBindings() { return false; }

    public Component currentComponent;
    
    public NSArray componentList() {
        return Component.orderedComponents(object().editingContext());
        
    }

    public void setObjectPropertyValue(EOEnterpriseObject c) {
        if (c!=null)
            object().addObjectToBothSidesOfRelationshipWithKey(c,key());
        else
            object().removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject)objectPropertyValue(),key());
            
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r,c);
        try {
            object().validateValueForKey(objectPropertyValue(),key());
        } catch (NSValidation.ValidationException e) {
            parent().validationFailedWithException(e, objectPropertyValue(),key());
        }
    }
}
