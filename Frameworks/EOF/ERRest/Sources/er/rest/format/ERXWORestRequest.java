package er.rest.format;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.webobjects.appserver.WORequest;

public class ERXWORestRequest implements IERXRestRequest {

	private WORequest _request;
	
	public ERXWORestRequest(WORequest request) {
		_request = request;
	}
	
	public String stringContent() {
		return _request.contentString();
	}

	public InputStream streamContent() {
		return new ByteArrayInputStream(_request.content().bytes());
	}

}
