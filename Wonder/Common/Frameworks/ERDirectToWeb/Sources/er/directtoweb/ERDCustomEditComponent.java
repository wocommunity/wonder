/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* CustomEditComponent.java created by patrice on Sat 25-Mar-2000 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;
import ERD2WCustomComponentWithArgs;
import ERD2WStatelessCustomComponentWithArgs;
import ERD2WCustomQueryComponentWithArgs;
import org.apache.log4j.*;

// A VERY USEFUL COMPONENT - has some crusty stuff in it, but look mainly at valueForBinding and hasBinding.
// 	this guy works in conjunction with D2WCustomComponentWithArgs
public abstract class ERDCustomEditComponent extends WOComponent {
   
   public ERDCustomEditComponent(WOContext context) {
        super(context);
    }



    ///////////////////////////  log4j category  ///////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.components.CustomEditComponent");
    ////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////// Static Methods //////////////////////////////////////////////////////////////
    protected static Integer TRUE = ERXConstant.OneInteger;
    protected static Integer FALSE = ERXConstant.ZeroInteger;

    //////////////////////////////////////// Instance Methods ////////////////////////////////////////////////////////////    
    private EOEnterpriseObject object;
    protected EOEditingContext editingContext;
    private String key;
    protected Object extraBindings;

    // Validation Support
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }
    public void clearValidationFailed() {
        // Since this component can be used stand alone, we might not necessarily
        // have an exception holder as our parent --> testing
        if (parent() instanceof ERXExceptionHolder)
            ((ERXExceptionHolder)parent()).clearValidationFailed();
    }
    
    public Object objectPropertyValue() {
        return objectKeyPathValue();
    }
    public Object objectKeyPathValue() {
        return key() != null && object() != null ? object().valueForKeyPath(key()) : null;
    }
    public void setObjectKeyPathValue(Object newValue) {
        if (key() != null && object() != null) object().takeValueForKeyPath(newValue,key());
    }

    public void setObject(EOEnterpriseObject newObject) {
        object=newObject;
        if (object!=null) // making sure the editing context stays alive
            editingContext=object.editingContext();
    }
    public void setKey(String newKey) { key=newKey; }
    
/*  A tiny bit of performance
    public void appendToResponse(WOResponse r, WOContext c) {
        WOComponent parent=parent();
        if (parent() instanceof D2WCustomComponent &&
            !(parent() instanceof D2WCustomComponentWithArgs)) {
            throw new RuntimeException("Validation for "+key()+" will not properly proceed with D2WCustomComponent -- Use D2WCustomComponentWithArgs");
        }
        super.appendToResponse(r,c);
    }
*/
    
    // Seemless integration of extraBindings into valueForBinding and hasBinding.
    // Note that this should only be used for non-synchronizing components

    public boolean isStateless() { return false; }
    public boolean synchronizesVariablesWithBindings() { return true; }
    // Used by stateless subclasses
    public void reset() {
        super.reset();
        extraBindings = null;
        key = null;
        object = null;
    }
    
    public NSDictionary extraBindings() {
        if (extraBindings == null && !synchronizesVariablesWithBindings())
            extraBindings = super.valueForBinding("extraBindings");
        return (NSDictionary)extraBindings;
    }

    public String key() {
        if (key==null && !synchronizesVariablesWithBindings())
            key=(String)super.valueForBinding("key");
        return key;
    }

    public EOEnterpriseObject object() {
        if (object==null && !synchronizesVariablesWithBindings())
            object=(EOEnterpriseObject)super.valueForBinding("object");
        return object;
    }


    public void appendToResponse(WOResponse r, WOContext c) {
        // in the case where we are non-synchronizing but not stateless, make sure we pull again
        if (!synchronizesVariablesWithBindings() && !isStateless()) {
            reset();
        }
        super.appendToResponse(r,c);
    }

    
    public boolean hasBinding(String binding) {
        // FIXME:  Turn this check off in production
        if (synchronizesVariablesWithBindings()) {
            throw new RuntimeException("HasBinding being used in a subclass of CustomEditComponent that synchronizesVariablesWithBindings == true");
        }
        return (super.hasBinding(binding) || valueForBinding(binding) != null);
    }

    // Defaults to true when not used in a D2W component.  In the rules the default is false.
    public boolean permissionToEdit() { return hasBinding("permissionToEdit") ? booleanForBinding("permissionToEdit") : true; }

    /**
        deprecated
     **/
    public boolean booleanForBinding(String binding) {        
        return booleanValueForBinding(binding);
    }
    public boolean booleanValueForBinding(String binding) {
        return  ERXUtilities.booleanValue(valueForBinding(binding));
    }

    public Integer integerBooleanForBinding(String binding) {
        return booleanForBinding(binding) ? ERDCustomEditComponent.TRUE : ERDCustomEditComponent.FALSE;
    }

    public Object valueForBinding(String binding) {
        Object value=null;
        if (cat.isDebugEnabled()) {
            cat.debug("***** CustomEditComponent.valueForBinding(binding = "+binding+")");
            cat.debug("***** CustomEditComponent: parent(): + (" + ((parent() == null) ? "null" : parent().getClass().getName()) + ")");
            cat.debug("                           " + parent());
            cat.debug("***** CustomEditComponent: parent() instanceof CustomEditComponent == " + (parent() instanceof ERDCustomEditComponent));
            cat.debug("***** CustomEditComponent: parent() instanceof D2WCustomComponentWithArgs == " + (parent() instanceof ERD2WCustomComponentWithArgs));
            cat.debug("***** CustomEditComponent: parent() instanceof D2WStatelessCustomComponentWithArgs == " + (parent() instanceof ERD2WStatelessCustomComponentWithArgs));
            cat.debug("***** CustomEditComponent: parent() instanceof D2WCustomQueryComponentWithArgs == " + (parent() instanceof ERD2WCustomQueryComponentWithArgs));
        }
        if (super.hasBinding(binding)) {
            cat.debug("***** CustomEditComponent: super.hasBinding(binding) == true");
            value = super.valueForBinding(binding);
            cat.debug("***** CustomEditComponent: value = " + value);
        } else {
            WOComponent parent=parent();
            if (parent instanceof ERDCustomEditComponent ||
                parent instanceof ERD2WCustomComponentWithArgs ||
                parent instanceof ERD2WStatelessCustomComponentWithArgs ||
                parent instanceof ERD2WCustomQueryComponentWithArgs) {
                cat.debug("***** CustomEditComponent: inside the parent instanceof branch");
                // this will eventually bubble up to a D2WCustomComponentWithArgs, where it will (depending on the actual binding)
                // go to the d2wContext
                value = parent.valueForBinding(binding);
            }
        }
        if (value == null && binding != null && extraBindings() != null) {
            cat.debug("***** CustomEditComponent: inside the extraBindings branch");
            value = extraBindings().objectForKey(binding);
        }
        if (cat.isDebugEnabled()) {
            if (value != null)
                cat.debug("***** CustomEditComponent: returning value: (" + value.getClass().getName() + ")" + value);
            else
                cat.debug("***** CustomEditComponent: returning value: null");
        }
        return value;
    }

}
