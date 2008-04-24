package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;

public class DerbyPlugIn extends JDBCPlugIn {

    private static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String DRIVER_NAME = "Derby";

    static {
        setPlugInNameForSubprotocol(DerbyPlugIn.class.getName(), "derby");
    }
    
    public static class DerbyExpression extends JDBCExpression {
        // more to come
        public DerbyExpression(EOEntity entity) {
            super(entity);
        }
    }

    public static class DerbySynchronizationFactory extends EOSynchronizationFactory {
        
        public NSArray primaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
            String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
            return new NSArray(_expressionForString("create table " + pkTable + " (name char(40) primary key, pk INT)"));
        }

        public NSArray dropPrimaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
            String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
            return new NSArray(_expressionForString("drop table " + pkTable));
        }

        public NSArray _statementsToDropPrimaryKeyConstraintsOnTableNamed(String tableName) {
            return new NSArray(_expressionForString("alter table " + tableName + " drop primary key"));
        }

        public boolean supportsSchemaSynchronization() {
            return true;
        }

        public DerbySynchronizationFactory(EOAdaptor adaptor) {
            super(adaptor);
        }
    }

    public DerbyPlugIn(JDBCAdaptor adaptor) {
        super(adaptor);
    }

    public String defaultDriverName() {
        return DRIVER_CLASS_NAME;
    }

    public String databaseProductName() {
        return DRIVER_NAME;
    }

    public String name() {
        return DRIVER_NAME;
    }

    public EOSynchronizationFactory createSynchronizationFactory() {
        return new DerbySynchronizationFactory(adaptor());
    }

}
