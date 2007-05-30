package er.rest;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXLocalizer;

/**
 * ERXDefaultRestDelegate is the default implementation of the IERXRestDelegate interface. It provides support for
 * registering custom IERXRestEntityDelegates for specific entities.
 * 
 * @author mschrag
 */
public class ERXDefaultRestDelegate implements IERXRestDelegate {
	private NSMutableDictionary _entityAliases;
	private NSMutableDictionary _entityDelegates;
	private IERXRestEntityDelegate _defaultDelegate;

	/**
	 * Constructs an ERXDefaultRestDelegate with an ERXDenyRestEntityDelegate
	 * as the default entity delegate. 
	 */
	public ERXDefaultRestDelegate() {
		this(new ERXDenyRestEntityDelegate());
	}

	/**
	 * Constructs an ERXDefaultRestDelegate with the given default entity delegate.  If no
	 * entity delegate is specified for a particular entity name, the default delegate
	 * will be returned.
	 * 
	 * @param defaultDelegate the default entity delegate to use
	 */
	public ERXDefaultRestDelegate(IERXRestEntityDelegate defaultDelegate) {
		_entityAliases = new NSMutableDictionary();
		_entityDelegates = new NSMutableDictionary();
		_defaultDelegate = defaultDelegate;
	}

	public ERXRestKey insert(ERXRestRequest insertRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		ERXRestKey insertResult;

		ERXRestKey lastKey = insertRequest.key();
		if (lastKey.isKeyAll()) {
			EOEntity lastEntity = lastKey.entity();
			insertResult = insertInto(lastEntity, insertRequest, null, null, null, context);
		}
		else if (lastKey.isKeyGID()) {
			throw new ERXRestException("You can't insert an object with a specific id.");
		}
		else {
			ERXRestKey previousKey = lastKey.previousKey();
			Object nextToLastValue = previousKey.value();
			if (nextToLastValue instanceof EOEnterpriseObject) {
				EOEnterpriseObject nextToLastEO = (EOEnterpriseObject) nextToLastValue;
				EOEntity nextEntity = lastKey.nextEntity();
				insertResult = insertInto(nextEntity, insertRequest, previousKey.entity(), nextToLastEO, lastKey.key(), context);
			}
			else {
				throw new ERXRestException("You attempted to insert something that could not be processed.");
			}
		}

		return insertResult;
	}

	protected ERXRestKey insertInto(EOEntity entity, ERXRestRequest insertRequest, EOEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		ERXRestKey insertResult;
		ERXRestRequestNode insertRootNode = insertRequest.rootNode();
		String entityAlias = entityDelegate(entity).entityAliasForEntityNamed(entity.name());
		String pluralEntityAlis = ERXLocalizer.currentLocalizer().plurifiedString(entityAlias, 2);

		String nodeName = insertRootNode.name();
		if (entityAlias.equals(nodeName)) {
			EOEnterpriseObject eo = insert(entity, insertRootNode, parentEntity, parentObject, parentKey, context);
			insertResult = new ERXRestKey(context, entity, "inserted", eo);
		}
		else if (pluralEntityAlis.equals(nodeName)) {
			NSMutableArray eos = new NSMutableArray();
			Enumeration insertNodesEnum = insertRootNode.children().objectEnumerator();
			while (insertNodesEnum.hasMoreElements()) {
				ERXRestRequestNode insertNode = (ERXRestRequestNode) insertNodesEnum.nextElement();
				EOEnterpriseObject eo = insert(entity, insertNode, parentEntity, parentObject, parentKey, context);
				eos.addObject(eo);
			}
			insertResult = new ERXRestKey(context, entity, "inserted", eos);
		}
		else {
			throw new ERXRestException("You attempted to put a " + nodeName + " into a " + entityAlias + ".");
		}
		return insertResult;
	}

	protected EOEnterpriseObject insert(EOEntity entity, ERXRestRequestNode insertNode, EOEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		boolean canInsert;
		if (parentObject == null) {
			canInsert = entityDelegate(entity).canInsertObject(entity, context);
		}
		else {
			canInsert = entityDelegate(entity).canInsertObject(parentEntity, parentObject, parentKey, entity, context);
		}
		if (!canInsert) {
			throw new ERXRestSecurityException("You are not allowed to insert this object.");
		}

		EOEnterpriseObject eo = entityDelegate(entity).insertObjectFromDocument(entity, insertNode, parentObject, parentKey, context);
		return eo;
	}

