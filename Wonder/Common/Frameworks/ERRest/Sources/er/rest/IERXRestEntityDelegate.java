package er.rest;

import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

public interface IERXRestEntityDelegate {
	public String entityAliasForEntityNamed(String entityName);

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, NSArray objs, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException;

	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	public EOEnterpriseObject insertObjectFromDocument(EOEntity entity, Element insertElement, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException;

	public void updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NodeList toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	public void updateObjectFromDocument(EOEntity entity, EOEnterpriseObject eo, Element eoElement, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException;

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

	public boolean canInsertObject(EOEntity entity, ERXRestContext context);

	public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context);

	public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context);

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context);

	public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

	public NSArray visibleObjects(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	public EOEntity nextEntity(EOEntity entity, String key);
}
