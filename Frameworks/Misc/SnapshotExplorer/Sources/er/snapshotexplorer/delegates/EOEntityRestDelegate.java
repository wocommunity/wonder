package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;
import er.rest.ERXRestContext;

public class EOEntityRestDelegate extends ERXAbstractRestDelegate {
    public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		throw new UnsupportedOperationException("Unable to create a new EOEntity");
	}
    
    public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		return EOModelGroup.defaultGroup().entityNamed((String) id);
	}
    
    public Object primaryKeyForObject(Object obj, ERXRestContext context) {
		return ((EOEntity) obj).name();
	}
}
