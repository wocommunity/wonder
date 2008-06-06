/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Superclass for most of the custom edit components.  <br />
 * 
 */

public abstract class ERDCustomEditComponent extends ERDCustomComponent {

    /** logging support */
    public final static Logger log = Logger.getLogger(ERDCustomEditComponent.class);

    /** interface for all the keys used in this pages code */
    public static interface Keys {
        public static final String object = "object";
        public static final String localContext = "localContext";
        public static final String permissionToEdit = "permissionToEdit";
    }

    /**
     * Public constructor
     * @param context current context
     */
    public ERDCustomEditComponent(WOContext context) {
        super(context);
    }

    //////////////////////////////////////// Instance Methods ////////////////////////////////////////////////////////////    
    private EOEnterpriseObject object;
    protected EOEditingContext editingContext;
    
    public Object objectPropertyValue() {
        return objectKeyPathValue();
    }
    public void setObjectPropertyValue(Object newValue) {
        setObjectKeyPathValue(newValue);
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
    public EOEnterpriseObject object() {
        if (object==null && !synchronizesVariablesWithBindings())
            object=(EOEnterpriseObject)valueForBinding(Keys.object);
        return object;
    }
    
    public boolean isStateless() { return false; }
    public boolean synchronizesVariablesWithBindings() { return true; }

    // Used by stateless subclasses
    public void reset() {
        super.reset();
        object = null;
        editingContext = null;
    }

    // Defaults to true when not used in a D2W component.  In the rules the default is false.
    public boolean permissionToEdit() {
        return hasBinding(Keys.permissionToEdit) ? booleanValueForBinding(Keys.permissionToEdit) : true;
    }
}