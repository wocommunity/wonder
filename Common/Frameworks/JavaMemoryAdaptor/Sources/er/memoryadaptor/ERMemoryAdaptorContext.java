/* FSAdaptorContext - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package er.memoryadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERMemoryAdaptorContext provides the adaptor context implementation for ERMemoryAdaptor.
 * For the most part, you don't need to interact with ERMemoryAdaptor at this level. However,
 * the context does expose resetEntity(EOEntity) and resetAllEntities() methods which allow
 * control over resetting the memory datastore.  This can be helpful when you need to reset
 * the "database" between test cases.
 * 
 * @author mschrag
 */
public class ERMemoryAdaptorContext extends EOAdaptorContext {
  private boolean _hasTransaction = false;
  private NSMutableDictionary<String, ERMemoryEntityStore> _entityStores;
  private NSMutableDictionary<String, ERMemoryEntityStore> _transactionEntityStores;

  public ERMemoryAdaptorContext(EOAdaptor adaptor) {
    super(adaptor);
    _entityStores = new NSMutableDictionary<String, ERMemoryEntityStore>();
  }

  /**
   * Resets all the entities in this context, removing any known rows and
   * clearing out any transactions.
   */
  public void resetAllEntities() {
    _entityStores.removeAllObjects();
    if (_transactionEntityStores != null) {
      _transactionEntityStores.removeAllObjects();
    }
  }

  /**
   * Resets the given entity, removing any known rows and
   * clearing out any transactions.
   * 
   * @param entity the entity to reset
   */
  public void resetEntity(EOEntity entity) {
    _entityStores.removeObjectForKey(entity);
    if (_transactionEntityStores != null) {
      _transactionEntityStores.removeObjectForKey(entity);
    }
  }

  /**
   * Returns the ERMemoryEntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @return the datastore for the entity
   */
  public ERMemoryEntityStore _entityStoreForEntity(EOEntity entity) {
    return _entityStoreForEntity(entity, _hasTransaction);
  }

  /**
   * Returns the ERMemoryEntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @param transactional if true, this will return a transactional view of the store 
   * @return the datastore for the entity
   */
  public ERMemoryEntityStore _entityStoreForEntity(EOEntity entity, boolean transactional) {
    String entityName = entity.name();
    ERMemoryEntityStore store = _entityStores.objectForKey(entityName);
    if (store == null) {
      store = new ERMemoryEntityStore();
      _entityStores.setObjectForKey(store, entityName);
    }
    if (transactional) {
      ERMemoryEntityStore transactionStore = _transactionEntityStores.objectForKey(entityName);
      if (transactionStore == null) {
        transactionStore = store.transactionStore();
        _transactionEntityStores.setObjectForKey(transactionStore, entityName);
      }
      store = transactionStore;
    }
    return store;
  }

  @Override
  public NSDictionary _newPrimaryKey(EOEnterpriseObject object, EOEntity entity) {
    ERMemoryEntityStore store = _entityStoreForEntity(entity, false);
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
      else {
        throw new IllegalArgumentException("Unknown value type '" + valueType + "'.");
      }
    }
    NSDictionary pk = new NSDictionary<String, Object>(pkValue, pkAttribute.name());
    return pk;
  }

  @Override
  public void beginTransaction() {
    if (!_hasTransaction) {
      _hasTransaction = true;
      _transactionEntityStores = new NSMutableDictionary<String, ERMemoryEntityStore>();
      transactionDidBegin();
    }
  }

  @Override
  public void commitTransaction() {
    if (_hasTransaction) {
      _hasTransaction = false;
      for (String entityName : _transactionEntityStores.allKeys()) {
        ERMemoryEntityStore transactionStore = _transactionEntityStores.objectForKey(entityName);
        ERMemoryEntityStore entityStore = _entityStores.objectForKey(entityName);
        entityStore.commitFromTransactionStore(transactionStore);
      }
      _transactionEntityStores = null;
      transactionDidCommit();
    }
  }

  @Override
  public EOAdaptorChannel createAdaptorChannel() {
    return new ERMemoryAdaptorChannel(this);
  }

  @Override
  public void handleDroppedConnection() {
    /* empty */
  }

  @Override
  public void rollbackTransaction() {
    if (_hasTransaction) {
      _hasTransaction = false;
      _transactionEntityStores = null;
      transactionDidRollback();
    }
  }
}
