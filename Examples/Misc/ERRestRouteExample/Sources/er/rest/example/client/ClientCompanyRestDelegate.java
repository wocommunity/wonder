/**
 * 
 */
package er.rest.example.client;

import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;

public class ClientCompanyRestDelegate extends ERXAbstractRestDelegate {
	@Override
	public boolean shouldCreateMissingObjects() {
		return true;
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		return ((ClientCompany) obj).getId();
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return ClientCompany.class.getSimpleName().equals(entity.entityName());
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		return null;
	}

	@Override
	protected Object _createObjectOfEntityWithID(EOClassDescription entity, Object id) {
		ClientCompany comp = new ClientCompany();
		comp.setId(String.valueOf(id));
		return comp;
	}
}