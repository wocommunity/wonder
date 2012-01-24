package er.rest.entityDelegates;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

import er.extensions.foundation.ERXProperties;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestException;
import er.rest.ERXRestRequestNode;

/**
 * ERXDefaultRestDelegate is the default implementation of the IERXRestDelegate interface. It provides support for
 * registering custom IERXRestEntityDelegates for specific entities.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXDefaultRestDelegate implements IERXRestDelegate {
	private NSMutableDictionary<String, String> _entityAliases;
	private NSMutableDictionary<String, IERXRestEntityDelegate> _entityDelegates;
	private IERXRestEntityDelegate _defaultDelegate;
	private boolean _guessDelegateNames;

	/**
	 * Constructs an ERXDefaultRestDelegate with an ERXDenyRestEntityDelegate as the default entity delegate.
	 */
	public ERXDefaultRestDelegate() {
		this(new ERXDenyRestEntityDelegate(), true);
	}

	/**
	 * Constructs an ERXDefaultRestDelegate with an ERXDenyRestEntityDelegate as the default entity delegate.
	 * 
	 * @param guessDelegateNames
	 *            if true, delegates names will be guessed "<EntityName>RestEntityDelegate" before falling back to the
	 *            default
	 */
	public ERXDefaultRestDelegate(boolean guessDelegateNames) {
		this(new ERXDenyRestEntityDelegate(), guessDelegateNames);
	}

	/**
	 * Constructs an ERXDefaultRestDelegate with the given default entity delegate and with delegate name guessing
	 * turned on. If no entity delegate is specified for a particular entity name, the default delegate will be returned.
	 * 
	 * @param defaultDelegate
	 *            the default entity delegate to use
	 */
	public ERXDefaultRestDelegate(IERXRestEntityDelegate defaultDelegate) {
		this(defaultDelegate, true);
	}

	/**
	 * Constructs an ERXDefaultRestDelegate with the given default entity delegate. If no entity delegate is specified
	 * for a particular entity name, the default delegate will be returned.
	 * 
	 * @param defaultDelegate
	 *            the default entity delegate to use
	 * @param guessDelegateNames
	 *            if true, delegates names will be guessed "<EntityName>RestEntityDelegate" before falling back to the
	 *            default
	 */
	public ERXDefaultRestDelegate(IERXRestEntityDelegate defaultDelegate, boolean guessDelegateNames) {
		_entityAliases = new NSMutableDictionary<String, String>();
		_entityDelegates = new NSMutableDictionary<String, IERXRestEntityDelegate>();
		_defaultDelegate = defaultDelegate;
		_guessDelegateNames = guessDelegateNames;
	}

	public ERXRestKey process(ERXRestRequest restRequest, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		ERXRestKey restResult;

		ERXRestKey lastKey = restRequest.key();
		EOEntity entity = lastKey.entity();
		IERXRestEntityDelegate entityDelegate = entityDelegate(entity);
		if (lastKey.isKeyGID()) {
			EOEnterpriseObject eo = entityDelegate.processObjectFromDocument(entity, restRequest.rootNode(), context);
			restResult = new ERXRestKey(context, entity, "processed", eo);
		}
		else if (lastKey.isKeyAll()) {
			NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<EOEnterpriseObject>();
			Enumeration childrenNodesEnum = restRequest.rootNode().children().objectEnumerator();
			while (childrenNodesEnum.hasMoreElements()) {
				ERXRestRequestNode node = (ERXRestRequestNode) childrenNodesEnum.nextElement();
				// Re-parse the entity, based on the current node. this allows subclasses to be held in collections, ie
				// <Superclass>
				// <superclass>
				// <subclass>
				EOEntity arrayEntity = ERXRestEntityDelegateUtils.requiredEntityNamed(context, context.delegate().entityNameForAlias(node.name()));
				EOEnterpriseObject eo = entityDelegate.processObjectFromDocument(arrayEntity, node, context);
				if (eo != null) {
					eos.addObject(eo);
				}
			}
			restResult = new ERXRestKey(context, entity, "processed", eos);
		}
		else {
			throw new IllegalArgumentException("Unable to process " + lastKey);
		}

		return restResult;
	}

	public ERXRestKey view(ERXRestRequest restRequest, ERXRestContext restContext) {
		return restRequest.key();
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
		IERXRestEntityDelegate entityDelegate = entityDelegate(entity);
		String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());
		String pluralEntityAlias = ERXLocalizer.currentLocalizer().plurifiedString(entityAlias, 2);

		String nodeName = insertRootNode.name();
		if (entityAlias.equals(nodeName)) {
			EOEnterpriseObject eo = entityDelegate.insertObjectFromDocument(entity, insertRootNode, parentEntity, parentObject, parentKey, context);
			insertResult = new ERXRestKey(context, entity, "inserted", eo);
		}
		else if (pluralEntityAlias.equals(nodeName)) {
			NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<EOEnterpriseObject>();
			Enumeration insertNodesEnum = insertRootNode.children().objectEnumerator();
			while (insertNodesEnum.hasMoreElements()) {
				ERXRestRequestNode insertNode = (ERXRestRequestNode) insertNodesEnum.nextElement();
				EOEnterpriseObject eo = entityDelegate.insertObjectFromDocument(entity, insertNode, parentEntity, parentObject, parentKey, context);
				if (eo != null) {
					eos.addObject(eo);
				}
				else {
					ERXRestRequestHandler.log.warn("Skipping inserted entry.  This should possibly throw a SecurityException?");
				}
			}
			insertResult = new ERXRestKey(context, entity, "inserted", eos);
		}
		else {
			throw new ERXRestException("You attempted to put a " + nodeName + " into a " + entityAlias + ".");
		}
		return insertResult;
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
			ERXRestRequestNode updateNode = updateRequest.rootNode();
			context.delegate().entityDelegate(lastEntity).updateObjectFromDocument(lastEntity, eo, updateNode, context);
		}
		else if (lastValue instanceof NSArray) {
			ERXRestKey nextToLastKey = lastKey.previousKey();
			Object nextToLastValue = nextToLastKey.value();
			if (nextToLastValue instanceof EOEnterpriseObject) {
				EOEntity previousEntity = nextToLastKey.entity();
				EOEnterpriseObject eo = (EOEnterpriseObject) nextToLastValue;

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
				ERXRestRequestNode reformedEORequestNode = new ERXRestRequestNode(entityDelegate(nextToLastEntity).entityAliasForEntityNamed(nextToLastEntity.name()), true);
				ERXRestRequestNode reformedPrimitiveRequestNode = new ERXRestRequestNode(lastKey.keyAlias(), false);
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
		IERXRestEntityDelegate entityDelegate = entityDelegate(entity);
		Object value = lastKey.value();
		if (value instanceof NSArray) {
			NSArray values = (NSArray) value;
			Enumeration valuesEnum = values.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
				entityDelegate.delete(entity, eo, context);
			}
		}
		else {
			entityDelegate.delete(entity, (EOEnterpriseObject) value, context);
		}
	}

	public String entityNameForAlias(String entityAlias) {
		String entityName = _entityAliases.objectForKey(entityAlias);
		if (entityName == null) {
			entityName = entityAlias;
		}
		return entityName;
	}

	@SuppressWarnings("unchecked")
	public IERXRestEntityDelegate entityDelegate(EOEntity entity) {
		IERXRestEntityDelegate entityDelegate = _entityDelegates.objectForKey(entity.name());
		if (entityDelegate == null) {
			String entityDelegateClassName = ERXProperties.stringForKey("ERXRest." + entity.name() + ".delegate");
			Class<IERXRestEntityDelegate> entityDelegateClass = null;
			if (entityDelegateClassName == null) {
				if (_guessDelegateNames) {
					entityDelegateClassName = entity.name() + "RestEntityDelegate";
					entityDelegateClass = _NSUtilities.classWithName(entityDelegateClassName);
				}
			}
			else {
				entityDelegateClass = _NSUtilities.classWithName(entityDelegateClassName);
				if (entityDelegateClass == null) {
					throw new IllegalArgumentException("There is no entity delegate named '" + entityDelegateClassName + "'.");
				}
			}
			if (entityDelegateClass != null) {
				try {
					entityDelegate = entityDelegateClass.newInstance();
				}
				catch (Exception e) {
					throw new RuntimeException("Failed to instantiate the entity delegate '" + entityDelegateClassName + "'.", e);
				}
			}
			else {
				entityDelegate = _defaultDelegate;
			}
			_entityDelegates.setObjectForKey(entityDelegate, entity.name());
		}
		entityDelegate.initializeEntityNamed(entity.name());
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