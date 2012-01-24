package er.rest.entityDelegates;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEC;

/**
 * ERXRestContext contains all the state for a single REST request. ERXRestContext provides access to the WOContext,
 * which will allow you to access the session should you desire (only cookie sessions can be used by clients right now).
 * Additionally, ERXRestContext acts as a dictionary, so you can put arbitrary values into it if you just want to use it
 * to pass the current User around that way. The EOEditingContext provided by this context is locked at the request
 * level, similar to the session defaultEditingContext.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXRestContext implements NSKeyValueCoding, NSKeyValueCoding.ErrorHandling {
	private WOContext _context;
	private EOEditingContext _editingContext;
	private IERXRestDelegate _delegate;
	private NSMutableDictionary<String, Object> _attributes;

	/**
	 * Shortcut for constructing a rest context with a single default entity delegate.
	 * 
	 * @param defaultEntityDelegate the default entity delegate
	 */
	public ERXRestContext(IERXRestEntityDelegate defaultEntityDelegate) {
		this(new ERXDefaultRestDelegate(defaultEntityDelegate));
	}
	
	/**
	 * Constructs a rest context.
	 * 
	 */
	public ERXRestContext(IERXRestDelegate delegate) {
		this(null, ERXEC.newEditingContext(), delegate);
	}

	/**
	 * Constructs a rest context.
	 * 
	 * @param context
	 *            the WOContext
	 * @param editingContext
	 *            the EOEditingContext
	 */
	@SuppressWarnings("unchecked")
	public ERXRestContext(WOContext context, EOEditingContext editingContext, IERXRestDelegate delegate) {
		_context = context;
		_editingContext = editingContext;
		_attributes = new NSMutableDictionary();
		_delegate = delegate;

		if (context != null) {
			WORequest request = context.request();
			if (request != null) {
				for (String key : (NSArray<String>) request.formValueKeys()) {
					Object formValue = request.formValueForKey(key);
					takeValueForKey(formValue, key);
				}
			}
		}
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
	 * @param delegate
	 *            the REST delegate for this context
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
		if ("delegate".equals(key)) {
			throw new IllegalArgumentException("You are not allowed to set the 'delegate' key through KVC.");
		}
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, key);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}
}
