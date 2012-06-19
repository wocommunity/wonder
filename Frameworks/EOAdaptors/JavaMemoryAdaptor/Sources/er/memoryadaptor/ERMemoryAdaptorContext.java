/* FSAdaptorContext - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package er.memoryadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOEntity;

/**
 * ERMemoryAdaptorContext provides the adaptor context implementation for ERMemoryAdaptor.
 * For the most part, you don't need to interact with ERMemoryAdaptor at this level. However,
 * the context does expose resetEntity(EOEntity) and resetAllEntities() methods which allow
 * control over resetting the memory datastore.  This can be helpful when you need to reset
 * the "database" between test cases.
 * 
 * @author mschrag
 */
public class ERMemoryAdaptorContext extends EREntityStoreAdaptorContext {
  private static final EREntityStoreFactory _storeFactory = new EREntityStoreFactory(ERMemoryEntityStore.class);

  public ERMemoryAdaptorContext(EOAdaptor adaptor) {
    super(adaptor);
  }

  /**
   * Resets all the entities in this context, removing any known rows and
   * clearing out any transactions.
   */
  public void resetAllEntities() {
    _storeFactory.resetAllEntities();
  }

  /**
   * Resets the given entity, removing any known rows and
   * clearing out any transactions.
   * 
   * @param entity the entity to reset
   */
  public void resetEntity(EOEntity entity) {
    _storeFactory.resetEntity(entity);
  }

  /**
   * Returns an EREntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @return the datastore for the entity
   */
  public EREntityStore _entityStoreForEntity(EOEntity entity) {
    return _storeFactory._entityStoreForEntity(entity);
  }

  /**
   * Returns an EREntityStore for the given entity.
   * 
   * @param entity the entity to lookup
   * @param transactional if true, this will return a transactional view of the store 
   * @return the datastore for the entity
   */
  public EREntityStore _entityStoreForEntity(EOEntity entity, boolean transactional) {
    return _storeFactory._entityStoreForEntity(entity, transactional);
  }

  @Override
  public void beginTransaction() {
    _storeFactory.beginTransaction();
    transactionDidBegin();
  }

  @Override
  public void commitTransaction() {
    _storeFactory.commitTransaction();
    transactionDidCommit();
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
    _storeFactory.rollbackTransaction();
    transactionDidRollback();
  }
}
