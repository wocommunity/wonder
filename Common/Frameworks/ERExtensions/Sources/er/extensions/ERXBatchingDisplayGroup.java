package er.extensions;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotificationCenter;

/**
 * Extends {@link WODisplayGroup} in order to provide real batching.
 * This is done by adding database specific code to the select statement
 * from the {@link EOFetchSpecification} from the {@link WODisplayGroup}'s
 * {@link EODataSource} which <b>must</b> be an {@link EODatabaseDataSource}.
 * If used with other datasources, it reverts to the default behaviour.
 * @author dt first version
 * @author ak gross hacks, made functional and usable.
 */
public class ERXBatchingDisplayGroup extends WODisplayGroup {

    /** Logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(ERXBatchingDisplayGroup.class);

    /** total number of batches */
    protected int _batchCount;

    /** cache for the displayed objects */
    protected NSArray _displayedObjects;

    /** cache batching flag */
    protected Boolean _isBatching;


    /**
     * Determines if batching is possible. 
     *
     * @return true if dataSource is an instance of EODatabaseDataSource
     */
    private boolean isBatching() {
        return _isBatching == null ? false : _isBatching.booleanValue();
    }
    
    /**
     * Overridden to set the isBatching flag to true if we have an EODatabaseDataSource.
     */
    public void setDataSource(EODataSource eodatasource) {
        _isBatching = (eodatasource instanceof EODatabaseDataSource) ? Boolean.TRUE : Boolean.FALSE;
        super.setDataSource(eodatasource);
    }
    
    /**
     * Overridden to return the pre-calculated number of batches
     */
    public int batchCount() {
        if(isBatching()) {
            return _batchCount;
        }
        return super.batchCount();
     }
    
    /**
     * Overriden to clear out our array of fetched objects.
     */
    public void setCurrentBatchIndex(int index) {
        if(isBatching() && currentBatchIndex() != index) {
            _displayedObjects = null;
        }
        super.setCurrentBatchIndex(index);
    }
    
    /**
     * Overriden to clear out our array of fetched objects.
     */
    public void setNumberOfObjectsPerBatch(int count) {
        if(isBatching() && numberOfObjectsPerBatch() != count) {
            _displayedObjects = null;
        }
        super.setNumberOfObjectsPerBatch(count);
    }
    
    /**
    * Overridden method in order to fetch -only- the rows that are needed. This is
     * different to the editors methods because a {@link WODisplayGroup} would always fetch
     * from the start until the end of the objects from the fetch limit.
     *
     * @return the objects that should be diplayed.
     */
    public NSArray displayedObjects() {
        if (isBatching()) {
            if(_displayedObjects == null) {
                refetch();
            }
            return _displayedObjects;
        }
        return super.displayedObjects();
    }
    
    /**
     * Utility that does the actual fetching.
     */
    protected void refetch() {
        EODatabaseDataSource ds = (EODatabaseDataSource) dataSource();
        EOFetchSpecification spec = ds.fetchSpecificationForFetch();
        EOEditingContext ec = ds.editingContext();
        
        int rowCount = ERXEOAccessUtilities.rowCountForFetchSpecification(ec, spec);
        
        int start = (currentBatchIndex()-1) * numberOfObjectsPerBatch();
        int end = start + numberOfObjectsPerBatch();

        if (numberOfObjectsPerBatch() == 0) {
            start = 0;
            end = rowCount;
        }
        
        if(end > rowCount) {
            end = rowCount;
        }
        
        setObjectArray(new FakeArray(rowCount));
        
        if(numberOfObjectsPerBatch() == 0) {
            _batchCount = 0;
        } else if(rowCount == 0) {
            _batchCount = 1;
        } else {
            _batchCount = (rowCount - 1) / numberOfObjectsPerBatch() + 1;
        }
        
        spec.setSortOrderings(sortOrderings());
        NSArray objects = ERXEOControlUtilities.objectsInRange(ec, spec, start, end);
        
        _displayedObjects = objects;
    }
    
    /**
     * Overridden to fetch only within displayed limits. 
     */
    public Object fetch() {
        if (isBatching()) {
            // TODO: call the respective delegate methods
            if(undoManager() != null) {
               undoManager().removeAllActionsWithTarget(this);
            }
            NSNotificationCenter.defaultCenter().postNotification("WODisplayGroupWillFetch", this);
            refetch();
            return null;
        }
        return super.fetch();
    }
    
    /**
     * Dummy array class that is used to provide a certain number of entries. 
     * We just fake that we an array with the number of objects the display group should display. 
     * */
    protected class FakeArray extends NSMutableArray {
        public FakeArray(int count) {
            for(int i = 0; i < count; i++) {
                // GROSS HACK: (ak) WO wants to sort the given array via KVC so we just 
                // let it sort "nothing" objects
                insertObjectAtIndex(new NSKeyValueCoding.ErrorHandling() {
                    public Object handleQueryWithUnboundKey(String anS) {
                        return null;
                    }
                    
                    public void handleTakeValueForUnboundKey(Object anObj, String anS) {
                    }
                    
                    public void unableToSetNullForKey(String anS) {
                    }}, i);
            }
        }
    }
}
