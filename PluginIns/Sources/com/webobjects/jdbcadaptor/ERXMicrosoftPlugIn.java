package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.eoaccess.synchronization.EOSchemaGenerationOptions;
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
    public static class MicrosoftSynchronizationFactory extends EOSynchronizationFactory {


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



        public NSArray primaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
            String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
            NSMutableArray statements = new NSMutableArray();
            statements.addObject(_expressionForString((new StringBuilder()).append("CREATE TABLE ").append(pkTable).append(" (NAME CHAR(40) PRIMARY KEY, PK INT)").toString()));
            return statements;
        }

        public NSArray dropPrimaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
            return new NSArray(_expressionForString((new StringBuilder()).append("DROP TABLE ").append(((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName()).toString()));
        }

        public NSArray _statementsToDeleteTableNamedOptions(String tableName, EOSchemaGenerationOptions options) {
            return new NSArray(_expressionForString((new StringBuilder()).append("DROP TABLE ").append(tableName).toString()));
        }

        public NSArray dropTableStatementsForEntityGroup(NSArray entityGroup) {
            if (entityGroup == null) {
                return NSArray.emptyArray();
            }
            else {
                com.webobjects.eoaccess.EOSQLExpression sqlExpr = _expressionForString((new StringBuilder()).append("DROP TABLE ").append(((EOEntity) entityGroup.objectAtIndex(0)).externalName()).toString());
                return new NSArray(sqlExpr);
            }
        }

        public boolean supportsSchemaSynchronization() {
            return false;
        }

        public NSArray _statementsToDropPrimaryKeyConstraintsOnTableNamed(String tableName) {
            return NSArray.emptyArray();
        }

        public NSArray statementsToRenameTableNamed(String tableName, String newName, EOSchemaGenerationOptions options) {
            String aTableName = tableName;
            int lastDot = aTableName.lastIndexOf('.');
            if (lastDot != -1) {
                aTableName = aTableName.substring(lastDot + 1);
            }
            String aNewName = newName;
            lastDot = aNewName.lastIndexOf('.');
            if (lastDot != -1) {
                aNewName = aNewName.substring(lastDot + 1);
            }
            return new NSArray(_expressionForString((new StringBuilder()).append("execute sp_rename ").append(aTableName).append(", ").append(aNewName).toString()));
        }

        public MicrosoftSynchronizationFactory(EOAdaptor adaptor) {
            super(adaptor);
        }

    }

}
