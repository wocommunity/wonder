package er.rest.entityDelegates;

import java.text.ParseException;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.rest.ERXRestException;
import er.rest.ERXRestRequestNode;
import er.rest.routes.model.IERXEntity;

/**
 * ERXDenyRestEntityDelegate is the safe default entity delegate (and is used by default
 * on ERXRestDelegate).  This delegate denies all requests (view, insert, update, etc) and 
 * throws security exceptions at any attempt to perform an action.
 * 
 * @author mschrag
 */
public class ERXDenyRestEntityDelegate implements IERXRestEntityDelegate {
	public void initializeEntityNamed(String entityName) {
		// DO NOTHING
	}
	
	public String entityAliasForEntityNamed(String entityName) {
		return entityName;
	}

	public String propertyNameForPropertyAlias(IERXEntity entity, String propertyAlias) {
		return propertyAlias;
	}

	public String propertyAliasForPropertyNamed(IERXEntity entity, String propertyName) {
		return propertyName;
	}
	
	public Object idForEO(IERXEntity entity, EOEnterpriseObject eo) {
		return null;
	}
	
	public boolean isEOID(ERXRestKey restKey) {
		return false;
	}
	
	public String stringIDForEO(IERXEntity entity, EOEnterpriseObject eo) {
		return null;
	}
	
	public EOEnterpriseObject processObjectFromDocument(IERXEntity entity, ERXRestRequestNode eoNode, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		return null;
	}

	public EOEnterpriseObject objectWithKey(IERXEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to access the " + entity.name() + " with the id '" + key + "'.");
	}
	
	public EOEnterpriseObject objectForNode(IERXEntity entity, ERXRestRequestNode node, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to access the requested " + entity.name() + ".");
	}
	
	public EOEnterpriseObject objectFromNode(IERXEntity entity, ERXRestRequestNode node, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to access the specified " + entity.name() + ".");
	}

	public EOEnterpriseObject objectWithKey(IERXEntity entity, String key, NSArray objs, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		throw new ERXRestSecurityException("You are not allowed to access the " + entity.name() + " with the id '" + key + "'.");
	}

	public EOEnterpriseObject insertObjectFromDocument(IERXEntity entity, ERXRestRequestNode insertNode, IERXEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		return null;
	}

	public void updateArrayFromDocument(IERXEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, IERXEntity entity, NSArray currentObjects, NSArray toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
	}

	public void updateObjectFromDocument(IERXEntity entity, EOEnterpriseObject eo, ERXRestRequestNode eoNode, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
	}

	public String formatAttributeValue(IERXEntity entity, Object object, String attributeName, Object attributeValue) throws ParseException, ERXRestException {
		return null;
	}

	public void takeValueForKey(IERXEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException {
		// DO NOTHING
	}

	public Object valueForKey(IERXEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return null;
	}

	public void delete(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void updated(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void inserted(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public boolean canInsertProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public boolean canUpdateProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray objectsForEntity(IERXEntity entity, ERXRestContext context) {
		return NSArray.EmptyArray;
	}

	public boolean canInsertObject(IERXEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canInsertObject(IERXEntity parentEntity, Object parentObject, String parentKey, IERXEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canDeleteObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canUpdateObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canViewObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canViewProperty(IERXEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray visibleObjects(IERXEntity parentEntity, Object parent, String key, IERXEntity entity, NSArray objects, ERXRestContext context) {
		return NSArray.EmptyArray;
	}

	public void preprocess(IERXEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public IERXEntity nextEntity(IERXEntity entity, String key) {
		return null;
	}

	public boolean displayDetails(ERXRestKey key, ERXRestContext context) {
		return false;
	}

	public String[] displayProperties(ERXRestKey key, boolean allProperties, boolean allToMany, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		return new String[0];
	}
}