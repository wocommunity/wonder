//
//  ERXPrimaryKeyBatchIterator.java
//  ERExtensions
//
//  Created by Max Muller on Mon Oct 21 2002.
//
package er.extensions.eof;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import er.extensions.eof.qualifiers.ERXInQualifier;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.jdbc.ERXSQLHelper;

/**
 * The goal of the fetch specification batch iterator is to have the ability to
 * iterate through a fetch specification that might fetch one million enterprise
 * objects. Fetching all of the objects into a single editing context is
 * prohibitive in the amount of memory needed and in the time taken to process
 * all of the rows. <br>
 * The iterator allows one to iterate through the fetched objects only hydrating
 * those objects need in small bite size pieces. The iterator also allows you to
 * swap out editing contexts between calls to <b>nextBatch()</b>, which will
 * allow the garbage collector to collect the old editing context and the
 * previous batch of enterprise objects.<br>
 * For your convenience, this class also implements Iterator and Enumeration, so
 * you can use it as such.<br>
 * Be aware that the batch size is primarily intended to govern the number of
 * objects requested from the database at once, and may differ from the number
 * of objects returned by <b>nextBatch()</b>, for instance if the batch size is
 * changed after fetching, or if <b>filtersBatches()</b> is set to true.
 */
public class ERXFetchSpecificationBatchIterator implements Iterator, Enumeration {

