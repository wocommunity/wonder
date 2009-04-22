package er.rest.format;

import com.webobjects.appserver.WOResponse;

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
}
