/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;

/**
 * Displays the URL in a hyperlink with target set to "_blank".  The target value may be overridden using the 
 * D2W key <code>urlTarget</code>.
 * @d2wKey urlTarget
 */
public class ERD2WDisplayURL extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayURL(WOContext context) { super(context); }

    /**
     * Gets the href property for the displayed hyperlink.
     * @return the href of the hyperlink
     */
    public String href() {
        String href = objectPropertyValue() != null ? objectPropertyValue().toString() : null;
        if(href != null && href.indexOf("://") < 0) {
            href = "http://" + href;
        }
        return href;
    }
    
    /**
     * Gets the string from the 
     * D2W key &lt;propertyKey&gt;
     * @return the display string of the hyperlink
     */
    public String string() {
    	String string = (String) d2wContext().valueForKey(propertyKey());
    	return string != null ? string : (String) objectPropertyValue();
    }

    /**
     * Gets the target for the displayed hyperlink.  The target defaults to "_blank", but can be overridden
     * using the D2W key <code>urlTarget</code>.
     * @return the target of the hyperlink
     */
    @Override
    public String target() {
        String target = (String)d2wContext().valueForKey("urlTarget");
        return target != null ? target : "_blank";
    }
}
