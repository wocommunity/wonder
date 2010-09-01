package er.rest.format;

import er.rest.ERXRestRequestNode;
import er.rest.format.ERXRestFormat.Delegate;

public class ERXSimpleRestWriter implements IERXRestWriter {

	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, Delegate delegate) {
		response.appendContentString(node.toString());
	}

}
