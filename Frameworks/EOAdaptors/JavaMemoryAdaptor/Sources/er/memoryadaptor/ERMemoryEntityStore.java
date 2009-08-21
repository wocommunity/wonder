package er.memoryadaptor;

import java.util.Enumeration;
import java.util.Iterator;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOKeyValueCodingAdditions;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERMemoryEntityStore is the actual datastore implementation for a single "table"
 * in ERMemoryAdaptor.  It's about as simple as you can get -- each row of the 
 * "database" is represented by a dictionary that maps attribute names to values.
 * Additionally, this tracks a sequence number for the entity (for pk generation).
 * 
 * @author mschrag
 */
public class ERMemoryEntityStore extends EREntityStore {
  private NSMutableArray<NSMutableDictionary<String, Object>> _rows;

  public ERMemoryEntityStore() {
    _rows = new NSMutableArray<NSMutableDictionary<String, Object>>();
  }

  public void clear() {
    _sequence = 0;
    _rows.removeAllObjects();
  }

  public ERMemoryEntityStore transactionStore() {
    ERMemoryEntityStore cloneStore = new ERMemoryEntityStore() {
      public int nextSequence() { 
        return ERMemoryEntityStore.this.nextSequence(); 
      }
    };
    for (NSMutableDictionary<String, Object> row : _rows) {
      cloneStore._rows.addObject(new NSMutableDictionary<String, Object>(row));
    }
    return cloneStore;
  }

  @Override
  public void commitFromTransactionStore(EREntityStore store) {
    _rows = new NSMutableArray<NSMutableDictionary<String, Object>>();
    Iterator<NSMutableDictionary<String, Object>> i = store.iterator();
    while (i.hasNext()) {
      _rows.addObject(new NSMutableDictionary<String, Object>(i.next()));
    }
  }
  
  @Override
  public Iterator<NSMutableDictionary<String, Object>> iterator() {
    return _rows.iterator();
  }

  @Override
  public int updateValuesInRowsDescribedByQualifier(NSDictionary updatedRow, EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;
      Iterator<NSMutableDictionary<String, Object>> i = iterator();
      while (i.hasNext()) {
        NSMutableDictionary<String, Object> row = i.next();
        if (qualifier == null || qualifier.evaluateWithObject(row)) {
          EOKeyValueCodingAdditions.Utility.takeValuesFromDictionary(row, updatedRow);
          count++;
        }
      }

      return count;
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to update '" + entity.name() + "' row " + updatedRow + " with qualifier " + qualifier + ": " + e.getMessage());
    }
  }

  @Override
  public void insertRow(NSDictionary<String, Object> row, EOEntity entity) {
    try {
      // AK: it looks like the higher levels sometimes do not add null values correctly,
      // so we make it up here and put NullValue in the dict
      NSMutableDictionary<String, Object> mutableRow = new NSMutableDictionary<String, Object>(row);
      if(entity.attributes().count() != row.allKeys().count()) {
        for (Enumeration e = entity.attributes().objectEnumerator(); e.hasMoreElements();) {
          EOAttribute attribute = (EOAttribute) e.nextElement();
          if(!attribute.isDerived() && mutableRow.objectForKey(attribute.name()) == null) {
            mutableRow.setObjectForKey(NSKeyValueCoding.NullValue, attribute.name());
          }
        }
      }
      _rows.addObject(mutableRow);
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to insert '" + entity.name() + "' with row " + row + ": " + e.getMessage());
    }
  }

  @Override
  public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;
      Iterator<NSMutableDictionary<String, Object>> i = iterator();
      while (i.hasNext()) {
        if (qualifier == null || qualifier.evaluateWithObject(i.next())) {
          i.remove();
          count++;
        }
      }
      return count;
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to delete '" + entity.name() + "' with qualifier " + qualifier + ": " + e.getMessage());
    }
  }
}
