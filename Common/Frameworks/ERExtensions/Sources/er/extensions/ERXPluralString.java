/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOContext;

/**
 * Given a count and a string pluralizes the string if count &gt; 1.<br />
 * 
 * @binding value the object name to plurify
 * @binding count the number of objects
 * @binding showNumber 
 */

public class ERXPluralString extends ERXStatelessComponent {

    public ERXPluralString(WOContext aContext) {
        super(aContext);
    }

    public String value() {
        Number c=(Number)valueForBinding("count");
        String value = (String)valueForBinding("value");
        return localizer().plurifiedString(value, c!=null ? c.intValue() : 0);
    }

    public ERXLocalizer localizer() {
        ERXLocalizer l=(ERXLocalizer)valueForBinding("localizer");
        return l!=null ? l : ERXLocalizer.currentLocalizer();
    }
    
    public boolean showNumber() {
        return booleanValueForBinding("showNumber", true);
    }
}
