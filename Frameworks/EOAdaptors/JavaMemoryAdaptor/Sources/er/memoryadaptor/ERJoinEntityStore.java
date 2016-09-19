package er.memoryadaptor;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.memoryadaptor.EREntityStore.JoinEntityStore;

/**
 * ERJoinEntityStore is a datastore implementation that provides a basic EREntityStore join implementation.
 * This class is used by EREntityStoreFactory when resolving an EREntityStore for an entity that has flattened
 * attributes but doesn't implement {@link EREntityStore.JoinEntityStore} 
 * 
 * @author q
 */
public class ERJoinEntityStore extends EREntityStore implements JoinEntityStore {
  private NSDictionary<EOEntity, EREntityStore> _stores;
  private EOEntity _entity;

  public ERJoinEntityStore(NSDictionary<EOEntity, EREntityStore> stores, EOEntity entity) {
    _stores = stores;
    _entity = entity;
  }

  @Override
  public NSMutableArray<NSMutableDictionary<String, Object>> fetch(NSArray<EOAttribute> attributesToFetch, EOFetchSpecification fetchSpecification,
      boolean shouldLock, EOEntity entity, ERMemoryAdaptorContext context) {
    EREntityStore store = joinedStore(attributesToFetch, fetchSpecification, entity);
    return store.fetch(attributesToFetch, fetchSpecification, shouldLock, entity, context);
  }

  private EREntityStore joinedStore(NSArray<EOAttribute> attributesToFetch, EOFetchSpecification fetchSpecification, EOEntity entity) {
    EREntityStore store = _stores.objectForKey(entity);
    for (EOAttribute attrib : attributesToFetch) {
      if (attrib.isFlattened()) {
        EOAttribute _attrib = entity._attributeForPath(attrib.definition());
        EORelationship _rel = entity._relationshipForPath(attrib.relationshipPath());
        store = join(_rel, store, _stores.objectForKey(_attrib.entity()));
      }
    }
    // We need to check the qualifier for _hiddenRelationships and include them if referenced
//    for (EORelationship rel : (NSArray<EORelationship>) entity._hiddenRelationships()) {
//      if (rel.isFlattened()) {
//        for (EORelationship compRel : (NSArray<EORelationship>) rel.componentRelationships()) {
//          store = join(compRel, store, _stores.objectForKey(compRel.destinationEntity()));
//        }
//      } else {
//        store = join(rel, store, _stores.objectForKey(rel.destinationEntity()));
//      }
//    }
    return store;
  }

  private EREntityStore join(EORelationship rel, EREntityStore store1, EREntityStore store2) {
    /* FIXME: Need to support outer joins too */
    if (rel.joinSemantic() == EORelationship.InnerJoin)
      return new InnerJoinEntityStore(rel, store1, store2);
    throw new UnsupportedOperationException("ERMemoryAdaptor does not support outer joins");
  }

  @Override
  public Iterator<NSMutableDictionary<String, Object>> iterator() {
    return joinedStore(_entity.attributesToFetch(), null, _entity).iterator();
  }

  @Override
  public void insertRow(NSDictionary<String, Object> row, EOEntity entity) {
    NSMutableDictionary<String, Object> newRow = new NSMutableDictionary<>();
    EOEntity target = entity;
    /* XXX: This assumes that EOF isn't going to try to insert into two different tables at once */
    for (Entry<String, Object> entry : row.entrySet()) {
      EOAttribute attrib = entity.anyAttributeNamed(entry.getKey());
      if (attrib.isFlattened()) {
        EOAttribute _attrib = entity._attributeForPath(attrib.definition());
        target = _attrib.entity();
        newRow.setObjectForKey(entry.getValue(), _attrib.name());
      } else {
        newRow.setObjectForKey(entry.getValue(), attrib.name());
      }
    }
    _stores.objectForKey(target).insertRow(newRow, target);
  }
  
