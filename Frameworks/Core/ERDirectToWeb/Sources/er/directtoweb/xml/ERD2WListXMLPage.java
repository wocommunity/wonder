/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.xml;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WListPage;
import er.extensions.eof.ERXConstant;

/**
 * @d2wKey propertyKey
 * @d2wKey groupingKey
 * @d2wKey displayNameForProperty
 * @d2wKey thirdLevelRelationshipKey
 * @d2wKey componentName
 */
public class ERD2WListXMLPage extends ERD2WListPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WListXMLPage(WOContext context) {super(context);}

    public int indexForOffset;

    public Object value() {
        return object().valueForKeyPath((String)d2wContext().valueForKey("propertyKey"));
    }

    public String displayNameForGroupingKey(){
        d2wContext().takeValueForKey(d2wContext().valueForKey("groupingKey"), "propertyKey");
        return (String)d2wContext().valueForKey("displayNameForProperty");
    }

    @Override
    public boolean userPreferencesCanSpecifySorting(){ return false; }


    public String componentName() {
        d2wContext().takeValueForKey(ERXConstant.OneInteger, "frame");
        d2wContext().takeValueForKey(d2wContext().valueForKey("thirdLevelRelationshipKey"), "propertyKey");
        return (String)d2wContext().valueForKey("componentName");
    }
}
