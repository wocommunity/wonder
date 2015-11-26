/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

// Subclasses in look frameworks provide custom images for mandatory fields.
/**
 * Displays a mandatory field marker. Extended in look frameworks to provide custom images.
 * 
 * @binding condition If the condition returns true, will display a '*', if false, it will
 *		display a transparent 1x1 pixel
 */
public class ERXMandatoryFieldMarker extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXMandatoryFieldMarker(WOContext aContext) {
        super(aContext);
    }

    @Override
    public boolean isStateless() { return true; }
}
