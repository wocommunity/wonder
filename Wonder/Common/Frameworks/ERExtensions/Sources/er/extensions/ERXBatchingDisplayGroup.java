package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
* Extends {@link WODisplayGroup} in order to provide real batching.
 * This is done by adding database specific code to the select statement
 * from the {@link EOFetchSpecification} from the {@link WODisplayGroup}'s
 * {@link EODataSource} which <b>must</b> be an {@link EODatabaseDataSource}.
 */
public class ERXBatchingDisplayGroup extends WODisplayGroup {

    /** total number of rows */
    protected int _rowCount = -1;

    /** cache for the displayed objects */
    protected NSArray _displayedObjects;

    protected Boolean _isBatching = null;

    public void setIsBatching(boolean value) {
        _isBatching = value ? Boolean.TRUE : Boolean.FALSE;
        redisplay();
    }
    
    /**
     * Determines if batching is possible. 
     *
     * @return true if dataSource is an instance of EODatabaseDataSource
     */
    public boolean isBatching() {
        if(_isBatching == null) {
            _isBatching = dataSource() instanceof EODatabaseDataSource ? Boolean.TRUE : Boolean.FALSE;
        }
        return _isBatching.booleanValue();
    }

    public void setDataSource(EODataSource eodatasource) {
        _isBatching = null;
        super.setDataSource(eodatasource);
    }

    /**
      * Overridden in order to use a custom method which determines the number of Objects / rows
     * for the existing EODatabaseDataSource.
     *
     * @return the number of rows from the EODatabaseDataSource
     */
    public int batchCount() {
        if(isBatching()) {
            if(numberOfObjectsPerBatch() == 0) {
                return 0;
            }
            if(rowCount() == 0) {
                return 1;
            } else {
                return (rowCount() - 1) / numberOfObjectsPerBatch() + 1;
            }
        }
        return super.batchCount();
    }
    
    /**
     * Returns the number of rows from the {@link EODatabaseDataSource}.
     *
     * @return the number of rows from the {@link EODatabaseDataSource}
     */
    public int rowCount() {
        if(_rowCount == -1) {
            EOFetchSpecification spec = databaseDataSource().fetchSpecificationForFetch();
            EOEditingContext ec = databaseDataSource().editingContext();
            _rowCount = ERXEOAccessUtilities.rowCountForFetchSpecification(ec, spec);
            setObjectArray(new FakeArray(_rowCount));
        }
        return _rowCount;
    }
    
    public EODatabaseDataSource databaseDataSource() {
        return (EODatabaseDataSource)dataSource();
    }

    public void redisplay() {
        _rowCount = -1;
        _displayedObjects = null;
        super.redisplay();
    }
    
    public void setCurrentBatchIndex(int index) {
        _displayedObjects = null;
        super.setCurrentBatchIndex(index);
    }
    
    /**
    * Overridden method in order to fetch -only- the rows that are needed. This is
     * different to the editors methods because a {@link WODisplayGroup} would always fetch
     * from the start until the end of the objects from the fetch limit.
     *
     * @return the objects that should be diplayed.
     */
    public NSArray displayedObjects() {
        if (!isBatching()) {
            return super.displayedObjects();
        } else {
            if(_displayedObjects == null) {
                //check the start and end based on currentBatchIndex and numberOfObjectsPerBatch()
                int count = rowCount();
                int start = (currentBatchIndex()-1) * numberOfObjectsPerBatch();
                int end = start + numberOfObjectsPerBatch();

                if (numberOfObjectsPerBatch() == 0) {
                    start = 0;
                    end = rowCount();
                }
                EOFetchSpecification spec = databaseDataSource().fetchSpecificationForFetch();
                //sortOrderings from the WODisplayGroup is only used in Memory: painful slow...
                spec.setSortOrderings(sortOrderings());

                EOEditingContext ec = databaseDataSource().editingContext();
                _displayedObjects = ERXEOControlUtilities.objectsInRange(ec, spec, start, end).mutableClone();
            }
        }
        return _displayedObjects;
    }
    
    /** we just fake that we are an array. */
    class FakeArray extends NSArray {
        int count = 0;

        public FakeArray(int count) {
            this.count = count;
        }
        private FakeArray(){}

        public int count() {
            return count;
        }

        public void insertObjectAtIndex(Object o, int i) {
            //do nothing
        }
    }
}
