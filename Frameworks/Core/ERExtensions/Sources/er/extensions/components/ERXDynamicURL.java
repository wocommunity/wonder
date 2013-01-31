package er.extensions.components;

import com.webobjects.appserver.WODynamicURL;
import com.webobjects.foundation._NSDelegate;

/**
 * 5.3/5.4-safe wrapper around a WODynamicURL (which changed classes).
 * 
 * @author mschrag
 * @deprecated use {@link WODynamicURL} instead
 */
@Deprecated
public class ERXDynamicURL {
	private _NSDelegate _delegate;

	/**
	 * Construct an ERXDynamicURL.
	 * 
	 * @param dynamicUrl a WODynamicURL (either from the 5.3 or the 5.4 package)
	 */
	public ERXDynamicURL(Object dynamicUrl) {
		_delegate = new _NSDelegate(WODynamicURL.class, dynamicUrl);
	}

	/**
	 * @see com.webobjects.appserver._private.WODynamicURL#requestHandlerPath()
	 */
	public String requestHandlerPath() {
		return (String) _delegate.perform("requestHandlerPath");
	}

	/**
	 * @see com.webobjects.appserver._private.WODynamicURL#queryString()
	 */
	public String queryString() {
		return (String) _delegate.perform("queryString");
	}
}
