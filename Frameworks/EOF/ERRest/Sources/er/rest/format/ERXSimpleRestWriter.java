package er.rest.format;

import er.rest.ERXRestRequestNode;
import er.rest.format.ERXRestFormat.Delegate;

public class ERXSimpleRestWriter implements IERXRestWriter {

	@Override
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		
	}

	@Override
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, Delegate delegate) {
		response.appendContentString(node.toString());
	}

}
