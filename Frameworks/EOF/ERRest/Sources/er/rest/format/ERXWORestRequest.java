package er.rest.format;

import java.io.InputStream;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;

public class ERXWORestRequest implements IERXRestRequest {
	private WORequest _request;
	
	public ERXWORestRequest(WORequest request) {
		_request = request;
	}
	
	public String stringContent() {
		return _request.contentString();
	}

	public InputStream streamContent() {
		InputStream is = _request.contentInputStream();
		if (is == null) {
			is = _request.content().stream();
		}
		return is;
	}
	
	public NSArray<String> keyNames() {
		return _request.formValueKeys();
	}
	
	public Object objectForKey(String key) {
		return _request.formValueForKey(key);
	}
}
