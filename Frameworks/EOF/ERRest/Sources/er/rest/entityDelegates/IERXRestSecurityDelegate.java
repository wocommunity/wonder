package er.rest.entityDelegates;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Handles allowed actions.
 *
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public interface IERXRestSecurityDelegate {

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
	public abstract boolean canInsertObject(EOEntity entity, ERXRestContext context);

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
	public abstract boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context);

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
	public abstract boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context);

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
	public abstract boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

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
	public abstract boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context);

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
	public abstract boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

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
	public abstract boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	/**
	 * Returns whether or not the given property can be seen. This is only called if canViewObject has returned true.
	 * 
	 * @param entity
	 *            the entity of the object
	 * @param obj
	 *            the object
	 * @param propertyName
	 *            the property name to check
	 * @param context
	 *            the rest context
	 * @return whether or not the given property can be seen
	 */
	public abstract boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

}