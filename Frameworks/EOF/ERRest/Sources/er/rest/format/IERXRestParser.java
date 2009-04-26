package er.rest.format;

import com.webobjects.appserver.WORequest;

import er.rest.ERXRestRequestNode;

public interface IERXRestParser {
	public ERXRestRequestNode parseRestRequest(WORequest request);

	public ERXRestRequestNode parseRestRequest(String contentStr);
}
