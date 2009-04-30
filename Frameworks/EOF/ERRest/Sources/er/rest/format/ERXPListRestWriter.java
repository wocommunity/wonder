package er.rest.format;

import com.webobjects.foundation.NSPropertyListSerialization;

import er.rest.ERXRestRequestNode;

public class ERXPListRestWriter implements IERXRestWriter {
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		response.setHeader("text/xml", "Content-Type");
	}
	
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		appendHeadersToResponse(node, response);
		Object object = node.toJava();
		response.appendContentString(NSPropertyListSerialization.stringFromPropertyList(object));
	}
}
