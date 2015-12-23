package er.rest.format;

import com.webobjects.foundation.NSData;

public interface IERXRestResponse {
	public void setHeader(String value, String key);

	public void appendContentCharacter(char _ch);

	public void appendContentString(String _str);
	
	public void appendContentData(NSData data);

	default public void setContentEncoding(String encoding) {};
}
