package er.rest.format;

import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

public class ERXWORestResponse implements IERXRestResponse {
	private WOResponse _response;

	public ERXWORestResponse(WOResponse response) {
		_response = response;
	}

	public void setHeader(String value, String key) {
		_response.setHeader(value, key);
	}

	public void appendContentCharacter(char ch) {
		_response.appendContentCharacter(ch);
	}

	public void appendContentString(String str) {
		_response._appendContentAsciiString(str);
	}
	
	public void appendContentData(NSData data) {
		_response.appendContentData(data);
	}

	@Override
	public void setContentEncoding(String encoding) {
		_response.setContentEncoding(encoding);
	}
}
