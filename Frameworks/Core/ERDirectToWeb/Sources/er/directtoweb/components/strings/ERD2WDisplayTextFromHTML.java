/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;

import er.extensions.ERXExtensions;

/**
 * Extracts text from html and displays the text.<br />
 * 
 */

public class ERD2WDisplayTextFromHTML extends D2WDisplayString {

    public ERD2WDisplayTextFromHTML(WOContext context) {super(context);}
    
    public String textString() {
        return ERXExtensions.removeHTMLTagsFromString((String)objectPropertyValue());
    }
}
