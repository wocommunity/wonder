package er.extensions;

import com.webobjects.foundation._NSDelegate;

/**
 * 5.3/5.4-safe wrapper around a WODynamicURL (which changed classes).
 * 
 * @author mschrag
 */
public class ERXDynamicURL {
	private _NSDelegate _delegate;
	private Object _dynamicUrl;

	/**
	 * Construct an ERXDynamicURL.
	 * 
	 * @param dynamicUrl a WODynamicURL (either from the 5.3 or the 5.4 package)
	 */
	public ERXDynamicURL(Object dynamicUrl) {
		try {
			if (ERXApplication.isWO54()) {
				_delegate = new _NSDelegate(Class.forName("com.webobjects.appserver.WODynamicURL"));
			}
			else {
				_delegate = new _NSDelegate(Class.forName("com.webobjects.appserver._private.WODynamicURL"));
			}
			_dynamicUrl = dynamicUrl;
			_delegate.setDelegate(_dynamicUrl);
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to create ERXDynamicURL.", t);
		}
	}

	/**
	 * @see com.webobjects.appserver(\._private|)WODynamicURL.requestHandlerPath()
	 * @return
	 */
	public String requestHandlerPath() {
		return (String) _delegate.perform("requestHandlerPath");
	}

	/**
	 * @see com.webobjects.appserver(\._private|)WODynamicURL.queryString()
	 * @return
	 */
	public String queryString() {
		return (String) _delegate.perform("queryString");
	}
}
