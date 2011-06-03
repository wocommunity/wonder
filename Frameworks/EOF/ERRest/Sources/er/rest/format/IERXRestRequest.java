package er.rest.format;

import java.io.InputStream;

import com.webobjects.foundation.NSArray;

public interface IERXRestRequest {
	public String stringContent();
	public InputStream streamContent();
	public NSArray<String> keyNames();
	public Object objectForKey(String key);
}
