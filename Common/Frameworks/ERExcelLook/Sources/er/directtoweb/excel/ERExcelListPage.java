//
// ERExcelListPage.java: Class file for WO Component 'ERExcelListPage'
// Project ERExcelLook
//
// Created by max on Mon Apr 26 2004
//
package er.directtoweb.excel;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.directtoweb.*;
import er.extensions.*;
import org.apache.log4j.Logger;

public class ERExcelListPage extends ERD2WListPage {
	
	public static final Logger log = Logger.getLogger(ERExcelListPage.class);

    public int index;

    protected ERXFetchSpecificationBatchIterator batchIterator;
    protected NSArray objects;
    protected EOEditingContext transientEditingContext;

    public ERExcelListPage(WOContext context) {
        super(context);
    }
    
    public NSDictionary styles() { return ERExcelLook.styles(); }

    public NSArray objectsForSheet() {
        NSArray objectsForSheet = displayGroup().allObjects();
        NSArray sortOrderings = displayGroup().sortOrderings();
        if (sortOrderings != null && sortOrderings.count() > 0) {
            objectsForSheet = EOSortOrdering.sortedArrayUsingKeyOrderArray(objectsForSheet, sortOrderings);
        }
        return objectsForSheet;
    }

    /**
        * Sets the data source to the specified data source.
     * Also sets the data source of the display group and fetches.
     *
     * @param dataSource  instance of EODataSource
     * @see D2WPage#setDataSource
     * @see #displayGroup
     */
    public void setDataSource(EODataSource dataSource) {
        if (dataSource instanceof EODatabaseDataSource) {
            EODatabaseDataSource ds = (EODatabaseDataSource)dataSource;
            setBatchIterator(new ERXFetchSpecificationBatchIterator(ds.fetchSpecificationForFetch(),
                                                                    ds.editingContext(),
                                                                    100));
        } else {
            objects = dataSource.fetchObjects();
        }
    }    

    public int batchCount() {
        return hasBatchIterator() ? batchIterator().batchCount() : 1;
    }

    public NSArray currentBatch() {
        log.info("Current batch, index: " + index);
        NSArray currentBatch = null;

        if (hasBatchIterator()) {
            if (transientEditingContext != null) {
                log.info("Disposing transientEditingContext");
                transientEditingContext.dispose();
            }
            transientEditingContext = ERXEC.newEditingContext();
            batchIterator().setEditingContext(transientEditingContext);
            currentBatch = batchIterator().batchWithIndex(index);
        } else {
            currentBatch = objects;
        }

        return currentBatch;
    }
    
    public boolean hasBatchIterator() { return batchIterator() != null; }
    
    public ERXFetchSpecificationBatchIterator batchIterator() { return batchIterator; }
    public void setBatchIterator(ERXFetchSpecificationBatchIterator value) { batchIterator = value; }
    
}
