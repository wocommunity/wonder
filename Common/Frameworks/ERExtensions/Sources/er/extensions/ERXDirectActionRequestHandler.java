//
// ERXDirectActionRequestHandler.java
// Project ERExtensions
//
// Created by tatsuya on Thu Aug 15 2002
//
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver._private.WODirectActionRequestHandler;

/**
 * Improved direct action request handler. Will automatically handle
 * character encodings and checks the {@link ERXWOResponseCache} before
 * actually calling the action.
 * 
 * NOTE: This class is multi thread safe. 
 */
public class ERXDirectActionRequestHandler extends WODirectActionRequestHandler {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXDirectActionRequestHandler.class);

    /** caches if automatic message encoding is enabled, defaults to true */
    protected static Boolean automaticMessageEncodingEnabled;
    
    /**
     * Allows the disabling of automatic message encoding. Useful for
     * backend services where you want to just use the default encoding.
     * @return if automatic message encoding is enabled.
     */
    public static boolean automaticMessageEncodingEnabled() {
        if (automaticMessageEncodingEnabled == null) {
            automaticMessageEncodingEnabled = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXMessageEncoding.Enabled", true) ? Boolean.TRUE : Boolean.FALSE;
        }
        return automaticMessageEncodingEnabled.booleanValue();
    }
    
    public ERXDirectActionRequestHandler() {
        super();
    }
    
    public ERXDirectActionRequestHandler(String actionClassName,
                                         String defaultActionName,
					boolean shouldAddToStatistics) {
        super(actionClassName, defaultActionName, shouldAddToStatistics);
    }

    public WOResponse handleRequest(WORequest request) {
        WOResponse response = null;
        
        String actionName = null;
        Class actionClass = null;
        boolean shouldCacheResult = false;
        
        if (ERXWOResponseCache.sharedInstance().isEnabled()) {
            try {
                // Caching scheme for 5.2 applications. Will uncomment once we are building 5.2 only ERExtensions
                Object[] actionClassAndName = getRequestActionClassAndNameForPath(getRequestHandlerPathForRequest(request));
                //Object[] actionClassAndName = null;
                if (actionClassAndName != null && actionClassAndName.length == 3) {
                    actionName = (String)actionClassAndName[1];
                    actionClass = (Class)actionClassAndName[2];
                    if (ERXWOResponseCache.Cacheable.class.isAssignableFrom(actionClass)) {
                        response = ERXWOResponseCache.sharedInstance().cachedResponseForRequest(actionClass,
                                actionName,
                                request);
                        // we need to cache only when there was no previous response in the cache
                        shouldCacheResult = (response == null);
                    }
                }
            } catch (Exception e) {
                log.error("Caught exception checking for cache. Leaving it up to the regular exception handler to cache. Request: " + request, e);
            } 
        }
        if (response == null) {
            response = super.handleRequest(request);
        } else {
            registerWillHandleActionRequest();
            registerDidHandleActionRequestWithActionNamed(actionName);
        }
        if (shouldCacheResult && response != null) {
            try {
                ERXWOResponseCache.sharedInstance().cacheResponseForRequest(actionClass, actionName, request, response);
            } catch (Exception e) {
                log.error("Caught exception when caching response. Request: " + request, e);
            }
        }
        if (automaticMessageEncodingEnabled()) {
            ERXMessageEncoding messageEncoding = null;
            
            // This should retrieve the session object belonging to the same
            // worker thread that's been calling the current handleRequest method.
            WOSession session;
            if(false) {
                // ak only enable when fixed
                // as we will create deadlocks checking out the session this early.
                WOContext context = ERXWOContext.currentContext();
                session = context != null ? context.session() : null;
            } else {
                session = ERXSession.session();   // get it from the thread specific storage
            }

            if (session != null  &&  session instanceof ERXSession) {
                ERXSession erxSession = (ERXSession)session;
                messageEncoding = erxSession.messageEncoding();
                erxSession.lastActionWasDA = true;
            } else if (request instanceof ERXRequest) {
                ERXBrowser browser = ((ERXRequest)request).browser();
                messageEncoding = browser.messageEncodingForRequest(request);
            } else {
                messageEncoding = new ERXMessageEncoding(request.browserLanguages());
            }
            messageEncoding.setEncodingToResponse(response);
        } 
        return response;
    }

}
