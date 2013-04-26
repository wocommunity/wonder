package er.rest.format;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import er.extensions.foundation.ERXProperties;
import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

/**
 * @property <code>er.rest.format.ERXJSONRestWriter.shouldPrettyPrint</code> Boolean property to enable pretty-printing of JSON response. Defaults to false.
 * @property <code>er.rest.format.ERXJSONRestWriter.prettyPrintIndent</code> Integer property to set the pretty print indentation space count. Defaults to <code>2</code>.
 */
public class ERXJSONRestWriter implements IERXRestWriter {

	// Lazily initialized static constants
	private static class CONSTANTS {
		final static boolean SHOULD_PRETTY_PRINT = ERXProperties.booleanForKeyWithDefault("er.rest.format.ERXJSONRestWriter.shouldPrettyPrint", false);
		final static int PRETTY_PRINT_INDENT = ERXProperties.intForKeyWithDefault("er.rest.format.ERXJSONRestWriter.prettyPrintIndent", 2);
	}

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
			JSON jsonObject = JSONSerializer.toJSON(object, configWithContext(context));
			String json = (CONSTANTS.SHOULD_PRETTY_PRINT ? jsonObject.toString(CONSTANTS.PRETTY_PRINT_INDENT) : jsonObject.toString());
			response.appendContentString(json);
		}
		response.appendContentString("\n");
	}
}
