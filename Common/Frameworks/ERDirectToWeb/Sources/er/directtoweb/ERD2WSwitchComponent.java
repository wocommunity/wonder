/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

import er.extensions.*;

// This works around the following bug: if you switch the pageConfiguration and refresh the page,
// the context is not regenerated.  This is only used for nested configurations.

// FIXME: This should be re-written as a Dynamic element.
// the right way to go is probably to use EOSwitchComponent
/**
 * Rewrite of D2WSwitchComponent to not cache the D2WContext. Useful for nesting
 * configurations. <br />
 *  
 */

public class ERD2WSwitchComponent extends D2WSwitchComponent {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WSwitchComponent.class);

    public ERD2WSwitchComponent(WOContext context) {
        super(context);
    }

    /**
     * Calling super is a bad thing with 5.2. Will perform binding checks that
     * shouldn't be done.
     */
    public void awake() {
    }

    //FIXME restting the caches breaks the context in the embedded component
    public void resetCaches() {
        //log.debug("Resetting caches");
        //takeValueForKey(null,"_task"); // this will break in 5.0 :-)
        //takeValueForKey(null,"_entityName");
        // Finalizing a context is a protected method, hence the utiltiy.
        //ERD2WUtilities.finalizeContext((D2WContext)valueForKey("subContext"));
        //takeValueForKey(null,"_context");

        //HACK HACK HACK ak: When you have several embedded list components in
        // a tab page
        // D2W gets very confused about the keys. It will assume that the
        // objects on the second tab somehow belong to the first
        // resetting the cache when setting a new page configuration prevents
        // this
        D2WContext subContext = (D2WContext) valueForKey("subContext");
        ERD2WUtilities.resetContextCache(subContext);
        subContext.setDynamicPage((String) valueForBinding("_dynamicPage"));
    }

    private String _pageConfiguration;

    public void maybeResetCaches() {
        String currentPageConfiguration = (String) valueForBinding("_dynamicPage");
        if (_pageConfiguration != null && currentPageConfiguration != null && !_pageConfiguration.equals(currentPageConfiguration)) {
            resetCaches();
        }
        if (currentPageConfiguration != null) _pageConfiguration = currentPageConfiguration;
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        try {
            maybeResetCaches();
            super.appendToResponse(r, c);
        } catch (Exception ex) {
            ERDirectToWeb.reportException(ex, subContext());
        }
    }

    private D2WContext _context;
    public D2WContext subContext() {
        if (_context == null) {
            String s = hasBinding("_dynamicPage") ? (String) valueForBinding("_dynamicPage") : null;
            if (s != null) {
                _context = makeSubContextForDynamicPageNamed(s, session());
            } else {
                _context = makeSubContextForTaskAndEntity(task(), EOModelGroup.defaultGroup().entityNamed(entityName()), session());
            }
            String s1 = lookFromSettings();
            if (s1 != null) {
                _context.takeValueForInferrableKey(lookFromSettings(), "look");
            }
            _context.takeValueForKey(_context.task() + "CurrentObject", D2WComponent
                    .keyForGenerationReplacementForVariableNamed("currentObject"));
        }
        NSDictionary nsdictionary = settings();
        if (nsdictionary != null) {
            String s2;
            for (Enumeration enumeration = nsdictionary.keyEnumerator(); enumeration.hasMoreElements(); _context.takeValueForInferrableKey(
                    nsdictionary.valueForKey(s2), s2)) {
                s2 = (String) enumeration.nextElement();
            }

        }

        if (log.isDebugEnabled()) log.debug(hashCode() + ": context: " + _context);
        return _context;
    }

    public static D2WContext makeSubContextForTaskAndEntity(String s, EOEntity eoentity, WOSession wosession) {
        D2WContext d2wcontext = new ERD2WContext(wosession);
        d2wcontext.setTask(s);
        d2wcontext.setEntity(eoentity);
        d2wcontext.takeValueForKey(D2WModel.One, "frame");
        return d2wcontext;
    }

    public static D2WContext makeSubContextForDynamicPageNamed(String s, WOSession wosession) {
        D2WContext d2wcontext = new ERD2WContext(wosession);
        d2wcontext.setDynamicPage(s);
        d2wcontext.takeValueForKey(D2WModel.One, "frame");
        return d2wcontext;
    }

    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e, value, keyPath);
    }
}