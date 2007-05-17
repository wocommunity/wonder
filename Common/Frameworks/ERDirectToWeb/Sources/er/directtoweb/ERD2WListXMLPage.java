/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;

import er.extensions.ERXConstant;

public class ERD2WListXMLPage extends ERD2WListPage {

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

    public boolean userPreferencesCanSpecifySorting(){ return false; }


    public String componentName() {
        d2wContext().takeValueForKey(ERXConstant.OneInteger, "frame");
        d2wContext().takeValueForKey(d2wContext().valueForKey("thirdLevelRelationshipKey"), "propertyKey");
        return (String)d2wContext().valueForKey("componentName");
    }
}
