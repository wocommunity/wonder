package er.extensions.appserver;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ajax.ERXAjaxServletContext;
import er.extensions.foundation.ERXMutableUserInfoHolderInterface;

/**
 * Replacement of WOServletContext.
 * This subclass is installed when the frameworks loads.
 */
public class ERXWOServletContext extends ERXAjaxServletContext implements ERXMutableUserInfoHolderInterface {
    public ERXWOServletContext(WORequest worequest) {
        super(worequest);
    }
    
    /**
	 * @return the (optionally) rewritten url.
	 */
    @Override
	public String _urlWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString, boolean isSecure, int somePort) {
		String url = super._urlWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString, isSecure, somePort);
		url = ERXApplication.erxApplication()._rewriteURL(url);
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
