package er.rest;

import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

/**
 * IERXRestEntityDelegate provides the interface for the applications to hook into the rest process on a per-entity
 * basis. Entity delegates are typically registered on the ERXRestDelegate of ERXRestContext in your Application
 * constructor.
 * 
 * <pre>
 * ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate();
 * restDelegate.addDelegateForEntityNamed(new OrganizationRestEntityDelegate(), Organization.ENTITY_NAME);
 * </pre>
 * 
 * @author mschrag
 */
public interface IERXRestEntityDelegate {
	/**
	 * Returns the alias for the given entity name. It is often the case that the actual name of the entity in your
	 * model may not be the name that you want to expose to the outside world. From this method, you can return the
	 * externally visible name. This method should never return null (just return entityName if you don't have an
	 * alias), and you will only get requests for entities that you are registered for.
	 * 
	 * @param entityName
	 *            the name of the entity to lookup
	 * @return the alias for the given entity name
	 */
	public String entityAliasForEntityNamed(String entityName);

	/**
	 * Just like for entity names, it may be necessary to rename certain properties of your entities for consumers of
	 * your restful service. This method should return the alias for the given propertyName for the given entity. This
	 * method should never return null (just return propertyName if you don't have an alias), and you will only get
	 * requests for entities that you are registered for.
	 * 
	 * @param entity
	 *            the entity that contains the property
	 * @param propertyName
	 *            the property name
	 * @return the alias for the given property name
	 */
	public String propertyAliasForPropertyNamed(EOEntity entity, String propertyName);

	/**
	 * The inverse of propertyAliasForPropertyNamed. Entity names are known, but property names aren't. So the inverse
	 * lookup must be provided. You must always provide an inverse lookup that matches the corresponding lookup from
	 * propertyAliasForPropertyNamed, and you must never return null form this method (just return propertyAlias if
	 * there is no alias).
	 * 
	 * @param entity
	 *            the entity that contains the property
	 * @param propertyAlias
	 *            the alias to lookup
	 * @return the original property names for the given alias
	 */
	public String propertyNameForPropertyAlias(EOEntity entity, String propertyAlias);

	/**
	 * Returns the object that has the given key. In all of the provided implementations, key is interpreted to be an
	 * integer primary key in string form.
	 * 
	 * @param entity
	 *            the entity
	 * @param key
	 *            the unique key for an object
	 * @param context
	 *            the rest context
	 * @return the matching object
	 * @throws ERXRestException
	 *             if a general failure occurs
	 * @throws ERXRestNotFoundException
	 *             if there is no object with the given key
	 * @throws ERXRestSecurityException
	 *             if the caller is not permitted to view the requested object
	 */
	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	/**
	 * Returns the object that has the given key from the provided array. This is just like objectWithKey except limited
	 * to an array instead of the entire set of objects for the entity. In all of the provided implementations, key is
	 * interpreted to be an integer primary key in string form.
	 * 
	 * @param entity
	 *            the entity
	 * @param key
	 *            the unique key for an object
	 * @param objs
	 *            the objects to restrict the match to
	 * @param context
	 *            the rest context
	 * @return the matching object
	 * @throws ERXRestException
	 *             if a general failure occurs
	 * @throws ERXRestNotFoundException
	 *             if there is no object in the array with the given key
	 * @throws ERXRestSecurityException
	 *             if the caller is not permitted to view the requested object
	 */
	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, NSArray objs, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Returns the value for the specified property name on the given object. This method does not need to deal with
	 * security issues.
	 * 
	 * @param entity
	 *            the entity of the given object
	 * @param obj
	 *            the object itself
	 * @param propertyName
	 *            the property name to lookup
	 * @param context
	 *            the rest context
	 * @return the value for the given property
	 */
	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

	/**
	 * Sets the value for the specified property name on the given object. Notice that the value is a String. You will
	 * need to parse the String appropriately to coerce it into the property type for the property. This method does not
	 * need to deal with security issues.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param obj
	 *            the object to set a property on
	 * @param propertyName
	 *            the property name to set
	 * @param value
	 *            the new value of the property
	 * @param context
	 *            the rest context
	 * @throws ParseException
	 *             if the property value cannot be parsed
	 * @throws ERXRestException
	 *             if a general failure occurs.
	 */
	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException;

