package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;

/**
This class allows you to develop your app using component actions while still providing bookmarkable URLs.
It should be considered <b>highly</b> experimental and it uses a few very dirty shortcuts, but no private API to work it's magic. The main problems may be thread safety and garbabe collection/space requirements. You might be better of to compress the responses.

 The mode of operation is as follows; given a component action in a typical page:

 public WOComponent myAction() {
     WOComponent nextPage = pageWithName("Main");
     nextPage.takeValueForKey(new Integer(100), "someValue");
     return nextPage;
 }

 then Main could be implemented something like this:

 public class Main extends WOComponent implements ERXComponentActionRedirector.Restorable {
     static ERXLogger log = ERXLogger.getERXLogger(Main.class);

     public Integer someValue = new Integer(10);

     public Main(WOContext aContext) {
         super(aContext);
     }
     // this page has a link to itself which just doubles the current value
     public WOComponent addAction() {
         someValue = new Integer(someValue.intValue()*2);
         log.info(someValue);
         return this;
     }

     public String urlForCurrentState() {
         return context().directActionURLForActionNamed("Main$Restore", new NSDictionary(someValue, "someValue"));
     }
     public static class Restore extends WODirectAction {
         public Restore(WORequest aRequest) {
             super(aRequest);
         }
         public WOActionResults defaultAction() {
             WOComponent nextPage = pageWithName("Main");
             Number someValue = context().request().numericFormValueForKey("someValue", new NSNumberFormatter("#"));
             if(someValue != null) {
                 nextPage.takeValueForKey(someValue, "someValue");
             }
             return nextPage;
         }
     }
 }
 
 But this is just one possibility. It only locates all the code in one place.

The actual workings are:
You create a page with typical component action links
 URL: /cgi-bin/WebObjects/myapp.woa/
 Links on page: /cgi-bin/WebObjects/myapp.woa/0.1.2.3

When you click on a link, the request-response loop gets executed, but instead of returning the response, we save it in a session-based cache and return a redirect intstead. The current page is asked for the URL for the redirect when it implements the Restorable interface.
So the users browser receives redirection to a "reasonable" URL like "/article/1234/edit?wosid=..." or "../wa/EditArticle?__key=1234&wosid=...". This URL is intercepted and looked up in the cache. If found, the sotred response is returned, else the request is handled normally.

 The major thing about this class is that you can detach URLs from actions. For example, it is very hard to create a direct action that creates a page that uses a Tab panel or a collapsible component because you need to store a tremendous amount of state in the URL. With this class, you say: "OK, I won't be able to totally restore everything, but I'll show the first page with everything collapsed.

 For all of this to work, your application should override the request-response loop like:
 public WOActionResults invokeAction(WORequest request, WOContext context) {
     WOActionResults results = super.invokeAction(request, context);
     ERXComponentActionRedirector.createRedirectorInContext(results, context);
     return results;
 }

 public void appendToResponse(WOResponse response, WOContext context) {
     super.appendToResponse(response, context);
     ERXComponentActionRedirector redirector = ERXComponentActionRedirector.currentRedirector();
     if(redirector != null) {
         redirector.setOriginalResponse(response);
     }
 }

 public WOResponse dispatchRequest(WORequest request) {
     ERXComponentActionRedirector redirector = ERXComponentActionRedirector.redirectorForRequest(request);
     WOResponse response = null;
     if(redirector == null) {
         response = super.dispatchRequest(request);
         redirector = ERXComponentActionRedirector.currentRedirector();
         if(redirector != null) {
             response = redirector.redirectionResponse();
         }
     } else {
         response = redirector.originalResponse();
     }
     return response;
 }
 
 
@author ak
 */
public class ERXComponentActionRedirector {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXComponentActionRedirector.class);


    public static interface Restorable {
        public String urlForCurrentState();
    }

    protected WOResponse originalResponse;
    protected WOResponse redirectionResponse;
    protected String sessionID;
    protected String url;

    protected static NSMutableDictionary responses = new NSMutableDictionary();

    public static void storeRedirector(ERXComponentActionRedirector redirector) {
        NSMutableDictionary sessionRef = (NSMutableDictionary)responses.objectForKey(redirector.sessionID());
        if(sessionRef == null) {
            sessionRef = new NSMutableDictionary();
            responses.setObjectForKey(sessionRef, redirector.sessionID());
        }
        sessionRef.setObjectForKey(redirector, redirector.url());
        log.info("Stored URL: " + redirector.url());
    }
    public static ERXComponentActionRedirector redirectorForRequest(WORequest request) {
        ERXComponentActionRedirector redirector = (ERXComponentActionRedirector)responses.valueForKeyPath(request.sessionID() + "." + request.uri());
        if(redirector != null) {
            log.info("Retrieved URL: " + redirector.url());
        } else {
            log.info("No Redirector for request: " + request.uri());
        }
        return redirector;
    }

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
                    log.info("Not restorable: " + context.request().uri() + ", " + component);
                }
            }
        }
    }
    public static ERXComponentActionRedirector currentRedirector() {
        return (ERXComponentActionRedirector)ERXThreadStorage.valueForKey("redirector");
    }

    public ERXComponentActionRedirector(Restorable r) {
        WOComponent component = (WOComponent)r;
        WOContext context = component.context();
        sessionID = component.session().sessionID();
        url = r.urlForCurrentState();
        String argsChar = url.indexOf("?") >= 0? "&" : "?";
        if(url.indexOf("wosid=") < 0) {
            url = url + argsChar + "wosid=" +sessionID + "&wocid=" + context.contextID();
        }
        redirectionResponse = WOApplication.application().createResponseInContext(context);
        redirectionResponse.setHeader(url, "location");
        redirectionResponse.setStatus(302);
        ERXThreadStorage.takeValueForKey(this, "redirector");
    }
    public WOResponse redirectionResponse() {
        return redirectionResponse;
    }
    public String url() {
        return url;
    }
    public String sessionID() {
        return sessionID;
    }
    public WOResponse originalResponse() {
        return originalResponse;
    }
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
    private static Observer observer;
    static {
        observer = new Observer();
    }
}
