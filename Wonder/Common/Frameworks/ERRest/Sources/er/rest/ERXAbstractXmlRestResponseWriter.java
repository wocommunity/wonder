package er.rest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXStringUtilities;

/**
 * Provides the common output methods for generating XML responses to a REST request.
 * 
 * @author mschrag
 */
public abstract class ERXAbstractXmlRestResponseWriter extends ERXAbstractRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		response.setHeader("text/xml", "Content-Type");
		response.appendContentString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		super.appendToResponse(context, response, result);
	}
	
	protected void appendTypeToResponse(WOResponse response, Object value) {
		if (value instanceof String) {
			// do nothing
		}
		else if (value instanceof NSTimestamp) {
			response.appendContentString(" type = \"datetime\"");
		}
		else if (value instanceof Integer) {
			response.appendContentString(" type = \"integer\"");
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
	}

	protected void indent(WOResponse response, int indent) {
		for (int i = 0; i < indent; i++) {
			response.appendContentString("  ");
		}
	}

	protected void appendArrayToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, String arrayName, String entityName, NSArray valueKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(arrayName);

		response.appendContentString(" type = \"");
		response.appendContentString(entityName);
		response.appendContentString("\"");

		response.appendContentString(">");
		response.appendContentString("\n");

		Enumeration valueKeysEnum = valueKeys.objectEnumerator();
		while (valueKeysEnum.hasMoreElements()) {
			ERXRestKey eoKey = (ERXRestKey) valueKeysEnum.nextElement();
			appendToResponse(context, response, eoKey, indent + 1, visitedObjects);
		}

		indent(response, indent);
		response.appendContentString("</");
		response.appendContentString(arrayName);
		response.appendContentString(">");
		response.appendContentString("\n");
	}
	
	protected void appendVisitedToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		if (!objectName.equals(entityName)) {
			response.appendContentString(" type = \"");
			response.appendContentString(entityName);
			response.appendContentString("\"");
		}
		
		response.appendContentString(" id = \"");
		response.appendContentString(String.valueOf(id));
		response.appendContentString("\"");
		
		response.appendContentString("/>");
		response.appendContentString("\n");
	}
	
	protected void appendNoDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		if (!objectName.equals(entityName)) {
			response.appendContentString(" type = \"");
			response.appendContentString(entityName);
			response.appendContentString("\"");
		}
		
		response.appendContentString(">");

		response.appendContentString(String.valueOf(id));

		response.appendContentString("</");
		response.appendContentString(objectName);
		response.appendContentString(">\n");
		
//		response.appendContentString(" id = \"");
//		response.appendContentString(String.valueOf(id));
//		response.appendContentString("\"");
//		response.appendContentString("/>\n");
//		
//		response.appendContentString(">\n");
//		
//		indent(response, indent + 1);
//		response.appendContentString("<id");
//		appendTypeToResponse(response, id);
//		response.appendContentString(">");
//		response.appendContentString(String.valueOf(id));
//		response.appendContentString("</id>");
//		response.appendContentString("\n");
//
//		indent(response, indent);
//		response.appendContentString("</");
//		response.appendContentString(objectName);
//		response.appendContentString(">\n");
	}

	protected void appendDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, NSArray displayKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		if (!objectName.equals(entityName)) {
			response.appendContentString(" type = \"");
			response.appendContentString(entityName);
			response.appendContentString("\"");
		}
		response.appendContentString(">");
		response.appendContentString("\n");
		
		indent(response, indent + 1);
		response.appendContentString("<id");
		appendTypeToResponse(response, id);
		response.appendContentString(">");
		response.appendContentString(String.valueOf(id));
		response.appendContentString("</id>");
		response.appendContentString("\n");
		
		Enumeration displayKeysEnum = displayKeys.objectEnumerator();
		while (displayKeysEnum.hasMoreElements()) {
			ERXRestKey displayKey = (ERXRestKey)displayKeysEnum.nextElement();
			String propertyName = displayKey.key();
			Object propertyValue = displayKey.value();
			if (propertyValue instanceof NSArray) {
				appendToResponse(context, response, displayKey, indent + 1, visitedObjects);
			}
			else if (propertyValue instanceof EOEnterpriseObject) {
				appendToResponse(context, response, displayKey, indent + 1, visitedObjects);
			}
			else {
				String formattedPropertyValue = context.delegate().entityDelegate(entity).formatAttributeValue(entity, eo, propertyName, propertyValue);
				if (formattedPropertyValue != null) {
					String propertyAlias = displayKey.keyAlias();
					indent(response, indent + 1);
					response.appendContentString("<");
					response.appendContentString(propertyAlias);
					appendTypeToResponse(response, propertyValue);
					response.appendContentString(">");

					String attributeValueStr = ERXStringUtilities.escapeNonXMLChars(formattedPropertyValue);
					response.appendContentString(attributeValueStr);

					response.appendContentString("</");
					response.appendContentString(propertyAlias);
					response.appendContentString(">");
					response.appendContentString("\n");
				}
			}
		}
		
		indent(response, indent);
		response.appendContentString("</");
		response.appendContentString(objectName);
		response.appendContentString(">");
		response.appendContentString("\n");
	}
	
	protected void appendPrimitiveToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, Object value) throws ERXRestException {
		indent(response, indent);
		response.appendContentString(String.valueOf(value));
	}
}