	/**
	 * Deletes the given object. canDeleteObject has already been called by the time this method is called, so the only
	 * security checks that need to to happen are ones that might not have been known when canDeleteObject was called.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the object to delete
	 * @param context
	 *            the rest context
	 * @throws ERXRestException
	 *             if a general failure occurs.
	 * @throws ERXRestSecurityException
	 *             if a security failure occurs
	 */
	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	/**
	 * Insert a new object of the given type into a parent object's keypath from an XML document. canInsertObject has
	 * already been called by this time.
	 * 
	 * @param entity
	 *            the entity of the object to insert
	 * @param insertElement
	 *            the document element that describes the insert
	 * @param parentObject
	 *            the parent object of the insert
	 * @param parentKey
	 *            the key on the parent that represents the relationship to this new object
	 * @param context
	 *            the rest context
	 * @return the newly created object
	 * @throws ERXRestSecurityException
	 *             if a security failure occurs
	 * @throws ERXRestException
	 *             if a general failure occurs
	 * @throws ERXRestNotFoundException
	 *             if a related object cannot be found
	 */
	public EOEnterpriseObject insertObjectFromDocument(EOEntity entity, Element insertElement, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException;

	/**
	 * Updates an array of objects for a to-many relationship from an XML document. canUpdateObject has already been
	 * called on the parent object by this time. This method is responsible for deleting and inserting objects into the
	 * specified relationship.
	 * 
	 * @param parentEntity
	 *            the entity of the parent object
	 * @param parentObject
	 *            the parent object
	 * @param attributeName
	 *            the name of the to-many key on the parent
	 * @param entity
	 *            the entity of the objects in the array
	 * @param currentObjects
	 *            the existing objects in the to-many relationship
	 * @param toManyNodes
	 *            the NodeList containing the XML document nodes that describe the update
	 * @param context
	 *            the rest context
	 * @throws ERXRestSecurityException
	 *             if a security failure occurs
	 * @throws ERXRestException
	 *             if a general failure occurs
	 * @throws ERXRestNotFoundException
	 *             if a related object cannot be found
	 */
	public void updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NodeList toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	/**
	 * Updates an existing object from an XML document. canUpdateObject has already been called by this time.
	 * 
	 * @param entity
	 *            the entity of the object to update
	 * @param eo
	 *            the object to update
	 * @param eoElement
	 *            the document element that describes the update
	 * @param context
	 *            the rest context
	 * @throws ERXRestSecurityException
	 *             if a security failure occurs
	 * @throws ERXRestException
	 *             if a general failure occurs
	 * @throws ERXRestNotFoundException
	 *             if a related object cannot be found
	 */
	public void updateObjectFromDocument(EOEntity entity, EOEnterpriseObject eo, Element eoElement, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException;

	/**
	 * Coerce the given value into a String for use in the restful response. This may move to the RestResponseWriter at
	 * some point, but it's kind of a strange design issue.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param object
	 *            the object
	 * @param attributeName
	 *            the name of the key
	 * @param attributeValue
	 *            the value of the key to format
	 * @return the formatted string for the attributeValue
	 * @throws ParseException
	 *             if a parse error occurs
	 * @throws ERXRestException
	 *             if a general error occurs
	 */
	public String formatAttributeValue(EOEntity entity, Object object, String attributeName, Object attributeValue) throws ParseException, ERXRestException;

	/**
	 * Returns an array of all of the EOs visible to the user for the given entity.
	 * 
	 * @param entity
	 *            the entity to fetch
	 * @param context
	 *            the rest context
	 * @return the array of EOs
	 * @throws ERXRestException
	 *             if there is a general failure
	 * @throws ERXRestSecurityException
	 *             if the user requests objects that he/she is not permitted to see
	 */
	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	/**
	 * Called before enumerating the given array of objects for display. This provides an opportunity to prefetch any of
	 * the properties that will be displayed.
	 * 
	 * @param entity
	 *            the entity of the objects
	 * @param objects
	 *            the objects to be displayed
	 * @throws ERXRestException
	 *             if there is a general failure
	 */
	public void preprocess(EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException;

	/**
	 * Returns whether or not the caller is allowed to insert a new object of the given entity. This variant is called
	 * if the caller tries to insert an object without traversing a keypath -- that is, a top level insert. You can
	 * return false from this without implying that canInsertObject(..., parentObject, parentKey ..) returns false --
	 * the two are mutually exclusive.
	 * 
	 * @param entity
	 *            the entity of the object to insert
	 * @param context
	 *            the rest context
	 * @return whether or not a new object can be inserted
	 */
	public boolean canInsertObject(EOEntity entity, ERXRestContext context);

	/**
	 * Returns whether or not a new object can be inserted into the specified relationship of an existing object.
	 * 
	 * @param parentEntity
	 *            the entity of the parent
	 * @param parentObject
	 *            the parent
	 * @param parentKey
	 *            the name of the relationship on the parent
	 * @param entity
	 *            the entity of the object to insert
	 * @param context
	 *            the rest context
	 * @return whether or not a new object can be inserted
	 */
	public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context);

	/**
	 * Returns whether or not the given property can be set during an insert. This is only called if canInsertObject has
	 * returned true.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the object
	 * @param propertyName
	 *            the property name to check
	 * @param context
	 *            the rest context
	 * @return whether or not the given property can be set during an insert
	 */
	public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context);

	/**
	 * Returns whether or not the given object can be updated. This is called prior to calling canUpdateProperty on any
	 * properties and acts as a first line of defenese to completely cut-off access to an object for update
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the object to check
	 * @param context
	 *            the rest context
	 * @return whether or not the given object can be updated
	 */
	public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	/**
	 * Returns whether or not the given property can be set during an update. This is only called if canUpdateObject has
	 * returned true.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the object
	 * @param propertyName
	 *            the property name to check
	 * @param context
	 *            the rest context
	 * @return whether or not the given property can be set during an update
	 */
	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context);

