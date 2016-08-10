/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.embed;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WInspect;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.delegates.ERD2WEmbeddedComponentActionDelegate;

// Only difference between this component and D2WInspect is that this one uses ERD2WSwitchComponent
/**
 * Uses a the ERD2WSwitchComponent so that this component won't cache the d2w context.  Useful for reusing of pages.
 * 
 * @binding action
 * @binding displayKeys
 * @binding object
 * @binding pageConfiguration
 * @binding entityName
 */

public class ERXD2WInspect extends D2WInspect {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXD2WInspect(WOContext context) { super(context); }
    
    @Override
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e, value, keyPath);
    }
    /**
     * Calling super is a bad thing in 5.2 when used as an embedded inspect. Also causes
     * errors when using deserialized components in 5.4.3
     */
    @Override
    public void awake() {}
    
    public String entityName() {
        String entityName = (String)valueForBinding("entityName");
        if(entityName == null) {
            EOEnterpriseObject eo = (EOEnterpriseObject)valueForBinding("object");
            if(eo != null)
                entityName = eo.entityName();
        }
        return entityName;
    }
    
    /**
     * Overridden to support serialization
     */
    @Override
    public NextPageDelegate newPageDelegate() {
    	return ERD2WEmbeddedComponentActionDelegate.instance;
    }
}