	public void update(ERXRestRequest updateRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		ERXRestKey lastKey = updateRequest.key();
		if (lastKey.isKeyAll()) {
			throw new ERXRestSecurityException("You are not allowed to update all " + entityDelegate(lastKey.entity()).entityAliasForEntityNamed(lastKey.entity().name()) + " objects.");
		}

		EOEntity lastEntity = lastKey.entity();
		Object lastValue = lastKey.value();
		if (lastValue instanceof EOEnterpriseObject) {
			EOEnterpriseObject eo = (EOEnterpriseObject) lastValue;

			if (!entityDelegate(lastEntity).canUpdateObject(lastEntity, eo, context)) {
				throw new ERXRestSecurityException("You are not allowed to update this object.");
			}

			ERXRestRequestNode updateNode = updateRequest.rootNode();
			context.delegate().entityDelegate(lastEntity).updateObjectFromDocument(lastEntity, eo, updateNode, context);
		}
		else if (lastValue instanceof NSArray) {
			ERXRestKey nextToLastKey = lastKey.previousKey();
			Object nextToLastValue = nextToLastKey.value();
			if (nextToLastValue instanceof EOEnterpriseObject) {
				EOEntity previousEntity = nextToLastKey.entity();
				EOEnterpriseObject eo = (EOEnterpriseObject) nextToLastValue;
				if (!entityDelegate(previousEntity).canUpdateObject(previousEntity, eo, context)) {
					throw new ERXRestSecurityException("You are not allowed to update this object.");
				}

				NSArray currentObjects = (NSArray) lastValue;
				ERXRestRequestNode arrayNode = updateRequest.rootNode();
				String arrayNodeName = arrayNode.name();
				String pluralToManyEntityName = ERXLocalizer.currentLocalizer().plurifiedString(entityDelegate(lastEntity).entityAliasForEntityNamed(lastEntity.name()), 2);
				if (!pluralToManyEntityName.equals(arrayNodeName)) {
					throw new ERXRestException("You attempted to put " + arrayNodeName + " into " + pluralToManyEntityName + ".");
				}

				NSArray toManyNodes = arrayNode.children();
				context.delegate().entityDelegate(lastEntity).updateArrayFromDocument(previousEntity, eo, nextToLastKey.key(), lastEntity, currentObjects, toManyNodes, context);
			}
			else {
				throw new ERXRestException("You attempted to put an array into something other than a relationship of a single object.");
			}
		}
		else {
			ERXRestKey nextToLastKey = lastKey.previousKey();
			Object nextToLastValue = nextToLastKey.value();
			EOEntity nextToLastEntity = nextToLastKey.entity();
			if (nextToLastValue instanceof EOEnterpriseObject) {
				ERXRestRequestNode reformedEORequestNode = new ERXRestRequestNode(entityDelegate(nextToLastEntity).entityAliasForEntityNamed(nextToLastEntity.name()));
				ERXRestRequestNode reformedPrimitiveRequestNode = new ERXRestRequestNode(lastKey.keyAlias());
				ERXRestRequestNode updateNode = updateRequest.rootNode();
				reformedPrimitiveRequestNode.setValue(updateNode.value());
				reformedEORequestNode.addChild(reformedPrimitiveRequestNode);
				ERXRestRequest primitiveRequest = new ERXRestRequest(nextToLastKey, reformedEORequestNode);
				update(primitiveRequest, context);
			}
			else {
				throw new ERXRestException("You attempted to update a keypath that is not editable.");
			}
		}
	}

	public void delete(ERXRestRequest deleteRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		ERXRestKey lastKey = deleteRequest.key();
		if (lastKey.isKeyAll()) {
			throw new ERXRestException("You are not allowed to delete all the objects for any entity.");
		}

		EOEntity entity = lastKey.entity();
		Object value = lastKey.value();
		if (value instanceof NSArray) {
			NSArray values = (NSArray) value;
			Enumeration valuesEnum = values.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
				_delete(entity, eo, context);
			}
		}
		else {
			_delete(entity, (EOEnterpriseObject) value, context);
		}
	}

	protected void _delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		if (!entityDelegate(entity).canDeleteObject(entity, eo, context)) {
			throw new ERXRestSecurityException("You are not allowed to delete the given obj.");
		}
		else {
			entityDelegate(entity).delete(entity, eo, context);
		}
	}

	public String entityNameForAlias(String entityAlias) {
		String entityName = (String) _entityAliases.objectForKey(entityAlias);
		if (entityName == null) {
			entityName = entityAlias;
		}
		return entityName;
	}

	public IERXRestEntityDelegate entityDelegate(EOEntity entity) {
		IERXRestEntityDelegate entityDelegate = (IERXRestEntityDelegate) _entityDelegates.objectForKey(entity.name());
		if (entityDelegate == null) {
			entityDelegate = _defaultDelegate;
		}
		return entityDelegate;
	}

	/**
	 * Call this method to register an entity-specific delegate for a particular entity name.
	 * 
	 * @param entityDelegate
	 *            the entity delegate
	 * @param entityName
	 *            the entity name to associate the delegate with
	 */
	public void addDelegateForEntityNamed(IERXRestEntityDelegate entityDelegate, String entityName) {
		_entityDelegates.setObjectForKey(entityDelegate, entityName);
		_entityAliases.setObjectForKey(entityName, entityDelegate.entityAliasForEntityNamed(entityName));
	}

	/**
	 * Removes the delegate for the given entity name.
	 * 
	 * @param entityName
	 *            the name of the entity
	 */
	public void removeDelegateForEntityNamed(String entityName) {
		_entityDelegates.removeObjectForKey(entityName);
	}
}