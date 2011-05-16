package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;
import er.rest.ERXRestContext;

public class EOModelGroupRestDelegate extends ERXAbstractRestDelegate {
	public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		throw new UnsupportedOperationException("Unable to create a new EOModelGroup");
	}
	
	public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		return "default".equals(id) ? EOModelGroup.defaultGroup() : null;
	}
	
	public Object primaryKeyForObject(Object obj, ERXRestContext context) {
		Object primaryKey = null;
		if (obj == EOModelGroup.defaultGroup()) {
			primaryKey = "default";
		}
		return primaryKey;
	}
}
