package er.rest;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 * ERXRequestOptions is an Options implementation on top of WORequest form values.
 * 
 * @author mschrag
 */
public class ERXRequestFormValues implements NSKeyValueCoding {
	private WORequest _request;

	/**
	 * Constructs a new ERXRequestFormValues.
	 * 
	 * @param request
	 *            the backing request
	 */
	public ERXRequestFormValues(WORequest request) {
		_request = request;
	}

	@Override
	public void takeValueForKey(Object value, String key) {
		throw new UnsupportedOperationException("Cannot set form values.");
	}

	@Override
	public Object valueForKey(String key) {
		Object value = _request.stringFormValueForKey(key);
		if (value == null) {
			value = _request.headerForKey(key);
		}
		return value;
	}
}