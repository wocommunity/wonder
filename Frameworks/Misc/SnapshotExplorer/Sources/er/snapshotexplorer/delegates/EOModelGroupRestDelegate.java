package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;

public class EOModelGroupRestDelegate extends ERXAbstractRestDelegate {
	@Override
	protected Object _createObjectOfEntityWithID(EOClassDescription entity, Object id) {
		throw new UnsupportedOperationException("Unable to create a new EOModelGroup");
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		return "default".equals(id) ? EOModelGroup.defaultGroup() : null;
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return "EOModelGroup".equals(entity.entityName());
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		Object primaryKey = null;
		if (obj == EOModelGroup.defaultGroup()) {
			primaryKey = "default";
		}
		return primaryKey;
	}
}
