/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.appserver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.woextensions.WOEventDisplayPage;
import com.webobjects.woextensions.WOEventSetupPage;
import com.webobjects.woextensions.WOStatsPage;

import er.extensions.ERXExtensions;
import er.extensions.components.ERXStringHolder;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXObjectStoreCoordinator;
import er.extensions.formatters.ERXUnitAwareDecimalFormat;
import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.logging.ERXLog4JConfiguration;
import er.extensions.logging.ERXLogger;
import er.extensions.statistics.ERXStats;

/**
 * Basic collector for direct action additions. All of the actions are password protected, 
 * you need to give an argument "pw" that matches the corresponding system property for the action.
 */
public class ERXDirectAction extends WODirectAction {

    /** logging support */
    public final static Logger log = Logger.getLogger(ERXDirectAction.class);

    /** holds a reference to the current browser used for this session */
    private ERXBrowser browser;

    /** Public constructor */
    public ERXDirectAction(WORequest r) { super(r); }


    /**
     * Checks if the action can be executed.
     * @param passwordKey
     */
    protected boolean canPerformActionWithPasswordKey(String passwordKey) {
    	if(ERXApplication.isDevelopmentModeSafe()) {
    		return true;
    	}
    	String password = ERXProperties.decryptedStringForKey(passwordKey);
    	if(password == null || password.length() == 0) {
    		log.error("Attempt to use action when key is not set: " + passwordKey);
    		return false;
    	}
    	String requestPassword = request().stringFormValueForKey("pw");
    	if(requestPassword == null) {
    		requestPassword = (String) context().session().objectForKey("ERXDirectAction." + passwordKey);
    	} else {
    		context().session().setObjectForKey(requestPassword, "ERXDirectAction." + passwordKey);
    	}
    	if(requestPassword == null || requestPassword.length() == 0) {
    		return false;
    	}
    	return password.equals(requestPassword);
    }

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
        if (canPerformActionWithPasswordKey("er.extensions.ERXJUnitPassword")) {
            
            result=pageWithName("ERXWOTestInterface");
            session().setObjectForKey(Boolean.TRUE, "ERXWOTestInterface.enabled");
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
     * Flushes the component cache to allow reloading components even when WOCachingEnabled=true.
     * 
     * @return "OK"
     */
    public WOActionResults flushComponentCacheAction() {
    	WOResponse response = new WOResponse();
    	if (canPerformActionWithPasswordKey("er.extensions.ERXFlushComponentCachePassword")) {
    		WOApplication.application()._removeComponentDefinitionCacheContents();
    		response.setContent("OK");
    	}
    	return response;
    }

    /**
     * Direct access to WOStats by giving over the password in the "pw" parameter.
     */
    public WOActionResults statsAction() {
        WOStatsPage nextPage = (WOStatsPage) pageWithName("ERXStatisticsPage");
        nextPage.password = context().request().stringFormValueForKey("pw");
        return nextPage.submit();
    }
    
    /**
     * Direct access to reset the stats by giving over the password in the "pw" parameter.  This
     * calls ERXStats.reset();
     * 
     */
    public WOActionResults resetStatsAction() {
    	WOActionResults result = null;
        if (canPerformActionWithPasswordKey("WOStatisticsPassword")) {
        	ERXStats.reset();
        	WORedirect redirect = new WORedirect(context());
        	redirect.setUrl(context().directActionURLForActionNamed("ERXDirectAction/stats", null));
        	result = redirect;
        }
        return result;
    }
    
    /**
     * Direct access to WOEventDisplay by giving over the password in the "pw" parameter.
     */
    public WOActionResults eventsAction() {
        WOEventDisplayPage nextPage = (WOEventDisplayPage) pageWithName("WOEventDisplayPage");
        nextPage.password = context().request().stringFormValueForKey("pw");
        nextPage.valueForKey("submit");
        return nextPage;
    }

    
    /**
     * Direct access to WOEventDisplay by giving over the password in the "pw" 
     * parameter and turning on all events.
     */
    public WOActionResults eventsSetupAction() {
        WOEventSetupPage nextPage = (WOEventSetupPage) pageWithName("WOEventSetupPage");
        nextPage.password = context().request().stringFormValueForKey("pw");
        nextPage.submit();
        nextPage.selectAll();
        return eventsAction();
    }

    
    /**
     * Action used for turning EOAdaptorDebugging output on or off.<br/>
     * <br/>
     * Synopsis:<br/>
     * pw=<i>aPassword</i>
     * <br/>
     * Form Values:<br/>
     * <strong>pw</strong> password to be checked against the system property <code>er.extensions.ERXEOAdaptorDebuggingPassword</code>.<br/>
     * <strong>debug</strong> flag signaling whether to turn EOAdaptorDebugging on or off (defaults to off).  The value should be one of:
     * <ul>
     *  <li>on</li>
     *  <li>true</li>
     *  <li>1</li>
     *  <li>y</li>
     *  <li>yes</li>
     *  <li>off</li>
     *  <li>false</li>
     *  <li>0</li>
     *  <li>n</li>
     *  <li>no</li>
     * <ul>
     * <br/>
     * Note: this action must be invoked against a specific instance (the instance number must be in the request URL).
     * @return a page showing what action was taken (with regard to EOAdaptorDebugging), if any.
     */
    public WOComponent eoAdaptorDebuggingAction() {
        ERXStringHolder result = (ERXStringHolder)pageWithName("ERXStringHolder");
        result.setEscapeHTML(false);

        if (canPerformActionWithPasswordKey("er.extensions.ERXEOAdaptorDebuggingPassword")) {
            String message;
            boolean currentState = ERXExtensions.adaptorLogging();
            int instance = request().applicationNumber();
            if (instance == -1) {
                log.info("EOAdaptorDebuggingAction requested without a specific instance.");
                message = "<p>You must invoke this action on a <em>specific</em> instance.</p>" +
                        "<p>Your url should look like: <code>.../WebObjects/1/wa/...</code>, where '1' would be the first instance of the target application.</p>";
            } else {
                String debugParam = request().stringFormValueForKey("debug");
                log.debug("EOAdaptorDebuggingAction requested with 'debug' param:" + debugParam);
                if (debugParam == null || debugParam.trim().length() == 0) {
                    message = "<p>EOAdaptorDebugging is currently <strong>" + (currentState ? "ON" : "OFF") + "</strong> for instance <strong>" + instance + "</strong>.</p>";
                    message += "<p>To change the setting, provide the 'debug' parameter to this action, e.g.: <code>...eoAdaptorDebugging?debug=on&pw=secret</code></p>";
                } else {
                    if (debugParam.trim().equalsIgnoreCase("on")) {
                        debugParam = "true";
                    } else if (debugParam.trim().equalsIgnoreCase("off")) {
                        debugParam = "false";
                    }

                    boolean desiredState = ERXValueUtilities.booleanValueWithDefault(debugParam, false);
                    log.debug("EOAdaptorDebuggingAction requested 'debug' state change to: '" + desiredState + "' for instance: " + instance + ".");
                    if (currentState != desiredState) {
                        ERXExtensions.setAdaptorLogging(desiredState);
                        message = "<p>Turned EOAdaptorDebugging <strong>" + (desiredState ? "ON" : "OFF") + "</strong> for instance <strong>" + instance + "</strong>.</p>";
                    } else {
                        message = "<p>EOAdaptorDebugging setting <strong>not changed</strong>.</p>";
                    }
                }
            }

            message += "<p><em>Please be mindful of using EOAdaptorDebugging as it may have a large impact on application performance.</em></p>";
            result.setValue(message);
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
        if (canPerformActionWithPasswordKey("er.extensions.ERXLog4JPassword")) {
            	result=pageWithName("ERXLog4JConfiguration");
            	session().setObjectForKey(Boolean.TRUE, "ERXLog4JConfiguration.enabled");
        }
        return result;
    }

    /**
     * Action used for sending shell commands to the server and receive the result
     * <br/>
     * <br/>
     * Synopsis:<br/>
     * pw=<i>aPassword</i>
     * <br/>
     * Form Values:<br/>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXRemoteShellPassword</code>.
     * <br/>
     * @return {@link ERXLog4JConfiguration} for modifying current logging settings.
     */
    public WOComponent remoteShellAction() {
        WOComponent result=null;
        if (canPerformActionWithPasswordKey("er.extensions.ERXRemoteShellPassword")) {
                result=pageWithName("ERXRemoteShell");
                session().setObjectForKey(Boolean.TRUE, "ERXRemoteShell.enabled");
        }
        return result;
    }

    /**
     * Action used for accessing the database console
     * <br/>
     * <br/>
     * Synopsis:<br/>
     * pw=<i>aPassword</i>
     * <br/>
     * Form Values:<br/>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXRemoteShellPassword</code>.
     * <br/>
     * @return {@link ERXLog4JConfiguration} for modifying current logging settings.
     */
    public WOComponent databaseConsoleAction() {
        WOComponent result=null;
        if (canPerformActionWithPasswordKey("er.extensions.ERXDatabaseConsolePassword")) {
                result=pageWithName("ERXDatabaseConsole");
                session().setObjectForKey(Boolean.TRUE, "ERXDatabaseConsole.enabled");
        }
        return result;
    }

    /**
     * Action used for forcing garbage collection. If WOCachingEnabled is true (we take this to mean 
     * that the application is in production) you need to give a password to access it.<br/>
     * <br/>
     * Synopsis:<br/>
     * pw=<i>aPassword</i>
     * <br/>
     * Form Values:<br/>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXGCPassword</code>.
     * <br/>
     * @return short info about free and used memory before and after GC.
     */
    public WOComponent forceGCAction() {
        ERXStringHolder result=(ERXStringHolder)pageWithName("ERXStringHolder");
        if (canPerformActionWithPasswordKey("er.extensions.ERXGCPassword")) {
            Runtime runtime = Runtime.getRuntime();
            ERXUnitAwareDecimalFormat decimalFormatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
            decimalFormatter.setMaximumFractionDigits(2);
           
            String info = "Before: ";
            info += decimalFormatter.format(runtime.maxMemory()) + " max, ";
            info += decimalFormatter.format(runtime.totalMemory()) + " total, ";
            info += decimalFormatter.format(runtime.totalMemory()-runtime.freeMemory()) + " used, ";
            info += decimalFormatter.format(runtime.freeMemory()) + " free\n";
            
            int count = 5;
            if(request().stringFormValueForKey("count") != null) {
            	count = Integer.parseInt(request().stringFormValueForKey("count"));
            }
            ERXExtensions.forceGC(count);
  
            info += "After: ";
            info += decimalFormatter.format(runtime.maxMemory()) + " max, ";
            info += decimalFormatter.format(runtime.totalMemory()) + " total, ";
            info += decimalFormatter.format(runtime.totalMemory()-runtime.freeMemory()) + " used, ";
            info += decimalFormatter.format(runtime.freeMemory()) + " free\n";

            result.setValue(info);
            log.info("GC forced\n"+info);
        }
        return result;
    }

    /**
     * Returns a list of the traces of open editing context locks.  This is only useful if
     * er.extensions.ERXApplication.traceOpenEditingContextLocks is enabled and 
     * er.extensions.ERXOpenEditingContextLocksPassword is set.
     */
    public WOComponent showOpenEditingContextLockTracesAction() {
      ERXStringHolder result = (ERXStringHolder)pageWithName("ERXStringHolder");
      if (canPerformActionWithPasswordKey("er.extensions.ERXOpenEditingContextLockTracesPassword")) {
        result.setEscapeHTML(false);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("<pre>");
        pw.println(ERXEC.outstandingLockDescription());
        pw.println("</pre>");
        pw.println("<hr>");
        pw.println("<pre>");
        pw.println(ERXObjectStoreCoordinator.outstandingLockDescription());
        pw.println("</pre>");
        pw.close();
        result.setValue(sw.toString());
      }
      return result;
    }

    public WOActionResults logoutAction() {
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
     * Synopsis:<br/>
     * pw=<i>aPassword</i>&key=<i>someSystemPropertyKey</i>&value=<i>someSystemPropertyValue</i>
     *
     * @return either null when the password is wrong or a new page showing the System properties
     */
    public WOActionResults systemPropertyAction() {
    	WOResponse r = null;
    	if (canPerformActionWithPasswordKey("er.extensions.ERXDirectAction.ChangeSystemPropertyPassword")) {
    		String key = request().stringFormValueForKey("key");
    		r = new WOResponse();
    		if (ERXStringUtilities.stringIsNullOrEmpty(key) ) {
        		String user = request().stringFormValueForKey("user");
        		Properties props = ERXConfigurationManager.defaultManager().defaultProperties();
        		if(user != null) {
        			System.setProperty("user.name", user);
        			props = ERXConfigurationManager.defaultManager().applyConfiguration(props);
        		}
    			r.appendContentString(ERXProperties.logString(props));
    		} else {
        		String value = request().stringFormValueForKey("value");
    			value = ERXStringUtilities.stringIsNullOrEmpty(value) ? "" : value;
    			java.util.Properties p = System.getProperties();
    			p.put(key, value);
    			System.setProperties(p);
                ERXLogger.configureLoggingWithSystemProperties();
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
    	}
		return r;
    }
    
    /**
     * Opens the localizer edit page if the app is in development mode.
     */
    public WOActionResults editLocalizedFilesAction() {
    	WOResponse r = null;
    	if (ERXApplication.isDevelopmentModeSafe()) {
    		return pageWithName("ERXLocalizationEditor");
    	}
		return r;
    }
    
    
    public WOActionResults dumpCreatedKeysAction() {
    	WOResponse r = new WOResponse();
    	if (ERXApplication.isDevelopmentModeSafe()) {
    		session();
            ERXLocalizer.currentLocalizer().dumpCreatedKeys();
    	}
		return r;
    }
    
    /**
     * Returns an empty response.
     * 
     * @return nothing
     */
    public WOActionResults emptyAction() {
    	WOResponse response = new WOResponse();
    	return response;
    }

    /**
     * To use this, include this line in appendToResponse on any pages with uploads:
     * <code>
     * AjaxUtils.addScriptResourceInHead(context, response, "Ajax", "prototype.js");
     * AjaxUtils.addScriptResourceInHead(context, response, "Ajax", "SafariUploadHack.js");
     * </code>
     *
     * <p>To be called before multi-form submits to get past Safari 3.2.1 and 4.x intermittent hang-ups
     * when posting binary data.  A nice succinct description and solution is posted here:
     * http://blog.airbladesoftware.com/2007/8/17/note-to-self-prevent-uploads-hanging-in-safari
     * The radar ticket is here: https://bugs.webkit.org/show_bug.cgi?id=5760</p>
     *
     * @return simple response to close the connection
     */
    public WOActionResults closeHTTPSessionAction() { 
    	WOResponse response = new WOResponse();
    	response.setContent(""); 
    	response.setHeader("close", "Connection"); 
    	return response; 
    }
        
    @SuppressWarnings("unchecked")
    public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
      return (T) super.pageWithName(componentClass.getName());
    }

	public WOActionResults stopAction() {
    	WOResponse response = new WOResponse();
    	response.setHeader("text/plain", "Content-Type");

		if (ERXApplication.isDevelopmentModeSafe()) {
	    	WOApplication.application().terminate();
			response.setContent("OK");
		} else {
			response.setStatus(401);
		}
		
    	return response;
	}
	
}
