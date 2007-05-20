package er.rest;

import org.w3c.dom.Document;

import com.webobjects.eoaccess.EOEntity;

/**
 * IERXRestDelegate provides the core hooks into REST processing.
 *  
 * @author mschrag
 */
public interface IERXRestDelegate {
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
	 * Returns the actual name for the entity from its
	 * aliased name.  The mappings for aliases is acquired
	 * when entity delegates are registered by calling
	 * entityAliasForEntityNamed.
	 * 
	 * @param entityAlias the entity alias
	 * @return the actual entity name
	 */
	public String entityNameForAlias(String entityAlias);
	
	/**
	 * Returns the per-entity rest delegate.
	 * 
	 * @param entity the entity
	 * @return the per-entity rest delegate
	 */
	public IERXRestEntityDelegate entityDelegate(EOEntity entity);
	
}
