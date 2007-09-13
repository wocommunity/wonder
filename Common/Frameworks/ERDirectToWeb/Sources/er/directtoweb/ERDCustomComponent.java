/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXConstant;
import er.extensions.ERXExceptionHolder;
import er.extensions.ERXNonSynchronizingComponent;

/**
 * Base class of many custom components.<br />
 * Has alot of nifty features including resolving bindings against the rule system and inherits all the value pulling methods from {@link ERXNonSynchronizingComponent}.
 * Subclasses should be able to run standalone without a D2W context. This is achieved by pulling values first from the bindings, then from the d2wContext and finally from an "extraBindings" binding.
 */

public abstract class ERDCustomComponent extends ERXNonSynchronizingComponent implements ERXExceptionHolder {

    public static interface Keys {
        public static final String key = "key";
        public static final String localContext = "localContext";
        public static final String d2wContext = "d2wContext";
        public static final String extraBindings = "extraBindings";
        public static final String propertyKey = "propertyKey";
    }

    /** logging support */
    public final static Logger log = Logger.getLogger(ERDCustomComponent.class);

    /** Designated constructor */
    public ERDCustomComponent(WOContext context) {
        super(context);
    }

    /** Holds the {@link D2WContext}. */
    private D2WContext d2wContext;

    /** Holds the property key. */
    private String key;

    /** Holds the extra bindings. */
    protected Object extraBindings;

    //CHECKME ak: who needs this?
    protected static Integer TRUE = ERXConstant.OneInteger;
    protected static Integer FALSE = ERXConstant.ZeroInteger;

    /** Sets the {@link D2WContext}. Applies when used inside a D2WCustomComponent.*/
    public void setLocalContext(D2WContext value) {
        setD2wContext(value);
    }

    /** Sets the {@link D2WContext}. Applies when used inside a property key repetition.*/
    public void setD2wContext(D2WContext value) {
        d2wContext = value;
    }

    /** The active {@link D2WContext}. Simply calls to {@link #d2wContext()}*/
    public D2WContext localContext() {
        return d2wContext();
    }

    /** The active {@link D2WContext}.*/
    public D2WContext d2wContext() {
        return d2wContextFromBindings();
    }

    /** Returns the active d2wContext. If the value was not set via KVC, tries to get the value from the bindings if the component is non-syncing */
    protected D2WContext d2wContextFromBindings() {
        if (d2wContext == null && !synchronizesVariablesWithBindings()) {
            d2wContext = (D2WContext)super.valueForBinding(Keys.localContext);
            if(d2wContext == null) {
                d2wContext = (D2WContext)super.valueForBinding(Keys.d2wContext);
            }
        }
        return d2wContext;
    }

