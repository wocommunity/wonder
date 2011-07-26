package er.rest;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXEOControlUtilities;

/**
 * EODelegate is an implementation of the ERXRestRequestNode.Delegate interface that understands EOF.
 * 
 * @author mschrag
 */
public class ERXEORestDelegate extends ERXAbstractRestDelegate {
	public ERXEORestDelegate() {
	}
	
	@Override
	public boolean __hasNumericPrimaryKeys(EOClassDescription classDescription) {
		boolean numericPKs = false;
		if (classDescription instanceof EOEntityClassDescription) {
			EOEntity entity = ((EOEntityClassDescription)classDescription).entity();
			NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
			if (primaryKeyAttributes.count() == 1) {
				EOAttribute primaryKeyAttribute = (EOAttribute) primaryKeyAttributes.objectAtIndex(0);
				Class primaryKeyClass = _NSUtilities.classWithName(primaryKeyAttribute.className());
				numericPKs = primaryKeyClass != null && Number.class.isAssignableFrom(primaryKeyClass);
			}
		}
		return numericPKs;
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return entity instanceof EOEntityClassDescription;
	}

	@Override
	protected Object _createObjectOfEntityWithID(EOClassDescription entity, Object id) {
		EOEditingContext editingContext = editingContext();
		editingContext.lock();
		try {
			EOEnterpriseObject eo = entity.createInstanceWithEditingContext(editingContext, null);
			editingContext.insertObject(eo);
			return eo;
		}
		finally {
			editingContext.unlock();
		}
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		Object pkValue;
		if (obj == null) {
			pkValue = null;
		}
		else {
			EOEnterpriseObject eo = (EOEnterpriseObject) obj;
			EOEditingContext editingContext = eo.editingContext();
			editingContext.lock();
			try {
				NSDictionary pkDict = EOUtilities.primaryKeyForObject(editingContext, eo);
				if (pkDict != null && pkDict.count() == 1) {
					pkValue = pkDict.allValues().lastObject();
				}
				else {
					pkValue = pkDict;
				}
			}
			finally {
				editingContext.unlock();
			}
		}
		return pkValue;
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		EOEntity eoEntity = ((EOEntityClassDescription) entity).entity();
		String strPKValue = String.valueOf(id);
		Object pkValue = ((EOAttribute) eoEntity.primaryKeyAttributes().objectAtIndex(0)).validateValue(strPKValue);
		EOEditingContext editingContext = editingContext();
		editingContext.lock();
		Object obj;
		try {
			return ERXEOControlUtilities.objectWithPrimaryKeyValue(editingContext, eoEntity.name(), pkValue, null, false);
		}
		finally {
			editingContext.unlock();
		}
	}
}