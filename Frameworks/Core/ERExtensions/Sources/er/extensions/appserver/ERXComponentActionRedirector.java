package er.extensions.appserver;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXThreadStorage;

/**
 * Allows you to develop your app using component actions while still providing bookmarkable URLs.
 * It should be considered <b>highly</b> experimental and it uses a few very dirty shortcuts, but no private API to work it's magic. 
 * The main problems may be garbage collection or space requirements. You might be better off to compress the responses.
 * <br>
 *  The mode of operation is as follows; given a component action in a typical page:
 * <br>
 * <pre><code>
 *  public WOComponent myAction() {
 *      WOComponent nextPage = pageWithName("Main");
 *      nextPage.takeValueForKey(Integer.valueOf(100), "someValue");
 *      return nextPage;
 *  }
 * 
 *  </code></pre>
 *  then Main could be implemented something like this:
 * <pre><code>
 *  public class Main extends WOComponent implements ERXComponentActionRedirector.Restorable {
 *      static Logger log = Logger.getLogger(Main.class);
 * 
 *      public Integer someValue = Integer.valueOf(10);
 * 
 *      public Main(WOContext aContext) {
 *          super(aContext);
 *      }
 *      
 *      // this page has a "Increment Some Value" link to itself which just doubles the current value
 *      public WOComponent addAction() {
 *          someValue = Integer.valueOf(someValue.intValue()*2);
 *          log.info(someValue);
 *          return this;
 *      }
 * 
 *      public String urlForCurrentState() {
 *          return context().directActionURLForActionNamed("Main$Restore", new NSDictionary(someValue, "someValue"));
 *      }
 *      public static class Restore extends WODirectAction {
 *          public Restore(WORequest aRequest) {
 *              super(aRequest);
 *          }
 *          public WOActionResults defaultAction() {
 *              WOComponent nextPage = pageWithName("Main");
 *              Number someValue = context().request().numericFormValueForKey("someValue", new NSNumberFormatter("#"));
 *              if(someValue != null) {
 *                  nextPage.takeValueForKey(someValue, "someValue");
 *              }
 *              return nextPage;
 *          }
 *      }
 *  }
 *  </code></pre>
 *  But this is just one possibility. It only locates all the code in one place. <br>
 * 
 * The actual workings are:
 * <ul>
 * <li>You create a page with typical component action links
 * <li>URL in browser: /cgi-bin/WebObjects/myapp.woa/
 * <li>Links on page: /cgi-bin/WebObjects/myapp.woa/0.1.2.3
 * </ul>
 * When you click on a link, the request-response loop gets executed, but instead of returning the response, we save it in a 
 * session-based cache and return a redirect instead. The current page is asked for the URL for the redirect when it implements 
 * the Restorable interface.<br>
 * So the users browser receives redirection to a "reasonable" URL like "/article/1234/edit?wosid=..." or 
 * "../wa/EditArticle?__key=1234&amp;wosid=...". This URL is intercepted and looked up in the cache. If found, the stored 
 * response is returned, else the request is handled normally.
 * <p>
 *  The major thing about this class is that you can detach URLs from actions. For example, it is very hard to create a 
 *  direct action that creates a page that uses a Tab panel or a collapsible component because you need to store a 
 *  tremendous amount of state in the URL. With this class, you say: "OK, I won't be able to totally restore everything, 
 *  but I'll show the first page with everything collapsed."<br>
 * 
 *  For all of this to work, your application should override the request-response loop like:
 * <pre><code>
 *  public WOActionResults invokeAction(WORequest request, WOContext context) {
 *      WOActionResults results = super.invokeAction(request, context);
 *      ERXComponentActionRedirector.createRedirector(results);
 *      return results;
 *  }
 * 
 *  public void appendToResponse(WOResponse response, WOContext context) {
 *      super.appendToResponse(response, context);
 *      ERXComponentActionRedirector redirector = ERXComponentActionRedirector.currentRedirector();
 *      if(redirector != null) {
 *          redirector.setOriginalResponse(response);
 *      }
 *  }
 * 
 *  public WOResponse dispatchRequest(WORequest request) {
 *      ERXComponentActionRedirector redirector = ERXComponentActionRedirector.redirectorForRequest(request);
 *      WOResponse response = null;
 *      if(redirector == null) {
 *          response = super.dispatchRequest(request);
 *          redirector = ERXComponentActionRedirector.currentRedirector();
 *          if(redirector != null) {
 *              response = redirector.redirectionResponse();
 *          }
 *      } else {
 *          response = redirector.originalResponse();
 *      }
 *      return response;
 *  }
 * </code></pre>
 * If you are using ERXApplication, you should set the 
 * <code>er.extensions.ERXComponentActionRedirector.enabled=true</code> property instead.
 *  
 * @author ak
 *  */
public class ERXComponentActionRedirector {

    /** logging support */
    protected static final Logger log = Logger.getLogger(ERXComponentActionRedirector.class);

