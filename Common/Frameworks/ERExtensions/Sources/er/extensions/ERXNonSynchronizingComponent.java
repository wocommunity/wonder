/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Abstract non-synchronizing component used as the super class for a number
 * of components within the ER frameworks. Adds a number of nice binding resolution
 * methods.
 */
public abstract class ERXNonSynchronizingComponent extends WOComponent {

    protected NSMutableDictionary _dynamicBindings = null;
    
    /** Public constructor */
    public ERXNonSynchronizingComponent(WOContext context) {
        super(context);
    }

    /** component does not synchronize variables */    
    public boolean synchronizesVariablesWithBindings() { return false; }

    /** component is not stateless */    
    public boolean isStateless() { return false; }

    /**
     * Resolves a given binding as a int value. Useful for image sizes and the like.
     * @param binding binding to be resolved as a int value.
     * @param defaultValue default int value to be used if the
     *        binding is not bound.
     * @return result of evaluating binding as a int.
     */
    public int intValueForBinding(String binding, int defaultValue) {
        return ERXValueUtilities.intValueForBindingOnComponentWithDefault(binding, this, defaultValue);
    }

    /**
     * Resolves a given binding as a boolean value. Defaults to
     * false.
     * @param binding binding to be resolved as a boolean value.
     * @return result of evaluating binding as a boolean. 
     */
    public boolean booleanValueForBinding(String binding) {
        return booleanValueForBinding(binding, false);
    }
    /**
     * Resolves a given binding as a boolean value. 
     * @param binding binding to be resolved as a boolean value.
     * @param defaultValue default boolean value to be used if the
     *        binding is not bound.
     * @return result of evaluating binding as a boolean.
     */
    // CHECKME: from the name of the method, one would think that
    // ERXValueUtilities.booleanValueForBindingOnComponentWithDefault
    // would be the correct method to use, but after reading the comment there, I'm not sure.
    public boolean booleanValueForBinding(String binding, boolean defaultValue) {
        if (hasBinding(binding)) {
            return ERXValueUtilities.booleanValueWithDefault(valueForBinding(binding), false);
        } else {
            return defaultValue;
        }
    }

    /**
     * Resolves a given binding as a boolean value with the option of
     * specifing a boolean operator as the default value.
     * @param binding name of the component binding.
     * @param defaultValue boolean operator to be evaluated if the
     *        binding is not present.
     * @return result of evaluating binding as a boolean.
     */
    public boolean booleanValueForBinding(String binding, ERXUtilities.BooleanOperation defaultValue) {
        if (hasBinding(binding)) {
            return booleanValueForBinding(binding, false);
        } else {
            return defaultValue.value();
        }
    }

    /**
     * Resolves a given binding as an object in the normal fashion of
     * calling <code>valueForBinding</code>. This has the one advantage
     * of being able to resolve the resulting object as
     * a {link ERXUtilities$Operation} if it is an Operation and
     * then returning the result as the evaluation of that operation.
     * @param binding name of the component binding.
     * @return the object for the given binding and in the case that
     *         it is an instance of an Operation the value of that operation.
     */
    public Object objectValueForBinding(String binding) {
        return objectValueForBinding(binding, null);
    }

    /**
     * Resolves a given binding as an object in the normal fashion of
     * calling <code>valueForBinding</code>. This has the one advantage
     * of being able to resolve the resulting object as
     * a {link ERXUtilities$Operation} if it is an Operation and
     * then returning the result as the evaluation of that operation.
     * @param binding name of the component binding.
     * @param defaultValue value to be used if <code>valueForBinding</code>
     *        returns null.
     * @return the object for the given binding and in the case that
     *         it is an instance of an Operation the value of that operation.
     */
    public Object objectValueForBinding(String binding, Object defaultValue) {
        Object result = null;
        if (hasBinding(binding)) {
            Object o = valueForBinding(binding);
            result = (o == null) ? defaultValue : o;
        } else {
            result = defaultValue;
        }
        if (result instanceof ERXUtilities.Operation) {
            result = ((ERXUtilities.Operation)result).value();
        }
        return result;
    }

    /**
     * Retrieves a given binding and if it is not null
     * then returns <code>toString</code> called on the
     * bound object.
     * @param binding to be resolved
     * @return resolved binding in string format
     */
    public String stringValueForBinding(String binding) {
        return stringValueForBinding(binding, null);
    }

    /**
     * Retrieves a given binding and if it is not null
     * then returns <code>toString</code> called on the
     * bound object.
     * @param binding to be resolved
     * @param defaultValue value to be used if <code>valueForBinding</code>
     *        returns null.
     * @return resolved binding in string format
     */
    public String stringValueForBinding(String binding, String defaultValue) {
        Object v=objectValueForBinding(binding, defaultValue);
        return v!=null ? v.toString() : null;
    }
    
    /**
     * Convenience method to get the localizer.
     */
    public ERXLocalizer localizer() {
        return ERXLocalizer.currentLocalizer();
    }
    
    /**
     * Lazily initialized dictionary which can be used for the 'item' binding in
     * a repetition for example: 'item = dynamicBindings.myVariable'. Useful in
     * rapid turnaround modes where adding a iVar would cause hot code swapping
     * to stop working.
     * 
     */
    public NSMutableDictionary dynamicBindings() {
        if (_dynamicBindings == null) {
            _dynamicBindings = new NSMutableDictionary();
        }
        return _dynamicBindings;
    }
    
    public void reset() {
    	super.reset();
    	if(_dynamicBindings != null) {
    		_dynamicBindings.removeAllObjects();
    	}
    }
    
}

