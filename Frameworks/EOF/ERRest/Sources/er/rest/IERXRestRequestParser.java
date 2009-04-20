package er.rest;

import com.webobjects.appserver.WORequest;

public interface IERXRestRequestParser {
	public ERXRestRequestNode parseRestRequest(WORequest request) throws ERXRestException;

	public ERXRestRequestNode parseRestRequest(String contentStr) throws ERXRestException;
}
