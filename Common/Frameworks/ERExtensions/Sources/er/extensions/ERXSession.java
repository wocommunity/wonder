/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Category;
import java.util.Enumeration;

/**
 * The ERXSession aguments the regular WOSession object
 * by adding a few nice additions. Of interest, notifications
 * are now posted when a session when a session
 * goes to sleep, David Neumann's browser backtracking detection
 * has been added from his security framework, a somewhat
 * comprehensive user-agent parsing is provided to know what type
 * of browser is being used, flags have also been added to tell
 * if javascript has been enabled, and enhanced localization
 * support has been added.
 */
public class ERXSession extends WOSession {

    /** logging support */
    public static Category cat = Category.getInstance(ERXSession.class);

    /** Notification name that is posted after a session wakes up. */
    // DELETEME: Now we can use SessionDidRestoreNotification
    public static final String SessionWillAwakeNotification = "SessionWillAwakeNotification";
    /**
     * Notification name that is posted when a session is about to sleep.
     */
    public static final String SessionWillSleepNotification = "SessionWillSleepNotification";

    /** cookie name that if set it means that the user has cookies enabled */
    // FIXME: This should be configurable
    public static final String JAVASCRIPT_ENABLED_COOKIE_NAME = "js";
    
    /** holds a reference to the current localizer used for this session */
    private ERXLocalizer localizer;

    /** holds a reference to the current message encoding used for this session */
    private ERXMessageEncoding messageEncoding;

    /** flag for if java script is enabled, defaults to true */
    protected boolean _javaScriptEnabled=true; // most people have JS by now
    /** flag to indicate if java script has been set */
    protected boolean _javaScriptInitialized=false;

    /** holder for the browser name */
    protected String _browser;
    /** flag if the browser is IE */
    protected boolean isIE;
    /** flag if the browser is Netscape */
    protected boolean isNetscape;
    /** flag if the browser is iCab */
    protected boolean isICab;
    /** flag if the browser is Omniweb */
    protected boolean isOmniweb;
    /** flag if the browser platform is Mac */    
    protected boolean isMac;
    /** flag if the browser platform is Windows */    
    protected boolean isWindows;
    /** flag if the browser platform is Linux */    
    protected boolean isLinux;
    /** flag if the browser is version 3 */
    protected boolean isVersion3;
    /** flag if the browser is version 4 */    
    protected boolean isVersion4;
    /** flag if the browser is version 5 */    
    protected boolean isVersion5;
    // ENHANCEME: Need a version6 test
    /** holds the platform string */
    protected String platform;
    /** holds the version string */    
    protected String version;

    /** holds a debugging store for a given session. */
    protected NSMutableDictionary _debuggingStore;

    /**
     * Method to get the current localizer for this
     * session. If local instance variable is null
     * then a localizer is fetched for the session's
     * <code>languages</code> array. See {@link ERXLocalizer}
     * for more information about using a localizer.
     * @return the current localizer for this session
     */
    public ERXLocalizer localizer() {
        if (localizer == null) {
            localizer = ERXLocalizer.localizerForLanguages(languages());
        }
        return localizer;
    }

    /**
     * Cover method to set the current localizer
     * to the localizer for that language.
     * @param language to set the current localizer
     *		for.
     */
    public void setLanguage(String language) {
        localizer = ERXLocalizer.localizerForLanguage(language);
        ERXLocalizer.setCurrentLocalizer(localizer);
        messageEncoding = new ERXMessageEncoding(localizer.language());
    }

    /**
     * Returns the primary language of the current
     * session's localizer. This method is just a
     * cover for calling the method
     * <code>localizer().language()</code>.
     * @return primary language
     */
    public String language() {
        return localizer().language();
    }

    /**
     * Returns the message encoding of the current session. 
     * If it's not already set up but no current <code>language()</code> 
     * available for the session, it creates one with 
     * the defailt encoding. 
     * @return message encoding object
     */
    public ERXMessageEncoding messageEncoding() { 
        if (messageEncoding == null) 
            messageEncoding = new ERXMessageEncoding(language());
        return messageEncoding; 
    }

    /**
     * Simple mutable dictionary that can be used at
     * runtime to stash objects that can be useful for
     * debugging.
     * @return debugging store dictionary
     */
    // ENHANCEME: Should perform a check to make sure that the app is not in production mode when this is being used.
    public NSMutableDictionary debuggingStore() {
        if (_debuggingStore==null)
            _debuggingStore = new NSMutableDictionary();
        return _debuggingStore;
    }

    /**
     * Ensures that the editing context has a delegate
     * set, if not it will set one via the
     * <code>setDefaultDelegate</code> method.
     * @return the session's default editing context with
     * 		the default delegate set.
     */
    // FIXME: This check should move to the session constructor
    //		also should use the ec factory methods to just
    //		create and set the editing context.
    public EOEditingContext defaultEditingContext() {
        EOEditingContext ec = super.defaultEditingContext();
        if (ec.delegate() == null)
            ERXExtensions.setDefaultDelegate(ec);
        return ec;
    }

