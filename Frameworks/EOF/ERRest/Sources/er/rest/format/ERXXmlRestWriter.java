package er.rest.format;

import java.math.BigDecimal;
import java.util.Map;

import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

public class ERXXmlRestWriter implements IERXRestWriter {
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		response.setHeader("text/xml", "Content-Type");
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate) {
		appendHeadersToResponse(node, response);
		response.appendContentString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		appendNodeToResponse(node, response, 0, delegate);
	}

	protected String coerceValueToString(Object value) {
		return ERXRestUtils.coerceValueToString(value);
	}

	protected void appendNodeToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent, ERXRestFormat.Delegate delegate) {
		delegate.nodeWillWrite(node);
		if (node.value() != null || node.isNull()) {
			appendValueToResponse(node, response, indent);
		}
		else if (node.isArray()) {
			appendArrayToResponse(node, response, indent, delegate);
		}
		else {
			appendDictionaryToResponse(node, response, indent, delegate);
		}
	}

	protected void appendValueToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent) {
		String name = node.name();
		Object value = node.value();
		String formattedValue = coerceValueToString(value);
		if (formattedValue == null) {
			indent(response, indent);
			response.appendContentString("<");
			response.appendContentString(name);
			appendAttributesToResponse(node, response);
			response.appendContentString("/>");
			response.appendContentString("\n");
		}
		else if (formattedValue != null) {
			indent(response, indent);
			response.appendContentString("<");
			response.appendContentString(name);
			appendAttributesToResponse(node, response);
			response.appendContentString(">");

			response.appendContentString(ERXStringUtilities.escapeNonXMLChars(formattedValue));

			response.appendContentString("</");
			response.appendContentString(name);
			response.appendContentString(">");
			response.appendContentString("\n");
		}
		else if (indent == 0) {
			response.appendContentString("<null/>");
		}
	}

	protected void appendAttributesToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		for (Map.Entry<String, String> attribute : node.attributes().entrySet()) {
			String key = attribute.getKey();
			String formattedValue = coerceValueToString(attribute.getValue());
			if (formattedValue != null) {
				response.appendContentString(" ");
				response.appendContentString(key);
				response.appendContentString("=\"");
				response.appendContentString(ERXStringUtilities.escapeNonXMLChars(formattedValue));
				response.appendContentString("\"");
			}
		}
	}

	protected void appendDictionaryToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent, ERXRestFormat.Delegate delegate) {
		String objectName = node.name();
		if (objectName == null) {
			objectName = node.type();
		}

		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		appendAttributesToResponse(node, response);

		if (node.children().size() == 0) {
			response.appendContentString("/>");
			response.appendContentString("\n");
		}
		else {
			response.appendContentString(">");
			response.appendContentString("\n");

			for (ERXRestRequestNode child : node.children()) {
				appendNodeToResponse(child, response, indent + 1, delegate);
			}

			indent(response, indent);
			response.appendContentString("</");
			response.appendContentString(objectName);
			response.appendContentString(">");
			response.appendContentString("\n");
		}
	}

	protected void appendTypeToResponse(Object value, IERXRestResponse response) {
		if (value instanceof String) {
			// do nothing
		}
		else if (!ERXProperties.booleanForKeyWithDefault("ERXRest.suppressTypeAttributesForSimpleTypes", false)) {
			if (value instanceof NSTimestamp) {
				response.appendContentString(" type = \"datetime\"");
			}
			else if (value instanceof Integer) {
				response.appendContentString(" type = \"integer\"");
			}
			else if (value instanceof Long) {
				response.appendContentString(" type = \"long\"");
			}
			else if (value instanceof Short) {
				response.appendContentString(" type = \"short\"");
			}
			else if (value instanceof Double) {
				response.appendContentString(" type = \"double\"");
			}
			else if (value instanceof Float) {
				response.appendContentString(" type = \"float\"");
			}
			else if (value instanceof Boolean) {
				response.appendContentString(" type = \"boolean\"");
			}
			else if (value instanceof BigDecimal) {
				response.appendContentString(" type = \"bigint\"");
			}
			else if (value instanceof Enum) {
				response.appendContentString(" type = \"enum\"");
			}
		}
	}

	protected void appendArrayToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent, ERXRestFormat.Delegate delegate) {
		indent(response, indent);	
		String arrayName = node.name();
		response.appendContentString("<");
		response.appendContentString(arrayName);

		appendAttributesToResponse(node, response);

		response.appendContentString(">");
		response.appendContentString("\n");

		for (ERXRestRequestNode child : node.children()) {
			appendNodeToResponse(child, response, indent + 1, delegate);
		}

		indent(response, indent);
		response.appendContentString("</");
		response.appendContentString(arrayName);
		response.appendContentString(">");
		response.appendContentString("\n");
	}

	protected void indent(IERXRestResponse response, int indent) {
		for (int i = 0; i < indent; i++) {
			response.appendContentString("  ");
		}
	}

}
