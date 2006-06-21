//
// ERXWOContext.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/** Replacement of WOContext.
 *  This subclass is installed when the frameworks loads.
 */
public class ERXWOContext extends WOContext implements ERXMutableUserInfoHolderInterface {
    private static Observer observer;
    
    public static class Observer {
    	public void applicationDidHandleRequest(NSNotification n) {
    		ERXThreadStorage.removeValueForKey("ERXWOContext.dict");
    	}
    }
    
    /**
     * Public constructor
     * @param context context of request
     */
    public static NSMutableDictionary contextDictionary() {
    	if(observer == null) {
    		synchronized (ERXWOContext.class) {
    			if(observer == null) {
    				observer = new Observer();

    				NSNotificationCenter.defaultCenter().addObserver(observer, 
    						ERXSelectorUtilities.notificationSelector("applicationDidHandleRequest"), 
    						WOApplication.ApplicationDidDispatchRequestNotification, null);
    			}
    		}
    	}
    	NSMutableDictionary result = (NSMutableDictionary)ERXThreadStorage.valueForKey("ERXWOContext.dict"); 
    	if(result == null) {
    		result = new NSMutableDictionary();
    		ERXThreadStorage.takeValueForKey(result, "ERXWOContext.dict");
    	}
     	return result;
    }


    public ERXWOContext(WORequest worequest) {
        super(worequest);
    }

    public static WOContext newContext(){
        WOApplication app = WOApplication.application();
        return app.createContextForRequest(app.createRequest("GET", app.cgiAdaptorURL() + "/" + app.name(), "HTTP/1.1", null, null, null));
    }


    public NSMutableDictionary mutableUserInfo() {
        return contextDictionary();
    }
    public void setMutableUserInfo(NSMutableDictionary userInfo) {
        ERXThreadStorage.takeValueForKey(userInfo, "ERXWOContext.dict");
    }
    public NSDictionary userInfo() {
        return mutableUserInfo();
    }
    
    /**
     * Returns a complete URL for the specified action. 
     * Works like {@link WOContext#directActionURLForActionNamed} 
     * but has one extra parameter to specify whether or not to include 
     * the current Session ID (wosid) in the URL. Convenient if you embed
     * the link for the direct action into an email message and don't want 
     * to keep the Session ID in it. 
     * <p>
     * <code>actionName</code> can be either an action -- "ActionName" -- 
     * or an action on a class -- "ActionClass/ActionName". You can also 
     * specify <code>queryDict</code> to be an NSDictionary which contains 
     * form values as key/value pairs. <code>includeSessionID</code> 
     * indicates if you want to include the Session ID (wosid) in the URL.  
     * 
     * @param actionName  String action name
     * @param queryDict   NSDictionary containing query key/value pairs
     * @param includeSessionID	true: to include the Session ID (if has one), <br>
     * 				false: not to include the Session ID
     * @return a String containing the URL for the specified action
     * @see WODirectAction
     */
    public String directActionURLForActionNamed(String actionName, NSDictionary queryDict, boolean includeSessionID) {
        String url = super.directActionURLForActionNamed(actionName, queryDict);
        
        if (includeSessionID) 
            return url;
        else 
            return stripSessionIDFromURL(url);
    }
    
    /**
     * Removes Session ID (wosid) query key/value pair from the given URL 
     * string. 
     * 
     * @param actionName  String URL
     * @return a String with the Session ID removed
     */ 
    public static String stripSessionIDFromURL(String url) {
	if (url == null)  return null;
        
        int startpos = url.indexOf("?wosid");
        if (startpos < 0)  startpos = url.indexOf("&wosid");

        if (startpos >= 0) {
            int endpos = url.indexOf('&', startpos + 1);
            if (endpos < 0)
                url = url.substring(0, startpos);
            else
                url = url.substring(0, startpos + 1) + url.substring(endpos + 1);
        }
        return url;
    }
    
}
