package er.rest;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;

public class ERXNoOpRestDelegate extends ERXAbstractRestDelegate {
	public ERXNoOpRestDelegate() {
	}

	public ERXNoOpRestDelegate(EOEditingContext editingContext) {
		super(editingContext);
	}

	@Override
	protected Object _createObjectOfEntity(EOClassDescription entity) {
		Object obj;
		if (entity instanceof BeanInfoClassDescription) {
			obj = ((BeanInfoClassDescription) entity).createInstance();
		}
		else {
			throw new UnsupportedOperationException("Unable to create an instance of the entity '" + entity + "'.");
		}
		return obj;
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		return null;
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return true;
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		return null;
	}
}
