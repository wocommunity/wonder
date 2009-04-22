package er.rest.format;

import com.webobjects.appserver.WORequest;

import er.rest.ERXRestException;
import er.rest.ERXRestRequestNode;

public interface IERXRestParser {
	public ERXRestRequestNode parseRestRequest(WORequest request) throws ERXRestException;

	public ERXRestRequestNode parseRestRequest(String contentStr) throws ERXRestException;
}
