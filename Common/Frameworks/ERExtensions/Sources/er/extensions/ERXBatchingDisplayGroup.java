package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation._NSDelegate;

/**
 * Extends {@link WODisplayGroup} in order to provide real batching. This is
 * done by adding database specific code to the select statement from the
 * {@link EOFetchSpecification} from the {@link WODisplayGroup}'s
 * {@link EODataSource} which <b>must</b> be an {@link EODatabaseDataSource}.
 * If used with other datasources, it reverts to the default behaviour.
 * 
 * @author dt first version
 * @author ak gross hacks, made functional and usable.
 */
public class ERXBatchingDisplayGroup extends ERXDisplayGroup {

	/** Logging support */
	private static final Logger log = Logger.getLogger(ERXBatchingDisplayGroup.class);

	/** total number of batches */
	protected int _batchCount;

	/** cache for the displayed objects */
	protected NSArray _displayedObjects;

	/** cache batching flag */
	protected Boolean _isBatching;

	/**
	 * If we're batching and the displayed objects have not been fetched,
	 * do a refetch() of them.
	 */
	protected void refetchIfNecessary() {
		if (isBatching() && _displayedObjects == null) {
			refetch();
		}
	}
	
	/**
	 * Determines if batching is possible.
	 * 
	 * @return true if dataSource is an instance of EODatabaseDataSource
	 */
	protected boolean isBatching() {
		return _isBatching == null ? false : _isBatching.booleanValue();
	}

	/**
	 * Overridden to set the isBatching flag to true if we have an
	 * EODatabaseDataSource.
	 */
	public void setDataSource(EODataSource eodatasource) {
		_isBatching = (eodatasource instanceof EODatabaseDataSource) ? Boolean.TRUE : Boolean.FALSE;
		super.setDataSource(eodatasource);
	}

	/**
	 * Overridden to return the pre-calculated number of batches
	 */
	public int batchCount() {
		if (isBatching()) {
			if (_displayedObjects == null) {
				refetch();
			}
			return _batchCount;
		}
		return super.batchCount();
	}

	/**
	 * Overriden to clear out our array of fetched objects.
	 */
	public void setCurrentBatchIndex(int index) {
		int previousBatchIndex = currentBatchIndex();
		super.setCurrentBatchIndex(index);
		if (isBatching() && previousBatchIndex != index) {
			_displayedObjects = null;
		}
	}

	/**
	 * Overriden to clear out our array of fetched objects.
	 */
	public void setNumberOfObjectsPerBatch(int count) {
		boolean didFetch = _displayedObjects != null;
		if (isBatching() && numberOfObjectsPerBatch() != count) {
			_displayedObjects = null;
		}
		NSArray selectedObjects = selectedObjects();
		super.setNumberOfObjectsPerBatch(count);
		setSelectedObjects(selectedObjects);
		// we have already fetched, so we need to adapt the batch count
		if (didFetch) {
			updateBatchCount();
		}
	}

	/**
	 * Overridden method in order to fetch -only- the rows that are needed. This
	 * is different to the editors methods because a {@link WODisplayGroup}
	 * would always fetch from the start until the end of the objects from the
	 * fetch limit.
	 * 
	 * @return the objects that should be diplayed.
	 */
	public NSArray displayedObjects() {
		if (isBatching()) {
			refetchIfNecessary();
			return _displayedObjects;
		}
		return super.displayedObjects();
	}

	/**
	 * Overridden to return allObjects() when batching, as we can't qualify in memory.
	 */
	public NSArray filteredObjects() {
		if (isBatching()) {
			return allObjects();
		}
		return super.filteredObjects();
	}

	/**
	 * Overridden to trigger a refetch.
	 */
	public void setQualifier(EOQualifier aEoqualifier) {
		super.setQualifier(aEoqualifier);
		_displayedObjects = null;
	}

	/**
	 * Overridden to preserve the selected objects.
	 */
	public void setSortOrderings(NSArray nsarray) {
		NSArray selectedObjects = selectedObjects();
		super.setSortOrderings(nsarray);
		setSelectedObjects(selectedObjects);
	}

	/**
	 * Utility to get the fetch spec from the datasource and the filter
	 * qualifier.
	 * 
	 * @return
	 */
	protected EOFetchSpecification fetchSpecification() {
		EODatabaseDataSource ds = (EODatabaseDataSource) dataSource();
		EOFetchSpecification spec = (EOFetchSpecification) ds.fetchSpecificationForFetch().clone();
		spec.setSortOrderings(sortOrderings());
		EOQualifier dgQualifier = qualifier();
		EOQualifier qualifier = spec.qualifier();
		if (dgQualifier != null) {
			if (qualifier != null) {
				qualifier = new EOAndQualifier(new NSArray(new Object[] { dgQualifier, qualifier }));
			}
			else {
				qualifier = dgQualifier;
			}
			spec.setQualifier(qualifier);
		}
		return spec;
	}

	/**
	 * Utility to get at the number of rows when batching.
	 * @return
	 */
	protected int rowCount() {
		EOEditingContext ec = dataSource().editingContext();
		EOFetchSpecification spec = fetchSpecification();

		int rowCount = ERXEOAccessUtilities.rowCountForFetchSpecification(ec, spec);
		return rowCount;
	}

