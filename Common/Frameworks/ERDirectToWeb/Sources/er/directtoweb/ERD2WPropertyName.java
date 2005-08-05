/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Used for displaying the propertyName in a template.<br />
 * 
 * @binding localContext
 */

public class ERD2WPropertyName extends D2WStatelessComponent {
    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WPropertyName.class);

    protected String _displayNameForProperty;
    protected NSDictionary _contextDictionary;
    public String currentKey;
  
    public ERD2WPropertyName(WOContext context) { 
        super(context); 
    }

    public String displayNameForProperty() {
        if(_displayNameForProperty == null) {
            _displayNameForProperty = (String)d2wContext().valueForKey("displayNameForProperty");
        }
        return _displayNameForProperty;
    }
    
    public void reset() {
        super.reset();
        _displayNameForProperty = null;
        _contextDictionary = null;
    }
    
    public boolean hasNoErrors() {
        if(false) {
            String keyPath = "errorMessages." + displayNameForProperty();
            return d2wContext().valueForKeyPath(keyPath) == null;
        }
        return !validationExceptionOccurredForPropertyKey();
    }

    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }

    public boolean d2wDebuggingEnabled() {
        return ERDirectToWeb.d2wDebuggingEnabled(session());
    }

    public Object currentValue() {
        return contextDictionaryForPropertyKey().valueForKey(currentKey);
    }

    public NSDictionary contextDictionary() {
        if(_contextDictionary == null) {
            _contextDictionary = (NSDictionary)d2wContext().valueForKey("contextDictionary");
            if(_contextDictionary == null) {
                ERD2WContextDictionary dict = new ERD2WContextDictionary(d2wContext().dynamicPage(), null, null);
                _contextDictionary = dict.dictionary();
                d2wContext().takeValueForKey(_contextDictionary, "contextDictionary");
            }
        }
        return _contextDictionary;
    }
    
    public NSDictionary contextDictionaryForPropertyKey() {
        return (NSDictionary)contextDictionary().valueForKeyPath("componentLevelKeys." + propertyKey());
    }
    
    public String d2wComponentName() {
        String name = (String)d2wContext().valueForKey("componentName");
        if(name != null && name.indexOf("CustomComponent")>=0) {
            name = (String)d2wContext().valueForKey("customComponentName");
        }
        return name;
    }

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
            if (!ERXValueUtilities.booleanValue(d2wContext().valueForKey("isMandatory"))) {
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

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        // no form values in here!
    }

    public boolean validationExceptionOccurredForPropertyKey() {
        if (d2wContext().propertyKey() == null) {
            return false;
        } else {
            String propertyKey = d2wContext().propertyKey();
            boolean contains = keyPathsWithValidationExceptions().containsObject(propertyKey);
            if (log.isDebugEnabled())
            	log.debug("propertyKey="+propertyKey+", keyPathsWithValidationExceptions="+keyPathsWithValidationExceptions());
            return contains;
        }
    }
    
    public NSArray keyPathsWithValidationExceptions() {
        NSArray exceptions = (NSArray)d2wContext().valueForKey("keyPathsWithValidationExceptions");
        return exceptions != null ? exceptions : NSArray.EmptyArray;
    }
}