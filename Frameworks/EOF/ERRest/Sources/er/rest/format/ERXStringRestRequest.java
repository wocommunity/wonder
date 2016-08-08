package er.rest.format;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation._NSStringUtilities;

public class ERXStringRestRequest implements IERXRestRequest {
	private String _contentString;
	private String _encoding;
	
	public ERXStringRestRequest(String contentString) {
		this(contentString, _NSStringUtilities.UTF8_ENCODING);
	}

	public ERXStringRestRequest(String contentString, String encoding) {
		_contentString = contentString;
		_encoding = encoding;
	}

	@Override
	public String stringContent() {
		return _contentString;
	}

	@Override
	public InputStream streamContent() {
		try {
			return new ByteArrayInputStream(_contentString.getBytes(_encoding));
		}
		catch (UnsupportedEncodingException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	@Override
	public NSArray<String> keyNames() {
		return NSArray.<String> emptyArray();
	}

	@Override
	public Object objectForKey(String key) {
		return null;
	}

}
