package er.rest.model;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Car is just a simple non-EO model class. It has the full set of
 * available instances cached in it, though this could just as
 * easily be exposing a fetch from a non-EO database.
 * 
 * @author mschrag
 */
public class Car {
    private static NSMutableArray<Car> _cars;

    public static NSArray<Car> cars() {
        if (_cars == null) {
            NSMutableArray<Car> cars = new NSMutableArray<>();
            cars.addObject(new Car(Manufacturer.MINI, "Cooper S"));
            cars.addObject(new Car(Manufacturer.PORSCHE, "911"));
            cars.addObject(new Car(Manufacturer.PORSCHE, "Cayenne"));
            cars.addObject(new Car(Manufacturer.TOYOTA, "Celica"));
            _cars = cars;
        }
        return _cars;
    }

    private String _name;
    private Manufacturer _manufacturer;

    public Car() {
    }

    public Car(Manufacturer _manufacturer, String name) {
        setManufacturer(_manufacturer);
        setName(name);
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        if (_manufacturer != null) {
            _manufacturer.removeCar(this);
        }
        _manufacturer = manufacturer;
        if (_manufacturer != null) {
            _manufacturer.addCar(this);
        }
    }

    public Manufacturer getManufacturer() {
        return _manufacturer;
    }
}
