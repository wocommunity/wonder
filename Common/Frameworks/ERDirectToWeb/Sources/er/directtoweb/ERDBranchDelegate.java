/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import er.extensions.ERXDictionaryUtilities;
import er.extensions.ERXLogger;
import er.extensions.ERXUtilities;
import er.extensions.ERXStringUtilities;

/**
 * The branch delegate is used in conjunction with the
 * {@link ERDMessagePageInterface ERDMessagePageInterface} to allow
 * flexible branching for message pages. Branch delegates can
 * only be used with templates that implement the 
 * {@link ERDBranchInterface ERDBranchInterface}.
 */
public abstract class ERDBranchDelegate implements ERDBranchDelegateInterface {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERDBranchDelegate.class);

    /** holds the WOComponent class array used to lookup branch delegate methods */
    // MOVEME: Should belong in a WO constants class
    public final static Class[] WOComponentClassArray = new Class[] { WOComponent.class };
    
    /**
     * Implementation of the {@link NextPageDelegate NextPageDelegate}
     * interface. This method provides the dynamic dispatch based on
     * the selected branch provided by the sender. Will call the 
     * method <branchName>(WOComponent) on itself returning the 
     * result. 
     * @param sender template invoking the branch delegate
     * @return result of dynamic method lookup and execution on itself.
     */
    public final WOComponent nextPage(WOComponent sender) {
        WOComponent nextPage = null;
        if (sender instanceof ERDBranchInterface) {
            String branchName = ((ERDBranchInterface)sender).branchName();
            if( branchName != null ) {
                if (log.isDebugEnabled())
                    log.debug("Branching to branch: " + branchName);
                try {
                    Method m = getClass().getMethod(branchName, WOComponentClassArray);
                    nextPage = (WOComponent)m.invoke(this, new WOComponent[] { sender });
                } catch (InvocationTargetException ite) {
                    log.error("Invocation exception occurred in ERBranchDelegate: " + ite.getTargetException() + " for branch name: " + branchName, ite.getTargetException());
                    throw new NSForwardException(ite.getTargetException());
                } catch (Exception e) {
                    log.error("Exception occurred in ERBranchDelegate: " + e.toString() + " for branch name: " + branchName);
                    throw new NSForwardException(e);
                }
            }
        } else {
            log.warn("Branch delegate being used with a component that does not implement the ERBranchInterface");
        }
        return nextPage;
    }
    
    /**
     * Utility to build branch choice dictionaries in code.
     * @param method name of the method in question
     * @param label label for the button, a beautified method name will be used if set to null.
     * @return NSDictionary suitable as a branch choice.
     */
    protected NSDictionary branchChoiceDictionary(String method, String label) {
    	if(label == null) {
    		label = ERXStringUtilities.displayNameForKey(method);
    	}
    	return ERXDictionaryUtilities.dictionaryWithObjectsAndKeys(new Object [] { method, "branchName", label, "branchButtonLabel"});
    }
    
    /**
     * Calculates which branches to show in the display first
     * asking the context for the key <b>branchChoices</b> if
     * this returns null then using reflection all of the 
     * public methods that take a single WOComponent as a 
     * parameter are returned.
     * @param context current D2W context
     * @return array of branch names.
     */
    public NSArray branchChoicesForContext(D2WContext context) {
        NSArray choices = (NSArray)context.valueForKey("branchChoices");
        if (choices == null || choices.count() == 0) {
            try {
                NSMutableArray methodChoices = new NSMutableArray();
                Method methods[] = getClass().getMethods();
                for (Enumeration e = new NSArray(methods).objectEnumerator(); e.hasMoreElements();) {
                    Method method = (Method)e.nextElement();
                    if (method.getParameterTypes().length == 1 && 
                    method.getParameterTypes()[0] == WOComponent.class && !method.getName().equals("nextPage")) {
                        NSDictionary branch = branchChoiceDictionary(method.getName(), null);
                        methodChoices.addObject(branch);        
                    }
                }
                choices = methodChoices;
            } catch (SecurityException e) {
                log.error("Caught security exception while calculating the branch choices for delegate: " 
                        + this + " exception: " + e.getMessage());
            }
        }
        return choices;
    }
}
