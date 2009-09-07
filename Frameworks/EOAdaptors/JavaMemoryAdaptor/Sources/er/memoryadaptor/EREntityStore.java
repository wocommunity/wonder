package er.memoryadaptor;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import ognl.Ognl;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
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
  private int _sequence = 0;
  
  public interface JoinEntityStore { }
  
  public void clear() {
    _sequence = 0;
  }
  
  public void commitFromTransactionStore(EREntityStore store) {
    throw new UnsupportedOperationException("Transactions are not supported in " + getClass().getName());
  }
  
  public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;
      Iterator<NSMutableDictionary<String, Object>> i = iterator();
      while (i.hasNext()) {
        NSMutableDictionary<String, Object> row = i.next();
        if (qualifier == null || qualifier.evaluateWithObject(row)) {
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
      NSMutableDictionary<String, Object> row = rowFromStoredValues(i.next(), entity);
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

  private NSMutableDictionary<String, Object> rowFromStoredValues(NSMutableDictionary<String, Object> rawRow, EOEntity entity) {
    NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>(rawRow.count()); 
    for (EOAttribute attribute : entity.attributesToFetch()) {
      Object value = rawRow.objectForKey(attribute.columnName());
      if (attribute.isDerived() && !attribute.isFlattened()) {
        // Evaluate derived attribute expression
        
        //This is a hack to support SQL string concatenation in derived attributes
        String expression = attribute.definition().replaceAll("\\|\\|", "+ '' +");
        try {
          value = Ognl.getValue(expression, rawRow);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
      row.setObjectForKey(value != null ? value : NSKeyValueCoding.NullValue, attribute.name());
    }
    return row;
  }

  protected abstract void _insertRow(NSMutableDictionary<String, Object> row, EOEntity entity);
  
  public void insertRow(NSDictionary<String, Object> row, EOEntity entity) {
    try {
      // AK: it looks like the higher levels sometimes do not add null values correctly,
      // so we make it up here and put NullValue in the dict
      NSMutableDictionary<String, Object> mutableRow = new NSMutableDictionary<String, Object>(row.size());
      if(entity.attributes().count() != row.allKeys().count()) {
        for (Enumeration e = entity.attributes().objectEnumerator(); e.hasMoreElements();) {
          EOAttribute attribute = (EOAttribute) e.nextElement();
          Object value = row.objectForKey(attribute.name());
          if (value != null)
            mutableRow.setObjectForKey(value, attribute.columnName());
        }
      }
      _insertRow(mutableRow, entity);
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to insert '" + entity.name() + "' with row " + row + ": " + e.getMessage());
    }
  }

  public abstract Iterator<NSMutableDictionary<String, Object>> iterator();
  
  public int nextSequence() {
    return ++_sequence;
  }

  public EREntityStore transactionStore() {
    throw new UnsupportedOperationException("Transactions are not supported in " + getClass().getName());
  }

  public int updateValuesInRowsDescribedByQualifier(NSDictionary<String, Object> updatedRow, EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;
      Iterator<NSMutableDictionary<String, Object>> i = iterator();
      while (i.hasNext()) {
        NSMutableDictionary<String, Object> rawRow = i.next();
        NSMutableDictionary<String, Object> row = rowFromStoredValues(rawRow, entity);
        
        if (qualifier == null || qualifier.evaluateWithObject(row)) {
          for (Map.Entry<String, Object> entry : updatedRow.entrySet()) {
            EOAttribute attribute = entity.attributeNamed(entry.getKey());
            rawRow.setObjectForKey(entry.getValue(), attribute.columnName());
          }
          count++;
        }
      }

      return count;
    }
    catch (EOGeneralAdaptorException e) {
      e.printStackTrace();
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to update '" + entity.name() + "' row " + updatedRow + " with qualifier " + qualifier + ": " + e.getMessage());
    }
  }  
  
}
