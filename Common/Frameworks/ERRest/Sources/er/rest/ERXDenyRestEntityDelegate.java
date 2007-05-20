package er.rest;

import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

/**
 * ERXDenyRestEntityDelegate is the safe default entity delegate (and is used by default
 * on ERXRestDelegate).  This delegate denies all requests (view, insert, update, etc) and 
 * throws security exceptions at any attempt to perform an action.
 * 
 * @author mschrag
 */
public class ERXDenyRestEntityDelegate implements IERXRestEntityDelegate {
	public String entityAliasForEntityNamed(String entityName) {
		return entityName;
	}

	public String propertyNameForPropertyAlias(EOEntity entity, String propertyAlias) {
		return propertyAlias;
	}

	public String propertyAliasForPropertyNamed(EOEntity entity, String propertyName) {
		return propertyName;
	}

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to access the " + entity.name() + " with the id '" + key + "'.");
	}

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, NSArray objs, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		throw new ERXRestSecurityException("You are not allowed to access the " + entity.name() + " with the id '" + key + "'.");
	}

	public EOEnterpriseObject insertObjectFromDocument(EOEntity entity, Element insertElement, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		return null;
	}

	public void updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NodeList toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
	}

	public void updateObjectFromDocument(EOEntity entity, EOEnterpriseObject eo, Element eoElement, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
	}

	public String formatAttributeValue(EOEntity entity, Object object, String attributeName, Object attributeValue) throws ParseException, ERXRestException {
		return null;
	}

	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException {
		// DO NOTHING
	}

	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return null;
	}

	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) {
		return NSArray.EmptyArray;
	}

	public boolean canInsertObject(EOEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray visibleObjects(EOEntity parentEntity, Object parent, String key, EOEntity entity, NSArray objects, ERXRestContext context) {
		return NSArray.EmptyArray;
	}

	public void preprocess(EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public EOEntity nextEntity(EOEntity entity, String key) {
		return null;
	}
}