    /** Validation Support. Passes errors to the parent. */
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }

    /** Implementation of the {@link ERXExceptionHolder} interface. Clears exceptions in the parent if possible.*/
    public void clearValidationFailed() {
        // Since this component can be used stand alone, we might not necessarily
        // have an exception holder as our parent --> testing
        if (parent() instanceof ERXExceptionHolder)
            ((ERXExceptionHolder)parent()).clearValidationFailed();
    }

    /** @deprecated use booleanValueForBinding() instead */
    public boolean booleanForBinding(String binding) {
        return booleanValueForBinding(binding);
    }

    // CHECKME ak who needs this?
    public Integer integerBooleanForBinding(String binding) {
        return booleanValueForBinding(binding) ? ERDCustomComponent.TRUE : ERDCustomComponent.FALSE;
    }

    /** Checks if the binding can be pulled. If the component is synching, throws an Exception. Otherwise checks the superclass and if the value for the binding is not null.*/
    public boolean hasBinding(String binding) {
        // FIXME:  Turn this check off in production
        if (synchronizesVariablesWithBindings()) {
            throw new IllegalStateException("HasBinding being used in an object of class " + getClass().getName() + " that synchronizesVariablesWithBindings == true");
        }
        return (super.hasBinding(binding) || valueForBinding(binding) != null);
    }

    /** Utility to dump some debug info about this component and its parent */
    protected void logDebugInfo() {
        if (log.isDebugEnabled()) {
            log.debug("***** ERDCustomComponent: this: " + this.getClass().getName());
            log.debug("***** ERDCustomComponent: parent(): + (" + ((parent() == null) ? "null" : parent().getClass().getName()) + ")");
            log.debug("                      " + parent());
            log.debug("***** ERDCustomComponent: parent() instanceof ERDCustomComponent == " + (parent() instanceof ERDCustomComponent));
            log.debug("***** ERDCustomComponent: parent() instanceof D2WCustomComponentWithArgs == " + (parent() instanceof ERD2WCustomComponentWithArgs));
            log.debug("***** ERDCustomComponent: parent() instanceof D2WStatelessCustomComponentWithArgs == " + (parent() instanceof ERD2WStatelessCustomComponentWithArgs));
            log.debug("***** ERDCustomComponent: parent() instanceof D2WCustomQueryComponentWithArgs == " + (parent() instanceof ERDCustomQueryComponentWithArgs));
        }
    }

    /** Utility to pull the value from the components parent, if the parent is a D2W wrapper component. */
    protected Object parentValueForBinding(String binding) {
        WOComponent parent = parent();
        if (parent instanceof ERDCustomComponent ||
            parent instanceof ERD2WCustomComponentWithArgs ||
            parent instanceof ERD2WStatelessCustomComponentWithArgs) {
            log.debug("inside the parent instanceof branch");
            // this will eventually bubble up to a D2WCustomComponentWithArgs, where it will (depending on the actual binding)
            // go to the d2wContext
            return parent.valueForBinding(binding);
        }
        return null;
    }

    /** Utility to pull the value from the components actual bindings. */
    protected Object originalValueForBinding(String binding) {
        return super.valueForBinding(binding);
    }

    /** Utility to pull the value from the {@link D2WContext}. */
    protected Object d2wContextValueForBinding(String binding) {
        return d2wContextFromBindings().valueForKey(binding);
    }

    /** Utility to pull the value from the extra bindings if supplied. */
    protected Object extraBindingsValueForBinding(String binding) {
        if(extraBindings() instanceof NSDictionary)
            return ((NSDictionary)extraBindings()).objectForKey(binding);
        return null;
    }

    /**
     * Fetches an object from the bindings.
     * Tries the actual supplied bindings, the supplied d2wContext, the parent and finally the extra bindings dictionary.
     */
    public Object valueForBinding(String binding) {
        Object value=null;
        logDebugInfo();
        if (super.hasBinding(binding)) {
        		if (log.isDebugEnabled())
        			log.debug("super.hasBinding(binding) == true for binding "+binding);
            value = originalValueForBinding(binding);
        } else if(d2wContextFromBindings() != null) {
    			if (log.isDebugEnabled())
    				log.debug("has d2wContext == true for binding "+binding);
            value = d2wContextValueForBinding(binding);
        } else {
            value = parentValueForBinding(binding);
        }
        if (value == null && binding != null && extraBindings() != null) {
    			if (log.isDebugEnabled())
    				log.debug("inside the extraBindings branch for binding "+binding);
            value = extraBindingsValueForBinding(binding);
        }
        if (log.isDebugEnabled()) {
            if (value != null)
                log.debug("returning " + value.getClass().getName() + ": " + value+" for binding "+binding);
            else
                log.debug("returning value: null for binding "+binding);
        }
        return value;
    }

    /** Used by stateless subclasses. */
    public void reset() {
        super.reset();
        extraBindings = null;
        key = null;
        d2wContext = null;
    }

    /** Sets the extra bindings. */
    public void setExtraBindings(Object value) { extraBindings = value; }

    /** Extra bindings supplied to the component. If this is a dictionary, it will be used for additional bindings.*/
    public Object extraBindings() {
        if (extraBindings == null && !synchronizesVariablesWithBindings())
            extraBindings = super.valueForBinding(Keys.extraBindings);
        return extraBindings;
    }

    /** Sets the property key. */
    public void setKey(String newKey) { key=newKey; }

    /** The active property key. */
    public String key() {
        if(!synchronizesVariablesWithBindings()) {
            if (key==null) {
                key=(String)super.valueForBinding(Keys.key);
            }
        }
        if (key==null && d2wContext() != null) {
            key=(String)d2wContext().valueForKey(Keys.propertyKey);
        }
        return key;
    }

    /** Overridden from superclass to turn on component synching, which is the default. */
    public boolean synchronizesVariablesWithBindings() { return true; }

    /** Is D2W debugging enabled. */
    public boolean d2wDebuggingEnabled() {
        return ERDirectToWeb.d2wDebuggingEnabled(session());
    }

    /** Should the component name be shown. */
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }

    /** Should the property keys be shown. */
    public boolean d2wPropertyKeyDebuggingEnabled() {
        return ERDirectToWeb.d2wPropertyKeyDebuggingEnabled(session());
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        if(!ERDirectToWeb.shouldRaiseException(false)) {
            // in the case where we are non-synchronizing but not stateless, make sure we pull again
            if (!synchronizesVariablesWithBindings() && !isStateless()) {
                reset();
            }
            super.appendToResponse(r,c);
        } else {
            try {
                // in the case where we are non-synchronizing but not stateless, make sure we pull again
                if (!synchronizesVariablesWithBindings() && !isStateless()) {
                    reset();
                }
                super.appendToResponse(r,c);
            } catch(Exception ex) {
                ERDirectToWeb.reportException(ex, d2wContext());
            }
        }
    }
}