    /** implemented by the pages that want to be restorable */
    public static interface Restorable {
        /** This method will be called directly after invokeAction(), so any temporary variables 
         * should have the same setting as they had when the action was invoked. 
         * @return url for the current state. */
        public String urlForCurrentState();
    }

    /** the original response */
    protected WOResponse originalResponse;

    /** the redirection response */
    protected WOResponse redirectionResponse;

    /** the session id for the request */
    protected String sessionID;

    /** the url for the redirected request */
    protected String url;

    /** static cache to hold the responses. They are stored on a by-session basis. */
    protected static final NSMutableDictionary responses = new NSMutableDictionary();

    /** stores the redirector in the cache. 
     * @param redirector The redirector to store. */
    protected static void storeRedirector(ERXComponentActionRedirector redirector) {
        synchronized (responses) {
            NSMutableDictionary sessionRef = (NSMutableDictionary)responses.objectForKey(redirector.sessionID());
            if(sessionRef == null) {
                sessionRef = new NSMutableDictionary();
                responses.setObjectForKey(sessionRef, redirector.sessionID());
            }
            sessionRef.setObjectForKey(redirector, redirector.url());
        }
        if(log.isDebugEnabled()) {
            log.debug("Stored URL: " + redirector.url());
        }
    }

    /**
     * @param request The request
     * @return the previously stored redirector for the given request */
    public static ERXComponentActionRedirector redirectorForRequest(WORequest request) {
        ERXComponentActionRedirector redirector = null;
        synchronized (responses) {
            redirector = (ERXComponentActionRedirector)responses.valueForKeyPath(request.sessionID() + "." + request.uri());
        }
        if(redirector != null) {
            if(log.isDebugEnabled()) {
                log.debug("Retrieved URL: " + redirector.url());
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("No Redirector for request: " + request.uri());
            }
        }
        return redirector;
    }

    /** Creates and stores a Redirector if the given results implement Restorable. 
     * @param results */
    public static void createRedirector(WOActionResults results) {
        ERXThreadStorage.removeValueForKey("redirector");
        if(results instanceof WOComponent) {
            WOComponent component = (WOComponent)results;
            WOContext context = component.context();
            if(context.request().requestHandlerKey().equals("wo")) {
                if(component instanceof Restorable) {
                    ERXComponentActionRedirector r = new ERXComponentActionRedirector((Restorable)component);
                    ERXComponentActionRedirector.storeRedirector(r);
                } else {
                    log.debug("Not restorable: " + context.request().uri() + ", " + component);
                }
            }
        }
    }

    /** Uses ERXThreadStorage with the key "redirector".
     * @return the currently active Redirector in the request-response loop. 
     */
    public static ERXComponentActionRedirector currentRedirector() {
        return (ERXComponentActionRedirector)ERXThreadStorage.valueForKey("redirector");
    }

    /** constructs the redirector from the Restorable. 
     * @param r - Restorable component used to construct a redirector */
    public ERXComponentActionRedirector(Restorable r) {
        WOComponent component = (WOComponent)r;
        WOContext context = component.context();
        sessionID = component.session().sessionID();
        url = r.urlForCurrentState();
        if(context.session().storesIDsInURLs()) {
	        String argsChar = url.indexOf("?") >= 0? "&" : "?";
	        String sessionIdKey = WOApplication.application().sessionIdKey();
	        if(url.indexOf(sessionIdKey + "=") < 0) {
	            url = url + argsChar + sessionIdKey + "=" +sessionID;
	            argsChar = "&";
	        }
	        if(url.indexOf("wocid=") < 0) {
	            url = url + argsChar + "wocid=" + context.contextID();
	        }
        }
        redirectionResponse = WOApplication.application().createResponseInContext(context);
        redirectionResponse.setHeader(url, "location");
        redirectionResponse.setStatus(302);
        ERXThreadStorage.takeValueForKey(this, "redirector");
    }

    /** @return the redirection response. */
    public WOResponse redirectionResponse() {
        return redirectionResponse;
    }

    /** @return the URL with which the component can be restored. */
    public String url() {
        return url;
    }

    /** @return the session ID for the Redirector. */
    public String sessionID() {
        return sessionID;
    }

    /** @return the original response. */
    public WOResponse originalResponse() {
        return originalResponse;
    }

    /**
     * Sets the original response.
     *
     * @param value the original response.
     */
    public void setOriginalResponse(WOResponse value) {
        originalResponse = value;
    }

    /**
     * Observer class manages the responses cache by watching the session.
     * Registers for sessionDidTimeout to maintain its data.
     */
    public static class Observer {
        protected Observer() {
            NSSelector sel = new NSSelector("sessionDidTimeout", ERXConstant.NotificationClassArray);
            NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidTimeOutNotification, null);
        }
        /**
        * Removes the timed out session from the internal array.
         * session.
         * @param n {@link WOSession#SessionDidTimeOutNotification}
         */
        public void sessionDidTimeout(NSNotification n) {
            String sessionID = (String) n.object();
            responses.removeObjectForKey(sessionID);
        }
    }
    @SuppressWarnings("unused")
	private static Observer observer;
    static {
        observer = new Observer();
    }
}
