package er.memoryadaptor;

import java.util.Iterator;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * EREntityStore is an abstract datastore implementation for a single "table"
 * in non relational EOAdaptors like ERMemoryAdaptor. It provides basic fetch support.
 * Additionally, this tracks a sequence number for the entity (for pk generation).
 * 
 * @author q
 */
public abstract class EREntityStore {
  protected int _sequence = 0;
  
  public interface JoinEntityStore { }
  
  public void commitFromTransactionStore(EREntityStore store) {
    throw new UnsupportedOperationException("Transactions are not supported in " + getClass().getName());
  }
  
  public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
    throw new UnsupportedOperationException("Deleting rows is not supported in " + getClass().getName());
  }

  public NSMutableArray<NSMutableDictionary<String, Object>> fetch(NSArray<EOAttribute> attributesToFetch, EOFetchSpecification fetchSpecification, boolean shouldLock, EOEntity entity) {
    EOQualifier qualifier = null;
    int fetchLimit = 0;
    NSArray sortOrderings = null;
    if (fetchSpecification != null) {
      qualifier = fetchSpecification.qualifier();
      fetchLimit = fetchSpecification.fetchLimit();
      sortOrderings = fetchSpecification.sortOrderings();
    }

    int count = 0;
    NSMutableArray<NSMutableDictionary<String, Object>> fetchedRows = new NSMutableArray<NSMutableDictionary<String, Object>>();
    Iterator<NSMutableDictionary<String, Object>> i = iterator();
    while (i.hasNext()) {
      NSMutableDictionary<String, Object> row = i.next();
      /* FIXME: This should technically map between columnName() and name() rather than just use name() */
      if (qualifier == null || qualifier.evaluateWithObject(row)) {
        fetchedRows.addObject(row);
        count++;
      }
      if (fetchLimit > 0 && count == fetchLimit) {
        break;
      }
    }

    if (sortOrderings != null) {
      EOSortOrdering.sortArrayUsingKeyOrderArray(fetchedRows, sortOrderings);
    }

    return fetchedRows;
  }

  public void insertRow(NSDictionary<String, Object> row, EOEntity entity) {
    throw new UnsupportedOperationException("Inserting rows is not supported in " + getClass().getName());
  }

  public abstract Iterator<NSMutableDictionary<String, Object>> iterator();
  
  public int nextSequence() {
    return ++_sequence;
  }

  public EREntityStore transactionStore() {
    throw new UnsupportedOperationException("Transactions are not supported in " + getClass().getName());
  }

  public int updateValuesInRowsDescribedByQualifier(NSDictionary updatedRow, EOQualifier qualifier, EOEntity entity) {
    throw new UnsupportedOperationException("Updating rows is not supported in " + getClass().getName());
  }
  
}
