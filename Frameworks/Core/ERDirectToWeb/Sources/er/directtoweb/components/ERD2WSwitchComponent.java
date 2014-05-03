/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.directtoweb.D2WSwitchComponent;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;

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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WSwitchComponent.class);
    
    protected transient NSMutableDictionary<String, Object> extraBindings = new NSMutableDictionary<String, Object>(16);

    public ERD2WSwitchComponent(WOContext context) {
        super(context);
    }

    /**
     * Calling super is a bad thing with 5.2. Will perform binding checks that
     * shouldn't be done.
     */
    @Override
    public void awake() {
    }

    //FIXME resetting the caches breaks the context in the embedded component
    public void resetCaches() {
        //log.debug("Resetting caches");
        //takeValueForKey(null,"_task"); // this will break in 5.0 :-)
        //takeValueForKey(null,"_entityName");
        // Finalizing a context is a protected method, hence the utility.
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
        subContext.takeValueForKey(D2WModel.One, D2WModel.FrameKey);
        subContext.takeValueForKey(session(), D2WModel.SessionKey);
    }

    private String _pageConfiguration;

    public void maybeResetCaches() {
        String currentPageConfiguration = (String) valueForBinding("_dynamicPage");
        if (_pageConfiguration != null && currentPageConfiguration != null && !_pageConfiguration.equals(currentPageConfiguration)) {
            resetCaches();
        }
        if (currentPageConfiguration != null) _pageConfiguration = currentPageConfiguration;
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        maybeResetCaches();
        super.appendToResponse(r, c);
    }

    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        maybeResetCaches();
        super.takeValuesFromRequest(r, c);
    }

    private D2WContext _context;
    @Override
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
                _context.takeValueForInferrableKey(lookFromSettings(), D2WModel.LookKey);
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
        D2WContext d2wcontext = ERD2WContext.newContext(wosession);
        d2wcontext.setTask(s);
        d2wcontext.setEntity(eoentity);
        d2wcontext.takeValueForKey(D2WModel.One, D2WModel.FrameKey);
        return d2wcontext;
    }

    public static D2WContext makeSubContextForDynamicPageNamed(String s, WOSession wosession) {
        D2WContext d2wcontext = ERD2WContext.newContext(wosession);
        d2wcontext.setDynamicPage(s);
        // NOTE AK: for whatever reason, when you set a page config
        d2wcontext.setEntity(d2wcontext.entity());
        d2wcontext.setTask(d2wcontext.task());
        d2wcontext.takeValueForKey(D2WModel.One, D2WModel.FrameKey);
        return d2wcontext;
    }

    @Override
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e, value, keyPath);
    }
    
	@Override
	public NSDictionary extraBindings() {
		if(extraBindings == null) {
			extraBindings = new NSMutableDictionary<String, Object>(16);
		}
		
		extraBindings.removeAllObjects();
		Enumeration e = possibleBindings.elements();
		do {
			if (!e.hasMoreElements())
				break;
			String key = (String) e.nextElement();
			if (hasBinding(key)) {
				Object value = valueForBinding(key);
				if (value != null)
					extraBindings.setObjectForKey(value, key);
				else
					extraBindings.setObjectForKey(NSKeyValueCoding.NullValue, key);
			}
		} while (true);
		return extraBindings;
	}

	@Override
	public void setExtraBindings(Object newValue) {
		extraBindings = (NSMutableDictionary) newValue;
		Enumeration e = possibleBindings.elements();
		do {
			if (!e.hasMoreElements())
				break;
			String key = (String) e.nextElement();
			if (hasBinding(key) && associationWithName(key).isValueSettableInComponent(this))
				setValueForBinding(extraBindings.objectForKey(key), key);
		} while (true);
	}

	private WOAssociation associationWithName(String name) {
		WOAssociation result = _keyAssociations.objectForKey(name);
		if (result == null) {
			NSLog.err.appendln(new StringBuilder().append("DirectToWeb - association with name ").append(name)
					.append(" not found on ").append(this).toString());
			throw new IllegalArgumentException(new StringBuilder().append("DirectToWeb - association with name ")
					.append(name).append(" not found on ").append(this).toString());
		} else {
			return result;
		}
	}
}