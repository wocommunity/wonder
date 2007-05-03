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

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/**
 *  ERXApplication is the abstract superclass of WebObjects applications
 *  built with the ER frameworks.<br/>
 *  <br/>
 *  Useful enhancements include the ability to change the deployed name of
 *  the application, support for automatic application restarting at given intervals
 *  and more context information when handling exceptions.
 */

public abstract class ERXApplication extends ERXAjaxApplication implements ERXGracefulShutdown.GracefulApplication {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXApplication.class);

    /** request logging support */
    public static final ERXLogger requestHandlingLog = ERXLogger.getERXLogger("er.extensions.ERXApplication.RequestHandling");
    
    private static boolean _wasERXApplicationMainInvoked = false;

    /** 
     * Called when the application starts up and saves the command line 
     * arguments for {@link ERXConfigurationManager}. 
     * 
     * @see WOApplication#main(String[], Class)
     */
    public static void main(String argv[], Class applicationClass) {
        _wasERXApplicationMainInvoked = true;
        ERXConfigurationManager.defaultManager().setCommandLineArguments(argv);
        WOApplication.main(argv, applicationClass);
    }

    /** improved streaming support*/
    public NSMutableArray _streamingRequestHandlerKeys = new NSMutableArray(streamActionRequestHandlerKey());

    /**
     * Installs several bufixes and enhancements to WODynamicElements.
     * Sets the Context class name to "er.extensions.ERXWOContext" if
     * it is "WOContext". Patches ERXWOForm, ERXWOFileUpload, ERXWOText
     * to be used instead of WOForm, WOFileUpload, WOText.
     */
    public void installPatches() {
        ERXPatcher.installPatches();
        if(contextClassName().equals("WOContext"))
            setContextClassName("er.extensions.ERXWOContext");
        if(contextClassName().equals("WOServletContext")
           || contextClassName().equals("com.webobjects.appserver.WOServletContext"))
            setContextClassName("er.extensions.ERXWOServletContext");
        ERXPatcher.setClassForName(ERXWOForm.class, "WOForm");
        ERXPatcher.setClassForName(ERXAnyField.class, "WOAnyField");
        
        // use our localizing string class
        // works around #3574558  
        if(ERXLocalizer.isLocalizationEnabled()) {
            ERXPatcher.setClassForName(ERXWOString.class, "WOString");
            ERXPatcher.setClassForName(ERXWOTextField.class, "WOTextField");
        }
        
        //ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");

        // Fix for 3190479 URI encoding should always be UTF8
        // See http://www.w3.org/International/O-URL-code.html
        // For WO 5.1.x users, please comment this statement to compile.
        com.webobjects.appserver._private.WOURLEncoder.WO_URL_ENCODING = "UTF8";
        
        // WO 5.1 specific patches
        if (ERXProperties.webObjectsVersionAsDouble() < 5.2d) {
            // ERXWOText contains a patch for WOText to not include the value 
            // attribute (#2948062). Fixed in WO 5.2
            ERXPatcher.setClassForName(ERXWOText.class, "WOText");

            // ERXWOFileUpload returns a better warning than throwing a ClassCastException. 
            // Fixed in WO 5.2
            ERXPatcher.setClassForName(ERXWOFileUpload.class, "WOFileUpload");
        }
    }

    /** holds the default model group */
    protected EOModelGroup defaultModelGroup;

    /**
     * Delegate method for the {@link EOModelGroup} class delegate.
     * @return a fixed ERXModelGroup
     */
    public EOModelGroup defaultModelGroup() {
        if(defaultModelGroup == null) {
            defaultModelGroup = ERXModelGroup.modelGroupForLoadedBundles();
        }
        return defaultModelGroup;
    }
    
    /**
     * The ERXApplication contructor.
     */
    public ERXApplication() {
        super();
        if (! ERXConfigurationManager.defaultManager().isDeployedAsServlet()  &&  
            ! _wasERXApplicationMainInvoked) {
            _displayMainMethodWarning();
        }        
        installPatches();

        EOModelGroup.setClassDelegate(this);

        registerRequestHandler(new ERXDirectActionRequestHandler(), directActionRequestHandlerKey());

        Long timestampLag = Long.getLong("EOEditingContextDefaultFetchTimestampLag");
        if (timestampLag != null)
            EOEditingContext.setDefaultFetchTimestampLag(timestampLag.longValue());

        String defaultMessageEncoding = System.getProperty("er.extensions.ERXApplication.DefaultMessageEncoding");
        if (defaultMessageEncoding != null) {
            log.debug("Setting WOMessage default encoding to \"" + defaultMessageEncoding + "\"");
            WOMessage.setDefaultEncoding(defaultMessageEncoding);
        }

        // Configure the WOStatistics CLFF logging since it can't be controled by a property, grrr.
        configureStatisticsLogging();

        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("finishInitialization",
                                                                        ERXConstant.NotificationClassArray),
                                                         WOApplication.ApplicationWillFinishLaunchingNotification,
                                                         null);
        
        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("didFinishLaunching",
                                                                        ERXConstant.NotificationClassArray),
                                                         WOApplication.ApplicationDidFinishLaunchingNotification,
                                                         null);
        ERXEC.setUseUnlocker(useEditingContextUnlocker());

        // Signal handling support
        if (ERXGracefulShutdown.isEnabled()) {
            ERXGracefulShutdown.installHandler();
        }        
    }

    /**
     * Decides whether to use editing context unlocking.
	 * @return true if ECs should be unlocked after each RR-loop
	 */
	public boolean useEditingContextUnlocker() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXApplication.useEditingContextUnlocker", false);
	}

	/**
     * Configures the statistics logging for a given application. By default will log to a
     * file <base log directory>/<WOApp Name>-<host>-<port>.log if the base log path is defined. The
     * base log path is defined by the property <code>er.extensions.ERXApplication.StatisticsBaseLogPath</code>
     * The default log rotation frequency is 24 hours, but can be changed by setting in milliseconds the
     * property <code>er.extensions.ERXApplication.StatisticsLogRotationFrequency</code>
     */
    public void configureStatisticsLogging() {
        String statisticsBasePath = System.getProperty("er.extensions.ERXApplication.StatisticsBaseLogPath");
        if (statisticsBasePath != null) {
            // Defaults to a single day
            int rotationFrequency = ERXProperties.intForKeyWithDefault("er.extensions.ERXApplication.StatisticsLogRotationFrequency", 24*60*60*1000);
            String logPath = statisticsBasePath + File.separator + name() + "-"
                + ERXConfigurationManager.defaultManager().hostName() + "-" + port() + ".log";
            if (log.isDebugEnabled()) {
                log.debug("Configured statistics logging to file path \"" + logPath + "\" with rotation frequency: "
                          + rotationFrequency);                
            }
            statisticsStore().setLogFile(logPath, (long)rotationFrequency);
        }
    }
    
    
    /**
     * Notification method called when the application posts
     * the notification {@link WOApplication#ApplicationWillFinishLaunchingNotification}. 
     * This method calls subclasse's {@link #finishInitialization} method. 
     * 
     * @param n notification that is posted after the WOApplication
     *      has been constructed, but before the application is
     *      ready for accepting requests.
     */
    public final void finishInitialization(NSNotification n) {
        finishInitialization();
    }

    /**
     * Notification method called when the application posts
     * the notification {@link WOApplication#ApplicationDidFinishLaunchingNotification}.
     * This method calls subclasse's {@link #didFinishLaunching} method.
     *
     * @param n notification that is posted after the WOApplication
     *      has finished launching and is ready for accepting requests.
     */    
    public final void didFinishLaunching(NSNotification n) {
        didFinishLaunching();
    }
    
    /**
     * Called when the application posts {@link WOApplication#ApplicationWillFinishLaunchingNotification}.  
     * Override this to perform application initialization. (optional)
     */
    public void finishInitialization() {
        // empty
    }

    /**
     * Called when the application posts {@link WOApplication#ApplicationDidFinishLaunchingNotification}.
     * Override this to perform application specific tasks after the application has been initialized.
     * THis is a good spot to perform batch application tasks.
     */
    public void didFinishLaunching() {
        // empty
    }    
    
    /**
     * The ERXApplication singleton.
     * @return returns the <code>WOApplication.application()</code> cast as an ERXApplication
     */
    public static ERXApplication erxApplication() { return (ERXApplication)WOApplication.application(); }
    
    /**
     *  Adds support for automatic application cycling. Applications can be configured
     *  to cycle in two ways:<br/>
     *  <br/>
     *  The first way is by setting the System property <b>ERTimeToLive</b> to the number
     *  of seconds that the application should be up before terminating. Note that when
     *  the application's time to live is up it will quit calling the method <code>killInstance</code>.<br/>
     *  <br/>
     *  The second way is by setting the System property <b>ERTimeToDie</b> to the number
     *  of seconds that the application should be up before starting to refuse new sessions.
     *  In this case when the application starts to refuse new sessions it will also register
     *  a kill timer that will terminate the application between 30 minutes and 1:30 minutes.<br/>
     */
    public void run() {
        int timeToLive=ERXProperties.intForKey("ERTimeToLive");
        if (timeToLive > 0) {
            log.info("Instance will live "+timeToLive+" seconds.");
            NSLog.out.appendln("Instance will live "+timeToLive+" seconds.");
            NSTimestamp now=new NSTimestamp();
            NSTimestamp exitDate=(new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, timeToLive);
            WOTimer t=new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
            t.schedule();
        }
        int timeToDie=ERXProperties.intForKey("ERTimeToDie");
        if (timeToDie > 0) {
            log.info("Instance will not live past "+timeToDie+":00.");
            NSLog.out.appendln("Instance will not live past "+timeToDie+":00.");
            NSTimestamp now=new NSTimestamp();
            int s=(timeToDie-ERXTimestampUtility.hourOfDay(now))*3600-ERXTimestampUtility.minuteOfHour(now)*60;
            if (s<0) s+=24*3600; // how many seconds to the deadline

            // deliberately randomize this so that not all instances restart at the same time
            // adding up to 1 hour
            s+=(new Random()).nextFloat()*3600;

            NSTimestamp stopDate=now.timestampByAddingGregorianUnits(0, 0, 0, 0, 0, s);
            WOTimer t=new WOTimer(stopDate, 0, this, "startRefusingSessions", null, null, false);
            t.schedule();
        }
        super.run();
    }

    /**
     * Creates the request object for this loop.
     * Overridden to use an {@link ERXRequest} object that fixes a bug
     * with localization.
     * @param aMethod the HTTP method object used to send the request, must be one of "GET", "POST" or "HEAD"
     * @param aURL - must be non-null
     * @param anHTTPVersion - the version of HTTP used
     * @param someHeaders - dictionary whose String keys correspond to header names
     * @param aContent - the HTML content of the receiver
     * @param someInfo - an NSDictionary that can contain any kind of information related to the current response.
     * @return a new WORequest object
     */
    public WORequest createRequest(String aMethod, String aURL,
                                   String anHTTPVersion,
                                   NSDictionary someHeaders, NSData aContent,
                                   NSDictionary someInfo) {

        // Workaround for #3428067 (Apache Server Side Include module will feed 
        // "INCLUDED" as the HTTP version, which causes a request object not to be 
        // created by an excepion.
        if (anHTTPVersion == null || anHTTPVersion.startsWith("INCLUDED")) 
            anHTTPVersion = "HTTP/1.0"; 

        WORequest worequest = new ERXRequest(aMethod, aURL, anHTTPVersion, someHeaders, aContent, someInfo);
        return worequest;
    }

    /** Used to instanciate a WOComponent when no context is available,
     * typically ouside of a session
     *
     * @param pageName - The name of the WOComponent that must be instanciated.
     * @return created WOComponent with the given name
     */
    public static WOComponent instantiatePage (String pageName) {
        // Create a context from a fake request
        WORequest fakeRequest = new ERXRequest("GET", "", "HTTP/1.1", null, null, null);
        WOContext context = application().createContextForRequest( fakeRequest );
        return application().pageWithName(pageName, context);
    }

    /**
     *  Stops the application from handling any new requests. Will still handle
     *  requests from existing sessions.
     */
    public void startRefusingSessions() {
        log.info("Refusing new sessions");
        NSLog.out.appendln("Refusing new sessions");
        refuseNewSessions(true);
    }

    protected WOTimer _killTimer;

    /**
     * Overridden to install/uninstall a timer that will terminate the application
     * in <code>ERTimeToKill</code> seconds from the time this method is called.
     * The timer will get uninstalled if you allow new sessions again during that
     * time span.
     */

    public void refuseNewSessions(boolean value) {
        super.refuseNewSessions(value);
        // we assume that we changed our mind about killing the instance.
        if(_killTimer != null) {
            _killTimer.invalidate();
            _killTimer = null;
        }
        if(isRefusingNewSessions()) {
            int timeToKill=ERXProperties.intForKey("ERTimeToKill");
            if (timeToKill > 0) {
                log.info("Registering kill timer");
                NSTimestamp exitDate=(new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, timeToKill);
                _killTimer=new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
                _killTimer.schedule();
            }
        }
    }

    /**
     *  Killing the instance will log a 'Forcing exit' message and then call <code>System.exit(1)</code>
     */
    public void killInstance() {
        log.info("Forcing exit");
        NSLog.out.appendln("Forcing exit");
        System.exit(1);
    }
    
    /** cached name suffix */
    private String _nameSuffix;
    /** has the name suffix been cached? */
    private boolean _nameSuffixLookedUp=false;
    /**
     *  The name suffix is appended to the current name of the application. This adds the ability to
     *  add a useful suffix to differentuate between different sets of applications on the same machine.<br/>
     *  <br/>
     *  The name suffix is set via the System property <b>ERApplicationNameSuffix</b>.<br/>
     *  <br/>
     *  For example if the name of an application is Buyer and you want to have a training instance
     *  appear with the name BuyerTraining then you would set the ERApplicationNameSuffix to Training.<br/>
     *  <br/>
     *  @return the System property <b>ERApplicationNameSuffix</b> or <code>null</code>
     */
    public String nameSuffix() {
        if (!_nameSuffixLookedUp) {
            _nameSuffix=System.getProperty("ERApplicationNameSuffix");
            _nameSuffix=_nameSuffix==null ? "" : _nameSuffix;
            _nameSuffixLookedUp=true;
        }
        return _nameSuffix;
    }
    /** cached computed name */
    private String _userDefaultName;
    /**
     *  Adds the ability to completely change the applications name by setting the System property
     *  <b>ERApplicationName</b>. Will also append the <code>nameSuffix</code> if one is set.<br/>
     *  <br/>
     *  @return the computed name of the application.
     */
    public String name() {
        if (_userDefaultName==null) {
            _userDefaultName=System.getProperty("ERApplicationName");
            if (_userDefaultName==null) _userDefaultName=super.name();
            if (_userDefaultName!=null) {
                String suffix=nameSuffix();
                if (suffix!=null && suffix.length()>0)
                    _userDefaultName+=suffix;
            }
        }
        return _userDefaultName;
    }

    /**
     *  This method returns {@link WOApplication}'s <code>name</code> method.<br/>
     *  @return the name of the application executable. 
     */
    public String rawName() { return super.name(); }

    /**
     *  Puts together a dictionary with a bunch of useful information relative to the current state when the exception
     *  occurred. Potentially added information:<br/>
     * <ol>
     * <li>the current page name</li>
     * <li>the current component</li>
     * <li>the complete hierarchy of nested components</li>
     * <li>the requested uri</li>
     * <li>the D2W page configuration</li>
     * <li>the previous page list (from the WOStatisticsStore)</li>
     * </ol>
     * <br/>
     * @return dictionary containing extra information for the current context.
     */
    public NSMutableDictionary extraInformationForExceptionInContext(Exception e, WOContext context) {
        NSMutableDictionary extraInfo=new NSMutableDictionary();
        if (context!=null && context.page()!=null) {
            extraInfo.setObjectForKey(context.page().name(), "CurrentPage");
            if (context.component() != null) {
                extraInfo.setObjectForKey(context.component().name(), "CurrentComponent");
                if (context.component().parent() != null) {
                    WOComponent component = context.component();
                    NSMutableArray hierarchy = new NSMutableArray(component.name());
                    while (component.parent() != null) {
                        component = component.parent();
                        hierarchy.addObject(component.name());
                    }
                    extraInfo.setObjectForKey(hierarchy, "CurrentComponentHierarchy");
                }
            }
            extraInfo.setObjectForKey(context.request().uri(), "uri");
            NSSelector d2wSelector = new NSSelector("d2wContext");
                if (d2wSelector.implementedByObject(context.page())) {
                    try {
                       NSKeyValueCoding c = (NSKeyValueCoding)d2wSelector.invoke(context.page());
                        if(c != null) {
                            String pageConfiguration=(String)c.valueForKey("pageConfiguration");
                            if (pageConfiguration!=null) {
                                extraInfo.setObjectForKey(pageConfiguration, "D2W-PageConfiguration");
                            }
                            String propertyKey=(String)c.valueForKey("propertyKey");
                            if (propertyKey!=null) {
                                extraInfo.setObjectForKey(propertyKey, "D2W-PropertyKey");
                            }
                            NSArray displayPropertyKeys=(NSArray)c.valueForKey("displayPropertyKeys");
                            if (displayPropertyKeys != null) {
                            	extraInfo.setObjectForKey(displayPropertyKeys, "D2W-DisplayPropertyKeys");
                            }
                        }
                    } catch(Exception ex) {
                    }
                }
                if (context.hasSession() && context.session().statistics() != null) {
                    extraInfo.setObjectForKey(context.session().statistics(), "PreviousPageList");
                }
        }
        return extraInfo;
    }


    /**
     * Reports an exception. This method only logs the error and could be
     * overriden to return a valid error page.
     * @param exception to be reported
     * @param extraInfo dictionary of extra information about what was
     *		happening when the exception was thrown.
     * @return a valid response to display or null. In that case the superclasses
     *         {@link #handleException(Exception, WOContext)} is called
     */
    public WOResponse reportException(Throwable exception, NSDictionary extraInfo) {
        Throwable t = exception instanceof NSForwardException ? ((NSForwardException) exception).originalException() : exception;
        
        log.error("Exception caught: " + exception.getMessage() + "\nExtra info: " + extraInfo + "\n", t);
        return null;
    }


    /**
     * Workaround for WO 5.2 DirectAction lock-ups.
     * As the super-implementation is empty, it is fairly safe to override here to call
     * the normal exception handling earlier than usual.
     * @see WOApplication#handleActionRequestError(WORequest, Exception, String, WORequestHandler, String, String, Class, WOAction)
     */
    // NOTE: if you use WO 5.1, comment out this method, otherwise it won't compile. 
    public WOResponse handleActionRequestError(WORequest aRequest, Exception exception, String reason, WORequestHandler aHandler, String actionClassName, String actionName, Class actionClass, WOAction actionInstance) {
        return handleException(exception, actionInstance != null ? actionInstance.context() : null);
    }
    
    /**
     * Logs extra information about the current state.
     * @param exception to be handled
     * @param context current context
     * @return the WOResponse of the generated exception page.
     */
    public WOResponse handleException(Exception exception, WOContext context) {
        // We first want to test if we ran out of memory. If so we need to quite ASAP.
        handlePotentiallyFatalException(exception);

        // Not a fatal exception, business as usual.
        NSDictionary extraInfo = extraInformationForExceptionInContext(exception, context);
        WOResponse response = reportException(exception, extraInfo);
        if(response == null)
            response = super.handleException(exception, context);
        return response;
    }

    /**
     * Standard exception page. Also logs error to standard out.
     * @param exception to be handled
     * @param context current context
     * @return the WOResponse of the generic exception page.
     */
    public WOResponse genericHandleException(Exception exception, WOContext context) {
        return super.handleException(exception, context);
    }
    
    /**
     * Handles the potentially fatal OutOfMemoryError by quiting the
     * application ASAP. Broken out into a separate method to make
     * custom error handling easier, ie generating your own error
     * pages in production, etc.
     * @param exception to check if it is a fatal exception.
     */
    public void handlePotentiallyFatalException(Exception exception) {
        Throwable throwable = null;
        if (exception instanceof InvocationTargetException) {
            throwable = ((InvocationTargetException)exception).getTargetException();
        } else if (exception instanceof NSForwardException ) {
            throwable = ((NSForwardException)exception).originalException();
        }
        if (throwable instanceof Error) {
            boolean shouldQuit = true;
            if (throwable instanceof OutOfMemoryError) {
                // We first log just in case the log4j call puts us in a bad state.
                NSLog.err.appendln("Ran out of memory, killing this instance");
                log.error("Ran out of memory, killing this instance");
            } else if (throwable instanceof NoClassDefFoundError) {
                shouldQuit = false;
            } else if (throwable instanceof StackOverflowError) {
                // hm. could we do something reasonable here?
                shouldQuit = false;
            } else {
                // We first log just in case the log4j call puts us in a bad state.
                NSLog.err.appendln("java.lang.Error \"" + throwable.getClass().getName() + "\" occured. Can't recover, I'm killing this instance.");
                log.error("java.lang.Error \"" + throwable.getClass().getName() + "\" occured. Can't recover, I'm killing this instance.", throwable);
            }
            if(shouldQuit)
                Runtime.getRuntime().exit(1);
        }
    }

    /** use the redirect feature */ 
    protected Boolean useComponentActionRedirection; 

    /**
     * Set the <code>er.extensions.ERXComponentActionRedirector.enabled=true</code>
     * property to actually the redirect feature.
     * @return flag if to use the redirect feature
     */
    public boolean useComponentActionRedirection() {
        if(useComponentActionRedirection == null) {
            useComponentActionRedirection = ERXProperties.booleanForKey("er.extensions.ERXComponentActionRedirector.enabled") ? Boolean.TRUE : Boolean.FALSE;
        }
        return useComponentActionRedirection.booleanValue();
    }

    /**
     * Overridden to allow for redirected responses.
     * @param request object
     * @param context object
     */
    public WOActionResults invokeAction(WORequest request, WOContext context) {
        WOActionResults results = super.invokeAction(request, context);
        if(useComponentActionRedirection())
            ERXComponentActionRedirector.createRedirector(results);
        return results;
    }

    /**
     * Overridden to allow for redirected responses.
     * @param response object
     * @param context object
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        if(useComponentActionRedirection()) {
            ERXComponentActionRedirector redirector = ERXComponentActionRedirector.currentRedirector();
            if(redirector != null) {
                redirector.setOriginalResponse(response);
            }
        }
    }

    /**
     * Overridden to allow for redirected responses and null the thread local storage.
     * @param request object
     * @return response
     */
    public WOResponse dispatchRequest(WORequest request) {
        WOResponse response = null;
        if (requestHandlingLog.isDebugEnabled()) {
            requestHandlingLog.debug("Dispatching request: " + request);
        }
        try {
            if(useComponentActionRedirection()) {
                ERXComponentActionRedirector redirector = ERXComponentActionRedirector.redirectorForRequest(request);
                if(redirector == null) {
                    response = super.dispatchRequest(request);
                    redirector = ERXComponentActionRedirector.currentRedirector();
                    if(redirector != null) {
                        response = redirector.redirectionResponse();
                    }
                } else {
                    response = redirector.originalResponse();
                }
            } else {
                response = super.dispatchRequest(request);
            }
        } finally {
            // We always want to get rid of the wocontext key.
            ERXThreadStorage.removeValueForKey("wocontext");
            // We *always* want to unlock left over ECs.
            ERXEC.unlockAllContextsForCurrentThread();
        }
        if (requestHandlingLog.isDebugEnabled()) {
            requestHandlingLog.debug("Returning, encoding: " + response.contentEncoding() + " response: " + response);
        }
        return response;
    }
    

    /**
     * When a context is created we push it into thread local storage.
     * This handles the case for direct actions.
     * @param request the request
     * @return the newly created context
     */
    public WOContext createContextForRequest(WORequest request) {
        WOContext context = super.createContextForRequest(request);
        // We only want to push in the context the first time it is
        // created, ie we don't want to lose the current context
        // when we create a context for an error page.
        if (ERXThreadStorage.valueForKey("wocontext") == null) {
            ERXThreadStorage.takeValueForKey(context, "wocontext");
        }
        return context;
    }

    /**
     * Override to perform any last minute cleanup before the application terminates.
     * See {@class er.extensions.ERXGracefulShutdown ERXGracefulShutdown} for where
     * this is called if signal handling is enabled. Default implementation calls
     * terminate.
     */
    public void gracefulTerminate() {
        terminate();
    }    
    
    /** 
     * Logs the warning message if the main method was not called 
     * during the startup.
     */
    private void _displayMainMethodWarning() {
        log.warn("\n\nIt seems that your application class " 
            + application().getClass().getName() + " did not call "   
            + ERXApplication.class.getName()
            + ".main(argv[], applicationClass) method. "
            + "Please modify your Application.java as the followings so that "
            + ERXConfigurationManager.class.getName() + " can provide its "
            + "rapid turnaround feature completely. \n\n"
            + "Please change Application.java like this: \n" 
            + "public static void main(String argv[]) { \n"
            + "    ERXApplication.main(argv, Application.class); \n"
            + "}\n\n");
    }

    public void registerStreamingRequestHandlerKey(String s) {
        if (!_streamingRequestHandlerKeys.containsObject(s)) _streamingRequestHandlerKeys.addObject(s);
    }

    public boolean isStreamingRequestHandlerKey(String s) {
        return _streamingRequestHandlerKeys.containsObject(s);
    }

    /** use the redirect feature */
    protected Boolean _useSessionStoreDeadlockDetection;

    /**
     * Deadlock in session-store detection.
     * Note that the detection only work in singlethreaded mode, and is mostly
     * useful to find cases when a session is checked out twice in a single RR-loop,
     * which will lead to a session store lockup.
     * Set the <code>er.extensions.ERXApplication.useSessionStoreDeadlockDetection=true</code>
     * property to actually the this feature.
     * @return flag if to use the this feature
     */
    public boolean useSessionStoreDeadlockDetection() {
        if(_useSessionStoreDeadlockDetection == null) {
            _useSessionStoreDeadlockDetection = ERXProperties.booleanForKey("er.extensions.ERXApplication.useSessionStoreDeadlockDetection") ? Boolean.TRUE : Boolean.FALSE;
            if(isConcurrentRequestHandlingEnabled() && _useSessionStoreDeadlockDetection.booleanValue()) {
                log.error("Sorry, useSessionStoreDeadlockDetection does not work with concurrent request handling enabled.");
                _useSessionStoreDeadlockDetection = Boolean.FALSE;
            }
        }
        return _useSessionStoreDeadlockDetection.booleanValue();
    }

    /** holds the info on checked-out sessions */
    private Hashtable _sessions = new Hashtable();

    private static final ERXFormatterFactory formatterProvider = new ERXFormatterFactory();

    /** Holds info about where and who checked out */
    private class SessionInfo {
        Exception _trace = new Exception();
        WOContext _context;
        
        public SessionInfo(WOContext wocontext) {
            _context = wocontext;
        }
        
        public Exception trace() { return _trace; }
        public WOContext context() { return _context; }
        
        public String exceptionMessageForCheckout(WOContext wocontext) {
            String contextDescription = null;
            if (_context != null) {
                contextDescription = "contextId: " + _context.contextID() + " request: " + _context.request();
            } else {
                contextDescription = "<NULL>";
            }

            log.error("There is an error in the session check-out: old context: " + contextDescription, trace());
            if(_context == null) {
                return "Original context was null";
            } else if(_context.equals(wocontext)) {
                return "Same context did check out twice";
            } else {
                return "Context with id '" + wocontext.contextID() + "' did check out again";
            }
        }
    }

    /** Overridden to check the sessions */
    public WOSession createSessionForRequest(WORequest worequest) {
        WOSession wosession = super.createSessionForRequest(worequest);
        if(useSessionStoreDeadlockDetection()) {
            _sessions.put(wosession.sessionID(), new SessionInfo(null));
        }
        return wosession;
    }

    /** Overridden to check the sessions */
    public void saveSessionForContext(WOContext wocontext) {
        if(useSessionStoreDeadlockDetection()) {
            WOSession wosession = wocontext._session();
            if(wosession != null) {
                String sessionID = wosession.sessionID();
                SessionInfo sessionInfo = (SessionInfo)_sessions.get(sessionID);
                if(sessionInfo == null) {
                    throw new IllegalStateException("Check-In of session that was not checked out");
                }
                _sessions.remove(sessionID);
            }
        }
        super.saveSessionForContext(wocontext);
    }

    /** Overridden to check the sessions */
    public WOSession restoreSessionWithID(String sessionID, WOContext wocontext) {
        WOSession session;
        if(useSessionStoreDeadlockDetection()) {
            SessionInfo sessionInfo = (SessionInfo)_sessions.get(sessionID);
            if(sessionInfo != null) {
                throw new IllegalStateException(sessionInfo.exceptionMessageForCheckout(wocontext));
            }
            session = super.restoreSessionWithID(sessionID,wocontext);
            if(session != null) {
                _sessions.put(session.sessionID(), new SessionInfo(wocontext));
            }
        } else {
            session = super.restoreSessionWithID(sessionID,wocontext);
        }
        return session;
    }

    public Number sessionTimeOutInMinutes() {
        return new Integer(sessionTimeOut().intValue() / 60);
    }
    
    public ERXFormatterFactory formatterFactory() {
        return formatterProvider;
    }
}


