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

    /** holds a reference to the current browser used for this session */
    private ERXBrowser browser;

    /** flag for if java script is enabled, defaults to true */
    protected boolean _javaScriptEnabled=true; // most people have JS by now
    /** flag to indicate if java script has been set */
    protected boolean _javaScriptInitialized=false;

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
     * Returns the browser object representing the web 
     * browser's "user-agent" string. You can obtain 
     * browser name, version, platform and Mozilla version, etc. 
     * through this object. <br>
     * Good for WOConditional's condition binding to deal 
     * with different browser versions. 
     * @return browser object
     */
    public ERXBrowser browser() { 
        if (browser == null  &&  context() != null) {
            WORequest request = context().request();
            if (request != null) {
                ERXBrowserFactory browserFactory = ERXBrowserFactory.factory();
                browser = browserFactory.browserMatchingRequest(request);
                browserFactory.retainBrowser(browser);
            }
        }
        return browser; 
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

    /**
     * Overridden to provide a few checks to
     * see if javascript is enabled.
     */
    public void awake() {
        super.awake();
        ERXExtensions.setSession(this);
        ERXLocalizer.setCurrentLocalizer(localizer());
        NSNotificationCenter.defaultCenter().postNotification(SessionWillAwakeNotification, this);

        WORequest request=context()!=null ? context().request() : null;
        if (request!=null && cat.isDebugEnabled()) cat.debug("Form values "+request.formValues());
        // FIXME: Shouldn't be hardcoded form value.
        if (request!=null && request.formValueForKey("javaScript")!=null) {
            String js=(String)request.formValueForKey("javaScript");
            if (cat.isDebugEnabled()) cat.debug("Received javascript form value "+js);
            setJavaScriptEnabled(js != null  &&  js.equals("1")  
                                    && (browser().browserName().equals(ERXBrowser.UNKNOWN_BROWSER) 
                                        ||  browser().isMozilla40Compatible()  
                                        ||  browser().isMozilla50Compatible()) );
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
                setJavaScriptEnabled(js.equals("1") 
                                    && (browser().browserName().equals(ERXBrowser.UNKNOWN_BROWSER) 
                                        ||  browser().isMozilla40Compatible()  
                                        ||  browser().isMozilla50Compatible()) );
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
        if (browser != null) 
            ERXBrowserFactory.factory().releaseBrowser(browser);
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

    // DELETEME: Need to get rid of navigation refs
    protected ERXNavigation _navigation;
}
