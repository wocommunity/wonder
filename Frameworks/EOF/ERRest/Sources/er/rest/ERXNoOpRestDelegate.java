package er.rest;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;

public class ERXNoOpRestDelegate extends ERXAbstractRestDelegate {
	public ERXNoOpRestDelegate() {
		setCreateMissingObjects(true);
	}

	public ERXNoOpRestDelegate(EOEditingContext editingContext) {
		super(editingContext);
	}

	@Override
	protected Object _createObjectOfEntityWithID(EOClassDescription entity, Object id) {
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
		// MS: This is kind of hacky, but basically we only want to say this is the delegate for this entity if there isn't another custom registered one
		return IERXRestDelegate.Factory.delegateForEntityNamed(entity.entityName(), null).getClass() == getClass();
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		return null;
	}
}
