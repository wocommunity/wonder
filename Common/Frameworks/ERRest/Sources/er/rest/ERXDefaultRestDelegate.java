package er.rest;

import java.util.Enumeration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXLocalizer;

public class ERXDefaultRestDelegate implements IERXRestDelegate {
	private NSMutableDictionary _entityDelegates;
	private IERXRestEntityDelegate _defaultDelegate;

	public ERXDefaultRestDelegate() {
		this(new ERXDenyRestEntityDelegate());
	}

	public ERXDefaultRestDelegate(IERXRestEntityDelegate defaultDelegate) {
		_entityDelegates = new NSMutableDictionary();
		_defaultDelegate = defaultDelegate;
	}

	public ERXRestKey insert(ERXRestKey lastKey, Document insertDocument, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		ERXRestKey insertResult;

		EOEntity lastEntity = lastKey.entity();
		if (lastKey.isKeyAll()) {
			insertResult = insertInto(lastEntity, insertDocument, null, null, null, context);
		}
		else if (lastKey.isKeyGID()) {
			throw new ERXRestException("You can't insert an object with a specific id.");
		}
		else {
			ERXRestKey previousKey = lastKey.previousKey();
			Object nextToLastValue = previousKey.value();
			if (nextToLastValue instanceof EOEnterpriseObject) {
				EOEnterpriseObject nextToLastEO = (EOEnterpriseObject) nextToLastValue;
				insertResult = insertInto(lastEntity, insertDocument, previousKey.entity(), nextToLastEO, lastKey.key(), context);
			}
			else {
				throw new ERXRestException("You attempted to insert something that could not be processed.");
			}
		}

		return insertResult;
	}

	protected ERXRestKey insertInto(EOEntity entity, Document insertDocument, EOEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		ERXRestKey insertResult;
		Element insertDocumentElement = insertDocument.getDocumentElement();
		String entityName = entity.name();
		String pluralEntityName = ERXLocalizer.currentLocalizer().plurifiedString(entityName, 2);

		String nodeName = insertDocumentElement.getNodeName();
		if (entityName.equals(nodeName)) {
			EOEnterpriseObject eo = insert(entity, insertDocumentElement, parentEntity, parentObject, parentKey, context);
			insertResult = new ERXRestKey(context, entity, "inserted", eo);
		}
		else if (pluralEntityName.equals(nodeName)) {
			NSMutableArray eos = new NSMutableArray();
			NodeList insertElements = insertDocumentElement.getChildNodes();
			for (int nodeNum = 0; nodeNum < insertElements.getLength(); nodeNum++) {
				Element insertElement = (Element) insertElements.item(nodeNum);
				EOEnterpriseObject eo = insert(entity, insertElement, parentEntity, parentObject, parentKey, context);
				eos.addObject(eo);
			}
			insertResult = new ERXRestKey(context, entity, "inserted", eos);
		}
		else {
			throw new ERXRestException("You attempted to put a " + nodeName + " into a " + entity.name() + ".");
		}
		return insertResult;
	}

	protected EOEnterpriseObject insert(EOEntity entity, Element insertElement, EOEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
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

		EOEnterpriseObject eo = entityDelegate(entity).insertObjectFromDocument(entity, insertElement, parentObject, parentKey, context);
		return eo;
	}

	public void update(ERXRestKey lastKey, Document updateDocument, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		if (lastKey.isKeyAll()) {
			throw new ERXRestSecurityException("You are not allowed to update all " + lastKey.entity().name() + " objects.");
		}

		EOEntity lastEntity = lastKey.entity();
		Object lastValue = lastKey.value();
		if (lastValue instanceof EOEnterpriseObject) {
			EOEnterpriseObject eo = (EOEnterpriseObject) lastValue;

			if (!entityDelegate(lastEntity).canUpdateObject(lastEntity, eo, context)) {
				throw new ERXRestSecurityException("You are not allowed to update this object.");
			}

			Element updateElement = updateDocument.getDocumentElement();
			context.delegate().entityDelegate(lastEntity).updateObjectFromDocument(lastEntity, eo, updateElement, context);
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
				Element arrayElement = updateDocument.getDocumentElement();
				String arrayNodeName = arrayElement.getNodeName();
				String pluralToManyEntityName = ERXLocalizer.currentLocalizer().plurifiedString(lastEntity.name(), 2);
				if (!pluralToManyEntityName.equals(arrayNodeName)) {
					throw new ERXRestException("You attempted to put " + arrayNodeName + " into " + pluralToManyEntityName + ".");
				}

				NodeList toManyNodes = arrayElement.getChildNodes();
				context.delegate().entityDelegate(lastEntity).updateArrayFromDocument(previousEntity, eo, nextToLastKey.key(), lastEntity, currentObjects, toManyNodes, context);
			}
			else {
				throw new ERXRestException("You attempted to put an array into something other than a relationship of a single object.");
			}
		}
		else {
			throw new ERXRestException("You attempted to update something that could not be processed.");
		}
	}

	public void delete(EOEntity entity, Object obj, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		if (obj instanceof NSArray) {
			NSArray values = (NSArray) obj;
			Enumeration valuesEnum = values.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
				delete(entity, eo, context);
			}
		}
		else {
			delete(entity, obj, context);
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

	public void addDelegateForEntityNamed(IERXRestEntityDelegate entityDelegate, String entityName) {
		_entityDelegates.setObjectForKey(entityDelegate, entityName);
	}

	public void removeDelegateForEntityNamed(String entityName) {
		_entityDelegates.removeObjectForKey(entityName);
	}

	public IERXRestEntityDelegate entityDelegate(EOEntity entity) {
		IERXRestEntityDelegate entityDelegate = (IERXRestEntityDelegate) _entityDelegates.objectForKey(entity.name());
		if (entityDelegate == null) {
			entityDelegate = _defaultDelegate;
		}
		return entityDelegate;
	}
}