/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;

/**
 * Basic collector for direct action additions. This class currectly adds two
 * direct actions: <b>testAction</b> for performing junit tests and <b>log4jAction</b>
 * for re-configuring logging settings at runtime.
 */
public class ERXDirectAction extends WODirectAction {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERXDirectAction.class);

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
    
    /**
        * Sets a System property. This is also active in deployment mode because one might want to change a System property
     * at runtime.
     * @param key the System property key
     * @param value the System property value, can be null or empty
     * @param password must be equal to the password set by the System property er.extensions.ERXDirectAction.ChangeSystemPropertyPassword
     * @return either null when the password is wrong or the key is missing or a new page showing the System properties
     */
    public WOActionResults systemPropertyAction() {
        if (ERXStringUtilities.stringIsNullOrEmpty(System.getProperty("er.extensions.ERXDirectAction.ChangeSystemPropertyPassword"))) {
            //returns null, do not give any feedback like password wrong or disabled
            return null;
        }
        String key = request().stringFormValueForKey("key");
        String value = request().stringFormValueForKey("value");
        String password = request().stringFormValueForKey("password");
        if (!System.getProperty("er.extensions.ERXDirectAction.ChangeSystemPropertyPassword").equals(password)) {
            return null;
        }
        WOResponse r = new WOResponse();
        if (ERXStringUtilities.stringIsNullOrEmpty(key) ) {
            r.appendContentString("key cannot be null or empty old System properties:\n"+System.getProperties());
        } else {
            value = ERXStringUtilities.stringIsNullOrEmpty(value) ? "" : value;
            java.util.Properties p = System.getProperties();
            p.put(key, value);
            System.setProperties(p);
            ERXLogger.configureLogging(System.getProperties());
            r.appendContentString("<html><body>New System properties:<br>");
            for (java.util.Enumeration e = p.keys(); e.hasMoreElements();) {
                Object k = e.nextElement();
                if (k.equals(key)) {
                    r.appendContentString("<b>'"+k+"="+p.get(k)+"'     <= you changed this</b><br>");
                } else {
                    r.appendContentString("'"+k+"="+p.get(k)+"'<br>");
                }
            }
            r.appendContentString("</body></html>");
        }
        return r;
    }
}
