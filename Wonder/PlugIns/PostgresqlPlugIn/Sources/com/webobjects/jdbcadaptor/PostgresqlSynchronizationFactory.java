package com.webobjects.jdbcadaptor;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

public class PostgresqlSynchronizationFactory extends EOSynchronizationFactory implements EOSchemaGeneration, EOSchemaSynchronization {

    public PostgresqlSynchronizationFactory(EOAdaptor adaptor) {
        super(adaptor);
    }

    private static PostgresqlExpression createExpression( EOEntity anEntity, String statement ) {
        PostgresqlExpression result = new PostgresqlExpression( anEntity );
        result.setStatement( statement );
        return result;
    }
    
    public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            String sql = "DROP SEQUENCE " + PostgresqlPlugIn.sequenceNameForEntity(entity);
            results.addObject(createExpression(entity, sql));
        }
        return results;
    }
    
    public NSArray dropTableStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            results.addObject( createExpression( entity, "DROP TABLE " + entity.externalName() ) );
        }
        return results;
    }

    public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
        NSArray superResults;
        NSMutableArray results;
        int count;
        int i;
        EOSQLExpression expression;

        results = new NSMutableArray();
        superResults = super.foreignKeyConstraintStatementsForRelationship(relationship);
        count = superResults.count();
        for ( i = 0 ; i < count ; i++ ) {
            expression = (EOSQLExpression) superResults.objectAtIndex(i);
            results.addObject( expression );
            String tableName = expression.entity().externalName();
            NSArray columNames = ( (NSArray) relationship.sourceAttributes().valueForKey( "columnName" ) );
            results.addObject( createExpression( expression.entity(), "CREATE INDEX "+ indexNameForRelationship( null, relationship ) +" ON "+
                                                 tableName +"( "+ columNames.componentsJoinedByString( ", " ) +" )" ) );
        }
        return results;
    }
    
    private String indexNameForRelationship( String alternativeTableName, EORelationship relationship ) {
        String tableName = alternativeTableName != null ? alternativeTableName : relationship.entity().externalName();
        String columnNames = ( (NSArray) relationship.sourceAttributes().valueForKey( "columnName" ) ).componentsJoinedByString( "_" );
        return tableName +"_"+ columnNames +"_IDX";
    }

    public NSArray primaryKeyConstraintStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;
        String statement;
        NSArray priKeyAttributes;
        EOAttribute priKeyAttribute;
        int priKeyAttributeCount;
        int j;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            statement = "ALTER TABLE " + entity.externalName() + " ADD CONSTRAINT " + entity.externalName() + "_PK PRIMARY KEY (";
            priKeyAttributes = entity.primaryKeyAttributes();
            priKeyAttributeCount = priKeyAttributes.count();
            for ( j = 0 ; j < priKeyAttributeCount ; j++ ) {
                priKeyAttribute = (EOAttribute)priKeyAttributes.objectAtIndex(j);
                statement += priKeyAttribute.columnName();
                if ( j < priKeyAttributeCount - 1 ) {
                    statement += ", ";
                } else {
                    statement += ")";
                }
            }
            results.addObject( createExpression( entity, statement) );
        }
        return results;
    }
    
    public NSArray primaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;
        NSArray priKeyAttributes;
        EOAttribute priKeyAttribute;
        String sequenceName;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            priKeyAttributes = entity.primaryKeyAttributes();
            if ( priKeyAttributes.count() == 1 ) {
                priKeyAttribute = (EOAttribute) priKeyAttributes.objectAtIndex(0);
                String sql;
                sql = "CREATE FUNCTION EOF_TMP_ID_MAX() RETURNS " + priKeyAttribute.externalType() + "AS \n'"
                    + "SELECT MAX(" + priKeyAttribute.columnName() +") FROM " + entity.externalName()
                    + "'\n    LANGUAGE 'sql'";
                results.addObject(createExpression(entity, sql));

                sequenceName = PostgresqlPlugIn.sequenceNameForEntity(entity);
                sql = "CREATE SEQUENCE " + sequenceName;
                results.addObject(createExpression(entity, sql));

                sql = "SELECT SETVAL('" + sequenceName + "', EOF_TMP_ID_MAX() + 1 ) INTO TEMP EOF_TMP_TABLE";
                results.addObject(createExpression(entity, sql));

                sql = "DROP TABLE EOF_TMP_TABLE";
                results.addObject(createExpression(entity, sql));

                sql = "DROP FUNCTION EOF_TMP_ID_MAX()";
                results.addObject(createExpression(entity, sql));

                sql =  "ALTER TABLE "+ entity.externalName() +" ALTER COLUMN "+ priKeyAttribute.columnName() +" SET DEFAULT nextval( '"+ sequenceName+ "' )" ;
                results.addObject(createExpression(entity, sql));
            }
        }
        return results;
    }
}
