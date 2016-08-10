/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Default custom component used when componentName = "D2WCustomComponentWithArgs" and custom component was not  specified.
 */
public class ERDDefaultCustomComponent extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDDefaultCustomComponent(WOContext context) { super(context); }

    /** logging support */
    public static final Logger log = Logger.getLogger(ERDDefaultCustomComponent.class);

    @Override
    public boolean isStateless() { return true; }

    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding("object"); }
    public String key() { return (String)valueForBinding("key"); }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        log.warn("Using default custom component for object: " + object() + " and key: " + key());
        super.appendToResponse(response, context);
    }

}
