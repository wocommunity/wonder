/**
 * 
 */
package er.rest.example.client;

import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;
import er.rest.ERXRestContext;

public class ClientCompanyRestDelegate extends ERXAbstractRestDelegate {
    public Object primaryKeyForObject(Object obj, ERXRestContext context) {
		return ((ClientCompany) obj).getId();
	}
    
    public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		return null;
	}
    
    public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		ClientCompany comp = new ClientCompany();
		comp.setId(String.valueOf(id));
		return comp;
	}
}