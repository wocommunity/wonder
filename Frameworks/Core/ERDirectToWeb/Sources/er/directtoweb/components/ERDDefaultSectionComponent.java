/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Displays section name as a string.<br />
 * 
 */

public class ERDDefaultSectionComponent extends WOComponent {

   public ERDDefaultSectionComponent(WOContext context) {super(context);}

    public boolean isStateless() { return true; }

    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding("object"); }
    
    public Object sectionTitle() {
        D2WContext c=(D2WContext)valueForBinding("d2wContext");
        Object result=object();
        boolean computed=false;
        if (result!=null) {
            if (c!=null) {
                String k=(String)c.valueForKey("keyWhenGrouping");
                if (k!=null) {
                    result=((EOEnterpriseObject)result).valueForKey(k);
                    computed=true;
                }
            }
        }
        return result;
    }
}
