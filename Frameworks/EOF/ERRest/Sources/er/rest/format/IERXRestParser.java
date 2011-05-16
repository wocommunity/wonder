package er.rest.format;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

public interface IERXRestParser {
	
	public ERXRestRequestNode parseRestRequest(IERXRestRequest request, ERXRestFormat.Delegate delegate, ERXRestContext context);
	
}
