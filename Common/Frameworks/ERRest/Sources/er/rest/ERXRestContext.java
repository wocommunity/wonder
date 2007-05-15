package er.rest;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

public class ERXRestContext implements NSKeyValueCoding, NSKeyValueCoding.ErrorHandling {
	private WOContext _context;
	private EOEditingContext _editingContext;
	private IERXRestDelegate _delegate;
	private NSMutableDictionary _attributes;

	public ERXRestContext(WOContext context, EOEditingContext editingContext) {
		_context = context;
		_editingContext = editingContext;
		_attributes = new NSMutableDictionary();
	}

	public WOContext context() {
		return _context;
	}

	public EOEditingContext editingContext() {
		return _editingContext;
	}

	public IERXRestDelegate delegate() {
		return _delegate;
	}

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
