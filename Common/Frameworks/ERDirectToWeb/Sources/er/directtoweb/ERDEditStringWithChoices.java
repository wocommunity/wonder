/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Provides a toOne relationship-like component except the value is pushed in as a string.<br />
 * The coices can be either given as an NSDictionary with {key1=val1;key2=val2...}, an NSArray of
 * NSDictionaries with ({key1=val1;},{key2=val2;}...) or a means not yet clear to me (ak:).
 */

public class ERDEditStringWithChoices extends ERDCustomEditComponent {

    public ERDEditStringWithChoices(WOContext context) {super(context);}
    
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERDEditStringWithChoices.class);
    
    public String entityForReportName;
    public ERXKeyValuePair currentElement;
    public ERXKeyValuePair selectedElement;

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
                                                                  entityForReportName, ERXLocalizer.localizerForSession(session()).language());
            }
            if(log.isDebugEnabled()) log.debug("availableElements = "+_availableElements);
        }
        return _availableElements;
    }

    public void reset(){
        super.reset();
        _availableElements = null;
        entityForReportName = null;
        selectedElement = null;
        currentElement = null;
    }

    public void setSelectedElement(Object value) {
        selectedElement = (ERXKeyValuePair)value;
        if (selectedElement!=null) {
            object().takeValueForKey(selectedElement.key(), key());
        } else {
            object().takeValueForKey(null, key());
        }
   }
 
    public void appendToResponse(WOResponse r, WOContext c) {
        String chosenKey = (String)objectPropertyValue();
        if(log.isDebugEnabled()) log.debug("chosenKey = "+chosenKey);
        if(chosenKey!=null){
            for(Enumeration e = availableElements().objectEnumerator(); e.hasMoreElements();){
                ERXKeyValuePair keyValue = (ERXKeyValuePair)e.nextElement();
                if(keyValue.key().equals(chosenKey))
                   selectedElement = keyValue;
            }
            if(log.isDebugEnabled()) {
                if(selectedElement != null) {
                    log.debug("selectedElement = "+selectedElement.key()+" , "+selectedElement.value());
                }
            }
        }
        super.appendToResponse(r,c);
    }
}
