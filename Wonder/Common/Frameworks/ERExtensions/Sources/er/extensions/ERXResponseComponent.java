package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

/**
 * Handy component when you need to return a WOActionResults and the interface 
 * requires a WOComponent.
 * @author ak
 */
public class ERXResponseComponent extends ERXStatelessComponent {
	
	protected WOResponse _response;
	
	public ERXResponseComponent(WOContext context) {
		super(context);
	}

	public void setResponse(WOResponse response) {
		_response = response;
	}

	public void setActionResults(WOActionResults results) {
		_response = results.generateResponse();
	}

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		aResponse.setContent(_response.content());
		aResponse.setContentEncoding(_response.contentEncoding());
		aResponse.setHeaders(_response.headers());
		aResponse.setStatus(_response.status());
	}
	
}
