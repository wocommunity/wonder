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
public class ERD2WSwitchComponent extends D2WSwitchComponent  {

    public ERD2WSwitchComponent(WOContext context) { super(context); }

    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WSwitchComponent.class);

    public void resetCaches() {
        log.debug("Resetting caches");
        takeValueForKey(null,"_task"); // this will break in 5.0 :-)
        takeValueForKey(null,"_entityName");
        // Finalizing a context is a protected method, hence the utiltiy.
        ERD2WUtilities.finalizeContext((D2WContext)valueForKey("_context"));
        takeValueForKey(null,"_context");
    }

    private String _pageConfiguration;
    public void maybeResetCaches() {
        String currentPageConfiguration=(String)valueForBinding("_dynamicPage");
        log.debug("currentPageConfiguration="+ currentPageConfiguration + ", _pageConfiguration="+ _pageConfiguration);
        if (_pageConfiguration!=null &&
            currentPageConfiguration!=null &&
            !_pageConfiguration.equals(currentPageConfiguration))
            resetCaches();
        if (currentPageConfiguration!=null) _pageConfiguration=currentPageConfiguration;
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        maybeResetCaches();
        super.appendToResponse(r, c);
    }

    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e, value, keyPath);
    }
}
