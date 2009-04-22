package er.rest.format;

import com.webobjects.foundation.NSPropertyListSerialization;

import er.rest.ERXRestRequestNode;

public class ERXPListRestWriter implements IERXRestWriter {
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		response.setHeader("text/plain", "Content-Type");
		Object obj = node.toCollection();
		response.appendContentString(NSPropertyListSerialization.stringFromPropertyList(obj));
	}
}
