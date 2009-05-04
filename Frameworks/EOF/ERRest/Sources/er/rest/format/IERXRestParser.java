package er.rest.format;

import com.webobjects.appserver.WORequest;

import er.rest.ERXRestRequestNode;

public interface IERXRestParser {
	public ERXRestRequestNode parseRestRequest(WORequest request, ERXRestFormat.Delegate delegate);

	public ERXRestRequestNode parseRestRequest(String contentStr, ERXRestFormat.Delegate delegate);
}
