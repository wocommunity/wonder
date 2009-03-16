/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.foundation.NSData;

/**
 * Displays an image via the src or data binding, with imageHeight and imageWidth from the d2wContext<br />
 * 
 */

public class ERD2WDisplayImage extends D2WDisplayString {
    
    public ERD2WDisplayImage(WOContext context) {
        super(context);
    }
    
    public boolean isData() {
    	return objectPropertyValue() instanceof NSData;
    }
}
