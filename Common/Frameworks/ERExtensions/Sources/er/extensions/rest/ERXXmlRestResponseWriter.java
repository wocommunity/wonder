package er.extensions.rest;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXLocalizer;
import er.extensions.ERXStringUtilities;

public class ERXXmlRestResponseWriter implements IERXRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, EOEntity entity, Object value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		response.setHeader("text/xml", "Content-Type");
		StringBuffer xmlBuffer = new StringBuffer();
		if (value instanceof NSArray) {
			String arrayName = ERXLocalizer.currentLocalizer().plurifiedString(entity.name(), 2);
			appendXmlToResponse(context, response, entity, arrayName, (NSArray) value, 0, new NSMutableSet());
		}
		else {
			String entityName = entity.name();
			appendXmlToResponse(context, response, entity, entityName, (EOEnterpriseObject) value, 0, new NSMutableSet());
		}
	}

	protected void indent(WOResponse response, int indent) {
		for (int i = 0; i < indent; i++) {
			response.appendContentString("  ");
		}
	}

	protected void appendXmlToResponse(ERXRestContext context, WOResponse response, EOEntity entity, String arrayName, NSArray values, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(arrayName);

		response.appendContentString(" type = \"");
		response.appendContentString(entity.name());
		response.appendContentString("\"");

		response.appendContentString(">");
		response.appendContentString("\n");

		context.delegate().preprocess(entity, values, context);
		Enumeration valuesEnum = values.objectEnumerator();
		while (valuesEnum.hasMoreElements()) {
			Object value = valuesEnum.nextElement();
			if (value instanceof EOEnterpriseObject) {
				appendXmlToResponse(context, response, entity, entity.name(), (EOEnterpriseObject) value, indent + 1, visitedObjects);
			}
		}

		indent(response, indent);
		response.appendContentString("</");
		response.appendContentString(arrayName);
		response.appendContentString(">");
		response.appendContentString("\n");
	}

	protected void appendXmlToResponse(ERXRestContext context, WOResponse response, EOEntity entity, String objectName, EOEnterpriseObject value, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("<");
		response.appendContentString(objectName);

		response.appendContentString(" type = \"");
		response.appendContentString(entity.name());
		response.appendContentString("\"");

		response.appendContentString(" id = \"");
		EOKeyGlobalID gid = (EOKeyGlobalID) value.editingContext().globalIDForObject(value);
		Object id = gid.keyValues()[0];
		response.appendContentString(id.toString());
		response.appendContentString("\"");

		if (!visitedObjects.containsObject(value)) {
			visitedObjects.addObject(value);

			response.appendContentString(">");
			response.appendContentString("\n");

			NSArray visiblePropertyNames = context.delegate().displayPropertyNames(entity, value, context);
			Enumeration propertyNamesEnum = visiblePropertyNames.objectEnumerator();
			while (propertyNamesEnum.hasMoreElements()) {
				String propertyName = (String) propertyNamesEnum.nextElement();
				Object propertyValue = NSKeyValueCoding.Utility.valueForKey(value, propertyName);
				if (propertyValue != null) {
					EORelationship relationship = entity.relationshipNamed(propertyName);
					if (relationship != null) {
						EOEntity destinationEntity = relationship.destinationEntity();
						if (propertyValue instanceof NSArray) {
							NSArray values = (NSArray) propertyValue;
							appendXmlToResponse(context, response, destinationEntity, relationship.name(), values, indent + 1, visitedObjects);
						}
						else {
							appendXmlToResponse(context, response, destinationEntity, relationship.name(), (EOEnterpriseObject) propertyValue, indent + 1, visitedObjects);
						}
					}
					else {
						indent(response, indent + 1);
						response.appendContentString("<");
						response.appendContentString(propertyName);
						response.appendContentString(">");

						String formattedPropertyValue = context.delegate().entityDelegate(entity).formatAttributeValue(entity, value, propertyName, propertyValue);
						String attributeValueStr = ERXStringUtilities.escapeNonXMLChars(formattedPropertyValue);
						response.appendContentString(attributeValueStr);

						response.appendContentString("</");
						response.appendContentString(propertyName);
						response.appendContentString(">");
						response.appendContentString("\n");
					}
				}
			}

			indent(response, indent);
			response.appendContentString("</");
			response.appendContentString(objectName);
			response.appendContentString(">");
		}
		else {
			response.appendContentString("/>");
		}
		response.appendContentString("\n");
	}
}