	/**
	 * Utility to fetch the object in a given range.
	 * @param start
	 * @param end
	 * @return
	 */
	protected NSArray objectsInRange(int start, int end) {
		EOEditingContext ec = dataSource().editingContext();
		EOFetchSpecification spec = fetchSpecification();

		// fetch the primary keys, turn them into faults, then batch-fetch all
		// the non-resident objects
		NSArray primKeys = ERXEOControlUtilities.primaryKeyValuesInRange(ec, spec, start, end);
		NSArray faults = ERXEOControlUtilities.faultsForRawRowsFromEntity(ec, primKeys, spec.entityName());
		NSArray objects = ERXEOControlUtilities.objectsForFaultWithSortOrderings(ec, faults, sortOrderings());
		return objects;
	}

	/**
	 * Utility that does the actual fetching, if a qualifier() is set, it adds
	 * it to the dataSource() fetch qualifier.
	 */
	protected void refetch() {
		int rowCount = rowCount();

		int start = (currentBatchIndex() - 1) * numberOfObjectsPerBatch();
		int end = start + numberOfObjectsPerBatch();

		if (numberOfObjectsPerBatch() == 0) {
			start = 0;
			end = rowCount;
		}

		if (end > rowCount) {
			end = rowCount;
		}

		if(filteredObjects().count() != rowCount) {
			NSArray selectedObjects = selectedObjects();
			setObjectArray(new FakeArray(rowCount));
			setSelectedObjects(selectedObjects);
		}
		
		_displayedObjects = objectsInRange(start, end);
	}
	
	protected void updateBatchCount() {
		if (numberOfObjectsPerBatch() == 0) {
			_batchCount = 0;
		}
		else if (allObjects().count() == 0) {
			_batchCount = 1;
		}
		else {
			_batchCount = (allObjects().count() - 1) / numberOfObjectsPerBatch() + 1;
		}
	}

	/**
	 * Overridden to update the batch count.
	 */
	public void setObjectArray(NSArray objects) {
		super.setObjectArray(objects);
		updateBatchCount();
	}

	/**
	 * Overridden to fetch only within displayed limits.
	 */
	public Object fetch() {
		if (isBatching()) {
			_NSDelegate delegate = null;
			if (this.delegate() != null) {
				delegate = new _NSDelegate(WODisplayGroup.Delegate.class, delegate());
				if(delegate.respondsTo("displayGroupShouldFetch") && !delegate.booleanPerform("displayGroupShouldFetch", this)) {
		            return null;
				}
			}
			
			if (undoManager() != null) {
				undoManager().removeAllActionsWithTarget(this);
			}
			NSNotificationCenter.defaultCenter().postNotification("WODisplayGroupWillFetch", this);
			refetch();
			if (delegate != null) {
				// was initialized above
				if (delegate.respondsTo("displayGroupDidFetchObjects")) {
					delegate.perform("displayGroupDidFetchObjects", this, _displayedObjects);
				}
			}
			return null;
		}
		return super.fetch();
	}

	public void updateDisplayedObjects() {
		if (isBatching()) {
			// refetch();
			NSMutableArray selectedObjects = (NSMutableArray) selectedObjects();
			NSArray obj = allObjects();
			if (delegate() != null) {
				_NSDelegate delegate = new _NSDelegate(WODisplayGroup.Delegate.class, delegate());
				if (delegate != null && delegate.respondsTo("displayGroupDisplayArrayForObjects")) {
					delegate.perform("displayGroupDisplayArrayForObjects", this, obj);
				}
			}
			// _displayedObjects = new NSMutableArray(obj);
			setSelectedObjects(selectedObjects);
			// selectObjectsIdenticalToSelectFirstOnNoMatch(selectedObjects,
			// false);
			redisplay();
		}
		else {
			super.updateDisplayedObjects();
		}
	}

	/**
	 * Selects the visible objects, overridden to fetch all objects. Note that
	 * this makes sense only when there are only a "few" objects in the list.
	 * 
	 */

	public Object selectFilteredObjects() {
		if (isBatching()) {
			setSelectedObjects(objectsInRange(0, rowCount()));
			return null;
		}
		return super.selectFilteredObjects();
	}

	/**
	 * Dummy array class that is used to provide a certain number of entries. We
	 * just fake that we an array with the number of objects the display group
	 * should display.
	 */
	protected class FakeArray extends NSMutableArray {
		public FakeArray(int count) {
			super(count);
			Object fakeObject = new NSKeyValueCoding.ErrorHandling() {
				public Object handleQueryWithUnboundKey(String anS) {
					return null;
				}

				public void handleTakeValueForUnboundKey(Object anObj, String anS) {
				}

				public void unableToSetNullForKey(String anS) {
				}
			};
			for (int i = 0; i < count; i++) {
				// GROSS HACK: (ak) WO wants to sort the given array via KVC so
				// we just
				// let it sort "nothing" objects
				insertObjectAtIndex(fakeObject, i);
			}
		}
	}
}
