package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;

import er.rest.ERXAbstractRestDelegate;
import er.rest.ERXRestContext;

public class EOModelRestDelegate extends ERXAbstractRestDelegate {
    public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
        throw new UnsupportedOperationException("Unable to create a new EOModel");
    }

    public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
        return EOModelGroup.defaultGroup().modelNamed((String) id);
    }

    public Object primaryKeyForObject(Object obj, ERXRestContext context) {
        return ((EOModel) obj).name();
    }
}
