package er.extensions.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

public interface IERXRestEntityDelegate {
	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException;
	
	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException;
	
	public NSArray displayPropertyNames(EOEntity entity, EOEnterpriseObject obj, ERXRestContext context) throws ERXRestException;

	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	public boolean canInsertObject(EOEntity entity, ERXRestContext context);

	public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context);

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject obj, String propertyName, ERXRestContext context);

	public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject obj, ERXRestContext context);

	public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject obj, ERXRestContext context);

	public boolean canViewObject(EOEntity entity, EOEnterpriseObject obj, ERXRestContext context);

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context);

	public NSArray visibleObjects(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	public boolean isNextKeyVisible(EOEntity entity, Object value, String nextKey, ERXRestContext context);

	public ERXRestResult nextNonModelResult(EOEntity entity, Object value, String nextKey, String nextPath, boolean includeContent, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;
}
