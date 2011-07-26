package er.rest.format;

import com.webobjects.foundation.NSPropertyListSerialization;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

public class ERXPListRestWriter implements IERXRestWriter {
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context) {
		response.setHeader("text/plain", "Content-Type");
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		if (node != null) {
			node._removeRedundantTypes();
		}
		appendHeadersToResponse(node, response, context);
		Object object = node.toNSCollection(delegate);
		response.appendContentString(NSPropertyListSerialization.stringFromPropertyList(object));
		response.appendContentString("\n");
	}
}
