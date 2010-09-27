package er.rest.format;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ERXStringRestRequest implements IERXRestRequest {

	private String _contentString;
	
	public ERXStringRestRequest(String contentString) {
		_contentString = contentString;
	}
	
	public String stringContent() {
		return _contentString;
	}

	public InputStream streamContent() {
		return new ByteArrayInputStream(_contentString.getBytes());
	}

}
