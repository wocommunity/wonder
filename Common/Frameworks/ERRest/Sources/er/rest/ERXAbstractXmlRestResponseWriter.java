package er.rest;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXStringUtilities;

/**
 * Provides the common output methods for generating XML responses to a REST request.
 * 
 * @author mschrag
 */
public abstract class ERXAbstractXmlRestResponseWriter extends ERXAbstractRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		response.setHeader("text/xml", "Content-Type");
		super.appendToResponse(context, response, result);
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
	
	protected void appendVisitedToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, int indent) {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		if (!objectName.equals(entityName)) {
			response.appendContentString(" type = \"");
			response.appendContentString(entityName);
			response.appendContentString("\"");
		}
		
		response.appendContentString(" id = \"");
		response.appendContentString(id);
		response.appendContentString("\"");
		
		response.appendContentString("/>");
		response.appendContentString("\n");
	}
	
	protected void appendNoDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, int indent) {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		if (!objectName.equals(entityName)) {
			response.appendContentString(" type = \"");
			response.appendContentString(entityName);
			response.appendContentString("\"");
		}
		
		response.appendContentString(" id = \"");
		response.appendContentString(id);
		response.appendContentString("\"");
		
		response.appendContentString("/>");
		response.appendContentString("\n");
	}

	protected void appendDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, NSArray displayKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		if (!objectName.equals(entityName)) {
			response.appendContentString(" type = \"");
			response.appendContentString(entityName);
			response.appendContentString("\"");
		}
		
		response.appendContentString(" id = \"");
		response.appendContentString(id);
		response.appendContentString("\"");
		
		response.appendContentString(">");
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
		System.out.println("ERXAbstractXmlRestResponseWriter.appendPrimitiveToResponse: write " + value);
		indent(response, indent);
		response.appendContentString(String.valueOf(value));
	}
}