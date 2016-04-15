package com.webobjects.eoaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSComparator.ComparisonException;
import com.webobjects.foundation.NSForwardException;

import er.extensions.eof.ERXEntityFKConstraintOrder;
import er.extensions.eof.ERXEntityOrder;



/**
 * <p>EODatabaseContext delegate to order adaptor operations by FK constraints.  This prevents most ordering
 * operations on databases like MS SQL that do not support deferred constraints like a real database.
 * The easiest way to use this is:</p>
 * <pre>
 * ERXDatabaseContextMulticastingDelegate.addDefaultDelegate(new ERXEntityDependencyOrderingDelegate());
 * </pre>
 * To turn this on for a Wonder application, just set this property:
 * <pre>
 * com.webobjects.eoaccess.ERXEntityDependencyOrderingDelegate.active = true
 * </pre>
 * 
 * @author chill
 */
public class ERXEntityDependencyOrderingDelegate {

	public static final String ERXEntityDependencyOrderingDelegateActiveKey = "com.webobjects.eoaccess.ERXEntityDependencyOrderingDelegate.active";
    protected NSComparator adaptorOpComparator;
    private static final Logger log = LoggerFactory.getLogger(ERXEntityDependencyOrderingDelegate.class);


    public ERXEntityDependencyOrderingDelegate() {
        super();
    }



    /**
     * Lazy creation of an EOAdaptorOpComparator that uses a list of entities that are in FK dependency order.
     * Enable DEBUG logging to see the ordered list of entity names.
     *
     * @see com.webobjects.eoaccess.EOAdaptorOpComparator
     * @return EOAdaptorOpComparator that uses a list of entities that are in FK dependency order
     */
    protected NSComparator adaptorOpComparator() {
        if (adaptorOpComparator == null) {
            ERXEntityFKConstraintOrder constraintOrder = new ERXEntityFKConstraintOrder();
            NSComparator entityOrderingComparator = new ERXEntityOrder.EntityInsertOrderComparator(constraintOrder);
            try {
                NSArray<EOEntity> entityOrdering = constraintOrder.allEntities().sortedArrayUsingComparator(entityOrderingComparator);
                NSArray<String> entityNameOrdering = (NSArray<String>)entityOrdering.valueForKey("name");

                if (log.isDebugEnabled()) {
                    log.debug("Entity ordering:\n{}", entityNameOrdering.componentsJoinedByString("\n"));
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
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextWillPerformAdaptorOperations(EODatabaseContext,NSArray,EOAdaptorChannel)
     * @return operations in an order that should avoid FK constraint violations
     */
	public NSArray<EOAdaptorOperation> databaseContextWillPerformAdaptorOperations(EODatabaseContext aDatabaseContext, 
																				   NSArray<EOAdaptorOperation> adaptorOperations, 
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
        	log.error("Unexpected non-EOGeneralAdaptorException exception", exception);
    	}
    	
    	return true;
    }
}
