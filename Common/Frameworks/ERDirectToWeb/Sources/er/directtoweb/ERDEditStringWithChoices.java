/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXKeyValuePair;
import er.extensions.ERXLocalizer;

/**
 * Provides a toOne relationship-like component except the value is pushed in as a string.<br />
 * The coices can be either given as an NSDictionary with {key1=val1;key2=val2...}, an NSArray of
 * NSDictionaries with ({key1=val1;},{key2=val2;}...) or a means not yet clear to me (ak:).
 */

public class ERDEditStringWithChoices extends ERDCustomEditComponent {

    public ERDEditStringWithChoices(WOContext context) {super(context);}
    
    /** logging support */
    public static final Logger log = Logger.getLogger(ERDEditStringWithChoices.class);
    
    public String entityForReportName;
    public ERXKeyValuePair currentElement;

    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }
    
    protected NSArray _availableElements;
    public NSArray availableElements(){
        if(_availableElements==null){
            Object choices = valueForBinding("possibleChoices");
            if(choices != null) {
                NSMutableArray keyChoices = new NSMutableArray();
                if(choices instanceof NSArray) {
                    for(Enumeration e = ((NSArray)choices).objectEnumerator(); e.hasMoreElements(); ) {
                        NSDictionary dict = (NSDictionary)e.nextElement();
                        String key = (String)dict.allKeys().lastObject();
                        String value = (String)dict.objectForKey(key);
                        keyChoices.addObject(new ERXKeyValuePair(key, ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(value)));
                    }
                } else if(choices instanceof NSDictionary) {
                    NSArray keys = ((NSDictionary)choices).allKeys();
                    keys = ERXArrayUtilities.sortedArraySortedWithKey(keys, "toString");
                    for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                        String key = (String)e.nextElement();
                        String value = (String)((NSDictionary)choices).objectForKey(key);
                        keyChoices.addObject(new ERXKeyValuePair(key, ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(value)));
                    }
                }
                _availableElements = keyChoices;
            }
            if(_availableElements==null){
                if(log.isDebugEnabled()) log.debug("key ="+key());
                String keyForAvailableObjects = key()+"Available";
                entityForReportName = (String)valueForBinding("entityNameForReport");
                _availableElements =
                    ERDirectToWeb.displayableArrayForKeyPathArray((NSArray)object().valueForKeyPath(keyForAvailableObjects),
                                                                  entityForReportName);
            }
            if(log.isDebugEnabled()) log.debug("availableElements = "+_availableElements);
        }
        return _availableElements;
    }

    public void reset(){
        super.reset();
        _availableElements = null;
        entityForReportName = null;
        currentElement = null;
    }

    public ERXKeyValuePair selectedElement() {
        Object value = objectPropertyValue();
        ERXKeyValuePair selectedElement = null;
        for(Enumeration e = availableElements().objectEnumerator(); e.hasMoreElements() && selectedElement == null;) {
            ERXKeyValuePair current = (ERXKeyValuePair) e.nextElement();
            if(current.key().equals(value)) {
                selectedElement = current;
            }
        }
        return selectedElement;
    }
    
    public void setSelectedElement(Object value) {
        ERXKeyValuePair kvp  = (ERXKeyValuePair)value;
        if (kvp!=null) {
            object().validateTakeValueForKeyPath(kvp.key(), key());
        } else {
            object().validateTakeValueForKeyPath(null, key());
        }
   }

    /** Extends the parent implementation in order to force validation. */
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r,c);
        if (c._wasFormSubmitted()) {
        	try {
        		object().validateTakeValueForKeyPath(objectPropertyValue(), key());
        	} catch (NSValidation.ValidationException e) {
        		validationFailedWithException(e, objectPropertyValue(), key());
        	}
		}
        
    }
    
}
