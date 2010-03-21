package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;

public class EOModelRestDelegate extends ERXAbstractRestDelegate {
	@Override
	protected Object _createObjectOfEntityWithID(EOClassDescription entity, Object id) {
		throw new UnsupportedOperationException("Unable to create a new EOModel");
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
    return EOModelGroup.defaultGroup().modelNamed((String) id);
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return "EOModel".equals(entity.entityName());
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
    return ((EOModel) obj).name();
	}
}
