/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

/**
 * Abstract stateless component used as the super class for a number
 * of components within the ER frameworks. Adds a number of nice binding resolution
 * methods.
 */
public abstract class ERXStatelessComponent extends WOComponent {

    /** Public constructor */
    public ERXStatelessComponent(WOContext context) {
        super(context);
    }

    /** component is stateless */
    public boolean isStateless() { return true; }
    
    /** component does not synchronize variables */    
    public boolean synchronizesVariablesWithBindings() { return false; }

    /**
     * Resolves a given binding as a int value. Useful for image sizes and the like.
     * @param binding binding to be resolved as a int value.
     * @param defaultValue default int value to be used if the
     *        binding is not bound.
     * @return result of evaluating binding as a int.
     */
    // RENAMEME: all of the valueForXXX method should be named
    // xxxValueForBinding() like the ones in WORequest.
    public int valueForIntBinding(String binding, int defaultValue) {
        return ERXValueUtilities.intValueForBindingOnComponentWithDefault(binding, this, defaultValue);
    }

    /**
     * Resolves a given binding as a boolean value. Defaults to
     * false.
     * @param binding binding to be resolved as a boolean value.
     * @return result of evaluating binding as a boolean. 
     */
    public boolean valueForBooleanBinding(String binding) {
        return valueForBooleanBinding(binding, false);
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
    public boolean valueForBooleanBinding(String binding, boolean defaultValue) {
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
    public boolean valueForBooleanBinding(String binding, ERXUtilities.BooleanOperation defaultValue) {
        if (hasBinding(binding)) {
            return valueForBooleanBinding(binding, false);
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
    public Object valueForObjectBinding(String binding) {
        return valueForObjectBinding(binding, null);
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
    public Object valueForObjectBinding(String binding, Object defaultValue) {
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
}
