package er.rest;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.Stack;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;

/**
 * Provides the common output methods for generating a dictionary response, which can be used by several other writers
 * (json, plist, etc).
 * 
 * @author mschrag
 */
public class ERXDictionaryRestResponseWriter extends ERXAbstractRestResponseWriter {
	private Stack<Object> _stack;

	/**
	 * Constructs an ERXDictionaryRestResponseWriter with displayAllProperties = false.
	 */
	public ERXDictionaryRestResponseWriter() {
		this(false, false);
	}

	/**
	 * Constructs an ERXDictionaryRestResponseWriter.
	 * 
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public ERXDictionaryRestResponseWriter(boolean displayAllProperties, boolean displayAllToMany) {
		super(displayAllProperties, displayAllToMany);
		_stack = new Stack<Object>();
		//_stack.push(new NSMutableDictionary<String, Object>());
	}

	public Object root() {
		return _stack.size() > 0 ? _stack.firstElement() : null;
	}

	public Object current() {
		return _stack.size() > 0 ? _stack.peek() : null;
	}

	@SuppressWarnings("unchecked")
	protected void addToCollection(String name, Object value) {
		Object collection = current();
		if (collection instanceof NSMutableDictionary) {
			((NSMutableDictionary<String, Object>) collection).setObjectForKey(value, name);
		}
		else if (collection instanceof NSMutableArray) {
			((NSMutableArray) collection).addObject(value);
		}
		else if (collection == null) {
			_stack.push(value);
		}
		else {
			throw new IllegalArgumentException("Fail");
		}
	}

	@Override
	protected void appendArrayToResponse(ERXRestContext context, IERXResponseWriter response, ERXRestKey result, String arrayName, String entityName, NSArray valueKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		NSMutableArray<Object> array = new NSMutableArray<Object>();
		_stack.push(array);
		Enumeration valueKeysEnum = valueKeys.objectEnumerator();
		while (valueKeysEnum.hasMoreElements()) {
			ERXRestKey eoKey = (ERXRestKey) valueKeysEnum.nextElement();
			appendToResponse(context, response, eoKey, indent + 1, visitedObjects);
		}
		_stack.pop();

		addToCollection(arrayName, array);
	}

	@Override
	protected void appendVisitedToResponse(ERXRestContext context, IERXResponseWriter response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
		NSMutableDictionary<String, Object> value = new NSMutableDictionary<String, Object>();
		value.setObjectForKey(entityName, "_type");
		value.setObjectForKey(String.valueOf(id), "id");
		addToCollection(objectName, value);
	}

	@Override
	protected void appendNoDetailsToResponse(ERXRestContext context, IERXResponseWriter response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent) {
		NSMutableDictionary<String, Object> value = new NSMutableDictionary<String, Object>();
		value.setObjectForKey(entityName, "_type");
		value.setObjectForKey(String.valueOf(id), "id");
		addToCollection(objectName, value);
	}

	@Override
	protected void appendDetailsToResponse(ERXRestContext context, IERXResponseWriter response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, NSArray displayKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		NSMutableDictionary<String, Object> value = new NSMutableDictionary<String, Object>();
		value.setObjectForKey(entityName, "_type");
		value.setObjectForKey(String.valueOf(id), "id");

		_stack.push(value);
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
					value.setObjectForKey(formattedPropertyValue, propertyAlias);
				}
			}
		}
		_stack.pop();

		addToCollection(objectName, value);
	}

	@Override
	protected void appendPrimitiveToResponse(ERXRestContext context, IERXResponseWriter response, ERXRestKey result, int indent, Object value) throws ERXRestException {
		addToCollection("_", value);
	}
}