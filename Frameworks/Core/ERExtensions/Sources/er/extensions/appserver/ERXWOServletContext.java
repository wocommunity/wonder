package er.extensions.appserver;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jspservlet.WOServletContext;

import er.extensions.foundation.ERXMutableUserInfoHolderInterface;

/** Replacement of WOServletContext.
 *  This subclass is installed when the frameworks loads.
 */
public class ERXWOServletContext extends WOServletContext implements ERXMutableUserInfoHolderInterface {
    public ERXWOServletContext(WORequest worequest) {
        super(worequest);
    }
    
    /**
     * Overridden to support rewritten urls via {@link ERXApplication#_rewriteURL(String)}.
     * @return the (optionally) rewritten url
     */
    public String _urlWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString, boolean secure) {
    	String url = super._urlWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString, secure);
    	if (!ERXApplication.isWO54()) {
    		url = ERXApplication.erxApplication()._rewriteURL(url);
    	}
    	return url;
    }

    protected NSMutableDictionary mutableUserInfo;
    public NSMutableDictionary mutableUserInfo() {
        if(mutableUserInfo == null) {
            mutableUserInfo = new NSMutableDictionary();
        }
        return mutableUserInfo;
    }
    public void setMutableUserInfo(NSMutableDictionary userInfo) {
        mutableUserInfo = userInfo;
    }
}
