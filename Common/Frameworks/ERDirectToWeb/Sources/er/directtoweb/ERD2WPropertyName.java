/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXUtilities;
import er.extensions.ERXConstant;

public class ERD2WPropertyName extends D2WStatelessComponent {

    public ERD2WPropertyName(WOContext context) { super(context); }
    
    public String width() { return hasPropertyName() ? "148" : null; }

    public boolean hasPropertyName() {
        String displayNameForProperty=displayNameForProperty();
        return displayNameForProperty!=null && displayNameForProperty.length()>0;
    }

    public boolean displayRequiredMarker() {
        boolean displayRequiredMarker = false;
        // avoiding attribute() and relationship() because of lame-ass caching scheme on D2WContext
        String task = (String)d2wContext().valueForKey("task");
        if (task==null || task.equals("edit")) {
            if (!ERXUtilities.booleanValue(d2wContext().valueForKey("isManditory"))) {
                EOAttribute a=(EOAttribute)d2wContext().valueForKey("smartAttribute");
                if (a!=null)
                    displayRequiredMarker = !a.allowsNull();
                else {
                    EORelationship r=(EORelationship)d2wContext().valueForKey("smartRelationship");
                    if (r!=null) displayRequiredMarker = r.isMandatory();
                }                
            } else
                displayRequiredMarker = true;
        }
        return displayRequiredMarker;
    }

    public void takeValueFromRequest(WORequest r, WOContext c) {
        // no form values in here!
    }

    public boolean validationExceptionOccurredForPropertyKey() {
        return d2wContext().propertyKey() != null ? keyPathsWithValidationExceptions().containsObject(d2wContext().propertyKey()) : false;
    }
    
    public NSArray keyPathsWithValidationExceptions() {
        NSArray exceptions = (NSArray)d2wContext().valueForKey("keyPathsWithValidationExceptions");
        return exceptions != null ? exceptions : ERXConstant.EmptyArray;
    }
}