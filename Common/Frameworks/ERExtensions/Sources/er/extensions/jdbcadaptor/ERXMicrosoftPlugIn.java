package er.extensions.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import com.webobjects.foundation.NSComparator.*;
import com.webobjects.jdbcadaptor.JDBCAdaptor;


import er.extensions.eoaccess.entityordering.*;



/**
 * <p>Sub-class of <code>com.webobjects.jdbcadaptor.MicrosoftPlugIn</code> to customize its functionality.
 * Use this by including this line in your connnection dictionary:</p>
 *
 * <pre>
 * plugin=er.extensions.jdbcadaptor.ERXMicrosoftPlugIn;
 * </pre>
 * @see com.webobjects.jdbcadaptor.MicrosoftPlugIn
 * @author chill
 */
public class ERXMicrosoftPlugIn extends com.webobjects.jdbcadaptor.MicrosoftPlugIn {

    /**
     * @param adaptor the JDBCAdaptor that this adaptor configures
     */
    public ERXMicrosoftPlugIn(JDBCAdaptor adaptor) {
        super(adaptor);
    }


    public EOSynchronizationFactory createSynchronizationFactory() {
        return new MicrosoftSynchronizationFactory(_adaptor);
    }


    /**
     * Sub-class of <code>com.webobjects.jdbcadaptor.MicrosoftPlugIn.MicrosoftSynchronizationFactory</code> to customize
     * SQL generation.
     *
     * @see com.webobjects.jdbcadaptor.MicrosoftPlugIn.MicrosoftSynchronizationFactory
     * @see com.webobjects.eoaccess.EOSynchronizationFactory
     */
    public static class MicrosoftSynchronizationFactory extends com.webobjects.jdbcadaptor.MicrosoftPlugIn.MicrosoftSynchronizationFactory {

        public MicrosoftSynchronizationFactory(EOAdaptor adaptor) {
            super(adaptor);
        }


        /**
         * Uses ERXEntityFKConstraintOrder to order the entities before generating the DROP TABLE statements as MSSQL does not
         * support deferred constraints.  Hence the tables need to be dropped so as to avoid foreign key constraint violations.
         *
         * @see com.webobjects.eoaccess.EOSynchronizationFactory#dropTableStatementsForEntityGroups(com.webobjects.foundation.NSArray)
         *
         * @param entityGroups array of arrays each containing one EOEntity
         * @return SQL to drop the tables for these entities in an order that avoids foreign key constraint violations
         */
        public NSArray dropTableStatementsForEntityGroups(NSArray entityGroups) {
            // SQL generation from Entity Modeler does not use the EOModelGroup.defaultGroup
            ERXEntityFKConstraintOrder constraintOrder = new ERXEntityFKConstraintOrder(ERXJDBCPlugInUtilities.modelGroupForEntityGroups(entityGroups));
            NSComparator entityOrderingComparator = new ERXJDBCPlugInUtilities.EntityGroupDeleteOrderComparator(constraintOrder);
            try {
                return super.dropTableStatementsForEntityGroups(entityGroups.sortedArrayUsingComparator(entityOrderingComparator));
            }
            catch (ComparisonException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
        }
    }

}
