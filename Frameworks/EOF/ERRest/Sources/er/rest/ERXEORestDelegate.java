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
public class ERXEORestDelegate extends ERXAbstractRestDelegate {
	public ERXEORestDelegate() {
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return entity instanceof EOEntityClassDescription;
	}

	@Override
	protected Object _createObjectOfEntity(EOClassDescription entity) {
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
		EOEnterpriseObject eo = (EOEnterpriseObject) obj;
		EOEditingContext editingContext = editingContext();
		editingContext.lock();
		try {
			NSDictionary pkDict = EOUtilities.primaryKeyForObject(editingContext, eo);
			if (pkDict.count() == 1) {
				pkValue = pkDict.allValues().lastObject();
			}
			else {
				pkValue = pkDict;
			}
		}
		finally {
			editingContext.unlock();
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