/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;

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
public class ERXSession extends ERXAjaxSession implements Serializable {

  /** logging support */
  public static Logger log = Logger.getLogger(ERXSession.class);

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
  transient private ERXLocalizer _localizer;

  /** 
   * special varialble to hold language name only for when 
   * session object gets serialized. 
   * Do not use this value to get the language name;  
   * use {@link #language} method instead.
   */
  private String _serializableLanguageName;

  /** holds a reference to the current message encoding used for this session */
  private ERXMessageEncoding _messageEncoding;

  /** holds a reference to the current browser used for this session */
  transient private ERXBrowser _browser;

  /** flag for if java script is enabled */
  protected Boolean _javaScriptEnabled; // most people have JS by now

  /** holds a debugging store for a given session. */
  protected NSMutableDictionary _debuggingStore;

  /** the receiver of the variours notifications */
  transient private Observer _observer;

  /**
   * _originalThreadName holds the original name from the WorkerThread whic
   * is the value before executing <code>awake()</code>
   */
  public String _originalThreadName;

  /** 
   * returns the observer object for this session. 
   * If it doesn't ever exist, one will be created. 
   * 
   * @return the observer
   */
  public Observer observer() {
    if (_observer == null)
      _observer = new Observer(this);
    return _observer;
  }

  /** 
   * The Observer inner class encapsulates functions 
   * to handle various notifications. 
   */
  public static class Observer {

    /** the parent session */
    transient protected ERXSession session;

    /** private constructor; prevents instantiation in this way */
    private Observer() {
      super();
    }

    /** creates observer object which works with the given session */
    public Observer(ERXSession session) {
      super();
      this.session = session;
    }

    /** 
     * resets the reference to localizer when localization 
     * templates or localizer class itself is updated. 
     */
    public void localizationDidReset(NSNotification n) {
      if (session._localizer == null)
        return;

      String currentLanguage = session._localizer.language();
      session._localizer = ERXLocalizer.localizerForLanguage(currentLanguage);
      if (log.isDebugEnabled()) {
        log.debug("Detected changes in the localizers. Reset reference to " + currentLanguage + " localizer for session " + session.sessionID());
      }
    }

