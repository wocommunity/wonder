package er.rest.format;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.format.ERXRestFormat.Delegate;

public class ERXSimpleRestWriter implements IERXRestWriter {
	@Override
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context) {
		
	}

	@Override
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, Delegate delegate, ERXRestContext context) {
		appendHeadersToResponse(node, response, context);
		response.appendContentString(node.toString());
	}

}
