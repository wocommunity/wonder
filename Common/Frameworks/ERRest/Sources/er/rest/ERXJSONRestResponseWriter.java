package er.rest;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;

public class ERXJSONRestResponseWriter extends ERXAbstractRestResponseWriter {
	private boolean _displayAllProperties;
	private boolean _displayAllToMany;

	/**
	 * Constructs an ERXJSONRestResponseWriter with displayAllProperties = false.
	 */
	public ERXJSONRestResponseWriter() {
		this(false, false);
	}

	/**
	 * Constructs an ERXJSONRestResponseWriter.
	 * 
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public ERXJSONRestResponseWriter(boolean displayAllProperties, boolean displayAllToMany) {
		_displayAllProperties = displayAllProperties;
		_displayAllToMany = displayAllToMany;
	}

	@Override
	protected void appendArrayToResponse(ERXRestContext context, WOResponse response, ERXRestKey key, String arrayName, String entityName, NSArray valueKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		response.appendContentString("[");

		Enumeration valueKeysEnum = valueKeys.objectEnumerator();
		while (valueKeysEnum.hasMoreElements()) {
			ERXRestKey eoKey = (ERXRestKey) valueKeysEnum.nextElement();
			appendToResponse(context, response, eoKey, indent + 1, visitedObjects);
			if (valueKeysEnum.hasMoreElements()) {
				response.appendContentString(", ");
			}
		}

		response.appendContentString("]");
	}

	@Override
	protected void appendDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, NSArray displayKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		indent(response, indent);
		response.appendContentString("{\n");
		
		indent(response, indent + 1);
		response.appendContentString("\"id\" = ");
		if (id instanceof String) {
			response.appendContentString("\"");
			response.appendContentString((String)id);
			response.appendContentString("\"");
		}
		else {
			response.appendContentString(String.valueOf(id));
		}

		Enumeration displayKeysEnum = displayKeys.objectEnumerator();
		if (displayKeysEnum.hasMoreElements()) {
			response.appendContentString(",\n");
		}
		while (displayKeysEnum.hasMoreElements()) {
			ERXRestKey displayKey = (ERXRestKey)displayKeysEnum.nextElement();
			String propertyName = displayKey.key();
			Object propertyValue = displayKey.value();
			
			indent(response, indent + 1);
			response.appendContentString("\"");
			String propertyAlias = displayKey.keyAlias();
			response.appendContentString(propertyAlias);
			response.appendContentString("\": ");
			
			boolean displayed = true;
			if (propertyValue instanceof NSArray) {
				appendToResponse(context, response, displayKey, indent + 1, visitedObjects);
			}
			else if (propertyValue instanceof EOEnterpriseObject) {
				appendToResponse(context, response, displayKey, indent + 1, visitedObjects);
			}
			else {
				String formattedPropertyValue = context.delegate().entityDelegate(entity).formatAttributeValue(entity, eo, propertyName, propertyValue);
				if (formattedPropertyValue != null) {
					if (propertyValue instanceof String || propertyValue instanceof NSTimestamp) {
						response.appendContentString("\"");
						response.appendContentString(formattedPropertyValue);
						response.appendContentString("\"");
					}
					else {
						response.appendContentString(formattedPropertyValue);
					}
				}
				else {
					displayed = false;
				}
			}
			if (displayed) {
				if (displayKeysEnum.hasMoreElements()) {
					response.appendContentString(",");
				}
				response.appendContentString("\n");
			}
		}
		response.appendContentString("}");
	}

	@Override
	protected void appendNoDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
		response.appendContentString("{ \"id\" = ");
		if (id instanceof String) {
			response.appendContentString("\"");
			response.appendContentString((String)id);
			response.appendContentString("\"");
		}
		else {
			response.appendContentString(String.valueOf(id));
		}
		response.appendContentString(" }");
	}

	@Override
	protected void appendPrimitiveToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, Object value) throws ERXRestException {
		indent(response, indent);
		response.appendContentString(String.valueOf(value));
	}

	@Override
	protected void appendVisitedToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
		response.appendContentString("{");
		response.appendContentString("}");
	}

	@Override
	protected boolean displayDetails(ERXRestContext context, ERXRestKey key) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		return _displayDetailsFromProperties(context, key);
	}

	@Override
	protected String[] displayProperties(ERXRestContext context, ERXRestKey key) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		return _displayPropertiesFromProperties(context, key, _displayAllProperties, _displayAllToMany);
	}

}
