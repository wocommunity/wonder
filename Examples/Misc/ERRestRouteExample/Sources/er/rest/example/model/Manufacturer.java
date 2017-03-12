package er.rest.example.model;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Manufacturer is just a simple non-EO model class. It has the full set of
 * available instances cached in it, though this could just as
 * easily be exposing a fetch from a non-EO database.
 * 
 * @author mschrag
 */
public class Manufacturer {
	public static Manufacturer MINI = new Manufacturer("Mini");
	public static Manufacturer PORSCHE = new Manufacturer("Porsche");
	public static Manufacturer TOYOTA = new Manufacturer("Toyota");

	private static NSMutableArray<Manufacturer> _manufacturers;

	public static NSArray<Manufacturer> manufacturers() {
		if (_manufacturers == null) {
			NSMutableArray<Manufacturer> manufacturers = new NSMutableArray<>();
			manufacturers.addObject(Manufacturer.MINI);
			manufacturers.addObject(Manufacturer.PORSCHE);
			manufacturers.addObject(Manufacturer.TOYOTA);
			_manufacturers = manufacturers;
		}
		return _manufacturers;
	}

	private String _name;

	public Manufacturer() {
	}

	public Manufacturer(String name) {
		setName(name);
	}

	public void setName(String name) {
		_name = name;
	}

	public String getName() {
		return _name;
	}
}
