/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class ERXStatelessComponent extends WOComponent {

    public ERXStatelessComponent(WOContext aContext) {
        super(aContext);
    }

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public boolean valueForBooleanBinding(String binding) {
        return valueForBooleanBinding(binding, false);
    }
    public boolean valueForBooleanBinding(String binding, boolean defaultValue) {
        if (hasBinding(binding)) {
            Object o = valueForBinding(binding);
            return (o == null) ? false : ERXUtilities.booleanValue(o);
        } else {
            return defaultValue;
        }
    }
    public boolean valueForBooleanBinding(String binding, ERXUtilities.BooleanOperation defaultValue) {
        if (hasBinding(binding)) {
            Object o = valueForBinding(binding);
            return (o == null) ? false : ERXUtilities.booleanValue(o);
        } else {
            return defaultValue.value();
        }
    }
    
    public Object valueForObjectBinding(String binding) {
        return valueForObjectBinding(binding, null);
    }
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
