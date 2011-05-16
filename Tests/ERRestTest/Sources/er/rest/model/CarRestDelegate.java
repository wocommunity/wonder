package er.rest.model;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXQ;
import er.rest.ERXAbstractRestDelegate;
import er.rest.ERXRestContext;

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
    
    public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
        return new Car();
    }
    
    public Object primaryKeyForObject(Object obj, ERXRestContext context) {
        return ((Car) obj).getName();
    }
    
    public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
        NSArray<Car> cars = ERXQ.filtered(Car.cars(), ERXQ.is("name", id));
        return cars.size() == 0 ? null : cars.objectAtIndex(0);
    }
}
