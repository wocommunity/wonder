/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXKeyValuePair;

/**
 * Crazy cool component that allows one to select strings (using arrow buttons), and organize them.
 */
// CHECKME: this can't ever have worked? Why Strings?
public class ERDListOrganizer extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDListOrganizer(WOContext context) { super(context); }
    
    /* logging support */
    public static final Logger log = Logger.getLogger(ERDListOrganizer.class);

    public ERXKeyValuePair availableObject;
    public NSMutableArray selectedObjects;   
    public NSMutableArray selectedChosenObjects;
    public NSArray chosenObjects;
    public ERXKeyValuePair chosenObject;    
    public String chosenKeyPaths;
    public String entityForReportName;

    private final static String DASH="-";
    private final static String DASHES="--------------------------------------------------------";
    private final static ERXKeyValuePair DEFAULT_PAIR=new ERXKeyValuePair(DASH, DASHES);
    private final static NSArray DEFAULT_ARRAY=new NSArray(DEFAULT_PAIR);

    @Override
    public void reset() {
        super.reset();
        chosenKeyPaths = null;
        entityForReportName = null;
        selectedChosenObjects = null;
        selectedObjects = null;
        availableObject = null;
        chosenObjects = null;
        chosenObject = null;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    @Override
    public boolean isStateless() { return true; }

    public NSArray availableElements() {
        if(log.isDebugEnabled())
            log.debug("availableElements = "
                    +ERDirectToWeb.displayableArrayForKeyPathArray((NSArray)object().valueForKeyPath(key()+"Available"),
                            entityForReportName));
        
        return ERDirectToWeb.displayableArrayForKeyPathArray((NSArray)object().valueForKeyPath(key()+"Available"),
                                                             entityForReportName);
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c){
        if(chosenKeyPaths == null){
            chosenKeyPaths = "";
            entityForReportName = (String)valueForBinding("entityNameForReport");
            String keyPathesFromDatabase = (String)objectPropertyValue();
            if(keyPathesFromDatabase!=null){
                NSArray keyPathsArray = (NSArray)NSPropertyListSerialization.propertyListFromString(keyPathesFromDatabase);
                if(log.isDebugEnabled()) log.debug("keyPathsArray = "+keyPathsArray);
                if(keyPathsArray!=null){
                    chosenObjects = ERDirectToWeb.displayableArrayForKeyPathArray(keyPathsArray,
                                                                                  entityForReportName);
                    if(((ERXSession)session()).browser().isNetscape()) {
                        NSMutableArray tmp = new NSMutableArray();
                        tmp.addObject(DEFAULT_PAIR);
                        tmp.addObjectsFromArray(chosenObjects);
                        chosenObjects = tmp;
                    }
                    chosenKeyPaths = keyPathsArray.componentsJoinedByString ( "," );
                }else {
                    chosenObjects = ERXConstant.EmptyArray;
                }
                if(log.isDebugEnabled()) log.debug("chosenObjects = "+chosenObjects);
            } else if(((ERXSession)session()).browser().isNetscape()) {
                chosenObjects = DEFAULT_ARRAY;
            }
        }
        super.appendToResponse(r,c);
    }

    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r, c);
        NSMutableArray result = new NSMutableArray();
        NSArray hiddenFieldValues = NSArray.componentsSeparatedByString(chosenKeyPaths, ",");
        if(log.isDebugEnabled()) log.debug("hiddenFieldValues = "+hiddenFieldValues);
        if(hiddenFieldValues != null){
            for(Enumeration e = hiddenFieldValues.objectEnumerator(); e.hasMoreElements();){
                String keyPath = (String)e.nextElement();
                if(log.isDebugEnabled()) log.debug("keyPath = "+keyPath);
                if(keyPath.length()>0)
                    result.addObject(keyPath);
            }
            if(log.isDebugEnabled()) log.debug("result = "+result);
            String value = NSPropertyListSerialization.stringFromPropertyList(result);
            try{
                object().validateTakeValueForKeyPath(value, key());
            } catch (NSValidation.ValidationException v) {
                parent().validationFailedWithException(v,value,key());
            }
        }
    }
}
