package er.extensions.rest;

import java.text.ParseException;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

public interface IERXRestEntityDelegate {
	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException;

	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException;

	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException;

	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException;

	public Object parseAttributeValue(EOEntity entity, Object object, String attributeName, String attributeValue) throws ParseException, ERXRestException;

	public String formatAttributeValue(EOEntity entity, Object object, String attributeName, Object attributeValue) throws ParseException, ERXRestException;

	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	public boolean canInsertObject(EOEntity entity, ERXRestContext context);

	public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context);

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context);

	public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context);

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

	public NSArray visibleObjects(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	public ERXRestResult nextNonModelResult(ERXRestResult currentResult, boolean includeContent, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;
}