    /**
     * Returns if this user has javascript enabled.
     * @return if js is enabled, defaults to true.
     */
    public boolean javaScriptEnabled() { return _javaScriptEnabled; }

    /**
     * Sets if javascript is enabled for this session.
     * crafty entry pages can set form values via
     * javascript to test if it is enabled.
     * @param newValue says if javascript is enabled
     */
    public void setJavaScriptEnabled(boolean newValue) {
        _javaScriptEnabled=newValue;
        _javaScriptInitialized=true;
    }

    // MOVEME: All of this browser stuff should move to it's own class ERXBrowser
    /**
     * Browser string
     * @return what type of browser
     */
    public String browser() { return _browser; }

    /**
     * browser is IE?
     * @return if browser is IE.
     */
    public boolean browserIsIE() { return isIE; }
    /**
     * browser is Omniweb?
     * @return if browser is Omniweb.
     */
    public boolean browserIsOmniweb() { return isOmniweb; }
    /**
     * browser is Netscape?
     * @return if browser is Netscape.
     */
    public boolean browserIsNetscape() { return isNetscape;  }
    /**
     * browser is not Netscape?
     * @return if browser is not Netscape.
     */
    public boolean browserIsNotNetscape() { return !isNetscape; }

    /**
     * browser is not netscape or is a version 5
     * browser.
     * @return if this browser can handle nested tables
     */
    public boolean browserRenderNestedTablesFast() {
        return browserIsNotNetscape() || isVersion5;
    }

    /**
     * Does the browser support IFramew?
     * @return if the browser is IE.
     */
    public boolean browserSupportsIFrames() { return browserIsIE(); }

    /**
     * Overridden to provide all the browser checking for
     * the current User-Agent. Also performs a few checks to
     * see if javascript is enabled.
     */
    public void awake() {
        super.awake();
        ERXExtensions.setSession(this);
        ERXLocalizer.setCurrentLocalizer(localizer());
        NSNotificationCenter.defaultCenter().postNotification(SessionWillAwakeNotification, this);
        WORequest request=context()!=null ? context().request() : null;
        // ENHANCEME: Should pull all of this browser detection out into it's own object with a factory
        //		so that others can return their own subclasses of the browser object or parse the
        //		the user-agent in a different manner. The factory could also maintain a cache of the
        //		user-agent to browser object.
        if (_browser==null && request!=null) {
            String userAgent=(String)request.headerForKey("User-Agent");
            isOmniweb=isICab=isIE=isNetscape=false;
            if (userAgent!=null) {
                if (userAgent.indexOf("OmniWeb")!=-1) {
                    isOmniweb=true;
                    _browser ="iCab";
                } else if (userAgent.indexOf("iCab")!=-1) {
                    isICab=true;
                    _browser ="iCab";
                } else if (userAgent.indexOf("MSIE")!=-1) {
                    isIE=true;
                    _browser ="MSIE";
                } else if (userAgent.indexOf("Mozilla")!=-1) {
                    isNetscape=true;
                    _browser ="Netscape";
                }

                if (userAgent.indexOf("Win")!=-1) {
                    isMac=isLinux=false;
                    isWindows=true;
                    platform="Windows";
                } else if (userAgent.indexOf("Mac")!=-1) {
                    isWindows=isLinux=false;
                    isMac=true;
                    platform="Mac";
                } else if (userAgent.indexOf("Linux")!=-1) {
                    isWindows=isMac=false;
                    isLinux=true;
                    platform="Linux";
                }
                if (userAgent.indexOf("5.0")!=-1 || userAgent.indexOf("5.5")!=-1) {
                    isVersion3=isVersion4=false;
                    isVersion5=true;
                    version="5.0 or 5.5";
                } else if (userAgent.indexOf("4.0")!=-1 ||
                           userAgent.indexOf("4.5")!=-1 || userAgent.indexOf("4.7")!=-1) {
                    isVersion3=isVersion5=false;
                    isVersion4=true;
                    version="4.0, 4.6, 4.7";
                } else if (userAgent.indexOf("3.0")!=-1) {
                    isVersion4=isVersion5=false;
                    isVersion3=true;
                    version="3.0";
                }
            }
            if (_browser==null) _browser="Unknown";
            if (cat.isDebugEnabled()) cat.debug("Browser="+_browser+" platform="+platform+" Version="+version);
        }
        if (request!=null && cat.isDebugEnabled()) cat.debug("Form values "+request.formValues());
        // FIXME: Shouldn't be hardcoded form value.
        if (request!=null && request.formValueForKey("javaScript")!=null) {
            String js=(String)request.formValueForKey("javaScript");
            if (cat.isDebugEnabled()) cat.debug("Received javascript form value "+js);
            setJavaScriptEnabled(js!=null && js.equals("1") && (_browser==null || _browser.indexOf("Netscape3.")==-1));
        }
        if (request!=null && /* !_javaScriptInitialized */true) {
            String js=null;
            try {
                request.cookieValueForKey(JAVASCRIPT_ENABLED_COOKIE_NAME);
            } catch (StringIndexOutOfBoundsException e) {
                // malformed cookies cause WO 5.1.3 to raise here
            }
            if (js!=null) { // a friend sent us a cookie!
                if (cat.isDebugEnabled()) cat.debug("Got JAVASCRIPT_ENABLED_COOKIE from a friend "+js);
                setJavaScriptEnabled(js.equals("1") && (_browser==null || _browser.indexOf("Netscape3.")==-1));
            }
        }
    }

