package com.webobjects.eoaccess;


import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSComparator.ComparisonException;

import er.extensions.eoaccess.entityordering.ERXEntityFKConstraintOrder;
import er.extensions.eoaccess.entityordering.ERXEntityOrder;



/**
 * <p>EODatabaseContext delegate to order adaptor operations by FK constraints.  This prevents most ordering
 * operations on databases like MS SQL that do not support deferred constraints like a real database.
 * The easiest way to use this is:</p>
 * <pre>
 * ERXDatabaseContextMulticastingDelegate.addDefaultDelegate(new ERXEntityDependencyOrderingDelegate());
 * </pre>
 *
 * @author chill
 */
public class ERXEntityDependencyOrderingDelegate {

    protected NSComparator adaptorOpComparator;
    private static Logger logger = Logger.getLogger(ERXEntityDependencyOrderingDelegate.class);


    public ERXEntityDependencyOrderingDelegate() {
        super();
    }



    /**
     * Lazy creation of an EOAdaptorOpComparator that uses a list of entities that are in FK dependancy order.
     * Enable DEBUG logging to see the ordered list of entity names.
     *
     * @see com.webobjects.eoaccess.EOAdaptorOpComparator
     * @return EOAdaptorOpComparator that uses a list of entities that are in FK dependancy order
     */
    protected NSComparator adaptorOpComparator() {
        if (adaptorOpComparator == null) {
            ERXEntityFKConstraintOrder constraintOrder = new ERXEntityFKConstraintOrder();
            NSComparator entityOrderingComparator = new ERXEntityOrder.EntityInsertOrderComparator(constraintOrder);
            try {
                NSArray entityOrdering = constraintOrder.allEntities().sortedArrayUsingComparator(entityOrderingComparator);
                NSArray entityNameOrdering = (NSArray)entityOrdering.valueForKey("name");

                if (logger.isDebugEnabled()) {
                    logger.debug("Entity ordering:\n " + entityNameOrdering.componentsJoinedByString("\n"));
                }

                adaptorOpComparator = new ERXAdaptorOpComparator(entityNameOrdering);
            }
            catch (ComparisonException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }

        }
        return adaptorOpComparator;
    }



    /**
     * EODatabaseContext.Delegate method to order a list of adaptor operations.  Uses adaptorOpComparator() for the ordering.
     *
     * @param aDatabaseContext EODatabaseContext that the operations will be executed in
     * @param adaptorOperations list of operations to execute
     * @param adaptorChannel the adaptor channel these will be executed on
     *
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextWillPerformAdaptorOperations(EODatabaseContext, NSArray,EOAdaptorChannel)
     * @return operations in an order that should avoid FK constraint violations
     */
    public NSArray databaseContextWillPerformAdaptorOperations(EODatabaseContext aDatabaseContext,
                                                               NSArray adaptorOperations,
                                                               EOAdaptorChannel adaptorChannel) {
        try {
            return adaptorOperations.sortedArrayUsingComparator(adaptorOpComparator());
        }
        catch (com.webobjects.foundation.NSComparator.ComparisonException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }


    public boolean databaseContextShouldHandleDatabaseException(EODatabaseContext dbCtxt, Throwable exception)
    {
    	// Useful for debugging
    	if ( ! (exception instanceof EOGeneralAdaptorException))
    	{
        	logger.error("Unexpected non-EOGeneralAdaptorException exception", exception);
    	}
    	
    	return true;
    }
}
