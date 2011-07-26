package er.rest;

import com.webobjects.eocontrol.EOClassDescription;

public class ERXNoOpRestDelegate extends ERXAbstractRestDelegate {
	public ERXNoOpRestDelegate() {
	}
	
	public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		if (entity instanceof IERXNonEOClassDescription) {
			return ((IERXNonEOClassDescription) entity).createInstance();
		}
		throw new UnsupportedOperationException("Unable to create an instance of the entity '" + entity + "'.");
	}
	
	public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		return null;
	}
	
	public Object primaryKeyForObject(Object obj, ERXRestContext context) {
		return null;
	}
}
