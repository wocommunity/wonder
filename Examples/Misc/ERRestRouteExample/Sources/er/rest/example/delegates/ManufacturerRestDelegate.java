package er.rest.example.delegates;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXQ;
import er.rest.ERXAbstractRestDelegate;
import er.rest.example.model.Manufacturer;

/**
 * ManufacturerRestDelegate is an example of a rest delegate for retrieving
 * and creating objects of non-EO types. The name of the class is 
 * important (the delegate for the "Manufacturer" entity), though you can manually 
 * register a different class name via IERXRestDelegate.Factory.
 *  
 * @author mschrag
 */
public class ManufacturerRestDelegate extends ERXAbstractRestDelegate {
	public ManufacturerRestDelegate() {
	}

	@Override
	protected Object _createObjectOfEntity(EOClassDescription entity) {
		return new Manufacturer();
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		return ((Manufacturer) obj).getName();
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		NSArray<Manufacturer> manufacturers = ERXQ.filtered(Manufacturer.manufacturers(), ERXQ.is("name", id));
		return manufacturers.size() == 0 ? null : manufacturers.objectAtIndex(0);
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return "Manufacturer".equals(entity.entityName());
	}
}
