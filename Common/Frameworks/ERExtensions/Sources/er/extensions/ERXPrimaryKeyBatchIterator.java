//
//  ERXPrimaryKeyBatchIterator.java
//  ERExtensions
//
//  Created by Max Muller on Mon Oct 21 2002.
//
package er.extensions;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

public class ERXPrimaryKeyBatchIterator {

    /** holds the default batch size, any bigger than this an Oracle has a fit */
    public static final int DefaultBatchSize = 250;

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXPrimaryKeyBatchIterator.class);
    
    protected int batchSize;
    protected EOEditingContext editingContext;
    protected String entityName;

    protected NSArray primaryKeys;
    protected int currentBatchIndex;

    public ERXPrimaryKeyBatchIterator(NSArray primaryKeys, String entityName) {
        this(primaryKeys, entityName, null, DefaultBatchSize);
    }

    public ERXPrimaryKeyBatchIterator(NSArray primaryKeys, String entityName, EOEditingContext ec) {
        this(primaryKeys, entityName, ec, DefaultBatchSize);
    }

    public ERXPrimaryKeyBatchIterator(NSArray primaryKeys, String entityName, EOEditingContext ec, int batchSize) {
        this.primaryKeys = primaryKeys;
        setEntityName(entityName);
        setEditingContext(ec);
        setBatchSize(batchSize);
    }

    public int batchSize() {
        return batchSize;
    }

    public int currentBatchIndex() {
        return currentBatchIndex;
    }
    
    public void setBatchSize(int batchSize) {
        if (batchSize > DefaultBatchSize)
            throw new RuntimeException("Currently batches larger than the the default batch size of 1000 is not supported");
        this.batchSize = batchSize;
    }

    public EOEditingContext editingContext() {
        return editingContext;
    }

    public void setEditingContext(EOEditingContext ec) {
        editingContext = ec;
    }

    public String entityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public boolean hasNextBatch() {
        return currentBatchIndex < primaryKeys.count();
    }
    
    public NSArray nextBatch() {
        if (editingContext() == null)
            throw new RuntimeException("ERXPrimaryKeyBatchIterator: Calling nextBatch with a null editing context!");
        if (entityName() == null)
            throw new RuntimeException("ERXPrimaryKeyBatchIterator: Calling nextBatch with a null entity name!");
        
        EOEntity entity = EOUtilities.entityNamed(editingContext(), entityName());
        if (entity.primaryKeyAttributes().count() > 1)
            throw new RuntimeException("ERXPrimaryKeyBatchIterator: Currently only single primary key entities are supported.");

        String primaryKeyAttributeName = ((EOAttribute)entity.primaryKeyAttributes().lastObject()).name();

        NSArray nextBatch = null;
        if (hasNextBatch()) {
            int length = primaryKeys.count() - currentBatchIndex > batchSize() ? batchSize() : primaryKeys.count() - currentBatchIndex;
            NSRange range = new NSRange(currentBatchIndex, length);
            NSArray primaryKeysToFetch = primaryKeys.subarrayWithRange(range);

            log.debug("Of primaryKey count: " + primaryKeys.count() + " fetching range: " + range + " which is: " + primaryKeysToFetch.count());

            ERXInQualifier qual = new ERXInQualifier(primaryKeyAttributeName, primaryKeysToFetch);
            EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName(), qual, null);
            nextBatch = editingContext().objectsWithFetchSpecification(fetchSpec);

            currentBatchIndex += length;
        }
        return nextBatch != null ? nextBatch : NSArray.EmptyArray;
    }
}
