package com.webobjects.jdbcadaptor;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
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
     * Name of the driver.
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
     * Returns a "pure java" synchronization factory.
     * Useful for testing purposes.
     */
    public EOSynchronizationFactory createSynchronizationFactory() {
        try {
             return new PostgresqlSynchronizationFactory(adaptor());
        } catch ( Exception e ) {
            throw new NSForwardException(e, "Couldn't create synchronization factory");
        }
    }

    /**                                                                                                                                                         
     * Expression class to create. We have custom code, so we need our own class.                                                                               
     */
    public Class defaultExpressionClass() {
        return PostgresqlExpression.class;
    }
    
    /** 
     * Overrides the parent implementation to provide a more efficient mechanism for generating primary keys,
     * while generating the primary key support on the fly.
     *
     * @param count the batch size
     * @param entity the entity requesting primary keys
     * @param n open JDBCChannel
     * @return An NSArray of NSDictionary where each dictionary corresponds to a unique  primary key value
     */
    public NSArray newPrimaryKeys (int count, EOEntity entity, JDBCChannel channel) {
        NSMutableArray results = null;
        if( entity.primaryKeyAttributes().count() == 1) {
            EOAttribute attribute = (EOAttribute) entity.primaryKeyAttributes().lastObject();
            if(attribute.adaptorValueType() == EOAttribute.AdaptorNumberType) {
                String sequenceName = entity.primaryKeyRootName() + "_SEQ";
                PostgresqlExpression expression = new PostgresqlExpression(entity);
                expression.setStatement("SELECT SETVAL( '"+ sequenceName +"', CURRVAL('"+ sequenceName +"') + "+ count +")");
                results = new NSMutableArray();
                attribute = ((EOAttribute)entity.primaryKeyAttributes().objectAtIndex(0));
                try {
                    channel.evaluateExpression(expression);
                } catch( Exception seqNotInitializedEx ) {
                    // Throws an exception if it's the first time we access the sequence in a session
                    PostgresqlExpression preExp = new PostgresqlExpression(entity);
                    preExp.setStatement("SELECT NEXTVAL('" + sequenceName + "')");
                    try {
                        channel.evaluateExpression(preExp);
                    } catch( Exception seqAbsentEx ) {
                        channel.cancelFetch();
                        EOSynchronizationFactory f = createSynchronizationFactory();
                        NSArray statements = f.primaryKeySupportStatementsForEntityGroup( new NSArray( entity ) );
                        int stmCount = statements.count();
                        for( int i = 0; i < stmCount; i++ ) {
                            channel.evaluateExpression( (EOSQLExpression) statements.objectAtIndex(i) );
                        }
                        channel.evaluateExpression(preExp);
                    }
                    channel.cancelFetch();
                    channel.evaluateExpression(expression);
                }
                try {
                    NSDictionary row = channel.fetchRow();
                    channel.cancelFetch();
                    long maxValue = ((Number) row.objectForKey("SETVAL")).longValue();
                    String attrName = attribute.name();
                    for( int i = 0; i < count; i++ ) {
                        results.addObject(new NSDictionary( new Long(maxValue - count + i), attrName));
                    }            
                } catch( Exception e ) {
                    throw new NSForwardException(e, "Couldn't get new primary key for entity: " + entity.name());
                }
            }
        }
        return results;
    }
    
    /**
     * Utility method that returns the name of the sequence associated
     * with <code>entity</code>
     *
     * @param entity    the entity
     * @return  the name of the sequence
     */
    protected static String sequenceNameForEntity(EOEntity entity) {
        return entity.primaryKeyRootName() + "_SEQ";
    }
    
}
