/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXDirectAction.java created by patrice on Thu 08-Nov-2001 */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

public class ERXDirectAction extends WODirectAction {

    ////////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXDirectAction.class);

    public ERXDirectAction(WORequest r) { super(r); }

    public WOComponent markReadAction() {
        return null;
    }

    public WOComponent testAction() {
        // FIXME: password protection?
        WOComponent result=null;
        if (!WOApplication.application().isCachingEnabled() ||
            !ERXExtensions.safeEquals(request().formValueForKey("pw"), System.getProperty("ERXLog4jPassword"))) {
            
            result=pageWithName("ERXWOTestInterface");
            String whichTest = context().request().stringFormValueForKey("case");
            if(whichTest != null) {
                result.takeValueForKey(whichTest, "theTest");
            }
        }
             
        return result;
    }
    
    public WOComponent log4jAction() {
        // FIXME: password protection?
        WOComponent result=null;
        if (!WOApplication.application().isCachingEnabled() ||
            !ERXExtensions.safeEquals(request().formValueForKey("pw"), System.getProperty("ERXLog4jPassword")))
            result=pageWithName("ERXLog4JConfiguration");
        return result;
    }

    
}
