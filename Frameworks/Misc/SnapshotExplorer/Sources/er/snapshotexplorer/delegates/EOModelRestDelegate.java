package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;

import er.rest.IERXRestDelegate;

public class EOModelRestDelegate implements IERXRestDelegate {
  public void setEditingContext(EOEditingContext editingContext) {
    // ignore
  }
  
  public Object createObjectOfEntity(EOClassDescription entity) {
    throw new UnsupportedOperationException("Unable to create a new EOModel");
  }

  public Object createObjectOfEntityNamed(String name) {
    throw new UnsupportedOperationException("Unable to create a new EOModel");
  }

  public Object objectOfEntityNamedWithID(String name, Object id) {
    return EOModelGroup.defaultGroup().modelNamed((String) id);
  }

  public Object objectOfEntityWithID(EOClassDescription entity, Object id) {
    return EOModelGroup.defaultGroup().modelNamed((String) id);
  }

  public Object primaryKeyForObject(Object obj) {
    return ((EOModel) obj).name();
  }

}
