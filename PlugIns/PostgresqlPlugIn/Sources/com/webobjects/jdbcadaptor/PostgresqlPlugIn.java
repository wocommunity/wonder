package com.webobjects.jdbcadaptor;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.jdbcadaptor.*;

/**
 * WO runtime plugin with support for Postgresql.
 * @author ak
 */
public class PostgresqlPlugIn extends JDBCPlugIn {
    /**
     * Designated constructor.
     */
    public PostgresqlPlugIn(JDBCAdaptor adaptor) {
        super(adaptor);
    }

    /**
     * Name of our driver.
     */
    public String defaultDriverName() {
        return "org.postgresql.Driver";
    }

    /**
     * Name of the database.
     */
    public String databaseProductName() {
        return "Postgresql";
    }

    /**
     * Expression class to create. We have custom code, so we need our own class.
     */
    public Class defaultExpressionClass() {
        return PostgresqlExpression.class;
    }


    /**
     * Overridden to create a subclass of our synchronization factory.
     */
    public EOSynchronizationFactory createSynchronizationFactory() {
        return new PostgresqlSynchronizationFactory(adaptor());
    }

    protected static String sequenceNameForEntity(EOEntity entity) {
        return entity.primaryKeyRootName() + "_SEQ";
    }
    
    /**
     * Creates a sequence for the supplied entity.
     */
    //ENHANCEME get the starting number from the pk of the entity
    private void createSequence(EOEntity entity, JDBCChannel channel) {
        EOSQLExpression expression = expressionFactory().createExpression(entity);
        expression.setStatement("create sequence " + PostgresqlPlugIn.sequenceNameForEntity(entity));
        channel.evaluateExpression(expression);
        channel.cancelFetch();
    }

    /**
     * Tries to get the specified number of PKs from the database,
     * creates appropriate sequences on the fly if they don't exist, so you don't need a
     * EOModeler plugin. 
     */
    public NSArray newPrimaryKeys(int count, EOEntity entity, JDBCChannel channel) {
        NSMutableArray results = null;
        if(entity.primaryKeyAttributes().count() == 1) {
            EOAttribute attribute = (EOAttribute)entity.primaryKeyAttributes().lastObject();
            if(attribute.adaptorValueType() == EOAttribute.AdaptorNumberType) {
                String pkName = attribute.name();
                String sequenceSQL = "select nextval('" + PostgresqlPlugIn.sequenceNameForEntity(entity) +"')";
                results = new NSMutableArray();
                EOSQLExpression expression = expressionFactory().createExpression(entity);
                expression.setStatement(sequenceSQL);
                for(int i = 0; i < count; i++ ) {
                    Object pk = null;
                    for(int tries = 0; tries < 2 && pk != null; tries++) {
                        try {
                            channel.evaluateExpression(expression);
                            channel.setAttributesToFetch(channel.describeResults());
                            NSDictionary row = channel.fetchRow();
                            channel.cancelFetch();
                            pk = row.objectForKey(row.allKeys().lastObject());
                            results.addObject(new NSDictionary(pk, pkName));
                        } catch(Exception ex) {
                            if(tries == 1) {
                                throw new NSForwardException(ex, "Couldn't get new primary key: " + entity.name());
                            }
                            createSequence(entity, channel);
                        }
                    }
                }
            }
        }
        return results;
    }
}
