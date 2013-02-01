package er.rest.format;

import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

public class ERXJSONRestWriter implements IERXRestWriter {
	public ERXJSONRestWriter() {
	}
	
	protected JsonConfig configWithContext(ERXRestContext context) {
		return _ERXJSONConfig.createDefaultConfig(context);
	}
	
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		return node;
	}

	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context) {
		response.setHeader("application/json", "Content-Type");
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		node = processNode(node);
		if (node != null) {
			node._removeRedundantTypes();
		}
		
		appendHeadersToResponse(node, response, context);
		Object object = node.toJavaCollection(delegate);
		if (object == null) {
			response.appendContentString("undefined");
		}
		else if (ERXRestUtils.isPrimitive(object)) {
			response.appendContentString(String.valueOf(object));
		}
		else {
			response.appendContentString(JSONSerializer.toJSON(object, configWithContext(context)).toString());
		}
		response.appendContentString("\n");
	}
}
