package er.memoryadaptor;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueCodingAdditions;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERMemoryEntityStore is the actual datastore implementation for a single entity
 * in ERMemoryAdaptor.  It's about as simple as you can get -- each row of the 
 * "database" is represented by a dictionary that maps attribute names to values.
 * Additionally, this tracks a sequence number for the entity (for pk generation).
 * 
 * @author mschrag
 */
public class ERMemoryEntityStore {
  private NSMutableArray<NSMutableDictionary<String, Object>> _rows;
  private int _sequence;

  public ERMemoryEntityStore() {
    _rows = new NSMutableArray<NSMutableDictionary<String, Object>>();
  }

  public void clear() {
    _sequence = 0;
    _rows.removeAllObjects();
  }

  public ERMemoryEntityStore transactionStore() {
    ERMemoryEntityStore cloneStore = new ERMemoryEntityStore();
    for (NSMutableDictionary<String, Object> row : _rows) {
      cloneStore._rows.addObject(new NSMutableDictionary<String, Object>(row));
    }
    cloneStore._sequence = _sequence;
    return cloneStore;
  }

  public void commitFromTransactionStore(ERMemoryEntityStore store) {
    _rows = new NSMutableArray<NSMutableDictionary<String, Object>>();
    for (NSMutableDictionary<String, Object> row : store._rows) {
      _rows.addObject(new NSMutableDictionary<String, Object>(row));
    }
    _sequence = store._sequence;
  }

  public int nextSequence() {
    _sequence++;
    return _sequence;
  }

  public NSMutableArray<NSMutableDictionary<String, Object>> fetch(NSArray attributesToFetch, EOFetchSpecification fetchSpecification, boolean shouldLock, EOEntity entity) {
    EOQualifier qualifier = null;
    if (fetchSpecification != null) {
      qualifier = fetchSpecification.qualifier();
    }

    NSMutableArray<NSMutableDictionary<String, Object>> fetchedRows = new NSMutableArray<NSMutableDictionary<String, Object>>();
    for (NSMutableDictionary<String, Object> row : _rows) {
      if (qualifier == null || qualifier.evaluateWithObject(row)) {
        fetchedRows.addObject(row);
      }
    }

    NSArray sortOrderings = fetchSpecification.sortOrderings();
    if (sortOrderings != null) {
      EOSortOrdering.sortArrayUsingKeyOrderArray(fetchedRows, sortOrderings);
    }

    return fetchedRows;
  }

  public int updateValuesInRowsDescribedByQualifier(NSDictionary updatedRow, EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;

      for (NSMutableDictionary<String, Object> row : _rows) {
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

  @SuppressWarnings("unchecked")
  public void insertRow(NSDictionary row, EOEntity entity) {
    try {
      _rows.addObject(new NSMutableDictionary<String, Object>(row));
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to insert '" + entity.name() + "' with row " + row + ": " + e.getMessage());
    }
  }

  public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;

      NSMutableArray<NSMutableDictionary<String, Object>> deletedRows = new NSMutableArray<NSMutableDictionary<String, Object>>();
      for (int i = _rows.count() - 1; i >= 0; i--) {
        NSMutableDictionary<String, Object> row = _rows.objectAtIndex(i);
        if (qualifier == null || qualifier.evaluateWithObject(row)) {
          _rows.removeObjectAtIndex(i);
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
