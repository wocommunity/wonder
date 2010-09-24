package er.rest.format;

import java.io.InputStream;

public interface IERXRestRequest {

	public String stringContent();
	public InputStream streamContent();
	
}
