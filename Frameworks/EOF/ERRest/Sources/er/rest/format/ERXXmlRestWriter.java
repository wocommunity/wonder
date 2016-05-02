package er.rest.format;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

/**
 *
 * @property ERXRest.suppressTypeAttributesForSimpleTypes (default "false") If set to true, primitive types, like type = "datetime", won't be added to the output
 * @property <code>ERXRest.includeNullValues</code> Boolean property to enable null values in return. Defaults
 *           to true.
 */
public class ERXXmlRestWriter implements IERXRestWriter {
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context) {
		response.setHeader("text/xml", "Content-Type");
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		appendHeadersToResponse(node, response, context);
		response.appendContentString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		appendNodeToResponse(node, response, 0, delegate, context);
	}

	protected String coerceValueToString(Object value, ERXRestContext context) {
		return ERXRestUtils.coerceValueToString(value, context);
	}

	protected void appendNodeToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		if (delegate != null) {
			delegate.nodeWillWrite(node);
		}
		if (node.value() != null || node.isNull()) {
			appendValueToResponse(node, response, indent, context);
		}
		else if (node.isArray()) {
			appendArrayToResponse(node, response, indent, delegate, context);
		}
		else {
			appendDictionaryToResponse(node, response, indent, delegate, context);
		}
	}

	protected void appendValueToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent, ERXRestContext context) {
		String name = node.name();
		Object value = node.value();
		String formattedValue = coerceValueToString(value, context);

		if (formattedValue == null) {
			if(ERXProperties.booleanForKeyWithDefault("ERXRest.includeNullValues", true)) {
				indent(response, indent);
				response.appendContentString("<");
				response.appendContentString(name);
				appendAttributesToResponse(node, response, context);
				appendTypeToResponse(value, response);
				response.appendContentString("/>");
				response.appendContentString("\n");
			}
		} else {
			indent(response, indent);
			response.appendContentString("<");
			response.appendContentString(name);
			appendAttributesToResponse(node, response, context);
			appendTypeToResponse(value, response);
			response.appendContentString(">");

			response.appendContentString(ERXStringUtilities.escapeNonXMLChars(formattedValue));

			response.appendContentString("</");
			response.appendContentString(name);
			response.appendContentString(">");
			response.appendContentString("\n");
		}
	}

	protected void appendAttributesToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context) {
		for (Map.Entry<String, Object> attribute : node.attributes().entrySet()) {
			String key = attribute.getKey();
			String formattedValue = coerceValueToString(attribute.getValue(), context);
			if (formattedValue != null) {
				response.appendContentString(" ");
				response.appendContentString(key);
				response.appendContentString("=\"");
				response.appendContentString(ERXStringUtilities.escapeNonXMLChars(formattedValue));
				response.appendContentString("\"");
			}
		}
	}

	protected void appendDictionaryToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		String objectName = node.name();
		if (objectName == null) {
			objectName = node.type();
		}

		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		appendAttributesToResponse(node, response, context);

		if (node.children().size() == 0) {
			response.appendContentString("/>");
			response.appendContentString("\n");
		}
		else {
			response.appendContentString(">");
			response.appendContentString("\n");

			for (ERXRestRequestNode child : node.children()) {
				appendNodeToResponse(child, response, indent + 1, delegate, context);
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
			else if (value instanceof Date) {
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

	protected void appendArrayToResponse(ERXRestRequestNode node, IERXRestResponse response, int indent, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		indent(response, indent);	
		String arrayName = node.name();
		response.appendContentString("<");
		response.appendContentString(arrayName);

		appendAttributesToResponse(node, response, context);

		response.appendContentString(">");
		response.appendContentString("\n");

		for (ERXRestRequestNode child : node.children()) {
			appendNodeToResponse(child, response, indent + 1, delegate, context);
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
