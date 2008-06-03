/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WInspect;
import com.webobjects.eocontrol.EOEnterpriseObject;

// Only difference between this component and D2WInspect is that this one uses ERD2WSwitchComponent
/**
 * Uses a the ERD2WSwitchComponent so that this component won't cache the d2w context.  Useful for reusing of pages.<br />
 * 
 * @binding action
 * @binding displayKeys
 * @binding object
 * @binding pageConfiguration
 * @binding entityName
 */

public class ERXD2WInspect extends D2WInspect {

    public ERXD2WInspect(WOContext context) { super(context); }
    
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e, value, keyPath);
    }
    /**
     * Calling super is a bad thing in 5.2 when used as an embedded inspect.
     */
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
}
