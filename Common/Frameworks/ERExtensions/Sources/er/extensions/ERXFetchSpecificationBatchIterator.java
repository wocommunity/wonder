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

/**
 * The goal of the fetch specification batch iterator is
 * to have the ability to iterate through a fetch specification
 * that might fetch one million enterprise objects. Fetching all
 * of the objects into a singel editing context is prohibitive
 * in the amount of memory needed and in the time taken to
 * process all of the rows. The iterator allows one to iterate
 * through the fetched objects only hydrating those objects need
 * in small bite size pieces. The iterator also allows you to
 * swap out editing contexts between calls to <b>nextBatch</b>,
 * which will allow the garbage collector to collect the old
 * editing context and the previous batch of enterprise objects.
 */
public class ERXFetchSpecificationBatchIterator {

    /** holds the default batch size, any bigger than this an Oracle has a fit */
    public static final int DefaultBatchSize = 250;

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXFetchSpecificationBatchIterator.class);

    /** holds the selected batch size */
    protected int batchSize;
    /** holds a reference to the selected editing context */
    protected EOEditingContext editingContext;
    /** holds a reference to the fetch spec to iterate over */
    protected EOFetchSpecification fetchSpecification;

    /** holds an array of primary key values to iterate through */
    protected NSArray primaryKeys;
    /** holds the current batch index */
    protected int currentBatchIndex;
    /** holds the number of objects fetched */
    protected int currentObjectFetchCount;
    
    /**
     * Constructs a fetch specification iterator for a given fetch
     * specification with the default batch size. Note you will have to
     * set an editingContext on the iterator before calling the
     * nextBatch method.
     * @param fetchSpecication to iterate through
     */
    public ERXFetchSpecificationBatchIterator(EOFetchSpecification fetchSpecication) {
        this(fetchSpecication, null);
    }

    /**
     * Constructs a fetch specification iterator for a given fetch
     * specification with the default batch size. All objects will be
     * fetched from the given editing context. Note that you can switch
     * out different editing contexts between calls to <b>nextBatch</b>
     * @param fetchSpecication to iterate through
     * @param ec editing context to fetch against
     */    
    public ERXFetchSpecificationBatchIterator(EOFetchSpecification fetchSpecification, EOEditingContext ec) {
        this(fetchSpecification, ec, DefaultBatchSize);
    }

    /**
     * Constructs a fetch specification iterator for a given fetch
     * specification and a batch size. All objects will be
     * fetched from the given editing context. Note that you can switch
     * out different editing contexts between calls to <b>nextBatch</b>
     * @param fetchSpecication to iterate through
     * @param ec editing context to fetch against
     * @param batchSize number of objects to fetch in a given batch
     */
    public ERXFetchSpecificationBatchIterator(EOFetchSpecification fetchSpecification, EOEditingContext ec, int batchSize) {
        this(fetchSpecification,(NSArray)null,ec,batchSize);
    }    

    /**
     * Constructs a fetch specification iterator for a fetch specification,
     * an optional set of pre-fetched primary keys
     * and a batch size. All objects will be
     * fetched from the given editing context. Note that you can switch
     * out different editing contexts between calls to <b>nextBatch</b>
     * @param fetchSpecication to iterate through
     * @param pkeys primary keys to iterate through
     * @param ec editing context to fetch against
     * @param batchSize number of objects to fetch in a given batch
     */
    public ERXFetchSpecificationBatchIterator(EOFetchSpecification fetchSpecification, NSArray pkeys, EOEditingContext ec, int batchSize) {
        super();
        this.fetchSpecification = fetchSpecification;
        this.primaryKeys = pkeys;
        setEditingContext(ec);
        setBatchSize(batchSize);
    }

    /**
     * Gets the batch size.
     * @return number of enterprise objects to fetch
     *		a batch.
     */
    public int batchSize() {
        return batchSize;
    }

    /**
     * Gets the current batch index.
     * @return number of batches fetched thus far
     */
    public int currentBatchIndex() {
        return currentBatchIndex;
    }

    /**
     * Gets the number of batches for a given iterator.
     * @return number of objects / batch size rounded up
     */
    public int batchCount() {
        if (!hasFetchedPrimaryKeys())
            fetchPrimaryKeys();
        return (int)Math.ceil((primaryKeys.count() * 1.0) / (batchSize() * 1.0));
    }
    
    /**
     * Gets the current number of objects
     * fetched thus far.
     * @return current number of objects fetched.
     */
    public int currentObjectFetchCount() {
        return currentObjectFetchCount;
    }

    /**
     * Sets the batch size.
     * @param batchSize to be set.
     */
    public void setBatchSize(int batchSize) {
        if (batchSize <= 0)
            throw new RuntimeException("Attempting to set a batch size of negative value.");
        if (batchSize > DefaultBatchSize)
            log.warn("Batches larger than the the default batch size of " + DefaultBatchSize
                     + " might cause JDBC issues.");
        this.batchSize = batchSize;
    }

    /**
     * Gets the currently set editing context.
     * @return editing context used to fetch against
     */
    public EOEditingContext editingContext() {
        return editingContext;
    }

    /**
     * Sets the editing context used to fetch objects
     * against. It is perfectly fine to change editing
     * contexts between fetching the next batch.
     * @param ec editing context used to fetch against
     */
    public void setEditingContext(EOEditingContext ec) {
        editingContext = ec;
    }

    /**
     * Determines if the iterator has another batch.
     * @return if calling <b>nextBatch</b> will have any effect
     */
    public boolean hasNextBatch() {
        if (!hasFetchedPrimaryKeys())
            fetchPrimaryKeys();
        return currentObjectFetchCount < primaryKeys.count();
    }

    /**
     * Gets the next batch of enterprise objects for the
     * given fetch specification. Note that the editing
     * context that is set will be used to fetch against.
     * You can swap out a different editing context before
     * calling this method to reduce memory consumption.
     * @param batch of enterprise objects
     */
    public NSArray nextBatch() {
        if (editingContext() == null)
            throw new RuntimeException("ERXFetchSpecificationBatchIterator: Calling nextBatch with a null editing context!");
        
        EOEntity entity = EOUtilities.entityNamed(editingContext(), fetchSpecification.entityName());
        if (entity.primaryKeyAttributes().count() > 1)
            throw new RuntimeException("ERXFetchSpecificationBatchIterator: Currently only single primary key entities are supported.");

        String primaryKeyAttributeName = ((EOAttribute)entity.primaryKeyAttributes().lastObject()).name();

        NSArray nextBatch = null;
        if (!hasFetchedPrimaryKeys())
            fetchPrimaryKeys();
        if (hasNextBatch()) {
            int length = primaryKeys.count() - currentObjectFetchCount > batchSize() ? batchSize() : primaryKeys.count() - currentObjectFetchCount;
            NSRange range = new NSRange(currentObjectFetchCount, length);
            NSArray primaryKeysToFetch = primaryKeys.subarrayWithRange(range);

            log.debug("Of primaryKey count: " + primaryKeys.count() + " fetching range: " + range + " which is: " + primaryKeysToFetch.count());

            ERXInQualifier qual = new ERXInQualifier(primaryKeyAttributeName, primaryKeysToFetch);
            EOFetchSpecification fetchSpec = new EOFetchSpecification(fetchSpecification.entityName(), qual, fetchSpecification.sortOrderings());
            if (fetchSpecification.prefetchingRelationshipKeyPaths() != null)
                fetchSpec.setPrefetchingRelationshipKeyPaths(fetchSpecification.prefetchingRelationshipKeyPaths());
            nextBatch = editingContext().objectsWithFetchSpecification(fetchSpec);

            log.debug("Actually fetched: " + nextBatch.count() + " with fetch speciifcation: " + fetchSpec);
            
            currentObjectFetchCount += length;
            currentBatchIndex++;
        }
        return nextBatch != null ? nextBatch : NSArray.EmptyArray;
    }

    /**
     * Determines if the primary keys have been fetched
     * yet for the given fetch specification.
     * @return if the primary keys array has been populated
     */
    protected boolean hasFetchedPrimaryKeys() {
        return primaryKeys != null;
    }

    /**
     * Method used to fetch the primary keys of the objects
     * for the given fetch specification. Note the sort
     * orderings for the fetch specification are respected.
     */
    protected void fetchPrimaryKeys() {
        if (!hasFetchedPrimaryKeys()) {
            if (editingContext() == null)
                throw new RuntimeException("Attempting to fetch the primary keys for a null editingContext");

            EOEntity entity = EOUtilities.entityNamed(editingContext(), fetchSpecification.entityName());
            
            if (entity.primaryKeyAttributes().count() > 1)
                throw new RuntimeException("ERXFetchSpecificationBatchIterator: Currently only single primary key entities are supported.");
            
            EOFetchSpecification pkFetchSpec = ERXEOControlUtilities.primaryKeyFetchSpecificationForEntity(editingContext(),
                                                                                                      fetchSpecification.entityName(),
                                                                                                      fetchSpecification.qualifier(),
                                                                                                      fetchSpecification.sortOrderings(),
                                                                                                      null);
            log.debug("Fetching primary keys.");
            NSArray primaryKeyDictionaries = editingContext().objectsWithFetchSpecification(pkFetchSpec);
            
            String pkAttributeName = ((EOAttribute)entity.primaryKeyAttributes().lastObject()).name();
            primaryKeys = (NSArray)primaryKeyDictionaries.valueForKey(pkAttributeName);
        }
    }

    /**
     * Resets the batch iterator so it will refetch it's primary keys again.
     */
    public void reset() {
        primaryKeys = null;
    }
}