    /** 
     * registers this observer object for 
     * {@link ERXLocalizer.LocalizationDidResetNotification} 
     */
    private void registerForLocalizationDidResetNotification() {
      NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("localizationDidReset", ERXConstant.NotificationClassArray), ERXLocalizer.LocalizationDidResetNotification, null);
    }
  }

  /**
   * Method to get the current localizer for this
   * session. If local instance variable is null
   * then a localizer is fetched for the session's
   * <code>languages</code> array. See {@link ERXLocalizer}
   * for more information about using a localizer.
   * @return the current localizer for this session
   */
  public ERXLocalizer localizer() {
    if (_localizer == null) {
      _localizer = ERXLocalizer.localizerForLanguages(languages());
      if (!WOApplication.application().isCachingEnabled())
        observer().registerForLocalizationDidResetNotification();
    }
    return _localizer;
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
   * Cover method to set the current localizer
   * to the localizer for that language.
   * <p>
   * Also updates languages list with the new single language. 
   * 
   * @param language to set the current localizer for.
   * @see #language
   * @see #setLanguages
   */
  public void setLanguage(String language) {
    ERXLocalizer newLocalizer = ERXLocalizer.localizerForLanguage(language);
    if (!newLocalizer.equals(_localizer)) {
      if (_localizer == null && !WOApplication.application().isCachingEnabled())
        observer().registerForLocalizationDidResetNotification();

      _localizer = newLocalizer;
      ERXLocalizer.setCurrentLocalizer(_localizer);

      if (browser() != null) {
        _messageEncoding = browser().messageEncodingForLanguage(_localizer.language());
      }

      NSMutableArray languageList = new NSMutableArray(_localizer.language());
      if (!languageList.containsObject("Nonlocalized"))
        languageList.addObject("Nonlocalized");
      setLanguages(languageList);
    }
  }

  /** 
   * Sets the languages list for which the session is localized. 
   * The ordering of language strings in the array determines 
   * the order in which the application will search .lproj 
   * directories for localized strings, images, and component 
   * definitions.
   * <p>
   * Also updates localizer and messageEncodings. 
   * 
   * @param languageList  the array of languages for the session
   * @see #language
   * @see #setLanguage
   */
  public void setLanguages(NSArray languageList) {
    super.setLanguages(languageList);
    ERXLocalizer newLocalizer = ERXLocalizer.localizerForLanguages(languageList);
    if (!newLocalizer.equals(_localizer)) {
      if (_localizer == null && !WOApplication.application().isCachingEnabled())
        observer().registerForLocalizationDidResetNotification();

      _localizer = newLocalizer;
      ERXLocalizer.setCurrentLocalizer(_localizer);
      if (browser() != null) {
        _messageEncoding = browser().messageEncodingForLanguage(_localizer.language());
      }
    }
  }

  /**
   * Returns the NSArray of language names available for  
   * this application. This is simply a cover method of 
   * {@link ERXLocalizer#availableLanguages}, 
   * but will be convenient for binding to dynamic elements 
   * like language selector popup. 
   * 
   * @return   NSArray of language name strings available 
   *           for this application
   * @see      #availableLanguagesForThisSession 
   * @see      ERXLocalizer#availableLanguages
   * @TypeInfo java.lang.String 
   */
  public NSArray availableLanguagesForTheApplication() {
    return ERXLocalizer.availableLanguages();
  }

  /** 
   * Returns the NSArray of language names available for 
   * this particular session. 
   * The resulting array is an intersect of web browser's 
   * language array ({@link ERXRequest#browserLanguages}) 
   * and localizer's available language array 
   * ({@link ERXLocalizer#availableLanguages}). 
   * <p>
   * Note that the order of the resulting language names  
   * is not defined at this morment.
   * 
   * @return   NSArray of language name strings available 
   *           for this particular session
   * @see      #availableLanguagesForTheApplication 
   * @see      ERXRequest#browserLanguages
   * @see      ERXLocalizer#availableLanguages
   * @TypeInfo java.lang.String 
   */
  public NSArray availableLanguagesForThisSession() {
    NSArray browserLanguages = null;
    if (context() != null && context().request() != null)
      browserLanguages = context().request().browserLanguages();
    return ERXArrayUtilities.intersectingElements(browserLanguages, ERXLocalizer.availableLanguages());
  }

  /**
   * Returns the message encoding of the current session. 
   * If it's not already set up but no current <code>language()</code> 
   * available for the session, it creates one with 
   * the default encoding. 
   * @return message encoding object
   */
  public ERXMessageEncoding messageEncoding() {
    if (_messageEncoding == null) {
      if (browser() != null) {
        _messageEncoding = browser().messageEncodingForLanguage(language());
      }
    }
    return _messageEncoding;
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
    if (_browser == null && context() != null) {
      WORequest request = context().request();
      if (request != null) {
        ERXBrowserFactory browserFactory = ERXBrowserFactory.factory();
        if (request instanceof ERXRequest) {
          _browser = ((ERXRequest) request).browser();
        }
        else {
          _browser = browserFactory.browserMatchingRequest(request);
        }
        browserFactory.retainBrowser(_browser);
      }
    }
    return _browser;
  }

  /**
   * Simple mutable dictionary that can be used at
   * runtime to stash objects that can be useful for
   * debugging.
   * @return debugging store dictionary
   */
  // ENHANCEME: Should perform a check to make sure that the app is not in production mode when this is being used.
  public NSMutableDictionary debuggingStore() {
    if (_debuggingStore == null)
      _debuggingStore = new NSMutableDictionary();
    return _debuggingStore;
  }

  /**
   * Ensures that the returned editingContext was created with
   * the {@link ERXEC} factory.
   * @return the session's default editing context with
   * 		the default delegate set.
   */
  private boolean _editingContextWasCreated = false;

  public EOEditingContext defaultEditingContext() {
    if (!_editingContextWasCreated) {
      setDefaultEditingContext(ERXEC.newEditingContext());
      _editingContextWasCreated = true;
    }
    return super.defaultEditingContext();
  }

  public void setDefaultEditingContext(EOEditingContext ec) {
    _editingContextWasCreated = true;
    super.setDefaultEditingContext(ec);
  }

  /**
   * Returns if this user has javascript enabled.
   * This checks a form value "javaScript" and a cookie "js"
   * if the value is 1.
   * @return if js is enabled, defaults to true.
   */
  //CHECKME: (ak) I don't understand this? Having a default of TRUE makes no sense. At least not without some ERXJSCheckJavaScript component that would set a value on &lt;NOSCRIPT&gt; or sth like this.
  public boolean javaScriptEnabled() {
    WORequest request = context() != null ? context().request() : null;
    if (_javaScriptEnabled == null && request != null) {
      // FIXME: Shouldn't be hardcoded form value.
      String js = request.stringFormValueForKey("javaScript");
      if (js != null) {
        if (log.isDebugEnabled())
          log.debug("Received javascript form value " + js);
      }
      else {
        try {
          js = request.cookieValueForKey(JAVASCRIPT_ENABLED_COOKIE_NAME);
        }
        catch (StringIndexOutOfBoundsException e) {
          // malformed cookies cause WO 5.1.3 to raise here
        }
      }
      if (js != null) {
        _javaScriptEnabled = (ERXValueUtilities.booleanValue(js) && (browser().browserName().equals(ERXBrowser.UNKNOWN_BROWSER) || browser().isMozilla40Compatible() || browser().isMozilla50Compatible())) ? Boolean.TRUE : Boolean.FALSE;
      }
    }
    // defaults to true as most browsers have javascript
    return _javaScriptEnabled == null ? true : _javaScriptEnabled.booleanValue();
  }

  /**
   * Sets if javascript is enabled for this session.
   * crafty entry pages can set form values via
   * javascript to test if it is enabled.
   * @param newValue says if javascript is enabled
   */
  public void setJavaScriptEnabled(boolean newValue) {
    _javaScriptEnabled = newValue ? Boolean.TRUE : Boolean.FALSE;
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

    WORequest request = context() != null ? context().request() : null;
    if (request != null && log.isDebugEnabled() && request.headerForKey("content-type") != null) {
      if ((request.headerForKey("content-type")).toLowerCase().indexOf("multipart/form-data") == -1)
        log.debug("Form values " + request.formValues());
      else
        log.debug("Multipart Form values found");
    }
    _originalThreadName = Thread.currentThread().getName();
    Thread.currentThread().setName(threadName());

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
    // reset backtracking
    _didBacktrack = null;
    Thread.currentThread().setName(_originalThreadName);
    removeObjectForKey("ERXActionLogging");
  }

  /** override this method in order to provide a different name for the WorkerThread for this rr loop
   * very useful for logging stuff: assign a log statement to a log entry. Something useful could be:
   * <code>return session().sessionID() + valueForKeyPath("user.username");
   * @return
   */
  public String threadName() {
    return Thread.currentThread().getName();
  }

  /*
   * Backtrack detection - Pulled from David Neumann's wonderful security framework.
   */

  /**
   * flag to indicate if the user is currently backtracking,
   * meaning they hit the back button and then clicked on a
   * link.
   */
  protected Boolean _didBacktrack = null;

  /** flag to indicate if the last action was a direct action */
  public boolean lastActionWasDA = false;

  /**
   * Utility method that gets the context ID string
   * from the passed in request.
   * @param aRequest request to get the context id from
   * @return the context id as a string
   */
  // MOVEME: ERXWOUtilities
  public String requestsContextID(WORequest aRequest) {
    String uri = aRequest.uri();
    int idx = uri.indexOf('?');
    if (idx != -1)
      uri = uri.substring(0, idx);
    String eID = NSPathUtilities.lastPathComponent(uri);
    NSArray eIDs = NSArray.componentsSeparatedByString(eID, ".");
    String reqCID = "1";
    if (eIDs.count() > 0) {
      reqCID = (String) eIDs.objectAtIndex(0);
    }
    return reqCID;
  }

  /**
   * Method inspects the passed in request to see if
   * the user backtracked. If the context ID for the request is 2 clicks
   * less than the context ID for the current WOContext, we know
   * the backtracked.
   * @return if the user has backtracked or not.
   */
  public boolean didBacktrack() {
    if (_didBacktrack == null) {
      _didBacktrack = Boolean.FALSE;
      //If the current request is a direct action, no way the user could have backtracked.
      if (!context().request().requestHandlerKey().equals(WOApplication.application().directActionRequestHandlerKey())) {
        int reqCID = Integer.parseInt(requestsContextID(context().request()));
        int cid = Integer.parseInt(context().contextID());
        int delta = cid - reqCID;
        if (delta > 2) {
          _didBacktrack = Boolean.TRUE;
        }
        else if (delta > 1) {
          // Might not have backtracked if their last
          // action was a direct action.
          // ERXDirectActionRequestHandler, which is the framework
          // built-in default direct action handler, sets this variable
          // to true at the end of its handleRequest method.
          if (!lastActionWasDA) {
            _didBacktrack = Boolean.TRUE;
          }
        }
      }
      lastActionWasDA = false;
    }

    return _didBacktrack.booleanValue();
  }

  /**
   * Provides automatic encoding support for component action 
   * with <code>messageEncoding</code> object.
   * @param aRequest current request
   * @param aContext current context
   * @return super's implementation of <code>invokeAction</code>
   */
  public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
    messageEncoding().setDefaultFormValueEncodingToRequest(aRequest);
    super.takeValuesFromRequest(aRequest, aContext);
  }

  /**
   * Provides automatic encoding support for component action 
   * with <code>messageEncoding</code> object.
   * @param aResponse current response object
   * @param aContext current context object
   */
  public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    messageEncoding().setEncodingToResponse(aResponse);
    super.appendToResponse(aResponse, aContext);
  }
  
  /**
   * Bringing application into KVC.
   * @return
   */
  public ERXApplication application() {
	  return ERXApplication.erxApplication();
  }

  /**
   * Overrides terminate to free up resources and unregister for notifications.
   */
  public void terminate() {
    if (_observer != null) {
      NSNotificationCenter.defaultCenter().removeObserver(_observer);
      _observer = null;
    }
    if (_browser != null) {
      ERXBrowserFactory.factory().releaseBrowser(_browser);
      _browser = null;
    }
    if (log.isDebugEnabled()) {
      log.debug("Will terminate, sessionId is " + sessionID());
    }
    super.terminate();
  }

  /** This is a cover method which enables use of the session's object store
   * which is usually access with setObjectForKey and objectForKey. One can use
   * this method with KVC, like for example in .wod bindings:
   * 
   * <code>
   * myString: WOString {
   *      value = session.objectStore.myLastSearchResult.count;
   * }
   * </code>
   * 
   * @return an Object which implements KVC + KVC additions
   */
  private Object _objectStore;

  public Object objectStore() {
    if (_objectStore == null) {
      _objectStore = new NSKeyValueCodingAdditions() {
        public void takeValueForKey(Object arg0, String arg1) {
          if (arg0 == null) {
            removeObjectForKey(arg1);
          }
          else {
            setObjectForKey(arg0, arg1);
          }
        }

        public Object valueForKey(String arg0) {
          return objectForKey(arg0);
        }

        public void takeValueForKeyPath(Object arg0, String arg1) {
          if (arg0 == null) {
            removeObjectForKey(arg1);
          }
          else {
            setObjectForKey(arg0, arg1);
          }
        }

        public Object valueForKeyPath(String arg0) {
          Object theObject = objectForKey(arg0);
          if (theObject == null && arg0.indexOf(".") > -1) {
            String key = "";
            String oriKey = arg0;
            do {
              key = key + oriKey.substring(0, oriKey.indexOf("."));
              oriKey = oriKey.substring(oriKey.indexOf(".") + 1);
              theObject = objectForKey(key);
              key += ".";
            } while (theObject == null && oriKey.indexOf(".") > -1);
            if (theObject != null && !ERXStringUtilities.stringIsNullOrEmpty(oriKey)) {
              theObject = NSKeyValueCodingAdditions.Utility.valueForKeyPath(theObject, oriKey);
            }
          }
          return theObject;
        }
      };
    }
    return _objectStore;
  }

  /*
   * Serialization support - enables to use a variety of session stores
   */
  private void writeObject(ObjectOutputStream stream) throws IOException {
    if (_localizer == null)
      _serializableLanguageName = null;
    else
      _serializableLanguageName = language();
    stream.defaultWriteObject();
  }

  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    if (_serializableLanguageName != null)
      setLanguage(_serializableLanguageName);
    if (log.isDebugEnabled())
      log.debug("Session has been deserialized: " + toString());
  }

  public NSTimestamp _birthDate() {
	  return super._birthDate();
  }
  
  public String toString() {
    String superString = super.toString();
    String thisString = " localizer=" + (_localizer == null ? "null" : _localizer.toString()) + " messageEncoding=" + (_messageEncoding == null ? "null" : _messageEncoding.toString()) + " browser=" + (_browser == null ? "null" : _browser.toString());

    int lastIndex = superString.lastIndexOf(">");
    String toStr;
    if (lastIndex > 0) { // ignores if ">" is the first char (lastIndex == 0)
      toStr = superString.substring(0, lastIndex - 1) + thisString + ">";
    }
    else {
      toStr = superString + thisString;
    }
    return toStr;
  }

  public static ERXSession session() {
	  return (ERXSession) ERXThreadStorage.valueForKey("session");
  }

  public static void setSession(ERXSession session) {
	  ERXThreadStorage.takeValueForKey(session, "session");
  }
}
