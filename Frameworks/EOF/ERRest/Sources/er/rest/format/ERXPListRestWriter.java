package er.rest.format;

import com.webobjects.foundation.NSPropertyListSerialization;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

public class ERXPListRestWriter extends ERXRestWriter {
	@Override
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		if (node != null) {
			node._removeRedundantTypes();
		}
		appendHeadersToResponse(node, response, context);
		response.setContentEncoding(contentEncoding());
		Object object = node.toNSCollection(delegate);
		response.appendContentString(NSPropertyListSerialization.stringFromPropertyList(object));
		response.appendContentString("\n");
	}

	@Override
	public String contentType() {
		return "text/plain";
	}
}
