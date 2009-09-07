package er.rest;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXEOControlUtilities;

/**
 * EODelegate is an implementation of the ERXRestRequestNode.Delegate interface that understands EOF.
 * 
 * @author mschrag
 */
public class ERXEORestDelegate implements IERXRestDelegate {
	private EOEditingContext _editingContext;

	public ERXEORestDelegate() {
	}

	public ERXEORestDelegate(EOEditingContext editingContext) {
		_editingContext = editingContext;
	}
	
	public void setEditingContext(EOEditingContext editingContext) {
		_editingContext = editingContext;
	}

	public Object primaryKeyForObject(Object obj) {
		Object pkValue;
		if (obj instanceof EOEnterpriseObject) {
			// Object pkValue = entity.primaryKeyValue(obj);
			EOEnterpriseObject eo = (EOEnterpriseObject) obj;
			NSDictionary pkDict = EOUtilities.primaryKeyForObject(_editingContext, eo);
			if (pkDict.count() == 1) {
				pkValue = pkDict.allValues().lastObject();
			}
			else {
				pkValue = pkDict;
			}
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
		if (entity instanceof EOEntityClassDescription) {
			_editingContext.lock();
			try {
				EOEnterpriseObject eo = entity.createInstanceWithEditingContext(_editingContext, null);
				_editingContext.insertObject(eo);
			      obj = eo;
			}
			finally {
				_editingContext.unlock();
			}
		}
		else {
			IERXRestDelegate delegate = IERXRestDelegate.Factory.delegateForEntityNamed(entity.entityName(), _editingContext);
			if (delegate == null) {
				if (entity instanceof BeanInfoClassDescription) {
					obj = ((BeanInfoClassDescription) entity).createInstance();
				}
				else {
					throw new UnsupportedOperationException("Unable to create an instance of the entity '" + entity + "'.");
				}
			}
			else {
				obj = delegate.createObjectOfEntity(entity);
			}
		}
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
			_editingContext.lock();
			try {
				obj = ERXEOControlUtilities.objectWithPrimaryKeyValue(_editingContext, eoEntity.name(), pkValue, null, false);
			}
			finally {
				_editingContext.unlock();
			}
		}
		else {
			IERXRestDelegate delegate = IERXRestDelegate.Factory.delegateForEntityNamed(entity.entityName(), _editingContext);
			if (delegate != null) {
				obj = delegate.objectOfEntityWithID(entity, id);
			}
			else {
				throw new UnsupportedOperationException("Unable to fetch an object for the entity '" + entity.entityName() + "'.");
			}
		}
		return obj;
	}
}