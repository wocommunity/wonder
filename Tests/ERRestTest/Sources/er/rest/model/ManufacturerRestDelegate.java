package er.rest.model;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXQ;
import er.rest.ERXAbstractRestDelegate;
import er.rest.ERXRestContext;

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
    
    public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
        return new Manufacturer();
    }
    
    public Object primaryKeyForObject(Object obj, ERXRestContext context) {
        return ((Manufacturer) obj).getName();
    }
    
    public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
        NSArray<Manufacturer> manufacturers = ERXQ.filtered(Manufacturer.manufacturers(), ERXQ.is("name", id));
        return manufacturers.size() == 0 ? null : manufacturers.objectAtIndex(0);
    }
}
