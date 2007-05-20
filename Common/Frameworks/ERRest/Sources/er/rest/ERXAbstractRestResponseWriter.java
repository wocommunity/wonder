package er.rest;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXLocalizer;

/**
 * ERXAbstractRestResponseWriter provides the output-method-agnostic methods for processing a rest response.
 * 
 * @author mschrag
 */
public abstract class ERXAbstractRestResponseWriter implements IERXRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		appendToResponse(context, response, result.trimPrevious(), 0, new NSMutableSet());
	}

	protected void appendArrayToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		String arrayName;
		EOEntity entity = result.nextEntity();
		IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
		String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());
		if (result.keyAlias() == null) {
			arrayName = entityAlias;
		}
		else {
			arrayName = result.keyAlias();
		}
		NSArray values = (NSArray) result.value();
		if (arrayName.equals(entityAlias)) {
			arrayName = ERXLocalizer.currentLocalizer().plurifiedString(arrayName, 2);
		}
		entityDelegate.preprocess(entity, values, context);

		NSMutableArray valueKeys = new NSMutableArray();
		Enumeration valuesEnum = values.objectEnumerator();
		while (valuesEnum.hasMoreElements()) {
			EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
			ERXRestKey eoKey = result.extend(ERXRestUtils.idForEO(eo), eo);
			valueKeys.addObject(eoKey);
		}

		appendArrayToResponse(context, response, result, arrayName, entityAlias, valueKeys, indent, visitedObjects);
	}

	protected void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		Object value = result.value();
		if (value == null) {
			// DO NOTHING
		}
		else if (value instanceof NSArray) {
			appendArrayToResponse(context, response, result, indent, visitedObjects);
		}
		else {
			EOEntity entity = result.nextEntity();
			IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
			String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());

			String objectName;
			if (result.previousKey() == null || result.isKeyGID()) {
				objectName = entityAlias;
			}
			else {
				objectName = result.keyAlias();
			}

			EOEnterpriseObject eo = (EOEnterpriseObject) value;
			String id = ERXRestUtils.idForEO(eo);

			boolean alreadyVisited = visitedObjects.containsObject(eo);
			if (alreadyVisited) {
				appendVisitedToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
			}
			else {
				visitedObjects.addObject(eo);
				boolean displayDetails = displayDetails(context, result);
				if (!displayDetails) {
					appendNoDetailsToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
				}
				else {
					NSMutableArray displayKeys = new NSMutableArray();
					String[] displayPropertyNames = displayProperties(context, result);
					if (displayPropertyNames != null && displayPropertyNames.length > 0) {
						for (int displayPropertyNum = 0; displayPropertyNum < displayPropertyNames.length; displayPropertyNum++) {
							String propertyName = displayPropertyNames[displayPropertyNum];
							if (entityDelegate.canViewProperty(entity, eo, propertyName, context)) {
								ERXRestKey nextKey = result.extend(entityDelegate.propertyAliasForPropertyNamed(entity, propertyName));
								displayKeys.addObject(nextKey);
								// EORelationship relationship = entity.relationshipNamed(propertyName);
								// if (relationship != null && !relationship.isToMany()) {
								//								
								// }
							}
						}
					}
					if (displayKeys.count() == 0) {
						appendNoDetailsToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
					}
					else {
						appendDetailsToResponse(context, response, entity, eo, objectName, entityAlias, id, displayKeys, indent, visitedObjects);
					}
				}
			}
		}
	}

	/**
	 * Writes the given array of objects to the response. Permission have already been checked by the time this method
	 * is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param key
	 *            the current key
	 * @param arrayName
	 *            the name of the array in the context of its parent
	 * @param entityName
	 *            the entity name of the contents of the array
	 * @param valueKeys
	 *            an array of ERXRestKeys that represent the entries in the array
	 * @param indent
	 *            the indent level
	 * @param visitedObjects
	 *            the list of objects that have been visited already in this request (to prevent infinite loops)
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 * @throws ParseException
	 *             if a parse error occurs
	 */
	protected abstract void appendArrayToResponse(ERXRestContext context, WOResponse response, ERXRestKey key, String arrayName, String entityName, NSArray valueKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;

	/**
	 * Returns whether or not the details (i.e. the keys of an EO) should displayed for the given key.
	 * 
	 * @param context
	 *            the rest context
	 * @param key
	 *            the current key
	 * @return whether or not the details (i.e. the keys of an EO) should displayed for the given key
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 */
	protected abstract boolean displayDetails(ERXRestContext context, ERXRestKey key) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	/**
	 * Returns the set of properties that can be displayed for the given key.
	 * 
	 * @param context
	 *            the rest context
	 * @param key
	 *            the current key
	 * @return the set of properties that can be displayed for the given key
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 */
	protected abstract String[] displayProperties(ERXRestContext context, ERXRestKey key) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	/**
	 * Write an object to the response that has already been visited. Typically this would just write out the type and
	 * id of the object, to prevent entering an infinite loop in the renderer. Permission have already been checked by
	 * the time this method is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the current object
	 * @param objectName
	 *            the name of the object (relative to its parent)
	 * @param entityName
	 *            the entity name of the object
	 * @param id
	 *            the id of the object
	 * @param indent
	 *            the indent level
	 */
	protected abstract void appendVisitedToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, int indent);

	/**
	 * Write an object to the response without showing its details. This is typically similar to
	 * appendVisitedToResponse, but is provided as a separate call because it is semantically a different scenario.
	 * Permission have already been checked by the time this method is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the current object
	 * @param objectName
	 *            the name of the object (relative to its parent)
	 * @param entityName
	 *            the entity name of the object
	 * @param id
	 *            the id of the object
	 * @param indent
	 *            the indent level
	 */
	protected abstract void appendNoDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, int indent);

	/**
	 * Writes the visible details of an object to the response. Permission have already been checked by the time this
	 * method is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the current object
	 * @param objectName
	 *            the name of the object (relative to its parent)
	 * @param entityName
	 *            the entity name of the object
	 * @param id
	 *            the id of the object
	 * @param displayKeys
	 *            the list of ERXRestKeys to display
	 * @param indent
	 *            the indent level
	 * @param visitedObjects
	 *            the list of objects that have been visited already in this request (to prevent infinite loops)
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 * @throws ParseException
	 *             if a parse error occurs
	 */
	protected abstract void appendDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, NSArray displayKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;
}