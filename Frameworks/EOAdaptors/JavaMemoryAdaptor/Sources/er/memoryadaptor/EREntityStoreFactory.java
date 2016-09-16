package er.memoryadaptor;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation._NSUtilities;

import er.memoryadaptor.EREntityStore.JoinEntityStore;

/**
 * EREntityStoreFactory is an factory class used for creating and managing EREntityStore
 * instances.
 * 
 * @author q
 */
public class EREntityStoreFactory {
  private boolean _hasTransaction = false;
  private NSMutableDictionary<String, EREntityStore> _entityStores;
  private NSMutableDictionary<String, EREntityStore> _transactionEntityStores;
  private final Class<? extends EREntityStore> _entityStoreClazz;
  public static final String RESET_ALL_ENTITIES = "EREntityStoreResetAllEntities";
  
  public EREntityStoreFactory(Class<? extends EREntityStore> clazz) {
    _entityStores = new NSMutableDictionary<>();
    _entityStoreClazz = clazz;
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("resetAllEntities", new Class[] { NSNotification.class }), RESET_ALL_ENTITIES, null);
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
  
  /* This is to satisfy NSNotificationCenter */
  public void resetAllEntities(NSNotification object) {
    resetAllEntities();
  }
  
  /**
   * Resets the given entity, removing any known rows and
   * clearing out any transactions. This is the equivalent
   * deleting all rows in a relational database table. Be
   * careful using this if you use single table inheritance.
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
   * Returns an EREntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @return the datastore for the entity
   */
  public EREntityStore _entityStoreForEntity(EOEntity entity) {
    return _entityStoreForEntity(entity, _hasTransaction);
  }
  
  /**
   * Returns an EREntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @param transactional if true, this will return a transactional view of the store 
   * @return the datastore for the entity
   */
  public EREntityStore _entityStoreForEntity(EOEntity entity, boolean transactional) {
    NSSet<EOEntity> relatedEntities = _relatedEntities(entity);
    EREntityStore store = _entityStoreForExternalName(_entityExternalName(entity), transactional);
    if (relatedEntities.count() > 0 && !(store instanceof JoinEntityStore)) {
      NSMutableDictionary<EOEntity, EREntityStore> stores = new NSMutableDictionary<>(new EREntityStore[] { store }, new EOEntity[] { entity });
      for (EOEntity related : relatedEntities) {
        store = _entityStoreForExternalName(_entityExternalName(related), transactional);
        stores.setObjectForKey(store, related);
      }
      store = new ERJoinEntityStore(stores, entity);
    } 
    return store;
  }

  private String _entityExternalName(EOEntity entity) {
    String externalName = entity.externalName();
    if (externalName == null || externalName.trim().length() == 0){ 
      externalName = entity.name();
    }
    return externalName;
  }

  private EREntityStore _entityStoreForExternalName(String name, boolean transactional) {
    EREntityStore store = _entityStores.objectForKey(name);
    if (store == null) {
      store = (EREntityStore) _NSUtilities.instantiateObject(_entityStoreClazz, null, null, false, false);
      _entityStores.setObjectForKey(store, name);
    }
    if (transactional) {
      EREntityStore transactionStore = _transactionEntityStores.objectForKey(name);
      if (transactionStore == null) {
        transactionStore = store.transactionStore();
        _transactionEntityStores.setObjectForKey(transactionStore, name);
      }
      store = transactionStore;
    }
    return store;
  }
  
  public void beginTransaction() {
    if (!_hasTransaction) {
      _hasTransaction = true;
      _transactionEntityStores = new NSMutableDictionary<>();
    }
  }

  public void commitTransaction() {
    if (_hasTransaction) {
      _hasTransaction = false;
      for (String entityName : _transactionEntityStores.allKeys()) {
        EREntityStore transactionStore = _transactionEntityStores.objectForKey(entityName);
        EREntityStore entityStore = _entityStores.objectForKey(entityName);
        entityStore.commitFromTransactionStore(transactionStore);
      }
      _transactionEntityStores = null;
    }
  }
  
  public void rollbackTransaction() {
    if (_hasTransaction) {
      _hasTransaction = false;
      _transactionEntityStores = null;
    }
  }
  
  private NSSet<EOEntity> _relatedEntities(EOEntity entity) {
    NSMutableSet<EOEntity> entities = new NSMutableSet<>();
    for (EOAttribute attrib : entity.attributesToFetch()) {
      if (attrib.isDerived()) {
        attrib = entity._attributeForPath(attrib.definition());
        if (attrib != null)
          entities.add(attrib.entity());
      }
    }
//    for (EORelationship rel : (NSArray<EORelationship>) entity._hiddenRelationships()) {
//      entities.add(rel.destinationEntity());
//    }
    return entities;
  }
}