	/** holds the default batch size, any bigger than this an Oracle has a fit */
    public static final int DefaultBatchSize = 250;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXFetchSpecificationBatchIterator.class);

    /** holds the selected batch size */
    protected int batchSize;
    /** holds a reference to the selected editing context */
    protected EOEditingContext editingContext;
    /** holds a reference to the fetch spec to iterate over */
    protected EOFetchSpecification fetchSpecification;
    /** holds the name of the primary key attribute corresponding to the entity being iterated over */
    protected String primaryKeyAttributeName;

    /** holds an array of primary key values to iterate through */
    protected NSArray primaryKeys;
    
    /** holds array of fetched but not-yet-returned objects; used by the Iterator and Enumeration interfaces */
    protected NSMutableArray cachedBatch;
    /** holds the number of objects fetched */
    protected int currentObjectFetchCount;
    /** determines whether we should re-apply the original qualifier to each batch of objects fetched */
    protected boolean shouldFilterBatches;

    /**
     * Constructs a fetch specification iterator for a given fetch
     * specification with the default batch size. Note you will have to
     * set an editingContext on the iterator before calling the
     * nextBatch method.
     * @param fetchSpecification to iterate through
     */
    public ERXFetchSpecificationBatchIterator(EOFetchSpecification fetchSpecification) {
        this(fetchSpecification, null);
    }

    /**
     * Constructs a fetch specification iterator for a given fetch
     * specification with the default batch size. All objects will be
     * fetched from the given editing context. Note that you can switch
     * out different editing contexts between calls to <b>nextBatch</b>
     * @param fetchSpecification to iterate through
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
     * @param fetchSpecification to iterate through
     * @param ec editing context to fetch against
     * @param batchSize number of objects to fetch in a given batch
     */
    public ERXFetchSpecificationBatchIterator(EOFetchSpecification fetchSpecification, EOEditingContext ec, int batchSize) {
        this(fetchSpecification, null, ec, batchSize);
    }    

    /**
     * Constructs a fetch specification iterator for a fetch specification,
     * an optional set of pre-fetched primary keys
     * and a batch size. All objects will be
     * fetched from the given editing context. Note that you can switch
     * out different editing contexts between calls to <b>nextBatch</b>.
     * <p>Note: if no ec is supplied a new one is initialized.</p>
     * @param fetchSpecification to iterate through
     * @param pkeys primary keys to iterate through
     * @param ec editing context to fetch against
     * @param batchSize number of objects to fetch in a given batch
     */
    public ERXFetchSpecificationBatchIterator(EOFetchSpecification fetchSpecification, NSArray pkeys, EOEditingContext ec, int batchSize) {
        super();

        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, fetchSpecification.entityName());
        NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
        if (primaryKeyAttributes.count() > 1) {
            throw new RuntimeException("ERXFetchSpecificationBatchIterator: Currently only single primary key entities are supported.");
        }

        primaryKeyAttributeName = ((EOAttribute)primaryKeyAttributes.lastObject()).name();
        this.fetchSpecification = (EOFetchSpecification) fetchSpecification.clone();
        primaryKeys = pkeys;
        setEditingContext(ec != null ? ec : ERXEC.newEditingContext());
        setBatchSize(batchSize);
        setFiltersBatches(false);
        
        EOQualifier qualifier = this.fetchSpecification.qualifier();
        if (qualifier != null) {
            editingContext().rootObjectStore().lock();
            try {
                this.fetchSpecification.setQualifier(entity.schemaBasedQualifier(qualifier));
            } finally {
                editingContext().rootObjectStore().unlock();
            }
        }
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
        return (int)Math.ceil((currentObjectFetchCount() * 1.0) / (batchSize() * 1.0));
    }

    /**
     * Gets the number of batches for a given iterator.
     * @return number of objects / batch size rounded up
     */
    public int batchCount() {
         return (int)Math.ceil((count() * 1.0) / (batchSize() * 1.0));
    }

    /**
     * Gets the number of objects.
     * @return number of objects
     */
    public int count() {
         return primaryKeys().count();
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
     * If true, each batch will be filtered based on the original qualifier.
     * @see #setFiltersBatches
     * @return whether batches will be re-filtered
     */
    public boolean filtersBatches() {
        return shouldFilterBatches;
    }

    /**
     * If set to true, each batch fetched will be filtered based on the qualifier attached
     * to the original fetch specification. The is useful to cover the case in which the
     * objects may have changed in important ways between the time their primary keys
     * were retrieved and the time they were fetched. Note that when filtering is on,
     * empty arrays may be returned from {@link #nextBatch()}, and null may be returned
     * from {@link #next()} and {@link #nextElement()}.
     *
     * Note that not all qualifiers can be applied in-memory, so this should not bet set
     * to true if such a qualifier is being used.
     *
     * Defaults to false.
     *
     * @param newValue whether batches should be re-filtered
     */
    public void setFiltersBatches(boolean newValue) {
        if(!newValue && shouldFilterBatches && cachedBatch != null) {
            //NOTE: This could be made to work "as expected", if we cached un-filtered batches, and only filtered when we're about to return something; but, probably not worth it
            log.warn("Setting filtersBatches from true to false while there is a cached batch--some objects may already have been discarded!");
        }
        shouldFilterBatches = newValue;
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
     * @return if ok to call {@link #nextBatch()}
     */
    public boolean hasNextBatch() {
        return (cachedBatch != null) || _hasMoreToFetch();
    }

    protected boolean _hasMoreToFetch() {
        return currentObjectFetchCount() < count();
    }

    /**
     * Gets the next batch of enterprise objects for the
     * given fetch specification. Note that the editing
     * context that is set will be used to fetch against.
     * You can swap out a different editing context before
     * calling this method to reduce memory consumption.
     * (However, if you are mixing calls to this method 
     * with calls to {@link #next()} or {@link #nextElement()},
     * this method may return a partial batch of already-cached
     * objects, in the editing context which was in place at the
     * time they were fetched.)
     * @return batch of enterprise objects
     */
    public NSArray nextBatch() {
        if(cachedBatch != null) {
            NSArray nextBatch = cachedBatch;
            cachedBatch = null;
            return nextBatch;
        }

        return _fetchNextBatch();
    }

    /**
     * Fetches the next batch unconditionally. Subclasses can
     * override this rather than {@link #nextBatch()}, to get
     * automatic support for the Iterator and Enumeration interfaces. 
     * @return next batch
     */
    protected NSArray _fetchNextBatch() {
        if (hasNextBatch()) {
            NSRange range = _rangeForOffset(currentObjectFetchCount);
            NSArray nextBatch = batchWithRange(range);
            currentObjectFetchCount += range.length();
            return nextBatch;
        }
        throw new IllegalStateException("Iterator is exhausted");
    }

    private NSRange _rangeForBatchIndex(int index) {
        int start = batchSize * index;
        return _rangeForOffset(start);
    }

    private NSRange _rangeForOffset(int start) {
        int batchSize = batchSize();
        int totalCountMinusStart = count() - start;
        int length = totalCountMinusStart > batchSize ? batchSize : totalCountMinusStart;
        if (length < 0) {
        	length = 0;
        }
        return new NSRange(start, length);
    }

    /**
     * Returns the batch corresponding to the given index, that is, the
     * batch beginning at {@link #batchSize()} * index.
     * Note that if the batch size has been changed after fetching, the
     * batches return by {@link #nextBatch()} may not line up with the
     * batches returned by this method. 
     *
     * Calling this method does not affect the position of the iterator.
     * @param index index of batch to retrieve
     * @return batch of enterprise objects
     */
    public NSArray batchWithIndex(int index) {
        NSRange range = _rangeForBatchIndex(index);
        return batchWithRange(range);
    }

    /**
     * Returns the batch corresponding to the given range.
     *
     * If the supplied range does not fall within the available range,
     * the results returned correspond to the intersection of the two.
     *
     * If no items are found, the supplied range does not intersect the
     * available range, or the supplied range has length zero, then an
     * empty array is returned.
     *
     * Calling this method does not affect the position of the iterator.
     * @param requestedRange range of batch to retrieve
     * @return batch of enterprise objects
     */
    public NSArray batchWithRange(NSRange requestedRange) {
        EOEditingContext ec = editingContext();
        if ( ec == null) {
            throw new IllegalStateException("ERXFetchSpecificationBatchIterator: Calling nextBatch with a null editing context!");
        }

        NSArray nextBatch = null;
        NSRange range = requestedRange.rangeByIntersectingRange( new NSRange(0, count()) ); //intersect with legal range
        if ( range.length() > 0 ) {
            NSArray primaryKeys = primaryKeys();
            NSArray primaryKeysToFetch = primaryKeys.subarrayWithRange(range);

            log.debug("Of primaryKey count: " + primaryKeys.count() + " fetching range: " + range + " which is: " + primaryKeysToFetch.count());

            ERXInQualifier qual = new ERXInQualifier(primaryKeyAttributeName, primaryKeysToFetch);
            EOFetchSpecification batchFS = new EOFetchSpecification(fetchSpecification.entityName(), qual, fetchSpecification.sortOrderings());
            if (fetchSpecification.prefetchingRelationshipKeyPaths() != null) {
            	batchFS.setPrefetchingRelationshipKeyPaths(fetchSpecification.prefetchingRelationshipKeyPaths());
            }
            batchFS.setRefreshesRefetchedObjects(fetchSpecification.refreshesRefetchedObjects());
            batchFS.setRawRowKeyPaths(fetchSpecification.rawRowKeyPaths());
            nextBatch = ec.objectsWithFetchSpecification(batchFS);

            if (log.isDebugEnabled()) {
                log.debug("Actually fetched: " + nextBatch.count() + " with fetch specification: " + batchFS);
                if (primaryKeysToFetch.count() > nextBatch.count()) {
                    NSArray missedKeys = ERXArrayUtilities.arrayMinusArray(primaryKeysToFetch, (NSArray)nextBatch.valueForKey(primaryKeyAttributeName));
                    log.debug("Primary Keys that were not found for this batch: " + missedKeys);
                }
            }

            if (shouldFilterBatches) {
                EOQualifier originalQualifier = fetchSpecification.qualifier();
                if (originalQualifier != null) {
                    nextBatch = EOQualifier.filteredArrayWithQualifier(nextBatch, originalQualifier);
                    log.debug("Filtered batch to: " + nextBatch.count());
                }
            }
        }
        return nextBatch != null ? nextBatch : NSArray.EmptyArray;
    }

    protected EOFetchSpecification batchFetchSpecificationForQualifier(EOQualifier qualifier) {
        EOFetchSpecification fetchSpec = (EOFetchSpecification)fetchSpecification.clone();
        fetchSpec.setQualifier(qualifier);
        fetchSpec.setRequiresAllQualifierBindingVariables(false);
        fetchSpec.setLocksObjects(false);
        fetchSpec.setPromptsAfterFetchLimit(false);
        return fetchSpec;
    }

    /**
     * Method used to fetch the primary keys of the objects
     * for the given fetch specification. Note the sort
     * orderings for the fetch specification are respected.
     *
     * @return array of primary keys to iterate over
     */
    protected NSArray primaryKeys() {
        if (primaryKeys == null) {
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
            pkFetchSpec.setFetchLimit(fetchSpecification.fetchLimit());
            pkFetchSpec.setUsesDistinct(fetchSpecification.usesDistinct());
            boolean performDistinctInMemory = ERXSQLHelper.newSQLHelper(entity).shouldPerformDistinctInMemory(pkFetchSpec);
            if (performDistinctInMemory) {
            	pkFetchSpec.setUsesDistinct(false);
            }
            log.debug("Fetching primary keys.");
            NSArray primaryKeyDictionaries = editingContext().objectsWithFetchSpecification(pkFetchSpec);

            String pkAttributeName = entity.primaryKeyAttributes().lastObject().name();
            primaryKeys = (NSArray)primaryKeyDictionaries.valueForKey(pkAttributeName);
            if (performDistinctInMemory) {
            	primaryKeys = ERXArrayUtilities.arrayWithoutDuplicates(primaryKeys);
            }
        }
        return primaryKeys;
    }
    
    /**
     * Resets the batch iterator so it will refetch its primary keys again.
     */
    public void reset() {
        primaryKeys = null;
        cachedBatch = null;
    }
    
	/**
	 * Implementation of the Iterator interface
	 */
    public boolean hasNext() {
 		return hasNextBatch(); //either there are more batches to fetch, or there is a cached batch already
	}

	/**
	 * Implementation of the Iterator interface
	 */
	public Object next() {
        if( cachedBatch == null) {
            NSArray nextBatch = _fetchNextBatch(); //will raise if no more batches, which is expected behavior if next() is called w/o first checking hasNext()
            while(nextBatch.count() == 0 && hasNextBatch()) { //if filtersBatches, we can get empty batches, so repeat until we get something, or run out
                nextBatch = _fetchNextBatch();
            }
            cachedBatch = nextBatch.mutableClone();
		}

        Object nextObject = null;
        if( cachedBatch.count() > 0 ) {
            nextObject = cachedBatch.removeObjectAtIndex(0);
        }
        if( cachedBatch.count() == 0 ) {
            cachedBatch = null;
        }
        return nextObject;
	}

	/**
	 * Implementation of the Iterator interface
	 */
	public void remove() {
		throw new UnsupportedOperationException("Can't remove, not implemented");
	}

	/**
	 * Implementation of the Enumeration interface
	 */
	public boolean hasMoreElements() {
		return hasNext();
	}

	/**
	 * Implementation of the Enumeration interface
	 */
	public Object nextElement() {
		return next();
	}
	
	/*
	 * Return useful debug info including fetchspec info.
	 * 
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.append("PKs Initialized", primaryKeys == null ? "No" : "Yes");
		if (primaryKeys != null) {
			b.append("Count", primaryKeys.count());
		}
		b.append("entityName", fetchSpecification.entityName());
		b.append("qualifier", fetchSpecification.qualifier());
		b.append("isDeep", fetchSpecification.isDeep());
		b.append("usesDistinct", fetchSpecification.usesDistinct());
		b.append("sortOrderings", fetchSpecification.sortOrderings());
		b.append("hints", fetchSpecification.hints());
		b.append("prefetchingRelationshipKeyPaths", fetchSpecification.prefetchingRelationshipKeyPaths());
		return b.toString();
	}
}
