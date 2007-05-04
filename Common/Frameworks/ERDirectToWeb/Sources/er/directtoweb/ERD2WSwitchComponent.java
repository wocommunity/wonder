/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.directtoweb.*;
import com.webobjects.directtoweb.ERD2WUtilities;
import er.extensions.ERXLogger;

// This works around the following bug: if you switch the pageConfiguration and refresh the page,
// the context is not regenerated.  This is only used for nested configurations.

// FIXME: This should be re-written as a Dynamic element.
// the right way to go is probably to use EOSwitchComponent
/**
 * Rewrite of D2WSwitchComponent to not cache the D2WContext.  Useful for nesting configurations.<br />
 * 
 */

public class ERD2WSwitchComponent extends D2WSwitchComponent  {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WSwitchComponent.class);
    
    public ERD2WSwitchComponent(WOContext context) { super(context); }

    /**
     * Calling super is a bad thing with 5.2. Will perform
     * binding checks that shouldn't be done.
     */
    public void awake() {}

	 //FIXME restting the caches breaks the context in the embedded component
    public void resetCaches() {
        //log.debug("Resetting caches");
        //takeValueForKey(null,"_task"); // this will break in 5.0 :-)
        //takeValueForKey(null,"_entityName");
        // Finalizing a context is a protected method, hence the utiltiy.
        //ERD2WUtilities.finalizeContext((D2WContext)valueForKey("subContext"));
        //takeValueForKey(null,"_context");
    }

    private String _pageConfiguration;
    public void maybeResetCaches() {
        String currentPageConfiguration=(String)valueForBinding("_dynamicPage");
        log.debug("currentPageConfiguration="+ currentPageConfiguration + ", _pageConfiguration="+ _pageConfiguration);
        if (_pageConfiguration!=null &&
            currentPageConfiguration!=null &&
            !_pageConfiguration.equals(currentPageConfiguration)) {
            resetCaches();

            //the better thing to do would be to null out the cached subContext in this case and let it be re-created,
            //but it's a private ivar in the parent and there's no setter, so this will have to suffice
            //the reason all this is necessary is that D2WSwitchComponent caches the subContext, and WOSwitchComponent
            //caches the component instances it creates (caching by component name), so in D2W tab pages with custom
            //components you end up with what appears to be the page configuration getting "stuck". resetting it when it
            //should change prevents that.
            D2WContext subContext = subContext();
            if( subContext != null ) {
                subContext.setDynamicPage(currentPageConfiguration);
            }
        }
        if (currentPageConfiguration!=null) _pageConfiguration=currentPageConfiguration;
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        try {
            maybeResetCaches();
            super.appendToResponse(r,c);
        } catch(Exception ex) {
            ERDirectToWeb.reportException(ex, subContext());
        }
    }

    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e, value, keyPath);
    }
}
