/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;
import org.apache.log4j.NDC;

public abstract class ERD2WPage extends D2WPage implements ERXExceptionHolder, ERDUserInfoInterface {

    public ERD2WPage(WOContext c) {
        super(c);
    }    
    
    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERD2WPage.class);
    public static final ERXLogger validationLog = ERXLogger.getERXLogger("er.directtoweb.validation.ERD2WPage");

    public NSMutableDictionary errorMessages() { return errorMessages; }
    public void setErrorMessages(NSMutableDictionary value) { errorMessages = value; }
    
    protected NSMutableDictionary errorMessages = new NSMutableDictionary();
    protected NSMutableArray errorKeyOrder = new NSMutableArray();
    protected NSMutableArray keyPathsWithValidationExceptions = new NSMutableArray();
    public String errorMessage="";
    
    protected EOEditingContext _context;
    public void setObject(EOEnterpriseObject eo) {
        _context = (eo != null) ? eo.editingContext() : null;
        // for SmartAssignment
        d2wContext().takeValueForKey(eo, "object");
        super.setObject(eo);
    }
    // debug helpers
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }
    public String d2wCurrentComponentName() {
        String name = (String)d2wContext().valueForKey("componentName");
        if(name.indexOf("CustomComponent")>=0) {
            name = (String)d2wContext().valueForKey("customComponentName");
        }
        return name;
    }
    
    // make kvc happy
    public void setD2wContext(D2WContext newValue) {}
    public void setLocalContext(D2WContext newValue) {
        if (ERXExtensions.safeDifferent(newValue,localContext())) {
            // HACK ALERT: this next line is made necessary by the brain-damageness of
            // D2WComponent.setLocalContext, which holds on to the first non null value it gets.
            // I swear if I could get my hands on the person who did that.. :-)
            _localContext=newValue;
        }
        super.setLocalContext(newValue);
        log.debug("SetLocalContext "+newValue);
        // This way
        d2wContext().takeValueForKey(keyPathsWithValidationExceptions, "keyPathsWithValidationExceptions");
    }
    
    public boolean shouldPropogateExceptions() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldPropogateExceptions")); }
    public boolean shouldCollectValidationExceptions() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldCollectValidationExceptions")); }
    
    public void clearValidationFailed() {
        errorMessages.removeAllObjects();
        errorKeyOrder.removeAllObjects();
        keyPathsWithValidationExceptions.removeAllObjects();
    }

    // Used to hold a cleaned-up validation key and message.
    private NSMutableDictionary _temp = new NSMutableDictionary();
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        if (validationLog.isDebugEnabled())
            validationLog.debug("Validation failed with exception: " + e + " value: " + value + " keyPath: " + keyPath);
        if (shouldCollectValidationExceptions()) {
            if (e instanceof ERXValidationException) {
                ERXValidationException erv = (ERXValidationException)e;
                erv.setContext(d2wContext());
                errorKeyOrder.addObject(d2wContext().displayNameForProperty());
                errorMessages.setObjectForKey(erv.getMessage(), d2wContext().displayNameForProperty());
                if (erv.eoObject() != null && erv.propertyKey() != null &&
                    ERXUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldSetFailedValidationValue"), false)) {
                    erv.eoObject().takeValueForKeyPath(value, erv.propertyKey());
                }   
            } else {
                _temp.removeAllObjects();
                ERXValidation.validationFailedWithException(e,
                                                        value,
                                                        keyPath,
                                                        _temp,
                                                        propertyKey(),
                                                        ERXLocalizer.localizerForSession(session()),d2wContext().entity(),
                                                        ERXUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldSetFailedValidationValue"), false));
                errorKeyOrder.addObjectsFromArray(_temp.allKeys());
                errorMessages.addEntriesFromDictionary(_temp);
            }
            d2wContext().takeValueForKey(errorMessages, "errorMessages");
            if (keyPath != null)
                keyPathsWithValidationExceptions.addObject(keyPath);
        } else if (parent() != null && shouldPropogateExceptions()) {
            parent().validationFailedWithException(e, value, keyPath);
        }
    }

    public boolean hasValidationExceptionForPropertyKey() {
        return d2wContext().propertyKey() != null && keyPathsWithValidationExceptions.count() != 0 ?
        keyPathsWithValidationExceptions.containsObject(d2wContext().propertyKey()) : false;
    }
    
    // This will allow d2w pages to be listed on a per configuration basis in stats collecting.
    public String descriptionForResponse(WOResponse aResponse, WOContext aContext) {
        String descriptionForResponse = (String)d2wContext().valueForKey("pageConfiguration");
        /*
        if (descriptionForResponse == null)
            log.info("Unable to find pageConfiguration in d2wContext: " + d2wContext());
         */
        return descriptionForResponse != null ? descriptionForResponse : super.descriptionForResponse(aResponse, aContext);
    }

    protected NSMutableDictionary _userInfo = new NSMutableDictionary();
    public NSMutableDictionary userInfo() { return _userInfo; }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        // Need to make sure that we have a clean plate, every time 
        clearValidationFailed();
        NDC.push("Page: " + getClass().getName()+ (d2wContext()!= null ? (" - Configuration: "+d2wContext().valueForKey("pageConfiguration")) : ""));
        try {
            super.takeValuesFromRequest(r, c);
        } finally {
            NDC.pop();
        }
    }

    public WOActionResults invokeAction(WORequest r, WOContext c) {
        WOActionResults result=null;
        NDC.push("Page: " + getClass().getName()+ (d2wContext()!= null ? (" - Configuration: "+d2wContext().valueForKey("pageConfiguration")) : ""));
        try {
            result= super.invokeAction(r, c);
        } finally {
            NDC.pop();
        }
        return result;
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        NDC.push("Page: " + getClass().getName()+ (d2wContext()!= null ? (" - Configuration: "+d2wContext().valueForKey("pageConfiguration")) : ""));
        try {
            super.appendToResponse(r,c);
        } finally {
            NDC.pop();
        }
    }   


}