	/**
	 * Returns whether or not the given object can be deleted. This is called prior to calling canDeleteProperty on any
	 * properties and acts as a first line of defenese to completely cut-off access to an object for delete. Note that
	 * the actual meaning of "delete" can be defined in your delete method, so returning true for this does not
	 * *necessarily* mean the object will be deleted. Instead it just means you will allow access to the delete method.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the object to check
	 * @param context
	 *            the rest context
	 * @return whether or not the given object can be deleted
	 */
	public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	/**
	 * Returns whether or not the given object can be seen. This is called prior to calling canViewProperty on any
	 * properties and acts as a first line of defenese to completely cut-off access to an object.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the object to check
	 * @param context
	 *            the rest context
	 * @return whether or not the given object can be seen
	 */
	public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	/**
	 * Returns whether or not the given property can be seen. This is only called if canViewObject has returned true.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the object
	 * @param propertyName
	 *            the property name to check
	 * @param context
	 *            the rest context
	 * @return whether or not the given property can be seen
	 */
	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

	/**
	 * Given an array, this method filters the array based on the callers permission level. This method should never
	 * return null. To cut off access to the array entirely, return NSArray.EmptyArray. This method is only called after
	 * having verified access to the specified key on the parent object.
	 * 
	 * @param parentEntity
	 *            the entity of the parent
	 * @param parentObject
	 *            the parent object
	 * @param parentKey
	 *            the key in the parent that references this array
	 * @param entity
	 *            the entity of the objects in the array
	 * @param objects
	 *            the actual array of objects to filter
	 * @param context
	 *            the rest context
	 * @return a filtered array
	 * @throws ERXRestException
	 *             if a general failure occurs
	 * @throws ERXRestSecurityException
	 *             if a security violation occurs (note that filtering results should not constitute a security
	 *             violation)
	 * @throws ERXRestNotFoundException
	 *             if an object cannot be found
	 */
	public NSArray visibleObjects(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Returns the destination entity for the given key on the specified entity. If the key is a non-entity, you can
	 * return null.
	 * 
	 * @param entity
	 *            the entity to check
	 * @param key
	 *            the key to return the entity for
	 * @return the destination entity for the given key (or null if there isn't one)
	 */
	public EOEntity nextEntity(EOEntity entity, String key);
}