    /**
     * Overridden to post the notification that
     * the session will sleep.
     */
    public void sleep() {
        NSNotificationCenter.defaultCenter().postNotification(SessionWillSleepNotification, this);
        super.sleep();
        ERXLocalizer.setCurrentLocalizer(null);
        ERXExtensions.setSession(null);
    }

    /*
     * Backtrack detection - Pulled from David Neuman's wonderful security framework.
     */

    /**
     * flag to indicate if the user is currently backtracking,
     * meaning they hit the back button and then clicked on a
     * link.
     */
     public boolean didBackTrack = false;
    /** flag to indicate if the last action was a direct action */
    // FIXME: Need to be setting this in ERXDirectAction or maybe we can be smart and set it when the
    //		direct action handler picks it up so that everything won't have to subclass one
    //		direct action class
    public boolean lastActionWasDA = false;
    /**
     * Utility method that gets the context ID string
     * from the passed in request.
     * @param aRequest request to get the context id from
     * @return the context id as a string
     */
    // MOVEME: ERXWOUtilities
    public String requestsContextID(WORequest aRequest){
        String uri = aRequest.uri();
        String eID = NSPathUtilities.lastPathComponent(uri);
        NSArray eIDs = NSArray.componentsSeparatedByString(eID, ".");
        String reqCID = "1";
        if(eIDs.count() > 0) {
            reqCID = (String)eIDs.objectAtIndex(0);
        }
        return reqCID;
    }

    /**
     * Method inspects the passed in request to see if
     * the user backtracked. If the context ID for the request is 2 clicks
     * less than the context ID for the current WOContext, we know
     * the backtracked.
     * @param aRequest request to check
     * @param aContext context to check against request
     * @return if the user has backtracked or not.
     */
    public boolean didBacktrack(WORequest aRequest, WOContext aContext){
        boolean didBacktrack = false;
        int reqCID = Integer.parseInt(requestsContextID(aRequest));
        int cid = Integer.parseInt(aContext.contextID());
        int delta = cid - reqCID;
        if(delta > 2){
            didBacktrack = true;
        }else if(delta > 1){
            // Might not have backtracked if their last
            // action was a direct action. I don't know the best way
            // to deal with this, but one way would include
            // overriding performActionNamed() on your WODirectActions
            // indicating the last request was a DirectAction
            if(!lastActionWasDA){
                didBacktrack = true;
            }
        }
        lastActionWasDA = false;
        return didBacktrack;
    }

