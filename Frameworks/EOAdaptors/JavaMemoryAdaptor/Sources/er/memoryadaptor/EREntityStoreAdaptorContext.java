package er.memoryadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;

public abstract class EREntityStoreAdaptorContext extends EOAdaptorContext {

  public EREntityStoreAdaptorContext(EOAdaptor adaptor) {
    super(adaptor);
  }

  /**
   * Returns an EREntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @return the datastore for the entity
   */
  public abstract EREntityStore _entityStoreForEntity(EOEntity entity);

  /**
   * Returns an EREntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @param transactional if true, this will return a transactional view of the store 
   * @return the datastore for the entity
   */
  public abstract EREntityStore _entityStoreForEntity(EOEntity entity, boolean transactional);

  @Override
  public NSDictionary _newPrimaryKey(EOEnterpriseObject object, EOEntity entity) {
    EREntityStore store = _entityStoreForEntity(entity, false);
    NSArray pkAttributes = entity.primaryKeyAttributes();
    if (pkAttributes.count() > 1) {
      throw new EOGeneralAdaptorException("Failed to generate primary key because " + entity.name() + " has a composite primary key.");
    }
    EOAttribute pkAttribute = (EOAttribute) pkAttributes.objectAtIndex(0);
    int nextSequence = store.nextSequence();
    Object pkValue;
    String className = pkAttribute.className();
    String valueType = pkAttribute.valueType();
    if ("NSData".equals(className)) {
      pkValue = new NSData(String.valueOf(nextSequence).getBytes());
    }
    else {
      if (valueType == null || "i".equals(valueType)) {
        pkValue = Integer.valueOf(nextSequence);
      }
      else if ("l".equals(valueType)) {
        pkValue = Long.valueOf(nextSequence);
      }
      else if ("f".equals(valueType)) {
        pkValue = Float.valueOf(nextSequence);
      }
      else if ("d".equals(valueType)) {
        pkValue = Double.valueOf(nextSequence);
      }
      else if ("s".equals(valueType)) {
        pkValue = Short.valueOf((short) nextSequence);
      }
      else if ("c".equals(valueType) && "NSString".equals(pkAttribute.valueClassName())) { // hack for bugtracker test cases
        pkValue = String.valueOf(nextSequence);
      }
      else {
        throw new IllegalArgumentException("Unknown value type '" + valueType + "' for '" + object + "' of entity '" + entity.name() + "'.");
      }
    }
    NSDictionary pk = new NSDictionary<String, Object>(pkValue, pkAttribute.name());
    return pk;
  }
}