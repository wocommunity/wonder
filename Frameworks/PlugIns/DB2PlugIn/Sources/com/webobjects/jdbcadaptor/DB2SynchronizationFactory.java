package com.webobjects.jdbcadaptor;

import java.util.Iterator;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSchemaSynchronization;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation._NSDictionaryUtilities;
import com.webobjects.foundation._NSStringUtilities;

/**
 * A synchronization factory usable outside EOModeler.  
 * Original code was for Postress changed for DB2
 * 
 * @author simpson
 * @author giorgio_v
 */
@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class DB2SynchronizationFactory extends EOSynchronizationFactory implements EOSchemaGeneration, EOSchemaSynchronization {
    public static final String USING_KEY = "USING";
    
    private Boolean _enableIdentifierQuoting;
    
    public DB2SynchronizationFactory(EOAdaptor adaptor) {
        super(adaptor);
    }

    private boolean enableIdentifierQuoting() {
        if(_enableIdentifierQuoting == null) {
            _enableIdentifierQuoting = Boolean.getBoolean(DB2Expression.class.getName() + ".enableIdentifierQuoting") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _enableIdentifierQuoting.booleanValue();
    }

    @Override
    protected String formatTableName(String name) {
        if (!enableIdentifierQuoting()) {
            return name;
        }
        return "\"" + name + "\"";
    }

    @Override
    protected String formatColumnName(String name) {
        if (!enableIdentifierQuoting()) {
            return name;
        }
        return "\"" + name + "\"";
    }

    @Override
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
                for (int j = 0; j < relCount; j++) {
                    EORelationship currentRelationship = ((EORelationship) relationships.objectAtIndex(j));
                    if (_shouldGenerateForeignKeyConstraints(currentRelationship)) {
                        NSArray statements = foreignKeyConstraintStatementsForRelationship(currentRelationship);
                        if (!generatedStatements.containsObject(statements.valueForKey("statement"))) {
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
        return !rel.isFlattened() && destinationEntity.externalName() != null && rel.entity().model() == destinationEntity.model();
    }

    /**
     * <code>DB2Expression</code> factory method.
     * 
     * @param entity
     *            the entity to which <code>DB2Expression</code> is to
     *            be rooted
     * @param statement
     *            the SQL statement
     * @return a <code>DB2Expression</code> rooted to
     *         <code>entity</code>
     */
    private static DB2Expression createExpression(EOEntity entity, String statement) {
        DB2Expression result = new DB2Expression(entity);
        result.setStatement(statement);
        return result;
    }

    /**
     * Generates the DB2-specific SQL statements to drop the primary key
     * support.
     * 
     * @param entityGroup
     *            an array of <code>EOEntity</code> objects
     * @return the array of SQL statements
     */
    @Override
    public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
        NSMutableSet sequenceNames = new NSMutableSet();
        NSMutableArray results = new NSMutableArray();
        int count = entityGroup.count();
        for (int i = 0; i < count; i++) {
            EOEntity entity = (EOEntity) entityGroup.objectAtIndex(i);
            String sequenceName = DB2PlugIn._sequenceNameForEntity(entity);
            if (!sequenceNames.containsObject(sequenceName)) {
                sequenceNames.addObject(sequenceName);
                String sql = "DROP SEQUENCE " + sequenceName ;
                results.addObject(createExpression(entity, sql));
            }
        }
        return results;
    }

    /**
     * Generates the DB2-specific SQL statements to drop tables.
     * 
     * @param entityGroup
     *            an array of <code>EOEntity</code> objects
     * @return the array of SQL statements
     */
    @Override
    public NSArray dropTableStatementsForEntityGroup(NSArray entityGroup) {
        NSMutableArray results = new NSMutableArray();
        int count = entityGroup.count();
        for (int i = 0; i < count; i++) {
            EOEntity entity = (EOEntity) entityGroup.objectAtIndex(i);
            // timc 2006-11-06 create result here so we can check for
            // enableIdentifierQuoting while building the statement
            DB2Expression result = new DB2Expression(entity);
            String tableName = result.sqlStringForSchemaObjectName(entity.externalName());
            if(entityUsesSeparateTable(entity)) {
                result.setStatement("DROP TABLE " + tableName );
                results.addObject(result);
            }
        }
        return results;
    }

    /**
     * Generates the DB2-specific SQL statements to enforce the foreign
     * key constraints for <code>relationship</code>.
     * 
     * @param relationship
     *            the relationship, as represented by EOF
     * @return the array of SQL statements
     */
    @Override
    public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
        NSArray superResults;
        NSMutableArray results;
        int count;
        int i;
        EOSQLExpression expression;

        results = new NSMutableArray();
        superResults = super.foreignKeyConstraintStatementsForRelationship(relationship);

        count = superResults.count();
        for (i = 0; i < count; i++) {
            expression = (EOSQLExpression) superResults.objectAtIndex(i);
            String s = expression.statement();
            s = replaceStringByStringInString(") INITIALLY DEFERRED", ") ENFORCED", s);
            expression.setStatement(s);
            results.addObject(expression);
            // timc 2006-11-06 check for enableIdentifierQuoting
            String tableName = expression.sqlStringForSchemaObjectName(expression.entity().externalName());
            NSArray columnNames = ((NSArray) relationship.sourceAttributes().valueForKey("columnName"));
            StringBuilder sbColumnNames = new StringBuilder();
            for (int j = 0; j < columnNames.count(); j++) {
                sbColumnNames.append((j == 0 ? "" : ", ") + expression.sqlStringForSchemaObjectName((String) columnNames.objectAtIndex(j)));
            }
            String indexName = externalNameForEntityWithoutSchema(relationship.entity()) + "_" + columnNames.componentsJoinedByString("_") + "_idx";
            results.addObject(createExpression(expression.entity(), "CREATE INDEX " + indexName + " ON " + tableName + "( " + sbColumnNames.toString() + " )"));
        }
        return results;
    }

    protected String externalNameForEntityWithoutSchema(EOEntity entity) {
      String externalName = entity.externalName();
      if (externalName != null) {
        int dotIndex = externalName.indexOf('.');
        if (dotIndex != -1) {
          externalName = externalName.substring(dotIndex + 1);
        }
      }
      return externalName;
    }
    
    /**
     * Generates the DB2-specific SQL statements to enforce primary key
     * constraints.
     * 
     * @param entityGroup
     *            an array of <code>EOEntity</code> objects
     * @return the array of SQL statements
     */
    @Override
    public NSArray primaryKeyConstraintStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;
        NSArray priKeyAttributes;
        EOAttribute priKeyAttribute;
        int priKeyAttributeCount;
        int j;

        results = new NSMutableArray();
        count = entityGroup.count();
        for (i = 0; i < count; i++) {
            entity = (EOEntity) entityGroup.objectAtIndex(i);
            if (!entityUsesSeparateTable(entity))
                continue;
            // timc 2006-11-06 create result here so we can check for
            // enableIdentifierQuoting while building the statement
            DB2Expression result = new DB2Expression(entity);
            String constraintName = result.sqlStringForSchemaObjectName(externalNameForEntityWithoutSchema(entity) + "_pk");
            String tableName = result.sqlStringForSchemaObjectName(entity.externalName());

            StringBuilder statement = new StringBuilder("ALTER TABLE ");
            statement.append(tableName);
            statement.append(" ADD CONSTRAINT ");
            statement.append(constraintName);
            statement.append(" PRIMARY KEY (");
            priKeyAttributes = entity.primaryKeyAttributes();
            priKeyAttributeCount = priKeyAttributes.count();
            for (j = 0; j < priKeyAttributeCount; j++) {
                priKeyAttribute = (EOAttribute) priKeyAttributes.objectAtIndex(j);
                String attributeName = result.sqlStringForAttribute(priKeyAttribute);
                statement.append(attributeName);
                if (j < priKeyAttributeCount - 1) {
                    statement.append(", ");
                } else {
                    statement.append(')');
                }
            }
            result.setStatement(statement.toString());
            results.addObject(result);
        }
        return results;
    }
    
    /**
     * Returns true if Entity Modeler is running the operation on this model.
     * 
     * @param model the model to check
     * @return true if Entity Modeler is running
     */
    protected boolean isInEntityModeler(EOModel model) {
      boolean inEntityModeler = false;
      if (model != null) {
        NSDictionary userInfo = model.userInfo();
        NSDictionary entityModelerDict = (NSDictionary) userInfo.objectForKey("_EntityModeler");
        if (entityModelerDict != null) {
          Boolean inEntityModelerBoolean = (Boolean)entityModelerDict.objectForKey("inEntityModeler");
          if (inEntityModelerBoolean != null && inEntityModelerBoolean.booleanValue()) {
            inEntityModeler = inEntityModelerBoolean.booleanValue();
          }
        }
      }
      return inEntityModeler;
    }

    /**
     * Generates the DB2-specific SQL statements to create the primary
     * key support.
     * 
     * @param entityGroup
     *            an array of <code>EOEntity</code> objects
     * @return the array of SQL statements
     */
    @Override
    public NSArray primaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;
        NSArray priKeyAttributes;
        EOAttribute priKeyAttribute;
        String sequenceName;

        NSMutableSet sequenceNames = new NSMutableSet();
        results = new NSMutableArray();
        count = entityGroup.count();
        for (i = 0; i < count; i++) {
            entity = (EOEntity) entityGroup.objectAtIndex(i);
            priKeyAttributes = entity.primaryKeyAttributes();
            if (priKeyAttributes.count() == 1) {
                priKeyAttribute = (EOAttribute) priKeyAttributes.objectAtIndex(0);
                
                // Q: Don't create a sequence for non number primary keys
                if (priKeyAttribute.adaptorValueType() != EOAttribute.AdaptorNumberType) {
                	continue;
                }
                String sql;

                sequenceName = DB2PlugIn._sequenceNameForEntity(entity);
                if (!sequenceNames.containsObject(sequenceName)) {
                    sequenceNames.addObject(sequenceName);
                    // timc 2006-11-06 create result here so we can check for
                    // enableIdentifierQuoting while building the statement

                    sql = "CREATE SEQUENCE " + sequenceName;
                    results.addObject(createExpression(entity, sql));

                }
            }
        }
        return results;
    }

    public static boolean entityUsesSeparateTable(EOEntity entity) {
        if (entity.parentEntity() == null)
            return true;
        EOEntity parent = entity.parentEntity();
        while (parent != null) {
            if (!entity.externalName().equals(parent.externalName()))
                return true;
            entity = parent;
            parent = entity.parentEntity();
        }
        return false;
    }
    
    /**
     * Quote table name if necessary
     */
    @Override
    public NSArray createTableStatementsForEntityGroup(NSArray entityGroup) {
		NSMutableSet columnNames = new NSMutableSet();
		StringBuffer aStatement = new StringBuffer(128);
		if (entityGroup != null && entityGroup.count() > 0) {
			EOSQLExpression sqlExpr = _expressionForEntity((EOEntity) entityGroup.objectAtIndex(0));
			for (Iterator entityIterator = entityGroup.iterator(); entityIterator.hasNext();) {
				EOEntity entity = (EOEntity) entityIterator.next();
				Iterator attributeIterator = entity.attributes().iterator();
				while (attributeIterator.hasNext()) {
					EOAttribute attribute = (EOAttribute) attributeIterator.next();
					String columnName = attribute.columnName();
					if (!attribute.isDerived() && !attribute.isFlattened() && columnName != null && columnName.length() > 0 && !columnNames.contains(columnName)) {
						sqlExpr.appendItemToListString(_columnCreationClauseForAttribute(attribute), aStatement);
						columnNames.addObject(columnName);
					}
				}
			}
			return new NSArray(_expressionForString(new StringBuilder().append("CREATE TABLE ").append(formatTableName(((EOEntity) entityGroup.objectAtIndex(0)).externalName())).append(" (").append(aStatement.toString()).append(')').toString()));
		}
		return new NSArray();
	}
    
    /**
	 * Replaces a given string by another string in a string.
	 * 
	 * @param old
	 *            string to be replaced
	 * @param newString
	 *            to be inserted
	 * @param buffer
	 *            string to have the replacement done on it
	 * @return string after having all of the replacement done.
	 */
    public static String replaceStringByStringInString(String old, String newString, String buffer) {
        int begin, end;
        int oldLength = old.length();
        int length = buffer.length();
        StringBuilder convertedString = new StringBuilder(length + 100);

        begin = 0;
        while (begin < length) {
            end = buffer.indexOf(old, begin);
            if (end == -1) {
                convertedString.append(buffer.substring(begin));
                break;
            }
            if (end == 0)
                convertedString.append(newString);
            else {
                convertedString.append(buffer.substring(begin, end));
                convertedString.append(newString);
            }
            begin = end + oldLength;
        }
        return convertedString.toString();
    }
    
    // I blame statementstToConvertColumnType for not taking a damn EOAttribute for
    // having to steal this from EOSQLExpression
    public String columnTypeStringForAttribute(EOAttribute attribute) {
      if (attribute.precision() != 0) {
        String precision = String.valueOf(attribute.precision());
        String scale = String.valueOf(attribute.scale());
        return _NSStringUtilities.concat(attribute.externalType(), "(", precision, ",", scale, ")");
      }
      if (attribute.width() != 0) {
        String width = String.valueOf(attribute.width());
        return _NSStringUtilities.concat(attribute.externalType(), "(", width, ")");
      }
      return attribute.externalType();
    }

    @Override
    public NSArray statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, NSDictionary nsdictionary) {
      NSArray statements;
      if (allowsNull) {
        statements = new NSArray(_expressionForString("alter table " + formatTableName(tableName) + " alter column " + formatColumnName(columnName) + " drop not null"));
      } else {
        statements = new NSArray(_expressionForString("alter table " + formatTableName(tableName) + " alter column " + formatColumnName(columnName) + " set not null"));
      }
      return statements;
    }

    @Override
    public NSArray statementsToConvertColumnType(String columnName, String tableName, ColumnTypes oldType, ColumnTypes newType, NSDictionary options) {
      EOAttribute attr = new EOAttribute();
      attr.setName(columnName);
      attr.setColumnName(columnName);
      attr.setExternalType(newType.name());
      attr.setScale(newType.scale());
      attr.setPrecision(newType.precision());
      attr.setWidth(newType.width());

      String usingClause = "";
      String columnTypeString = columnTypeStringForAttribute(attr);
      if (options != null) {
        String usingExpression = (String) options.objectForKey(DB2SynchronizationFactory.USING_KEY);
        if (usingExpression != null) {
          usingClause = " USING " + usingExpression;
        }
      }
      NSArray statements = new NSArray(_expressionForString("alter table " + formatTableName(tableName) + " alter column " + formatColumnName(columnName) + "set data type " + columnTypeString + usingClause));
      return statements;
    }

    @Override
    public NSArray statementsToRenameColumnNamed(String columnName, String tableName, String newName, NSDictionary nsdictionary) {
      return new NSArray(_expressionForString("alter table " + formatTableName(tableName) + " rename column " + formatColumnName(columnName) + " to " + formatColumnName(newName)));
    }

    @Override
    public NSArray statementsToInsertColumnForAttribute(EOAttribute attribute, NSDictionary options) {
      String clause = _columnCreationClauseForAttribute(attribute);
      return new NSArray(_expressionForString("alter table " + formatTableName(attribute.entity().externalName()) + " add " + clause));
    }

    @Override
    public NSArray statementsToRenameTableNamed(String tableName, String newName, NSDictionary options) {
    	return new NSArray(_expressionForString("alter table " + formatTableName(tableName) + " rename to " + formatTableName(newName)));
    }
    
    @Override
    public NSArray statementsToDeleteColumnNamed(String columnName, String tableName, NSDictionary options) {
    	return new NSArray(_expressionForString("alter table " + formatTableName(tableName) + " drop column " + formatTableName(columnName) + " cascade"));
    }

