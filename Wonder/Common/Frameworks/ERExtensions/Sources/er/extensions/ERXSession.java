/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXSession.java created by patrice on Sat 19-Feb-2000 */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Category;
import java.util.Enumeration;

public class ERXSession extends WOSession {

    ////////////////////////////////////  log4j category  /////////////////////////////////////////////////
    public static Category cat = Category.getInstance(ERXSession.class);

    ///////////////////////////////////// Notification Methods ////////////////////////////////////////////
    public static final String SessionWillAwakeNotification = "SessionWillAwakeNotification";
    public static final String SessionWillSleepNotification = "SessionWillSleepNotification";
    public static final String JAVASCRIPT_ENABLED_COOKIE_NAME = "js";

    private static NSMutableDictionary _classAdditions = new NSMutableDictionary();
    public static NSDictionary sessionAdditions() { return _classAdditions; }
    public static void registerSessionAddition(ERXSessionAdditionInterface addition) {
        registerSessionAdditionForName(addition, addition.sessionAdditionName());
    }

    public ERXSessionAdditionInterface additionForName(String name) {
        return (ERXSessionAdditionInterface)_classAdditions.objectForKey(name);
    }

    // Useful for registering a session addition for multiple names.
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

    public static class Observer {
        public void sessionWillAwake(NSNotification n) {
            ERXSession.sessionWillAwake((ERXSession)n.object());
        }
        public void sessionWillSleep(NSNotification n) {
            ERXSession.sessionWillSleep((ERXSession)n.object());
        }
    }

    private ERXLocalizer localizer;
    public ERXLocalizer localizer() {
        if(localizer == null) {
            localizer = ERXLocalizer.localizerForLanguages(languages());
        }
        return localizer;
    }

    public void setLanguage(String language) {
        localizer = ERXLocalizer.localizerForLanguage(language);
    }

    private static boolean registered = false;
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

    // WARNING: Not MT-safe
    // This isn't thread safe.  If we want it to be thread safe we should have a dictionary to hold a different set of
    // session additions for each thread, then restrict the notification to the session additions for that thread.
    protected static void sessionWillAwake(ERXSession session) {
        for (Enumeration e = _classAdditions.allValues().objectEnumerator(); e.hasMoreElements();) {
            ERXSessionAdditionInterface sai = (ERXSessionAdditionInterface)e.nextElement();
            sai.setSession(session);
        }
    }

    // Need to do this to avoid cyclic retain cycles.
    protected static void sessionWillSleep(ERXSession session) {
        for (Enumeration e = _classAdditions.allValues().objectEnumerator(); e.hasMoreElements();) {
            ERXSessionAdditionInterface sai = (ERXSessionAdditionInterface)e.nextElement();
            sai.setSession(null);
        }
    }

    // Used to bind up things in the UI, ie session.additions.someAddition.foo might return a WOComponent.
    public NSDictionary additions() { return _classAdditions; }

    private boolean _javaScriptEnabled=true; // most people have JS by now
    private boolean _javaScriptInitialized=false;
    private String _browser;
    protected boolean isIE;
    protected boolean isNetscape;
    protected boolean isICab;
    protected boolean isOmniweb;
    protected boolean isMac;
    protected boolean isWindows;
    protected boolean isLinux;
    protected boolean isVersion4;
    protected boolean isVersion5;
    protected boolean isVersion3;
    protected String platform;
    protected String version;

    protected ERXNavigation _navigation;

    // FIXME this dictionary can be used for debugging -- e.g. put repetitions in components
    // and store the item without having to re-start
    // not necessary in deployment

    private NSMutableDictionary _debuggingStore;
    public NSMutableDictionary debuggingStore() {
        if (_debuggingStore==null) {
            _debuggingStore=new NSMutableDictionary();
        }
        return _debuggingStore;
    }

    // Instance Methods

    public ERXSession() {
        // Setting the default editing context delegate
        _navigation = createNavigation();
        _navigation.setIsDisabled(false);
    }

