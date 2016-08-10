/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * ERXDirectActionImage is the same as a ERXDirectActionHyperlink, but the direct action
 * response have to return a URL to an image, that URL will be used as the source of the
 * image to display with this component.
 * 
 */

public class ERXDirectActionImage extends ERXDirectActionHyperlink {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXDirectActionImage(WOContext context) {
        super(context);
    }

}
