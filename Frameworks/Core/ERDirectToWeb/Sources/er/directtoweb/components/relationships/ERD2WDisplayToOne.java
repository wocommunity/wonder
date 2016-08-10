/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayToOne;

import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Same as original except allows display of noSelectionString if relationship is null.
 * Also, links are disabled if no object exists.
 * 
 * @d2wKey noSelectionString
 * @d2wKey disabled
 */
public class ERD2WDisplayToOne extends D2WDisplayToOne {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayToOne(WOContext context) { super(context); }
    
    @Override
    public Object toOneDescription() {
        Object description = super.toOneDescription();
        return description != null ? description : ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault((String) d2wContext().valueForKey("noSelectionString"));
    }
    
    public boolean isDisabled() {
        return objectPropertyValue() == null || ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("disabled"), false);
    }
}
