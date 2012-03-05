package er.rest.entityDelegates;

import com.webobjects.eoaccess.EOEntity;

import er.rest.ERXRestException;


/**
 * IERXRestDelegate provides the core hooks into REST processing.
 *  
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public interface IERXRestDelegate {

	/**
	 * Views the object defined by the incoming url.
	 * 
	 * @param restRequest the incoming viewing request.
	 * @param restContext the rest context
         *
	 * @return the actual result as an ERXRestResult
         *
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to insert or updates objects that he/she is not permitted to
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public ERXRestKey view(ERXRestRequest restRequest, ERXRestContext restContext);

	/**
	 * Inserts or updates the objects defined by the XML document.
	 * 
	 * @param restRequest the incoming inserts or updates
	 * @param context the rest context
	 * @return the inserted or updates objects as an ERXRestResult
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to insert or updates objects that he/she is not permitted to
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public ERXRestKey process(ERXRestRequest restRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Creates the objects defined by the XML document (which can contain a single object or an array inserts).
	 * 
	 * @param insertRequest the incoming inserts
	 * @param context the rest context
	 * @return the inserted objects as an ERXRestResult
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to insert objects that he/she is not permitted to insert
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public ERXRestKey insert(ERXRestRequest insertRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Updates the objects defined by the XML document (which can contain partial updates or array updates).
	 * 
	 * @param updateRequest the incoming updates 
	 * @param context the rest context
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to update objects that he/she is not permitted to update
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public void update(ERXRestRequest updateRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;

	/**
	 * Deletes the given object (NSArray of EOEnterpriseObject).
	 * 
	 * @param deleteRequest the incoming delete
	 * @param context the rest context
	 * @throws ERXRestException if there is a general failure
	 * @throws ERXRestSecurityException if the user attempts to delete objects that he/she is not permitted to delete
	 * @throws ERXRestNotFoundException if one of the requested objects does not exist
	 */
	public void delete(ERXRestRequest deleteRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException;
	
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
