package er.rest.format;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class ERXStringBufferRestResponse implements IERXRestResponse {
	private NSMutableDictionary<String, String> _headers;
	private StringBuffer _buffer;

	public ERXStringBufferRestResponse() {
		this(new StringBuffer());
	}

	public ERXStringBufferRestResponse(StringBuffer buffer) {
		_headers = new NSMutableDictionary<String, String>();
		_buffer = buffer;
	}

	public NSDictionary<String, String> headers() {
		return _headers;
	}

	public void setHeader(String value, String key) {
		_headers.setObjectForKey(value, key);
	}

	public void appendContentCharacter(char ch) {
		_buffer.append(ch);
	}

	public void appendContentString(String str) {
		_buffer.append(str);
	}

	@Override
	public String toString() {
		return _buffer.toString();
	}
}
