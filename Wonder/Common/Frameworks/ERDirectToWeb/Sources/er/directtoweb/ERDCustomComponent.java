/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERCustomComponent.java created by max on Fri 15-Dec-2000 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public abstract class ERDCustomComponent extends WOComponent {

public ERDCustomComponent(WOContext context) {super(context);}

    //////////////////////////////////////// Static Methods //////////////////////////////////////////////////////////////
    protected static Integer TRUE = ERXConstant.OneInteger;
    protected static Integer FALSE = ERXConstant.ZeroInteger;
    
    //////////////////////////////////////// Instance Methods ////////////////////////////////////////////////////////////
    protected EOEnterpriseObject _object;
    protected EOEditingContext _editingContext;
    protected String _key;

    public String key() {
        return (String)(_key == null && !synchronizesVariablesWithBindings() ? super.valueForBinding("key") : _key);
    }

    public EOEnterpriseObject object() {
        return (EOEnterpriseObject)(_object == null && !synchronizesVariablesWithBindings() ?
                                    super.valueForBinding("object") : _object);
    }
    
    public void setObject(EOEnterpriseObject value) {
        _object = value;
        if (_object!=null) // making sure the editing context stays alive
            _editingContext = _object.editingContext();
    }
    
    public Object objectKeyPathValue() {
        return key() != null && object() != null ? object().valueForKeyPath(key()) : null;
    }

    
    public boolean hasBinding(String binding) {
        // FIXME:  Turn this check off in production
        if (synchronizesVariablesWithBindings()) {
            throw new RuntimeException("HasBinding being used in a subclass of CustomEditComponent that synchronizesVariablesWithBindings == true");
        }
        return (valueForBinding(binding) != null);
    }
    
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

    public boolean booleanForBinding(String binding) {
        boolean bool = false;
        Object booleanBinding = valueForBinding(binding);
        if (booleanBinding == null) {
            // Need this because binding NO to a binding results in the component having a binding but the value being null.  Go figure.
            bool = !super.hasBinding(binding);
        } else if (booleanBinding instanceof Integer) {
            bool = ((Integer)booleanBinding).intValue() == 1;
        } else if (booleanBinding instanceof String && ((String)booleanBinding).equals("YES") ||
                   ((String)booleanBinding).equals("1")) {
            bool = true;
        }
        return bool;
    }

    public Integer integerBooleanForBinding(String binding) {
        return booleanForBinding(binding) ? ERDCustomComponent.TRUE : ERDCustomComponent.FALSE;
    }

    public Object valueForBinding(String binding) {
        Object value=null;
        if (super.hasBinding(binding)) {
            value = super.valueForBinding(binding);
        } else if (parent() instanceof ERDCustomEditComponent || parent() instanceof ERD2WCustomComponentWithArgs) {
            // this will eventually bubble up to a D2WCustomComponentWithArgs, where it will (depending on the actual binding)
            // go to the d2wContext
            value = parent().valueForBinding(binding);
        }
        return value;
    }
}