    public EOEditingContext defaultEditingContext() {
        EOEditingContext ec = super.defaultEditingContext();
        if(ec.delegate() == null)
            ERXExtensions.setDefaultDelegate(ec);
        return ec;
    }

    public boolean javaScriptEnabled() {
        return _javaScriptEnabled;
    }

    public void setJavaScriptEnabled(boolean newValue) {
        _javaScriptEnabled=newValue;
        _javaScriptInitialized=true;
    }
    public String browser() { return _browser; }

    public boolean browserIsIE() { return isIE; }
    public boolean browserIsOmniweb() { return isOmniweb; }
    public boolean browserIsNetscape() { return isNetscape;  }
    public boolean browserIsNotNetscape() { return !isNetscape; }

    public boolean browserRenderNestedTablesFast() {
        return browserIsNotNetscape() || isVersion5;
    }

    public boolean browserSupportsIFrames() { return browserIsIE(); }

    public void awake() {
        // FIXME: this probably ought to be replaced for deployment
        // by a few well chosen entry points
        // too early in ExtendedSession.ExtendedSession
        super.awake();
        ERXExtensions.setSession(this);
        NSNotificationCenter.defaultCenter().postNotification(SessionWillAwakeNotification, this);
        WORequest request=context()!=null ? context().request() : null;
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
        if (request!=null && request.formValueForKey("javaScript")!=null) {
            String js=(String)request.formValueForKey("javaScript");
            if (cat.isDebugEnabled()) cat.debug("Received javascript form value "+js);
            //System.out.println("context().request().formValues() ==== " + context().request().formValues());
            setJavaScriptEnabled(js!=null && js.equals("1") && (_browser==null || _browser.indexOf("Netscape3.")==-1));
        }
        if (request!=null && /* !_javaScriptInitialized */true) {
            String js=request.cookieValueForKey(JAVASCRIPT_ENABLED_COOKIE_NAME);
            if (js!=null) { // a friend sent us a cookie!
                if (cat.isDebugEnabled()) cat.debug("Got JAVASCRIPT_ENABLED_COOKIE from a friend "+js);
                setJavaScriptEnabled(js.equals("1") && (_browser==null || _browser.indexOf("Netscape3.")==-1));
            }
        }
    }

    // Subclasses override this method to provide their own navigation abilities
    public ERXNavigation createNavigation() { return new ERXNavigation(); }
    public ERXNavigation erxNavigation() { return _navigation; }

    protected boolean _hideHelp=false;
    public boolean hideHelp() { return _hideHelp; }
    public void setHideHelp(boolean newValue) { _hideHelp=newValue; }

    public void sleep() {
        NSNotificationCenter.defaultCenter().postNotification(SessionWillSleepNotification, this);
        super.sleep();
        ERXExtensions.setSession(null);
    }

    /*
     * Backtrack detection - Pulled from David Neuman's wonderful security framework.
     */

    public boolean didBackTrack = false;
    public boolean lastActionWasDA = false;
    /**
        * Utility method that gets the context ID string
     * from the passed in request
     */
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
     * flag
     */
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext){
        String reqCID = requestsContextID(aRequest);
        didBackTrack = didBacktrack(aRequest, aContext);
        if (didBackTrack) cat.debug("User backtracking.");
        return super.invokeAction(aRequest, aContext);
    }

    public void takeValuesFromRequest (WORequest aRequest, WOContext aContext){
        String reqCID = requestsContextID(aRequest);
        didBackTrack = didBacktrack(aRequest, aContext);
        if (didBackTrack) cat.debug("User backtracking.");
        super. takeValuesFromRequest (aRequest, aContext);
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        if (didBackTrack) cat.debug("User backtracking.");
        super.appendToResponse(r, c);
    }

    public String wrapperPageName() { return "PageWrapper"; }

    public void terminate() {
        // work around a bug in WO 5.1.1 where the sessions EC will keep a lock on the SEC
        defaultEditingContext().setSharedEditingContext(null);
        super.terminate();
    }

}
