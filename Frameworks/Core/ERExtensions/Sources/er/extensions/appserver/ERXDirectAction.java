/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.appserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.woextensions.WOEventDisplayPage;
import com.webobjects.woextensions.WOEventSetupPage;
import com.webobjects.woextensions.WOStatsPage;

import er.extensions.ERXExtensions;
import er.extensions.components.ERXLocalizationEditor;
import er.extensions.components.ERXRemoteShell;
import er.extensions.components.ERXStringHolder;
import er.extensions.eof.ERXDatabaseConsole;
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
import er.extensions.statistics.ERXStatisticsPage;
import er.extensions.statistics.ERXStats;
import er.testrunner.ERXWOTestInterface;

/**
 * Basic collector for direct action additions. All of the actions are password protected, 
 * you need to give an argument "pw" that matches the corresponding system property for the action.
 */
public class ERXDirectAction extends WODirectAction {
    private final static Logger log = LoggerFactory.getLogger(ERXDirectAction.class);

    /** holds a reference to the current browser used for this session */
    private ERXBrowser browser;

    public ERXDirectAction(WORequest r) { super(r); }


    /**
     * Checks if the action can be executed.
     * 
     * @param passwordKey the password to test
     * @return <code>true</code> if action is allowed to be invoked
     */
    protected boolean canPerformActionWithPasswordKey(String passwordKey) {
    	if(ERXApplication.isDevelopmentModeSafe()) {
    		return true;
    	}
    	String password = ERXProperties.decryptedStringForKey(passwordKey);
    	if(password == null || password.length() == 0) {
    		log.error("Attempt to use action when key is not set: {}", passwordKey);
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
     * disabled (we take this to mean that the application is not in production).
     * <h3>Synopsis:</h3>
     * pw=<i>aPassword</i>&amp;case=<i>classNameOfTestCase</i>
     * <h3>Form Values:</h3>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXJUnitPassword</code>.
     * <b>case</b> class name for unit test to be performed.
     * 
     * @return {@link er.testrunner.ERXWOTestInterface ERXWOTestInterface} 
     * with the results after performing the given test.
     */
    public WOActionResults testAction() {
        if (canPerformActionWithPasswordKey("er.extensions.ERXJUnitPassword")) {
        	ERXWOTestInterface result = pageWithName(ERXWOTestInterface.class);
            session().setObjectForKey(Boolean.TRUE, "ERXWOTestInterface.enabled");
            String testCase = request().stringFormValueForKey("case");
            if(testCase != null) {
                result.theTest = testCase;
                // (ak:I wish we could return a direct test result...)
                // return (WOComponent)result.valueForKey("performTest");
            }
            return result;
        }
        return forbiddenResponse();
    }
    
    /**
     * Flushes the component cache to allow reloading components even when WOCachingEnabled=true.
     * 
     * @return "OK"
     */
    public WOActionResults flushComponentCacheAction() {
    	if (canPerformActionWithPasswordKey("er.extensions.ERXFlushComponentCachePassword")) {
    		WOApplication.application()._removeComponentDefinitionCacheContents();
    		return new ERXResponse("OK");
    	}
    	return forbiddenResponse();
    }

    /**
     * Direct access to WOStats by giving over the password in the "pw" parameter.
     * 
     * @return statistics page
     */
    public WOActionResults statsAction() {
        WOStatsPage nextPage = pageWithName(ERXStatisticsPage.class);
        nextPage.password = context().request().stringFormValueForKey("pw");
        return nextPage.submit();
    }
    
    /**
     * Direct access to reset the stats by giving over the password in the "pw" parameter.  This
     * calls ERXStats.reset();
     * 
     * @return statistics page
     */
    public WOActionResults resetStatsAction() {
        if (canPerformActionWithPasswordKey("WOStatisticsPassword")) {
        	ERXStats.reset();
        	ERXRedirect redirect = pageWithName(ERXRedirect.class);
        	redirect.setDirectActionName("stats");
        	redirect.setDirectActionClass("ERXDirectAction");
        	return redirect;
        }
        return forbiddenResponse();
    }
    
    /**
     * Direct access to WOEventDisplay by giving over the password in the "pw" parameter.
     * 
     * @return event page
     */
    public WOActionResults eventsAction() {
        WOEventDisplayPage nextPage = pageWithName(WOEventDisplayPage.class);
        nextPage.password = context().request().stringFormValueForKey("pw");
        nextPage.submit();
        return nextPage;
    }

    
    /**
     * Direct access to WOEventDisplay by giving over the password in the "pw" 
     * parameter and turning on all events.
     * 
     * @return event setup page
     */
    public WOActionResults eventsSetupAction() {
        WOEventSetupPage nextPage = pageWithName(WOEventSetupPage.class);
        nextPage.password = context().request().stringFormValueForKey("pw");
        nextPage.submit();
        nextPage.selectAll();
        return eventsAction();
    }

    
    /**
     * Action used for turning EOAdaptorDebugging output on or off.
     * <h3>Synopsis:</h3>
     * pw=<i>aPassword</i>
     * <h3>Form Values:</h3>
     * <strong>pw</strong> password to be checked against the system property <code>er.extensions.ERXEOAdaptorDebuggingPassword</code>.<br>
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
     * </ul>
     * <p>
     * Note: this action must be invoked against a specific instance (the instance number must be in the request URL).
     * 
     * @return a page showing what action was taken (with regard to EOAdaptorDebugging), if any.
     */
    public WOActionResults eoAdaptorDebuggingAction() {
        if (canPerformActionWithPasswordKey("er.extensions.ERXEOAdaptorDebuggingPassword")) {
        	ERXStringHolder result = pageWithName(ERXStringHolder.class);
        	result.setEscapeHTML(false);
            String message;
            boolean currentState = ERXExtensions.adaptorLogging();
            int instance = request().applicationNumber();
            if (instance == -1) {
                log.info("EOAdaptorDebuggingAction requested without a specific instance.");
                message = "<p>You must invoke this action on a <em>specific</em> instance.</p>" +
                        "<p>Your url should look like: <code>.../WebObjects/1/wa/...</code>, where '1' would be the first instance of the target application.</p>";
            } else {
                String debugParam = request().stringFormValueForKey("debug");
                log.debug("EOAdaptorDebuggingAction requested with 'debug' param: {}", debugParam);
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
                    log.debug("EOAdaptorDebuggingAction requested 'debug' state change to: '{}' for instance: {}.", desiredState, instance);
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
            return result;
        }

        return forbiddenResponse();
    }
    
    /**
     * Action used for changing logging settings at runtime. This method is only active
     * when WOCachingEnabled is disabled (we take this to mean that the application is
     *                                    not in production).
     * <h3>Synopsis:</h3>
     * pw=<i>aPassword</i>
     * <h3>Form Values:</h3>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXLog4JPassword</code>.
     * 
     * @return {@link ERXLog4JConfiguration} for modifying current logging settings.
     */
    public WOActionResults log4jAction() {
        if (canPerformActionWithPasswordKey("er.extensions.ERXLog4JPassword")) {
        	session().setObjectForKey(Boolean.TRUE, "ERXLog4JConfiguration.enabled");
            return pageWithName(ERXLog4JConfiguration.class);
        }
        return forbiddenResponse();
    }

    /**
     * Action used for sending shell commands to the server and receive the result
     * <h3>Synopsis:</h3>
     * pw=<i>aPassword</i>
     * <h3>Form Values:</h3>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXRemoteShellPassword</code>.
     * 
     * @return {@link ERXLog4JConfiguration} for modifying current logging settings.
     */
    public WOActionResults remoteShellAction() {
        if (canPerformActionWithPasswordKey("er.extensions.ERXRemoteShellPassword")) {
        	session().setObjectForKey(Boolean.TRUE, "ERXRemoteShell.enabled");
            return pageWithName(ERXRemoteShell.class);
        }
        return forbiddenResponse();
    }

    /**
     * Action used for accessing the database console
     * <h3>Synopsis:</h3>
     * pw=<i>aPassword</i>
     * <h3>Form Values:</h3>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXRemoteShellPassword</code>.
     * 
     * @return {@link ERXLog4JConfiguration} for modifying current logging settings.
     */
    public WOActionResults databaseConsoleAction() {
        if (canPerformActionWithPasswordKey("er.extensions.ERXDatabaseConsolePassword")) {
        	session().setObjectForKey(Boolean.TRUE, "ERXDatabaseConsole.enabled");
        	return pageWithName(ERXDatabaseConsole.class);
        }
        return forbiddenResponse();
    }

    /**
     * Action used for forcing garbage collection. If WOCachingEnabled is true (we take this to mean 
     * that the application is in production) you need to give a password to access it.
     * <h3>Synopsis:</h3>
     * pw=<i>aPassword</i>
     * <h3>Form Values:</h3>
     * <b>pw</b> password to be checked against the system property <code>er.extensions.ERXGCPassword</code>.
     * 
     * @return short info about free and used memory before and after GC.
     */
    public WOActionResults forceGCAction() {
        if (canPerformActionWithPasswordKey("er.extensions.ERXGCPassword")) {
        	ERXStringHolder result = pageWithName(ERXStringHolder.class);
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
            log.info("GC forced\n{}", info);
            return result;
        }
        return forbiddenResponse();
    }

    /**
     * Returns a list of the traces of open editing context locks.  This is only useful if
     * er.extensions.ERXEC.traceOpenLocks is enabled and 
     * er.extensions.ERXOpenEditingContextLocksPassword is set.
     * 
     * @return list of lock traces
     */
    public WOActionResults showOpenEditingContextLockTracesAction() {
      if (canPerformActionWithPasswordKey("er.extensions.ERXOpenEditingContextLockTracesPassword")) {
        ERXStringHolder result = pageWithName(ERXStringHolder.class);
        result.setEscapeHTML(false);
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            pw.println("<pre>");
            pw.println(ERXEC.outstandingLockDescription());
            pw.println("</pre>");
            pw.println("<hr>");
            pw.println("<pre>");
            pw.println(ERXObjectStoreCoordinator.outstandingLockDescription());
            pw.println("</pre>");
            result.setValue(sw.toString());
        }
        catch (IOException e) {
            // ignore
        }
        return result;
      }
      return forbiddenResponse();
    }

    /**
     * Will terminate an existing session and redirect to the default action.
     * 
     * @return redirect to default action
     */
    public WOActionResults logoutAction() {
        if (existingSession()!=null) {
            existingSession().terminate();
        }
        ERXRedirect r = pageWithName(ERXRedirect.class);
        r.setDirectActionName("default");
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

    @Override
    public WOActionResults performActionNamed(String actionName) {
        WOActionResults actionResult = super.performActionNamed(actionName);
        if (browser != null) 
            ERXBrowserFactory.factory().releaseBrowser(browser);
        return actionResult;
    }
    
    /**
     * Sets a System property. This is also active in deployment mode because one might want to change a System property
     * at runtime.
     * <h3>Synopsis:</h3>
     * pw=<i>aPassword</i>&amp;key=<i>someSystemPropertyKey</i>&amp;value=<i>someSystemPropertyValue</i>
     * 
     * @return either null when the password is wrong or a new page showing the System properties
     */
    public WOActionResults systemPropertyAction() {
    	if (canPerformActionWithPasswordKey("er.extensions.ERXDirectAction.ChangeSystemPropertyPassword")) {
    		String key = request().stringFormValueForKey("key");
    		ERXResponse r = new ERXResponse();
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
    		return r;
    	}
		return forbiddenResponse();
    }
    
    /**
     * Opens the localizer edit page if the app is in development mode.
     * 
     * @return localizer editor
     */
    public WOActionResults editLocalizedFilesAction() {
    	if (ERXApplication.isDevelopmentModeSafe()) {
    		return pageWithName(ERXLocalizationEditor.class);
    	}
		return null;
    }
    
    /**
     * Will dump all created keys of the current localizer via log4j and
     * returns an empty response.
     * 
     * @return empty response
     */
    public WOActionResults dumpCreatedKeysAction() {
    	if (ERXApplication.isDevelopmentModeSafe()) {
    		session();
            ERXLocalizer.currentLocalizer().dumpCreatedKeys();
            return new ERXResponse();
    	}
    	return null;
    }
    
    /**
     * Returns an empty response.
     * 
     * @return nothing
     */
    public WOActionResults emptyAction() {
    	return new ERXResponse();
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
    	ERXResponse response = new ERXResponse("");
    	response.setHeader("close", "Connection"); 
    	return response; 
    }
        
    @SuppressWarnings("unchecked")
    public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
      return (T) super.pageWithName(componentClass.getName());
    }

    /**
     * Terminates the application when in development.
     * 
     * @return "OK" if application has been shut down
     */
	public WOActionResults stopAction() {
    	ERXResponse response = new ERXResponse();
    	response.setHeader("text/plain", "Content-Type");

		if (ERXApplication.isDevelopmentModeSafe()) {
	    	WOApplication.application().terminate();
			response.setContent("OK");
		} else {
			response.setStatus(401);
		}
		
    	return response;
	}
	
	/**
	 * Creates a response object with HTTP status code 403.
	 * 
	 * @return 403 response
	 */
	protected WOResponse forbiddenResponse() {
		return new ERXResponse(null, ERXHttpStatusCodes.FORBIDDEN);
	}
}
