/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

/**
 * Basic collector for direct action additions. This class currectly adds two
 * direct actions: <b>testAction</b> for performing junit tests and <b>log4jAction</b>
 * for re-configuring logging settings at runtime.
 */
public class ERXDirectAction extends WODirectAction {

    /** logging support */
    public final static Category cat = Category.getInstance(ERXDirectAction.class);

    /** holds a reference to the current browser used for this session */
    private ERXBrowser browser;

    /** Public constructor */
    public ERXDirectAction(WORequest r) { super(r); }

    /**
     * Action used for junit tests. This method is only active when WOCachingEnabled is
     * disabled (we take this to mean that the application is not in production).<br/>
     * <br/>
     * Synopsis:<br/>
     * pw=<i>aPassword</i>&case=<i>classNameOfTestCase</i>
     * <br/>
     * Form Values:<br/>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXJUnitPassword</code>.
     * <b>case</b> class name for unit test to be performed.<br/>
     * <br/>
     * @return {@link er.testrunner.ERXWOTestInterface ERXWOTestInterface} 
     * with the results after performing the given test.
     */
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

    /**
     * Action used for changing logging settings at runtime. This method is only active
     * when WOCachingEnabled is disabled (we take this to mean that the application is
     *                                    not in production).<br/>
     * <br/>
     * Synopsis:<br/>
     * pw=<i>aPassword</i>
     * <br/>
     * Form Values:<br/>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXLog4JPassword</code>.
     * <br/>
     * @return {@link ERXLog4JConfiguration} for modifying current logging settings.
     */
    public WOComponent log4jAction() {
        WOComponent result=null;
        if (!WOApplication.application().isCachingEnabled() ||
            ERXExtensions.safeEquals(request().stringFormValueForKey("pw"), System.getProperty("er.extensions.ERXLog4JPassword")))
            result=pageWithName("ERXLog4JConfiguration");
        return result;
    }


    public WOComponent logoutAction() {
        if (existingSession()!=null) {
            existingSession().terminate();
        }
        WORedirect r=(WORedirect)pageWithName("WORedirect");
        r.setUrl(context().directActionURLForActionNamed("default", null));
        return r;
    }
    
    /**
     * Returns the browser object representing the web 
     * browser's "user-agent" string. You can obtain 
     * browser name, version, platform and Mozilla version, etc. 
     * through this object. <br>
     * Good for WOConditional's condition binding to deal 
     * with different browser versions. 
     * @return browser object
     */
    public ERXBrowser browser() { 
        if (browser == null  &&  request() != null) {
            ERXBrowserFactory browserFactory = ERXBrowserFactory.factory();
            browser = browserFactory.browserMatchingRequest(request());
            browserFactory.retainBrowser(browser);
        }
        return browser; 
    }

    public WOActionResults performActionNamed(String actionName) {
        WOActionResults actionResult = super.performActionNamed(actionName);
        if (browser != null) 
            ERXBrowserFactory.factory().releaseBrowser(browser);
        return actionResult;
    }
    
}
