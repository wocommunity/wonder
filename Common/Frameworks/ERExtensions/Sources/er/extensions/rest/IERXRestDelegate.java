package er.extensions.rest;

import org.w3c.dom.Document;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

/**
 * IERXRestDelegate provides the core hooks into REST processing.
 *  
 * @author mschrag
 */
public interface IERXRestDelegate {
	/**
	 * Returns the EO that has the given key.
	 * 
	 * @param entity the entity of the object
	 * @param key the key of the object
	 * @param context the rest context
	 * @return the matching object
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestNotFoundException if the user requests an object that does not exist
	 * @throws ERXRestSecurityException if the user requests an object that he/she is not permitted to see
	 */
	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	/**
	 * Returns the EO that has the given key from the given array.
	 * 
	 * @param entity the entity of the object
	 * @param key the key of the object
	 * @param objs the array of objects to search
	 * @param context the rest context
	 * @return the matching object
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestNotFoundException if the user requests an object that does not exist
	 * @throws ERXRestSecurityException if the user requests an object that he/she is not permitted to see
	 */
	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, NSArray objs, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Returns an array of all of the EOs visible to the user for the given entity.
	 * 
	 * @param entity the entity to fetch
	 * @param context the rest context
	 * @return the array of EOs
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user requests objects that he/she is not permitted to see
	 */
	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	/**
	 * Creates the objects defined by the XML document (which can contain a single object or an array inserts).
	 * 
	 * @param nextToLastResult the next-to-the-last keypath (in case you don't want it to fetch the last results)
	 * @param insertDocument the XML document containing inserts
	 * @param context the rest context
	 * @return the inserted objects as an ERXRestResult
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to insert objects that he/she is not permitted to insert
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public ERXRestKey insert(ERXRestKey lastKey, Document insertDocument, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Updates the objects defined by the XML document (which can contain partial updates or array updates).
	 * 
	 * @param nextToLastResult the next-to-the-last keypath (in case you don't want it to fetch the last results)
	 * @param updateDocument the XML document containing updates 
	 * @param context the rest context
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to update objects that he/she is not permitted to update
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public void update(ERXRestKey lastKey, Document updateDocument, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Deletes the given object (NSArray of EOEnterpriseObject).
	 * 
	 * @param entity the entity of the object or objects
	 * @param obj the object to delete
	 * @param context the rest context
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to delete objects that he/she is not permitted to delete
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public void delete(EOEntity entity, Object obj, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Called before enumerating the given array of objects for display.  This provides an
	 * opportunity to prefetch any of the properties that will be displayed.
	 * 
	 * @param entity the entity of the objects
	 * @param objects the objects to be displayed
	 * @throws ERXRestException if there is a general failure
	 */
	public void preprocess(EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException;
	
	/**
	 * Returns the per-entity rest delegate.
	 * 
	 * @param entity the entity
	 * @return the per-entity rest delegate
	 */
	public IERXRestEntityDelegate entityDelegate(EOEntity entity);
	
}
