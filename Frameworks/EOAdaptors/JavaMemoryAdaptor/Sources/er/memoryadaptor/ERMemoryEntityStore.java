package er.memoryadaptor;

import java.util.Iterator;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERMemoryEntityStore is the actual datastore implementation for a single "table"
 * in ERMemoryAdaptor.  It's about as simple as you can get -- each row of the 
 * "database" is represented by a dictionary that maps attribute names to values.
 * 
 * @author mschrag
 * @author q
 */
public class ERMemoryEntityStore extends EREntityStore {
  protected NSMutableArray<NSMutableDictionary<String, Object>> _rows;

  public ERMemoryEntityStore() {
    _rows = new NSMutableArray<NSMutableDictionary<String, Object>>();
  }

  @Override
  public void clear() {
    super.clear();
    _rows.removeAllObjects();
  }

  @Override
  public ERMemoryEntityStore transactionStore() {
    ERMemoryEntityStore cloneStore = new ERMemoryEntityStore() {
      @Override
      public int nextSequence() { 
        return ERMemoryEntityStore.this.nextSequence(); 
      }
    };
    for (NSMutableDictionary<String, Object> row : _rows) {
      cloneStore._rows.addObject(row.mutableClone());
    }
    return cloneStore;
  }

  @Override
  public void commitFromTransactionStore(EREntityStore store) {
    _rows = ((ERMemoryEntityStore)store)._rows.mutableClone();
  }
  
  @Override
  public Iterator<NSMutableDictionary<String, Object>> iterator() {
    return _rows.iterator();
  }

  @Override
  protected void _insertRow(NSMutableDictionary<String, Object> row, EOEntity entity) {
    _rows.addObject(row);
  }
}
