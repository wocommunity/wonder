package er.rest;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXLocalizer;
import er.extensions.ERXStringUtilities;

public abstract class ERXAbstractXmlRestResponseWriter implements IERXRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		response.setHeader("text/xml", "Content-Type");
		StringBuffer xmlBuffer = new StringBuffer();
		appendXmlToResponse(context, response, result.trimPrevious(), 0, new NSMutableSet());
	}

	protected void indent(WOResponse response, int indent) {
		for (int i = 0; i < indent; i++) {
			response.appendContentString("  ");
		}
	}

	protected void appendArrayXmlToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("<");
		String arrayName;
		EOEntity entity = result.nextEntity();
		if (result.key() == null) {
			arrayName = entity.name();
		}
		else {
			arrayName = result.key();
		}
		NSArray values = (NSArray) result.value();
		if (arrayName.equals(entity.name())) {
			arrayName = ERXLocalizer.currentLocalizer().plurifiedString(arrayName, 2);
		}
		response.appendContentString(arrayName);

		response.appendContentString(" type = \"");
		response.appendContentString(entity.name());
		response.appendContentString("\"");

		response.appendContentString(">");
		response.appendContentString("\n");

		context.delegate().preprocess(entity, values, context);
		Enumeration valuesEnum = values.objectEnumerator();
		while (valuesEnum.hasMoreElements()) {
			EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
			ERXRestKey eoKey = result.extend(ERXRestUtils.idForEO(eo), eo, true);
			appendXmlToResponse(context, response, eoKey, indent + 1, visitedObjects);
		}

		indent(response, indent);
		response.appendContentString("</");
		response.appendContentString(arrayName);
		response.appendContentString(">");
		response.appendContentString("\n");
	}

	protected abstract boolean displayDetails(ERXRestContext context, ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	protected abstract String[] displayProperties(ERXRestContext context, ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	protected void appendXmlToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		Object value = result.value();
		if (value == null) {
			// DO NOTHING
		}
		else if (value instanceof NSArray) {
			appendArrayXmlToResponse(context, response, result, indent, visitedObjects);
		}
		else {
			indent(response, indent);
			response.appendContentString("<");
			String objectName;
			if (result.previousKey() == null || result.isKeyGID()) {
				objectName = result.nextEntity().name();
			}
			else {
				objectName = result.key();
			}
			response.appendContentString(objectName);

			EOEntity entity = result.nextEntity();
			EOEnterpriseObject eo = (EOEnterpriseObject) value;
			if (!objectName.equals(entity.name())) {
				response.appendContentString(" type = \"");
				response.appendContentString(entity.name());
				response.appendContentString("\"");
			}

			response.appendContentString(" id = \"");
			response.appendContentString(ERXRestUtils.idForEO(eo));
			response.appendContentString("\"");

			boolean displayDetails = displayDetails(context, result);
			if (!visitedObjects.containsObject(eo) && displayDetails) {
				visitedObjects.addObject(eo);

				response.appendContentString(">");
				response.appendContentString("\n");

				String[] displayPropertyNames = displayProperties(context, result);
				if (displayPropertyNames != null) {
					IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
					for (int displayPropertyNum = 0; displayPropertyNum < displayPropertyNames.length; displayPropertyNum++) {
						String propertyName = displayPropertyNames[displayPropertyNum];
						if (context.delegate().entityDelegate(entity).canViewProperty(entity, eo, propertyName, context)) {
							// EORelationship relationship = entity.relationshipNamed(propertyName);
							// if (relationship != null && !relationship.isToMany()) {
							//								
							// }
							ERXRestKey nextKey = result.extend(propertyName, true);
							Object propertyValue = nextKey.value();
							if (propertyValue instanceof NSArray) {
								appendXmlToResponse(context, response, nextKey, indent + 1, visitedObjects);
							}
							else if (propertyValue instanceof EOEnterpriseObject) {
								appendXmlToResponse(context, response, nextKey, indent + 1, visitedObjects);
							}
							else {
								String formattedPropertyValue = context.delegate().entityDelegate(entity).formatAttributeValue(entity, eo, propertyName, propertyValue);
								if (formattedPropertyValue != null) {
									indent(response, indent + 1);
									response.appendContentString("<");
									response.appendContentString(propertyName);
									response.appendContentString(">");

									String attributeValueStr = ERXStringUtilities.escapeNonXMLChars(formattedPropertyValue);
									response.appendContentString(attributeValueStr);

									response.appendContentString("</");
									response.appendContentString(propertyName);
									response.appendContentString(">");
									response.appendContentString("\n");
								}
							}
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
}