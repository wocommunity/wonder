package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;

import er.rest.IERXRestDelegate;

public class EOEntityRestDelegate implements IERXRestDelegate {
  public void setEditingContext(EOEditingContext editingContext) {
    // ignore
  }
  
  public Object createObjectOfEntity(EOClassDescription entity) {
    throw new UnsupportedOperationException("Unable to create a new EOEntity");
  }

  public Object createObjectOfEntityNamed(String name) {
    throw new UnsupportedOperationException("Unable to create a new EOEntity");
  }

  public Object objectOfEntityNamedWithID(String name, Object id) {
    return EOModelGroup.defaultGroup().entityNamed((String) id);
  }

  public Object objectOfEntityWithID(EOClassDescription entity, Object id) {
    return EOModelGroup.defaultGroup().entityNamed((String) id);
  }

  public Object primaryKeyForObject(Object obj) {
    return ((EOEntity) obj).name();
  }

}
