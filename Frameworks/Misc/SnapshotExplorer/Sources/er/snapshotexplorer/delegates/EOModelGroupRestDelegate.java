package er.snapshotexplorer.delegates;

import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;

import er.rest.IERXRestDelegate;

public class EOModelGroupRestDelegate implements IERXRestDelegate {
  public void setEditingContext(EOEditingContext editingContext) {
    // ignore
  }
  
  public Object createObjectOfEntity(EOClassDescription entity) {
    throw new UnsupportedOperationException("Unable to create a new EOModelGroup");
  }

  public Object createObjectOfEntityNamed(String name) {
    throw new UnsupportedOperationException("Unable to create a new EOModelGroup");
  }

  public Object objectOfEntityNamedWithID(String name, Object id) {
    return "default".equals(id) ? EOModelGroup.defaultGroup() : null;
  }

  public Object objectOfEntityWithID(EOClassDescription entity, Object id) {
    return "default".equals(id) ? EOModelGroup.defaultGroup() : null;
  }

  public Object primaryKeyForObject(Object obj) {
    Object primaryKey = null;
    if (obj == EOModelGroup.defaultGroup()) {
      primaryKey = "default";
    }
    return primaryKey;
  }

}
