package er.rest;

import com.webobjects.appserver.WORequest;

public interface IERXRestRequestParser {
	public ERXRestRequest parseRestRequest(ERXRestContext context, WORequest request, String requestPath) throws ERXRestException, ERXRestNotFoundException;
}
