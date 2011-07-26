package er.rest.format;

import er.rest.ERXRestRequestNode;

public interface IERXRestParser {
	
	public ERXRestRequestNode parseRestRequest(IERXRestRequest request, ERXRestFormat.Delegate delegate);
	
}
