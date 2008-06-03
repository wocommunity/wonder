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
