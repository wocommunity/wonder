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

    public WOComponent testAction() {
        WOComponent result=null;
        if (!WOApplication.application().isCachingEnabled() ||
            ERXExtensions.safeEquals(request().stringFormValueForKey("pw"), System.getProperty("er.extensions.ERXJUnitPassword"))) {
            
            result=pageWithName("ERXWOTestInterface");
            String testCase = request().stringFormValueForKey("case");
            if(testCase != null) {
                result.takeValueForKey(testCase, "theTest");
                // (ak:I wish we could return a direct test result...)
                // return (WOComponent)result.valueForKey("performTest");
            }
        }
             
        return result;
    }
    
    public WOComponent log4jAction() {
        WOComponent result=null;
        if (!WOApplication.application().isCachingEnabled() ||
            ERXExtensions.safeEquals(request().stringFormValueForKey("pw"), System.getProperty("er.extensions.ERXLog4JPassword")))
            result=pageWithName("ERXLog4JConfiguration");
        return result;
    }

    
}