/*
    public StringBuffer addCreateClauseForAttribute(EOAttribute eoattribute) {
      EOSQLExpression expression = _expressionForEntity(eoattribute.entity());
      expression.addCreateClauseForAttribute(eoattribute);
      return new StringBuffer(expression.listString());
    }

    public String _columnCreationClauseForAttribute(EOAttribute attribute) {
      return addCreateClauseForAttribute(attribute).toString();
    }
*/

    @Override
    public String schemaCreationScriptForEntities(NSArray allEntities, NSDictionary options)
    {
/* 741*/        StringBuffer result = new StringBuffer();
/* 744*/        if(options == null)
/* 745*/            options = NSDictionary.EmptyDictionary;
/* 747*/        NSArray statements = schemaCreationStatementsForEntities(allEntities, options);
/* 748*/        int i = 0;
/* 748*/        for(int count = statements.count(); i < count; i++)
/* 749*/            appendExpressionToScript((EOSQLExpression)statements.objectAtIndex(i), result);

/* 751*/        return result.toString();
    }

    @Override
    public NSArray schemaCreationStatementsForEntities(NSArray allEntities, NSDictionary options)
    {
/* 879*/        NSMutableArray result = new NSMutableArray();
/* 880*/        if(allEntities == null || allEntities.count() == 0)
/* 881*/            return result;
/* 883*/        if(options == null)
/* 884*/            options = NSDictionary.EmptyDictionary;
/* 889*/        NSDictionary connectionDictionary = ((EOEntity)allEntities.lastObject()).model().connectionDictionary();
/* 892*/        boolean createDatabase = _NSDictionaryUtilities.boolValueForKeyDefault(options, "createDatabase", false);
/* 893*/        boolean dropDatabase = _NSDictionaryUtilities.boolValueForKeyDefault(options, "dropDatabase", false);
/* 895*/        if(createDatabase || dropDatabase)
        {
/* 896*/            boolean adminCommentsNeeded = false;
/* 897*/            NSArray dropDatabaseStatements = null;
/* 898*/            NSArray createDatabaseStatements = null;
/* 900*/            if(dropDatabase)
            {
/* 901*/                dropDatabaseStatements = dropDatabaseStatementsForConnectionDictionary(connectionDictionary, null);
/* 903*/                if(dropDatabaseStatements == null)
/* 904*/                    dropDatabaseStatements = new NSArray(_expressionForString("/* The 'Drop Database' option is unavailable. */"));
/* 907*/                else
/* 907*/                    adminCommentsNeeded = true;
            }
/* 911*/            if(createDatabase)
            {
/* 912*/                createDatabaseStatements = createDatabaseStatementsForConnectionDictionary(connectionDictionary, null);
/* 914*/                if(createDatabaseStatements == null)
/* 915*/                    createDatabaseStatements = new NSArray(_expressionForString("/* The 'Create Database' option is unavailable. */"));
/* 918*/                else
/* 918*/                    adminCommentsNeeded = true;
            }
/* 922*/            if(adminCommentsNeeded)
/* 923*/                result.addObject(_expressionForString("/* connect as an administrator */"));
/* 926*/            if(dropDatabaseStatements != null)
/* 927*/                result.addObjectsFromArray(dropDatabaseStatements);
/* 930*/            if(createDatabaseStatements != null)
/* 931*/                result.addObjectsFromArray(createDatabaseStatements);
/* 934*/            if(adminCommentsNeeded)
/* 935*/                result.addObject(_expressionForString("/* connect as the user from the connection dictionary */"));
        }
/* 939*/        if(_NSDictionaryUtilities.boolValueForKeyDefault(options, "dropPrimaryKeySupport", true))
        {
/* 940*/            NSArray entityGroups = primaryKeyEntityGroupsForEntities(allEntities);
/* 941*/            result.addObjectsFromArray(dropPrimaryKeySupportStatementsForEntityGroups(entityGroups));
        }
/* 944*/        if(_NSDictionaryUtilities.boolValueForKeyDefault(options, "dropTables", true))
        {
/* 945*/            NSArray entityGroups = tableEntityGroupsForEntities(allEntities);
/* 946*/            result.addObjectsFromArray(dropTableStatementsForEntityGroups(entityGroups));
        }
/* 949*/        if(_NSDictionaryUtilities.boolValueForKeyDefault(options, "createTables", true))
        {
/* 950*/            NSArray entityGroups = tableEntityGroupsForEntities(allEntities);
/* 951*/            result.addObjectsFromArray(createTableStatementsForEntityGroups(entityGroups));
        }
/* 954*/        if(_NSDictionaryUtilities.boolValueForKeyDefault(options, "createPrimaryKeySupport", true))
        {
/* 955*/            NSArray entityGroups = primaryKeyEntityGroupsForEntities(allEntities);
/* 956*/            result.addObjectsFromArray(primaryKeySupportStatementsForEntityGroups(entityGroups));
                }
/* 959*/        if(_NSDictionaryUtilities.boolValueForKeyDefault(options, "primaryKeyConstraints", true))
                {
/* 960*/            NSArray entityGroups = tableEntityGroupsForEntities(allEntities);
/* 961*/            result.addObjectsFromArray(primaryKeyConstraintStatementsForEntityGroups(entityGroups));
                }
/* 964*/        if(_NSDictionaryUtilities.boolValueForKeyDefault(options, "foreignKeyConstraints", false))
                {
/* 965*/            NSArray entityGroups = tableEntityGroupsForEntities(allEntities);
/* 966*/            int i = 0;
/* 966*/            for(int iCount = entityGroups.count(); i < iCount; i++)
/* 967*/                result.addObjectsFromArray(_foreignKeyConstraintStatementsForEntityGroup((NSArray)entityGroups.objectAtIndex(i)));

                }
/* 970*/        return result;
            }

}
