package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
/**
 * A synchronization factory usable outside EOModeler
 * @author giorgio_v
 */
public class PostgresqlSynchronizationFactory extends EOSynchronizationFactory implements EOSchemaGeneration, EOSchemaSynchronization {

    public PostgresqlSynchronizationFactory(EOAdaptor adaptor) {
        super(adaptor);
    }

    
    public NSArray _foreignKeyConstraintStatementsForEntityGroup(NSArray group) {
	if (group == null)
	    return NSArray.EmptyArray;
	NSMutableArray result = new NSMutableArray();
	NSMutableSet generatedStatements = new NSMutableSet();
	int i = 0;
	for (int groupCount = group.count(); i < groupCount; i++) {
	    EOEntity currentEntity = (EOEntity) group.objectAtIndex(i);
	    if (currentEntity.externalName() != null) {
		NSArray relationships = currentEntity.relationships();
		    int relCount = relationships.count();
		    for(int j = 0; j < relCount; j++) {
			EORelationship currentRelationship
                        = ((EORelationship)
                           relationships.objectAtIndex(j));
			EOEntity destinationEntity
			    = currentRelationship.destinationEntity();
			if (_shouldGenerateForeignKeyConstraints(currentRelationship)) {
                            NSArray statements = foreignKeyConstraintStatementsForRelationship(currentRelationship);
                            if(!generatedStatements.containsObject(statements.valueForKey("statement"))) {
                                result.addObjectsFromArray(statements);
                                generatedStatements.addObject(statements.valueForKey("statement"));
                            } 
			}
		    }
	    }
	}
	return result;
    }
    
    protected boolean _shouldGenerateForeignKeyConstraints(EORelationship rel) {
        EOEntity destinationEntity = rel.destinationEntity();
        return !rel.isFlattened()
            && destinationEntity.externalName() != null
            && rel.entity().model() == destinationEntity.model()
            && (destinationEntity.subEntities() == null
                || destinationEntity.subEntities().count() == 0
                || destinationEntity.subEntities().containsObject(rel.entity()));          
    }
    
    /**
     * <code>PostgresqlExpression</code> factory method.
     *
     * @param entity    the entity to which <code>PostgresqlExpression</code> is to be rooted
     * @param statement the SQL statement
     * @return a <code>PostgresqlExpression</code> rooted to <code>entity</code>
     */
    private static PostgresqlExpression createExpression( EOEntity entity, String statement ) {
        PostgresqlExpression result = new PostgresqlExpression( entity );
        result.setStatement( statement );
        return result;
    }
        
    /**
     * Generates the PostgreSQL-specific SQL statements to drop the primary key support.
     *
     * @param entityGroup   an array of <code>EOEntity</code> objects
     * @return  the array of SQL statements
     */
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
    
    /**
     * Generates the PostgreSQL-specific SQL statements to drop tables.
     *
     * @param entityGroup   an array of <code>EOEntity</code> objects
     * @return  the array of SQL statements
     */    
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

    /**
     * Generates the PostgreSQL-specific SQL statements to enforce
     * the foreign key constraints for <code>relationship</code>.
     *
     * @param relationship  the relationship, as represented by EOF
     * @return  the array of SQL statements
     */    
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
            String indexName = relationship.entity().externalName() + "_" +
                ( (NSArray) relationship.sourceAttributes().valueForKey( "columnName" ) ).componentsJoinedByString( "_" ) +"_IDX";
            results.addObject( createExpression( expression.entity(), "CREATE INDEX "+ indexName +" ON "+
                                                 tableName +"( "+ columNames.componentsJoinedByString( ", " ) +" )" ) );
        }
        return results;
    }
    
    /**
     * Generates the PostgreSQL-specific SQL statements to enforce
     * primary key constraints.
     *
     * @param entityGroup   an array of <code>EOEntity</code> objects
     * @return  the array of SQL statements
     */        
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
    
    /**
     * Generates the PostgreSQL-specific SQL statements to create the primary key support.
     *
     * @param entityGroup   an array of <code>EOEntity</code> objects
     * @return  the array of SQL statements
     */        
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
                sql = "CREATE FUNCTION EOF_TMP_ID_MAX() RETURNS " + priKeyAttribute.externalType() + " AS \n'"
                    + "SELECT MAX(" + priKeyAttribute.columnName() +") FROM " + entity.externalName()
                    + "'\n    LANGUAGE 'sql'";
                results.addObject(createExpression(entity, sql));
                
                sequenceName = PostgresqlPlugIn.sequenceNameForEntity(entity);
                sql = "CREATE SEQUENCE " + sequenceName;
                results.addObject(createExpression(entity, sql));
                
                sql = "SELECT SETVAL('" + sequenceName + "', EOF_TMP_ID_MAX() ) INTO TEMP EOF_TMP_TABLE";
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