  @Override
  public int nextSequence() {
    return _stores.objectForKey(_entity).nextSequence();
  }

  private class InnerJoinEntityStore extends EREntityStore {
    NSMutableDictionary<EOAttribute, EOAttribute> attributeMap = new NSMutableDictionary<>();
    EREntityStore srcStore;
    EREntityStore destStore;
    EORelationship relationship;

    @SuppressWarnings("cast")
    public InnerJoinEntityStore(EORelationship rel, EREntityStore store1, EREntityStore store2) {
      srcStore = store1;
      destStore = store2;
      relationship = rel;
      for (EOJoin join : (NSArray<EOJoin>)rel.joins()) {
        attributeMap.setObjectForKey(join.destinationAttribute(), join.sourceAttribute());
      }
    }

    @Override
    public Iterator<NSMutableDictionary<String, Object>> iterator() {
      return new InnerJoinIterator();
    }

    class InnerJoinIterator implements Iterator<NSMutableDictionary<String, Object>> {
      private Iterator<NSMutableDictionary<String, Object>> srcIterator;
      private Iterator<NSMutableDictionary<String, Object>> destIterator;
      private NSMutableDictionary<String, Object> src;
      private NSMutableDictionary<String, Object> dst;
      private Boolean _hasNext = null;

      public InnerJoinIterator() {
        srcIterator = srcStore.iterator();
        destIterator = destStore.iterator();
        if (srcIterator.hasNext()) {
          src = srcIterator.next();
        }
      }

      public boolean hasNext() {
        if (_hasNext != null) {
          return _hasNext.booleanValue();
        }
        while (_hasNext == null) {
          if (destIterator.hasNext()) {
            dst = destIterator.next();
          } else {
            if (srcIterator.hasNext()) {
              src = srcIterator.next();
              destIterator = destStore.iterator();
              continue;
            }
            _hasNext = Boolean.FALSE;
            break;
          }
          if (src == null) {
            _hasNext = Boolean.FALSE;
            break;
          }
          for (Entry<EOAttribute, EOAttribute> entry : attributeMap.entrySet()) {
            String srcKey = entry.getKey().columnName();
            String dstKey = entry.getValue().columnName();
            Object srcValue = src.objectForKey(srcKey);
            Object dstValue = dst.objectForKey(dstKey);
            if (srcValue == dstValue || srcValue != null && srcValue.equals(dstValue)) {
              _hasNext = Boolean.TRUE;
              break;
            }
          }
        }
        return _hasNext.booleanValue();
      }

      @SuppressWarnings("cast")
      public NSMutableDictionary<String, Object> next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more rows are available");
        }
        _hasNext = null;
        NSMutableDictionary<String, Object> row = new NSMutableDictionary<>(src);
        EOEntity entity = relationship.entity();
        for (EOAttribute attrib : (NSArray<EOAttribute>) entity.attributesToFetch()) {
          EORelationship rel = entity._relationshipForPath(attrib.relationshipPath());
          if (attrib.isFlattened() && relationship.equals(rel)) {
            String dstKey = entity._attributeForPath(attrib.definition()).columnName();
            Object value = dst.objectForKey(dstKey);
            row.setObjectForKey(value != null ? value : NSKeyValueCoding.NullValue, attrib.definition());
          }
        }
        //row.takeValueForKeyPath(rowFromStoredValues(dst, relationship.entity()), relationship.name());
        return row;
      }

      public void remove() {
        srcIterator.remove();
        //XXX: This should probably invalidate the destIterator
      }
    }

    @Override
    protected void _insertRow(NSMutableDictionary<String, Object> row, EOEntity entity) {
      throw new UnsupportedOperationException("Inserting rows is not supported in " + getClass().getName());
    }
  }

  @Override
  protected void _insertRow(NSMutableDictionary<String, Object> row, EOEntity entity) {
    throw new UnsupportedOperationException("Inserting rows is not supported in " + getClass().getName());
  }
}
