package er.rest;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERXRestContext contains all the state for a single REST request.  ERXRestContext
 * provides access to the WOContext, which will allow you to access the session should
 * you desire (only cookie sessions can be used by clients right now).  Additionally,
 * ERXRestContext acts as a dictionary, so you can put arbitrary values into it if you
 * just want to use it to pass the current User around that way.  The EOEditingContext
 * provided by this context is locked at the request level, similar to the session
 * defaultEditingContext.
 * 
 * @author mschrag
 */
public class ERXRestContext implements NSKeyValueCoding, NSKeyValueCoding.ErrorHandling {
	private WOContext _context;
	private EOEditingContext _editingContext;
	private IERXRestDelegate _delegate;
	private NSMutableDictionary _attributes;

	/**
	 * Constructs a rest context.
	 * 
	 * @param context the WOContext
	 * @param editingContext the EOEditingContext
	 */
	public ERXRestContext(WOContext context, EOEditingContext editingContext) {
		_context = context;
		_editingContext = editingContext;
		_attributes = new NSMutableDictionary();
	}

	/**
	 * Returns the WOContext for this request.
	 * 
	 * @return the WOContext for this request
	 */
	public WOContext context() {
		return _context;
	}

	/**
	 * Returns the EOEditingContext for this request.
	 * 
	 * @return the EOEditingContext for this request
	 */
	public EOEditingContext editingContext() {
		return _editingContext;
	}

	/**
	 * Returns the REST delegate for this context.
	 * 
	 * @return the REST delegate for this context
	 */
	public IERXRestDelegate delegate() {
		return _delegate;
	}

	/**
	 * Sets the REST delegate for this context (called by ERXRestRequestHandler).
	 * 
	 * @param delegate the REST delegate for this context
	 */
	public void setDelegate(IERXRestDelegate delegate) {
		_delegate = delegate;
	}

	public Object handleQueryWithUnboundKey(String key) {
		return _attributes.objectForKey(key);
	}

	public void handleTakeValueForUnboundKey(Object value, String key) {
		_attributes.setObjectForKey(value, key);
	}

	public void unableToSetNullForKey(String key) {
		_attributes.removeObjectForKey(key);
	}

	public void takeValueForKey(Object obj, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, key);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}
}
