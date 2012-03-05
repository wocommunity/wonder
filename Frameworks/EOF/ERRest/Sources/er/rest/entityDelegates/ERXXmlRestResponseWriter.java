package er.rest.entityDelegates;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXKeyFilter;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestException;
import er.rest.format.IERXRestResponse;

/**
 * <p>
 * Provides the common output methods for generating XML responses to a REST request.
 * </p>
 * 
 * <p>
 * The response writers can be used in two styles. In one style, they can be connected
 * to a rest request handler and are controlled with Properties. In another form, they 
 * can be used programmatically and can be configured with ERXKeyFilters to control
 * their output. ERXKeyFilters provides a really bad version of Rails' to_json 
 * :include maps.
 * </p>
 * 
 * <pre>
 * ERXKeyFilter companyFilter = new ERXKeyFilter(ERXKeyFilter.Base.Attributes);
 * ERXKeyFilter remindersFilter = companyFilter.include(Company.REMINDERS);
 * remindersFilter.include(Reminder.SUMMARY);
 * remindersFilter.exclude(Reminder.CREATION_DATE);
 * 
 * ERXPListRestResponseWriter writer = new ERXPListRestResponseWriter(companyFilter);
 * String str = writer.toString(Company.fetchRequiredCompany(ERXEC.newEditingContext(), Company.NAME.is("mDT Consulting")));
 * </pre>

 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXXmlRestResponseWriter extends ERXAbstractRestResponseWriter {
	/**
	 * Constructs an ERXXmlRestResponseWriter with displayAllProperties = false.
	 */
	public ERXXmlRestResponseWriter() {
		this(false, false);
	}

	/**
	 * Constructs an ERXXmlRestResponseWriter.
	 * 
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public ERXXmlRestResponseWriter(boolean displayAllProperties, boolean displayAllToMany) {
		super(displayAllProperties, displayAllToMany);
	}
	
	/**
	 * Constructs an ERXXmlRestResponseWriter.
	 */
	public ERXXmlRestResponseWriter(ERXKeyFilter filter) {
		super(filter);
	}


	@Override
	public void appendToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		response.setHeader("text/xml", "Content-Type");
		response.appendContentString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		super.appendToResponse(context, response, result);
	}

	protected void appendTypeToResponse(IERXRestResponse response, Object value) {
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

	@Override
	protected void appendArrayToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result, String arrayName, String entityName, NSArray valueKeys, int indent, NSMutableSet<Object> visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
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

	@Override
	protected void appendVisitedToResponse(ERXRestContext context, IERXRestResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
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

	@Override
	protected void appendNoDetailsToResponse(ERXRestContext context, IERXRestResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
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

		// response.appendContentString(">");
		//
		// response.appendContentString(String.valueOf(id));
		//
		// response.appendContentString("</");
		// response.appendContentString(objectName);
		// response.appendContentString(">\n");

		// response.appendContentString(" id = \"");
		// response.appendContentString(String.valueOf(id));
		// response.appendContentString("\"");
		// response.appendContentString("/>\n");
		//		
		// response.appendContentString(">\n");
		//		
		// indent(response, indent + 1);
		// response.appendContentString("<id");
		// appendTypeToResponse(response, id);
		// response.appendContentString(">");
		// response.appendContentString(String.valueOf(id));
		// response.appendContentString("</id>");
		// response.appendContentString("\n");
		//
		// indent(response, indent);
		// response.appendContentString("</");
		// response.appendContentString(objectName);
		// response.appendContentString(">\n");
	}

	@Override
	protected void appendDetailsToResponse(ERXRestContext context, IERXRestResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, NSArray displayKeys, int indent, NSMutableSet<Object> visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
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
			ERXRestKey displayKey = (ERXRestKey) displayKeysEnum.nextElement();
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
					if (propertyValue instanceof String) {
						appendTypeToResponse(response, ERXStringUtilities.escapeNonBasicLatinChars((String) propertyValue));
					}
					else {
						appendTypeToResponse(response, propertyValue);
					}
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

	@Override
	protected void appendPrimitiveToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result, int indent, Object value) throws ERXRestException {
		indent(response, indent);
		response.appendContentString(String.valueOf(value));
	}
}