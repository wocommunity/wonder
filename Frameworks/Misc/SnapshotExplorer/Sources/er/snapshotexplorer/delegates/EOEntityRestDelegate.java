package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;

public class EOEntityRestDelegate extends ERXAbstractRestDelegate {
	@Override
	protected Object _createObjectOfEntityWithID(EOClassDescription entity, Object id) {
		throw new UnsupportedOperationException("Unable to create a new EOEntity");
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		return EOModelGroup.defaultGroup().entityNamed((String) id);
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return "EOEntity".equals(entity.entityName()) || "ERXEntity".equals(entity.entityName());
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		return ((EOEntity) obj).name();
	}
}
