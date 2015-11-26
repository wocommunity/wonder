/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.ERD2WContextDictionary;
import er.directtoweb.ERDirectToWeb;
import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Used for displaying the propertyName in a template.
 * 
 * @binding localContext
 * @d2wKey displayRequiredMarkerCell
 * @d2wKey escapeHTML
 * @d2wKey displayNameForProperty
 * @d2wKey componentName
 * @d2wKey customComponentName
 * @d2wKey hidePropertyName
 * @d2wKey displayRequiredMarker
 * @d2wKey keyPathsWithValidationExceptions
 */
public class ERD2WPropertyName extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final Logger log = Logger.getLogger(ERD2WPropertyName.class);

    protected String _displayNameForProperty;
    protected NSDictionary _contextDictionary;
    public String currentKey;
  
    public ERD2WPropertyName(WOContext context) { 
        super(context); 
    }

    @Override
    public String displayNameForProperty() {
        if(_displayNameForProperty == null) {
            _displayNameForProperty = (String)d2wContext().valueForKey("displayNameForProperty");
        }
        return _displayNameForProperty;
    }
    
    @Override
    public void reset() {
        super.reset();
        _displayNameForProperty = null;
        _contextDictionary = null;
    }
    
    public boolean hasNoErrors() {
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
            String key = "contextDictionary." + d2wContext().dynamicPage();
            _contextDictionary = (NSDictionary)ERXWOContext.contextDictionary().objectForKey(key);
            if(_contextDictionary == null) {
            	ERD2WContextDictionary dict = new ERD2WContextDictionary(d2wContext().dynamicPage(), null, null);
            	_contextDictionary = dict.dictionary();
            	ERXWOContext.contextDictionary().setObjectForKey(_contextDictionary, key);
            }
        }
        return _contextDictionary;
    }
    
    public NSDictionary contextDictionaryForPropertyKey() {
        Object o = contextDictionary().valueForKeyPath("componentLevelKeys." + propertyKey());
        if(o instanceof NSDictionary) {
            return (NSDictionary)o;
        }
        return NSDictionary.EmptyDictionary;
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
        return !ERXValueUtilities.booleanValue(d2wContext().valueForKey("hidePropertyName"));
    }

    public boolean displayRequiredMarker() {
    	return ERXValueUtilities.booleanValue(d2wContext().valueForKey("displayRequiredMarker"));
    }

    @Override
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