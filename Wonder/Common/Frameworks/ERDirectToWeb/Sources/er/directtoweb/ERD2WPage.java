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

/**
 * Common superclass for all ERD2W templates.<br />
 * 
 */

public abstract class ERD2WPage extends D2WPage implements ERXExceptionHolder, ERDUserInfoInterface, ERXComponentActionRedirector.Restorable, ERDBranchInterface {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERD2WPage.class);
    public static final ERXLogger validationLog = ERXLogger.getERXLogger("er.directtoweb.validation.ERD2WPage");    
    
    /**
     * Default public constructor.
     * @param c current context.
     */
    public ERD2WPage(WOContext c) {
        super(c);
    }    

    /**
     * Implementation of the {@see ERXComponentActionRedirector.Restorable} interface.
     * This implementation creates an URL with the name of the current pageConfiguration as a direct action,
     * which assumes a {@see ERD2WDirectAction} as the default direct action.
     * Subclasses need to implement more sensible behaviour.
     * @returns url for the current page
     */
    public String urlForCurrentState() {
        return context().directActionURLForActionNamed(d2wContext().dynamicPage(), null);
    }

    // error message handling
    public NSMutableDictionary errorMessages() { return errorMessages; }
    public void setErrorMessages(NSMutableDictionary value) { errorMessages = value; }
    
    protected NSMutableDictionary errorMessages = new NSMutableDictionary();
    protected NSMutableArray errorKeyOrder = new NSMutableArray();
    protected NSMutableArray keyPathsWithValidationExceptions = new NSMutableArray();

    protected String errorMessage = "";
    public String errorMessage() { return errorMessage; }
    public void setErrorMessage(String message) { errorMessage = message; }

    /** {@see EOEditingContext} for the current object */
    protected EOEditingContext _context;

    /** Implementation of the {@see InspectPageInterface} */
    public void setObject(EOEnterpriseObject eo) {
        _context = (eo != null) ? eo.editingContext() : null;
        // for SmartAssignment
        d2wContext().takeValueForKey(eo, "object");
        super.setObject(eo);
    }
    
    /** debug helper */
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }

    /** debug helper */
    public String d2wCurrentComponentName() {
        String name = (String)d2wContext().valueForKey("componentName");
        if(name.indexOf("CustomComponent")>=0) {
            name = (String)d2wContext().valueForKey("customComponentName");
        }
        return name;
    }

    /**
     * {@see} D2WContext for this page. Checks if there is a "d2wContext" binding, too.
     * @returns d2wContext
     */
    public D2WContext d2wContext() {
        if(hasBinding("localContext") && super.d2wContext()==null) {
            setLocalContext((D2WContext)valueForBinding("localContext"));
        }
        return super.d2wContext();
    }

    /** Key-Value-Coding needs this method. It should not be called */
    public void setD2wContext(D2WContext newValue) {}

    /** Sets the d2wContext for this page */
    public void setLocalContext(D2WContext newValue) {
        if (ERXExtensions.safeDifferent(newValue,localContext())) {
            // HACK ALERT: this next line is made necessary by the brain-damageness of
            // D2WComponent.setLocalContext, which holds on to the first non null value it gets.
            // I swear if I could get my hands on the person who did that.. :-)
            _localContext=newValue;
            log.debug("SetLocalContext:"+newValue);
        }
        super.setLocalContext(newValue);
        if(newValue != null)
            newValue.takeValueForKey(keyPathsWithValidationExceptions, "keyPathsWithValidationExceptions");
        else
            log.warn("D2WContext was null!");
    }

    /** Should exceptions be propagated through to the parent page. If false, the validation errors are not shown at all. */
    public boolean shouldPropagateExceptions() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldPropogateExceptions")); }
    
    /** Should exceptions also be handled here or only handled by the parent.*/
    public boolean shouldCollectValidationExceptions() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldCollectValidationExceptions")); }

    /** Clears all of the collected validation exceptions. Implementation of the {@see ERXExceptionHolder} interface. */
    public void clearValidationFailed() {
        errorMessages.removeAllObjects();
        errorKeyOrder.removeAllObjects();
        keyPathsWithValidationExceptions.removeAllObjects();
    }

    /** Should incorrect values still be set into the EO. If not set, then the user must re-enter them. */
    public boolean shouldSetFailedValidationValue() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldSetFailedValidationValue"));
    }

    /** Used to hold a cleaned-up validation key and message. */
    private NSMutableDictionary _temp = new NSMutableDictionary();

    /** Handles validation errors. */
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        if (validationLog.isDebugEnabled())
            validationLog.debug("Validation failed with exception: " + e + " value: " + value + " keyPath: " + keyPath);
        if (shouldCollectValidationExceptions()) {
            if (e instanceof ERXValidationException) {
                ERXValidationException erv = (ERXValidationException)e;
                erv.setContext(d2wContext());
                errorKeyOrder.addObject(d2wContext().displayNameForProperty());
                errorMessages.setObjectForKey(erv.getMessage(), d2wContext().displayNameForProperty());
                if (erv.eoObject() != null && erv.propertyKey() != null && shouldSetFailedValidationValue()) {
                    erv.eoObject().takeValueForKeyPath(value, erv.propertyKey());
                }   
            } else {
                _temp.removeAllObjects();
                ERXValidation.validationFailedWithException(e, value, keyPath, _temp, propertyKey(), ERXLocalizer.currentLocalizer(), d2wContext().entity(), shouldSetFailedValidationValue());
                errorKeyOrder.addObjectsFromArray(_temp.allKeys());
                errorMessages.addEntriesFromDictionary(_temp);
            }
            d2wContext().takeValueForKey(errorMessages, "errorMessages");
            if (keyPath != null) {
                // this is set when you have multiple keys failing
                // your keyPath should look like "foo,bar.baz"
                if(keyPath.indexOf(",") > 0) {
                    keyPathsWithValidationExceptions.addObjectsFromArray(NSArray.componentsSeparatedByString(keyPath,","));
                } else {
                    keyPathsWithValidationExceptions.addObject(keyPath);
                }
            }
        } else if (parent() != null && shouldPropagateExceptions()) {
            parent().validationFailedWithException(e, value, keyPath);
        }
    }

    /** Checks if there is a validation exception in the D2WContext for the current property key. */
    public boolean hasValidationExceptionForPropertyKey() {
        return d2wContext().propertyKey() != null && keyPathsWithValidationExceptions.count() != 0 ?
        keyPathsWithValidationExceptions.containsObject(d2wContext().propertyKey()) : false;
    }
    
    /** This will allow d2w pages to be listed on a per configuration basis in stats collecting. */
    public String descriptionForResponse(WOResponse aResponse, WOContext aContext) {
        String descriptionForResponse = (String)d2wContext().valueForKey("pageConfiguration");
        /*
        if (descriptionForResponse == null)
            log.info("Unable to find pageConfiguration in d2wContext: " + d2wContext());
         */
        return descriptionForResponse != null ? descriptionForResponse : super.descriptionForResponse(aResponse, aContext);
    }

    /** Holds the user info. */
    protected NSMutableDictionary _userInfo = new NSMutableDictionary();
    
    /** Implementation of the {@see ERDUserInfoInterface} */
    public NSMutableDictionary userInfo() { return _userInfo; }

    /** Overridden from the parent for better logging. Also clears validation errors */
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

    /** Overridden from the parent for better logging. */
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

    /** Overridden from the parent for better logging. Reports exceptions in the console for easier debugging. */
    public void appendToResponse(WOResponse r, WOContext c) {
        NDC.push("Page: " + getClass().getName()+ (d2wContext()!= null ? (" - Configuration: "+d2wContext().valueForKey("pageConfiguration")) : ""));
        try {
            super.appendToResponse(r,c);
        } catch(Exception ex) {
            ERDirectToWeb.reportException(ex, d2wContext());
        } finally {
            NDC.pop();
        }
    }

    /** holds the chosen branch */
    protected NSDictionary _branch;

    /**
     * Cover method for getting the choosen branch.
     * @return user choosen branch.
     */
    public NSDictionary branch() { return _branch; }

    /**
     * Sets the user choosen branch.
     * @param branch choosen by user.
     */
    public void setBranch(NSDictionary branch) { _branch = branch; }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    // ENHANCEME: Should be localized
    public String branchName() { return (String)branch().valueForKey("branchName"); }

    /**
     * Calculates the branch choices for the current
     * page. This method is just a cover for calling
     * the method <code>branchChoicesForContext</code>
     * on the current {@link ERDBranchDelegate ERDBranchDelegate}.
     * @return array of branch choices
     */
    public NSArray branchChoices() {
        NSArray branchChoices = null;
        if (nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface) {
            branchChoices = ((ERDBranchDelegateInterface)nextPageDelegate()).branchChoicesForContext(d2wContext());
        } else {
            log.error("Attempting to call branchChoices on a page with a delegate: " + nextPageDelegate() + " that doesn't support the ERDBranchDelegateInterface!");
        }
        return branchChoices;
    }

    /**
     * Determines if this message page should display branch choices.
     * @return if the current delegate supports branch choices.
     */
    public boolean hasBranchChoices() {
        return nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface;
    }
}
