package er.memoryadaptor;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOStoredProcedure;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERMemoryAdaptorChannel provides the adaptor channel implementation for ERMemoryAdaptor.
 * 
 * @author mschrag
 */
public class ERMemoryAdaptorChannel extends EOAdaptorChannel {
  private NSArray<EOAttribute> _attributes;
  private NSMutableArray<NSMutableDictionary<String, Object>> _fetchedRows;
  private int _fetchIndex;
  private boolean _open;
  // During a fetch, the adaptor channel is called:
  //   1) selectAttributes
  //   2) Multiple calls to fetchRow()
  // We need a variable for the "is fetch in progress" because it should be true from 1) to the last call in 2).
  // Of course, it become false if there is an exception during this process (cancelFetch is called in such case).
  private boolean _fetchInProgress = false;

  public ERMemoryAdaptorChannel(ERMemoryAdaptorContext context) {
    super(context);
    _fetchIndex = -1;
  }
  
  // HP: Even though this method is deprecated in the superclass {@code EOAdaptorChannel}, it's still referenced
  // across the WebObjects and Wonder frameworks. For instance, the following methods depend on its implementation:
  // 
  // - AdaptorChannel.primaryKeysForNewRowsWithEntity
  // - ERXEOControlUtilities.newPrimaryKeyDictionaryForEntityNamed
  // 
  // It might be tempting to remove a deprecated method, but is this case, please, don't. :) 
  @Override
  @Deprecated
  public NSDictionary primaryKeyForNewRowWithEntity(EOEntity entity) {
    return adaptorContext()._newPrimaryKey(null, entity);
  }

  @Override
  public ERMemoryAdaptorContext adaptorContext() {
    return (ERMemoryAdaptorContext) super.adaptorContext();
  }

  @Override
  public NSArray<EOAttribute> attributesToFetch() {
    return _attributes;
  }

  @Override
  public void cancelFetch() {
	_fetchInProgress = false;
    _fetchedRows = null;
    _fetchIndex = -1;
  }

  @Override
  public void closeChannel() {
    _open = false;
  }

  @Override
  public NSArray describeResults() {
    return _attributes;
  }

  @Override
  public NSArray describeTableNames() {
    return NSArray.EmptyArray;
  }

  @Override
  public EOModel describeModelWithTableNames(NSArray anArray) {
    return null;
  }

  @Override
  public void evaluateExpression(EOSQLExpression anExpression) {
    throw new UnsupportedOperationException("ERMemoryAdaptorChannel.evaluateExpression");
  }

  @Override
  public void executeStoredProcedure(EOStoredProcedure aStoredProcedure, NSDictionary someValues) {
    throw new UnsupportedOperationException("ERMemoryAdaptorChannel.executeStoredProcedure");
  }

  @Override
  public NSMutableDictionary fetchRow() {
	if (!_fetchInProgress) {
		return null;
	}
    NSMutableDictionary row = null;
    if (hasMoreRowsToReturn()) {
      row = _fetchedRows.objectAtIndex(_fetchIndex++);
    }
    _fetchInProgress = hasMoreRowsToReturn();
    return row;
  }
  
  private boolean hasMoreRowsToReturn() {
	  return _fetchedRows != null && _fetchIndex < _fetchedRows.count();
  }

  @Override
  public boolean isFetchInProgress() {
    return _fetchInProgress;
  }

  @Override
  public boolean isOpen() {
    return _open;
  }

  @Override
  public void openChannel() {
    if (!_open) {
      _open = true;
    }
  }

  @Override
  public NSDictionary returnValuesForLastStoredProcedureInvocation() {
    throw new UnsupportedOperationException("ERMemoryAdaptorChannel.returnValuesForLastStoredProcedureInvocation");
  }

  @Override
  public void selectAttributes(NSArray<EOAttribute> attributesToFetch, EOFetchSpecification fetchSpecification, boolean shouldLock, EOEntity entity) {
    if (entity == null) {
      throw new IllegalArgumentException("null entity.");
    }
    if (attributesToFetch == null) {
      throw new IllegalArgumentException("null attributes.");
    }
    _fetchInProgress = true;
    setAttributesToFetch(attributesToFetch);

    EREntityStore store = adaptorContext()._entityStoreForEntity(entity);
    try {
      _fetchIndex = 0;
      _fetchedRows = store.fetch(attributesToFetch, fetchSpecification, shouldLock, entity, adaptorContext());
    }
    catch (EOGeneralAdaptorException e) {
      cancelFetch();
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      cancelFetch();
      throw new EOGeneralAdaptorException("Failed to fetch '" + entity.name() + "' with fetch specification '" + fetchSpecification + "': " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setAttributesToFetch(NSArray attributesToFetch) {
    if (attributesToFetch == null) {
      throw new IllegalArgumentException("ERMemoryAdaptorChannel.setAttributesToFetch: null attributes.");
    }
    _attributes = attributesToFetch;
  }

  @Override
  public int updateValuesInRowsDescribedByQualifier(NSDictionary updatedRow, EOQualifier qualifier, EOEntity entity) {
    try {
      EREntityStore store = adaptorContext()._entityStoreForEntity(entity);
      return store.updateValuesInRowsDescribedByQualifier(updatedRow, qualifier, entity);
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
  public void insertRow(NSDictionary row, EOEntity entity) {
    try {
      EREntityStore store = adaptorContext()._entityStoreForEntity(entity);
      store.insertRow(row, entity);
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
      EREntityStore store = adaptorContext()._entityStoreForEntity(entity);
      return store.deleteRowsDescribedByQualifier(qualifier, entity);
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