    /**
     * Overrides the ComponentAction handler to set the didBackTrack
     * flag by calling the method <code>didBacktrack</code>. 
     * @param aRequest current request
     * @param aContext current context
     * @return super's implementation of <code>invokeAction</code>
     */
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext){
        String reqCID = requestsContextID(aRequest);
        didBackTrack = didBacktrack(aRequest, aContext);
        if (didBackTrack) cat.debug("User backtracking in invokeAction.");
        return super.invokeAction(aRequest, aContext);
    }

    /**
     * Overrides the ComponentAction handler to set the didBackTrack
     * flag by calling the method <code>didBacktrack</code>. 
     * Also provides automatic encoding support for component action 
     * with <code>messageEncoding</code> object.
     * @param aRequest current request
     * @param aContext current context
     * @return super's implementation of <code>invokeAction</code>
     */
    public void takeValuesFromRequest (WORequest aRequest, WOContext aContext){
        String reqCID = requestsContextID(aRequest);
        didBackTrack = didBacktrack(aRequest, aContext);
        if (didBackTrack) cat.debug("User backtracking in takeValuesFromRequest.");
        messageEncoding().setDefaultFormValueEncodingToRequest(aRequest);
        super. takeValuesFromRequest (aRequest, aContext);
    }

    /**
     * Overridden to display debugging information when a
     * user backtracks. 
     * Also provides automatic encoding support for component action 
     * with <code>messageEncoding</code> object.
     * @param aResponse current response object
     * @param aContext current context object
     */
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        if (didBackTrack) cat.debug("User backtracking in appendToResponse.");
        messageEncoding().setEncodingToResponse(aResponse);
        super.appendToResponse(aResponse, aContext);
    }

    /**
     * Ovverrides terminate to set the shared editing context of the default
     * editing context to null to avoid a locking problem in WO 5.1.1.
     */
    public void terminate() {
        // WOFIX: 5.1.2
        // work around a bug in WO 5.1.2 where the sessions EC will keep a lock on the SEC
        defaultEditingContext().setSharedEditingContext(null);
        super.terminate();
    }

    //// Below this point is all the stuff to be deleted ///////

    // DELETEME: don't need navigation
    public ERXSession() {
        // Setting the default editing context delegate
        _navigation = createNavigation();
        _navigation.setIsDisabled(false);
    }

    // DELETEME: Should be removed
    public ERXNavigation createNavigation() { return new ERXNavigation(); }
    // DELETEME: Should be removed
    public ERXNavigation erxNavigation() { return _navigation; }
    // DELETEME: Should be removed
    protected boolean _hideHelp=false;
    // DELETEME: Should be removed
    public boolean hideHelp() { return _hideHelp; }
    // DELETEME: Should be removed
    public void setHideHelp(boolean newValue) { _hideHelp=newValue; }
    
    // DELETEME: Should remove.
    public String wrapperPageName() { return "PageWrapper"; }

    // DELETEME: Not needed with ERXThreadStorage
    private static NSMutableDictionary _classAdditions = new NSMutableDictionary();
    // DELETEME: Not needed with ERXThreadStorage
    public static NSDictionary sessionAdditions() { return _classAdditions; }
    // DELETEME: Not needed with ERXThreadStorage
    public static void registerSessionAddition(ERXSessionAdditionInterface addition) {
        registerSessionAdditionForName(addition, addition.sessionAdditionName());
    }
    // DELETEME: Not needed with ERXThreadStorage
    public ERXSessionAdditionInterface additionForName(String name) {
        return (ERXSessionAdditionInterface)_classAdditions.objectForKey(name);
    }

    // DELETEME: not needed
    public NSDictionary additions() { return _classAdditions; }

    // DELETEME: Not needed with ERXThreadStorage
    public static void registerSessionAdditionForName(ERXSessionAdditionInterface addition, String name) {
        if (addition != null && name != null) {
            if (_classAdditions.objectForKey(name) != null) {
                cat.warn("Registering multiple session additions for the same name: " + name+" -- "+ addition + " and "+_classAdditions.objectForKey(name));
                _classAdditions.removeObjectForKey(name);
            }
            _classAdditions.setObjectForKey(addition, name);
            cat.debug("Registered session addition: " + addition + " for name: " + name);
        } else {
            cat.error("Attempting to register null session addition or under null name");
        }
    }

    // DELETEME: Not needed with ERXThreadStorage
    public static class Observer {
        public void sessionWillAwake(NSNotification n) {
            ERXSession.sessionWillAwake((ERXSession)n.object());
        }
        public void sessionWillSleep(NSNotification n) {
            ERXSession.sessionWillSleep((ERXSession)n.object());
        }
    }
    // DELETEME: Not needed with ERXThreadStorage
    private static boolean registered = false;
    // DELETEME: Not needed with ERXThreadStorage
    public static void registerNotifications() {
        if (!registered) {
            Observer observer = new Observer();
            ERXRetainer.retain(observer); // has to be retained on the objC side!!
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("sessionWillAwake", ERXConstant.NotificationClassArray),
                                                             ERXSession.SessionWillAwakeNotification,
                                                             null);

            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("sessionWillSleep", ERXConstant.NotificationClassArray),
                                                             ERXSession.SessionWillSleepNotification,
                                                             null);
        }
    }

    // DELETEME: Not needed with ERXThreadStorage
    protected static void sessionWillAwake(ERXSession session) {
        for (Enumeration e = _classAdditions.allValues().objectEnumerator(); e.hasMoreElements();) {
            ERXSessionAdditionInterface sai = (ERXSessionAdditionInterface)e.nextElement();
            sai.setSession(session);
        }
    }

    // DELETEME: Not needed with ERXThreadStorage
    protected static void sessionWillSleep(ERXSession session) {
        for (Enumeration e = _classAdditions.allValues().objectEnumerator(); e.hasMoreElements();) {
            ERXSessionAdditionInterface sai = (ERXSessionAdditionInterface)e.nextElement();
            sai.setSession(null);
        }
    }
    // DELETEME: Need to get rid of navigation refs
    protected ERXNavigation _navigation;

}
