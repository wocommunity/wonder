/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.buttons;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGuardedObjectInterface;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Nice edit button for editing a toMany relationship in another page.
 * @d2wKey useNestedEditingContext
 * @d2wKey isEntityEditable
 */
public class ERDEditButton extends ERDActionButton {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDEditButton.class);

    public ERDEditButton(WOContext context) {super(context);}
    
    protected EOEnterpriseObject localInstanceOfObject() {
    	Object value = d2wContext().valueForKey("useNestedEditingContext");
    	boolean createNestedContext = ERXValueUtilities.booleanValue(value);
    	return ERXEOControlUtilities.editableInstanceOfObject(object(), createNestedContext);
    }

    public boolean isEditable() {
        boolean result = ERXValueUtilities.booleanValue(d2wContext().valueForKey("isEntityEditable"));
        Object o = object();
        if (o instanceof ERXGuardedObjectInterface) {
            result = result && ((ERXGuardedObjectInterface)o).canUpdate();
        }
        return result;
    }

    public WOComponent editObjectAction() {
        EOEnterpriseObject localObject = localInstanceOfObject();
        String configuration = (String)valueForBinding("editConfigurationName");
        if(log.isDebugEnabled()){
           log.debug("configuration = "+configuration);
        }
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(configuration, session());
        epi.setObject(localObject);
        epi.setNextPage(context().page());
        localObject.editingContext().hasChanges(); // Ensuring it survives.
        return (WOComponent)epi;
    }
}
