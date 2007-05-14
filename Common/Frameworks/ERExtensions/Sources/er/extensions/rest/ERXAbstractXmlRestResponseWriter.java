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

public abstract class ERXAbstractXmlRestResponseWriter implements IERXRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestResult result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		response.setHeader("text/xml", "Content-Type");
		StringBuffer xmlBuffer = new StringBuffer();
		appendXmlToResponse(context, response, result, 0, new NSMutableSet());
	}

	protected void indent(WOResponse response, int indent) {
		for (int i = 0; i < indent; i++) {
			response.appendContentString("  ");
		}
	}

	protected void appendArrayXmlToResponse(ERXRestContext context, WOResponse response, ERXRestResult result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("<");
		String arrayName;
		EOEntity entity = result.entity();
		ERXRestResult previousResult = result.previousResult();
		if (previousResult == null || previousResult.nextKey() == null) {
			arrayName = entity.name();
		}
		else {
			arrayName = previousResult.nextKey();
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
			Object value = valuesEnum.nextElement();
			ERXRestResult nextResult = new ERXRestResult(result.extendResult(null), entity, value, null);
			appendXmlToResponse(context, response, nextResult, indent + 1, visitedObjects);
		}

		indent(response, indent);
		response.appendContentString("</");
		response.appendContentString(arrayName);
		response.appendContentString(">");
		response.appendContentString("\n");
	}

	protected abstract boolean displayDetails(ERXRestContext context, ERXRestResult result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	protected abstract String[] displayProperties(ERXRestContext context, ERXRestResult result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	protected void appendXmlToResponse(ERXRestContext context, WOResponse response, ERXRestResult result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
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
			ERXRestResult previousResult = result.previousResult();
			if (previousResult == null || previousResult.isKeyGID() || previousResult.nextKey() == null) {
				objectName = result.entity().name();
			}
			else {
				objectName = previousResult.nextKey();
			}
			response.appendContentString(objectName);

			EOEntity entity = result.entity();
			EOEnterpriseObject eo = (EOEnterpriseObject) value;
			if (!objectName.equals(entity.name())) {
				response.appendContentString(" type = \"");
				response.appendContentString(entity.name());
				response.appendContentString("\"");
			}

			response.appendContentString(" id = \"");
			EOKeyGlobalID gid = (EOKeyGlobalID) eo.editingContext().globalIDForObject(eo);
			Object id = gid.keyValues()[0];
			response.appendContentString(id.toString());
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
						if (entityDelegate.canViewProperty(entity, eo, propertyName, context)) {
							EORelationship relationship = entity.relationshipNamed(propertyName);
							if (relationship != null) {
								ERXRestResult nextResult = result.extendResult(propertyName).nextResult(context);
								if (relationship != null) {
									appendXmlToResponse(context, response, nextResult, indent + 1, visitedObjects);
								}
							}
							else {
								Object propertyValue = NSKeyValueCoding.Utility.valueForKey(eo, propertyName);
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