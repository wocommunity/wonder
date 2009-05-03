package er.rest;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXEntityClassDescription;

/**
 * EODelegate is an implementation of the ERXRestRequestNode.Delegate interface that understands EOF.
 * 
 * @author mschrag
 */
public class ERXEORestDelegate implements IERXRestDelegate {
	private EOEditingContext _editingContext;

	public ERXEORestDelegate(EOEditingContext editingContext) {
		_editingContext = editingContext;
	}

	public Object primaryKeyForObject(Object obj) {
		Object pkValue;
		if (obj instanceof EOEnterpriseObject) {
			// Object pkValue = entity.primaryKeyValue(obj);
			EOEnterpriseObject eo = (EOEnterpriseObject) obj;
			pkValue = ERXEOControlUtilities.primaryKeyObjectForObject(eo);
		}
		else {
			pkValue = null;
		}
		return pkValue;
	}

	public Object createObjectOfEntityNamed(String name) {
		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(name);
		return createObjectOfEntity(classDescription);
	}

	public Object createObjectOfEntity(EOClassDescription entity) {
		Object obj;
		if (entity instanceof ERXEntityClassDescription) {
			EOEnterpriseObject eo = entity.createInstanceWithEditingContext(_editingContext, null);
			_editingContext.insertObject(eo);
			obj = eo;
		}
		else if (entity instanceof BeanInfoClassDescription) {
			obj = ((BeanInfoClassDescription) entity).createInstance();
		}
		else {
			throw new UnsupportedOperationException("Unable to create an instance of the entity '" + entity + "'.");
		}
		// Object obj = entity.createInstance(_editingContext);
		return obj;
	}

	public Object objectOfEntityNamedWithID(String entityName, Object id) {
		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName);
		// IERXEntity entity = IERXEntity.Factory.entityNamed(_editingContext, entityName);
		return objectOfEntityWithID(classDescription, id);
	}

	public Object objectOfEntityWithID(EOClassDescription entity, Object id) {
		Object obj;
		if (id == null) {
			obj = createObjectOfEntity(entity);
		}
		else {
			obj = fetchObjectOfEntityWithID(entity, id);
		}

		return obj;
	}

	protected Object fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		Object obj;
		if (entity instanceof EOEntityClassDescription) {
			EOEntity eoEntity = ((EOEntityClassDescription) entity).entity();
			String strPKValue = String.valueOf(id);
			Object pkValue = ((EOAttribute) eoEntity.primaryKeyAttributes().objectAtIndex(0)).validateValue(strPKValue);
			obj = ERXEOControlUtilities.objectWithPrimaryKeyValue(_editingContext, eoEntity.name(), pkValue, null, false);
		}
		else {
			throw new UnsupportedOperationException("Unable to fetch objects for anything except EOs.");
		}
		return obj;
	}
}