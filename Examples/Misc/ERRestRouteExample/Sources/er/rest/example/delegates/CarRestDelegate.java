package er.rest.example.delegates;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXQ;
import er.rest.ERXAbstractRestDelegate;
import er.rest.example.model.Car;

/**
 * CarsRestDelegate is an example of a rest delegate for retrieving
 * and creating objects of non-EO types. The name of the class is 
 * important (the delegate for the "Car" entity), though you can manually 
 * register a different class name via IERXRestDelegate.Factory.
 *  
 * @author mschrag
 */
public class CarRestDelegate extends ERXAbstractRestDelegate {
	public CarRestDelegate() {
	}

	@Override
	protected Object _createObjectOfEntity(EOClassDescription entity) {
		return new Car();
	}

	@Override
	protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
		return ((Car) obj).getName();
	}

	@Override
	protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
		NSArray<Car> cars = ERXQ.filtered(Car.cars(), ERXQ.is("name", id));
		return cars.size() == 0 ? null : cars.objectAtIndex(0);
	}

	@Override
	protected boolean _isDelegateForEntity(EOClassDescription entity) {
		return "Car".equals(entity.entityName());
	}
}
