/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

// This component right now is only good for displaying an image via the src tag
// Other important d2w keys, imageHeight and imageWidth
/**
 * Displays an image via the src binding.<br />
 * 
 */

public class ERD2WDisplayImage extends D2WDisplayString {
    
    public ERD2WDisplayImage(WOContext context) {
        super(context);
    }
}
