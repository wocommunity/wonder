//
//  ERXEOAccessUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Sat Feb 22 2003.
//
package er.extensions;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Collection of EOAccess related utilities.
 */
public class ERXEOAccessUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEOAccessUtilities.class);
    public static NSRecursiveLock adaptorOperationsLock = new NSRecursiveLock();

    /**
     * Finds an entity that is contained in a string. This is used a lot in
     * DirectToWeb. Example: "ListAllStudios"=>Studio
     * 
     * @param ec
     *            editing context
     * @param string
     *            string to look into
     * @return found entity or null
     */
    public static EOEntity entityMatchingString(EOEditingContext ec, String string) {
        EOEntity result = null;
        if (string != null) {
            NSArray entityNames = null;
            String lowerCaseName = string.toLowerCase();
            if (entityNames == null) {
                EOModelGroup group = modelGroup(ec);
                entityNames = (NSArray) ERXUtilities.entitiesForModelGroup(group).valueForKeyPath("name.toLowerCase");
            }
            NSMutableArray possibleEntities = new NSMutableArray();
            for (Enumeration e = entityNames.objectEnumerator(); e.hasMoreElements();) {
                String lowercaseEntityName = (String) e.nextElement();
                if (lowerCaseName.indexOf(lowercaseEntityName) != -1) possibleEntities.addObject(lowercaseEntityName);
            }
            if (possibleEntities.count() == 1) {
                result = ERXUtilities.caseInsensitiveEntityNamed((String) possibleEntities.lastObject());
            } else if (possibleEntities.count() > 1) {
                ERXArrayUtilities.sortArrayWithKey(possibleEntities, "length");
                if (((String) possibleEntities.objectAtIndex(0)).length() == ((String) possibleEntities.lastObject()).length())
                        log.warn("Found multiple entities of the same length for string: " + string + " possible entities: "
                                + possibleEntities);
                result = ERXUtilities.caseInsensitiveEntityNamed((String) possibleEntities.lastObject());
            }
            if (log.isDebugEnabled())
                    log.debug("Found possible entities: " + possibleEntities + " for string: " + string + " result: " + result);
        }
        return result;
    }

    /**
     * Method used to determine if a given entity is a shared entity.
     * 
     * @param ec
     *            editing context
     * @param entityName
     *            name of the entity
     * @return if the entity is a shared entity
     */
    public static boolean entityWithNamedIsShared(EOEditingContext ec, String entityName) {
        if (entityName == null)
            throw new IllegalStateException("Entity name argument is null for method: entityWithNamedIsShared");
        EOEntity entity = entityNamed(ec, entityName);
        return entity.sharedObjectFetchSpecificationNames().count() > 0;
    }

    /**
     * Convenience method to get the next unique ID from a sequence.
     * 
     * @param ec
     *            editing context
     * @param modelName
     *            name of the model which connects to the database that has the
     *            sequence in it
     * @param sequenceName
     *            name of the sequence
     * @return next value in the sequence
     */
    // ENHANCEME: Need a non-oracle specific way of doing this. Should poke
    // around at
    //		the adaptor level and see if we can't find something better.
    public static Number getNextValFromSequenceNamed(EOEditingContext ec, String modelName, String sequenceName) {
        String sqlString = "select " + sequenceName + ".nextVal from dual";
        NSArray array = EOUtilities.rawRowsForSQL(ec, modelName, sqlString, null);
        if (array.count() == 0) { throw new RuntimeException("Unable to generate value from sequence named: " + sequenceName
                + " in model: " + modelName); }
        NSDictionary dictionary = (NSDictionary) array.objectAtIndex(0);
        NSArray valuesArray = dictionary.allValues();
        return (Number) valuesArray.objectAtIndex(0);
    }

    /**
     * Utility method used to execute arbitrary SQL. This has the advantage over
     * the {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * <code>rawRowsForSQL</code> in that it can be used with other statements
     * besides just SELECT without throwing exceptions.
     * 
     * @param ec
     *            editing context that determines which model group and database
     *            context to use.
     * @param entityName
     *            name of an entity in the model connected to the database you
     *            wish to execute SQL against
     * @param exp
     *            SQL expression
     */
    // ENHANCEME: Should support the use of bindings
    // ENHANCEME: Could also support the option of using a seperate EOF stack so
    // as to execute
    //		sql in a non-blocking fashion.
    public static void evaluateSQLWithEntityNamed(EOEditingContext ec, String entityName, String exp) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        evaluateSQLWithEntity(ec, entity, exp);
    }

    /**
     * Utility method used to execute arbitrary SQL. This has the advantage over
     * the {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * <code>rawRowsForSQL</code> in that it can be used with other statements
     * besides just SELECT without throwing exceptions.
     * 
     * @param ec
     *            editing context that determines which model group and database
     *            context to use.
     * @param entity
     *            an entity in the model connected to the database you wish to
     *            execute SQL against
     * @param exp
     *            SQL expression
     */
    // ENHANCEME: Should support the use of bindings
    // ENHANCEME: Could also support the option of using a seperate EOF stack so
    // as to execute
    // sql in a non-blocking fashion.
    public static void evaluateSQLWithEntity(EOEditingContext ec, EOEntity entity, String exp) {
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
        if (!adaptorChannel.isOpen()) adaptorChannel.openChannel();
        EOSQLExpressionFactory factory = adaptorChannel.adaptorContext().adaptor().expressionFactory();
        adaptorChannel.evaluateExpression(factory.expressionForString(exp));
    }

    /**
     * Creates the SQL which is used by the provides EOFetchSpecification.
     * 
     * @param ec
     *            the EOEditingContext
     * @param spec
     *            the EOFetchSpecification in question
     * 
     * @return the SQL which the EOFetchSpecification would use
     */
    public static String sqlForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec) {
        return sqlExpressionForFetchSpecification(ec, spec, 0, -1).statement();
    }

    /**
     * Returns the raw rows for the given EOSQLExpression.
     * 
     * @param ec
     *            the EOEditingContext
     * @param spec
     *            the EOFetchSpecification in question
     * @param modelName
     *            the name of the model in question
     * @param expression
     *            the EOSQLExpression in question
     * 
     * @return array of dictionaries
     */
    public static NSArray rawRowsForSQLExpression(EOEditingContext ec, String modelName, EOSQLExpression expression) {
        EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, modelName);
        dbc.lock();
        NSMutableArray results = null;
        try {
            EOAdaptorChannel channel = dbc.availableChannel().adaptorChannel();
            if (!channel.isOpen()) channel.openChannel();
            channel.evaluateExpression(expression);
            try {
                channel.setAttributesToFetch(channel.describeResults());
                results = new NSMutableArray();
                NSDictionary row;
                while ((row = channel.fetchRow()) != null)
                    results.addObject(row);
            } catch (EOGeneralAdaptorException ex) {
                channel.cancelFetch();
                throw ex;
            }
        } finally {
            dbc.unlock();
        }
        return results;
    }

    /**
     * Creates the SQL which is used by the provided EOFetchSpecification,
     * limited by the given range.
     * 
     * @param ec
     *            the EOEditingContext
     * @param spec
     *            the EOFetchSpecification in question
     * @param start
     *            start of rows to fetch
     * @param end
     *            end of rows to fetch (-1 if not used)
     * 
     * @return the EOSQLExpression which the EOFetchSpecification would use
     */
    public static EOSQLExpression sqlExpressionForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec, long start, long end) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        EOModel model = entity.model();
        EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, model.name());
        EOAdaptor adaptor = dbc.adaptorContext().adaptor();
        EOSQLExpressionFactory sqlFactory = adaptor.expressionFactory();
        
        NSArray attributes =  entity.attributesToFetch();
        if(spec.fetchesRawRows()) {
            NSMutableArray arr = new NSMutableArray();
            for(Enumeration e = spec.rawRowKeyPaths().objectEnumerator(); e.hasMoreElements(); ) {
                String keyPath = (String)e.nextElement();
                arr.addObject(entity.anyAttributeNamed(keyPath));
            }
            attributes = arr.immutableClone();
        }
        
        EOQualifier qualifier = spec.qualifier();
        if(qualifier != null) {
            qualifier = EOQualifierSQLGeneration.Support._schemaBasedQualifierWithRootEntity(qualifier, entity);
        }
        if(qualifier != spec.qualifier()) {
            spec = (EOFetchSpecification)spec.clone();
            spec.setQualifier(qualifier);
        }
        if(spec.fetchLimit() > 0) {
            spec = (EOFetchSpecification)spec.clone();
            spec.setFetchLimit(0);
            spec.setPromptsAfterFetchLimit(false);
        }
        if(spec.sortOrderings() != null ) {
            NSMutableArray ommitedOrderings = new NSMutableArray();
            for(Enumeration e = spec.sortOrderings().objectEnumerator(); e.hasMoreElements();) {
                EOSortOrdering ordering = (EOSortOrdering) e.nextElement();
                if(ordering.key().indexOf(".") > 0) {
                    ommitedOrderings.addObject(ordering);
                }
            }
            if(ommitedOrderings.count() > 0) {
                log.warn("Dropped some sort key as key paths are not supported here: " + ommitedOrderings);
                spec = (EOFetchSpecification)spec.clone();
                spec.setSortOrderings(ERXArrayUtilities.arrayMinusArray(spec.sortOrderings(), ommitedOrderings));
           }
        }
        EOSQLExpression sqlExpr = sqlFactory.selectStatementForAttributes(attributes, false, spec, entity);
        String sql = sqlExpr.statement();
        if (end >= 0) {
            String url = (String) model.connectionDictionary().objectForKey("URL");
            if (url != null) {
                String lowerCaseURL= url.toLowerCase();
                if (lowerCaseURL.indexOf("frontbase") != -1) {
                    //add TOP(start, (end - start)) after the SELECT word
                    int index = sql.indexOf("select");
                    if (index == -1) {
                        index = sql.indexOf("SELECT");
                    }
                    index += 6;
                    sql = sql.substring(0, index) 
                        + " TOP(" + start + ", " + (end-start) + ")" 
                        + sql.substring(index + 1, sql.length());
                } else if (lowerCaseURL.indexOf("openbase") != -1) {
                    // Openbase support for limiting result set
                    sql += " return results " + start + " to " + end;
                } else if (lowerCaseURL.indexOf("mysql") != -1 ) {
                    sql += " LIMIT " + start + ", " + (end - start);
                } else if (lowerCaseURL.indexOf("postgresql") != -1 ) {
                    sql += " LIMIT " + (end - start) + " OFFSET " + start;
                }
            }
            sqlExpr.setStatement(sql);
        }

        return sqlExpr;
    }

    /**
     * Returns the number of rows the supplied EOFetchSpecification would
     * return.
     * 
     * @param ec
     *            the EOEditingContext
     * @param spec
     *            the EOFetchSpecification in question
     * @return the number of rows
     */
    public static int rowCountForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec) {
        int rowCount = -1;
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        EOModel model = entity.model();
        if(spec.fetchLimit() > 0 || spec.sortOrderings() != null) {
            spec = new EOFetchSpecification(spec.entityName(), spec.qualifier(), null);
        }
        
        EOSQLExpression sql = ERXEOAccessUtilities.sqlExpressionForFetchSpecification(ec, spec, 0, -1);
        String statement = sql.statement();
        int index = statement.toLowerCase().indexOf(" from ");
        statement = "select count(*) " + statement.substring(index, statement.length());
        sql.setStatement(statement);
        NSArray result = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, model.name(), sql);

        if (result.count() > 0) {
            NSDictionary dict = (NSDictionary) result.objectAtIndex(0);
            NSArray values = dict.allValues();
            if (values.count() > 0) {
                Object value = values.objectAtIndex(0);
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else {
                    try {
                        int c = Integer.parseInt(value.toString());
                        rowCount = c;
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("sql " + sql + " returned a wrong result, could not convert " + value
                                + " into an int!");
                    }
                }
            } else {
                throw new IllegalStateException("sql " + sql + " returned no result!");
            }
        } else {
            throw new IllegalStateException("sql " + sql + " returned no result!");
        }
        return rowCount;
    }

    /**
     * Similar to the helper in EUUtilities, but allows for null editingContext.
     * If ec is null, it will try to get at the session via thread storage and
     * use its defaultEditingContext. This is here now so we can remove the
     * delgate in ERXApplication.
     * 
     * @param ec
     *            editing context used to locate the model group (can be null)
     */

    public static EOModelGroup modelGroup(EOEditingContext ec) {
        if (ec == null) {
            ERXSession s = ERXExtensions.session();
            if (s != null) {
                ec = s.defaultEditingContext();
            }
        }
        EOModelGroup group;
        if (ec == null) {
            group = EOModelGroup.defaultGroup();
        } else {
            group = EOModelGroup.modelGroupForObjectStoreCoordinator((EOObjectStoreCoordinator) ec.rootObjectStore());
        }
        return group;
    }

    /**
     * Similar to the helper in EUUtilities, but allows for null editingContext.
     * 
     * @param ec
     *            editing context used to locate the model group (can be null)
     * @param entityName
     *            entity name
     */
    public static EOEntity entityNamed(EOEditingContext ec, String entityName) {
        EOModelGroup modelGroup = modelGroup(ec);
        return modelGroup.entityNamed(entityName);
    }

    /**
     * Creates an aggregate attribute for a given function name. These can then
     * be used to query on when using raw rows.
     * 
     * @param ec
     *            editing context used to locate the model group
     * @param function
     *            name of the function MAX, MIN, etc
     * @param attributeName
     *            name of the attribute
     * @param entityName
     *            name of the entity
     * @return aggregate function attribute
     */
    public static EOAttribute createAggregateAttribute(EOEditingContext ec, String function, String attributeName, String entityName) {
        if (function == null) throw new IllegalStateException("Function is null.");
        if (attributeName == null) throw new IllegalStateException("Attribute name is null.");
        if (entityName == null) throw new IllegalStateException("Entity name is null.");

        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);

        if (entity == null) throw new IllegalStateException("Unable find entity named: " + entityName);

        EOAttribute attribute = entity.attributeNamed(attributeName);

        if (attribute == null)
                throw new IllegalStateException("Unable find attribute named: " + attributeName + " for entity: " + entityName);

        EOAttribute aggregate = new EOAttribute();
        aggregate.setName("p_object" + function + "Attribute");
        aggregate.setColumnName("p_object" + function + "Attribute");
        aggregate.setClassName("java.lang.Number");
        aggregate.setValueType("i");
        aggregate.setReadFormat(function + "(t0." + attribute.columnName() + ")");
        return aggregate;
    }

    /** oracle 9 has a maximum length of 30 characters for table names, column names and constraint names
     * Foreign key constraint names are defined like this from the plugin:<br/><br/>
     * 
     * TABLENAME_FOEREIGNKEYNAME_FK <br/><br/>
     * 
     * The whole statement looks like this:<br/><br/>
     * 
     * ALTER TABLE [TABLENAME] ADD CONSTRAINT [CONSTRAINTNAME] FOREIGN KEY ([FK]) REFERENCES [DESTINATION_TABLE] ([PK]) DEFERRABLE INITIALLY DEFERRED
     * 
     * THIS means that the tablename and the columnname together cannot
     * be longer than 26 characters.<br/><br/>
     * 
     * This method checks each foreign key constraint name and if it is longer than 30 characters its replaced
     * with a unique name.
     * 
     * @see createSchemaSQLForEntitiesInModelWithNameAndOptions
     */
    public static String createSchemaSQLForEntitiesInModelWithNameAndOptionsForOracle9(NSArray entities, String modelName,
            NSDictionary optionsCreate) {
        String oldConstraintName = null;
        int i = 0;
        String s = createSchemaSQLForEntitiesInModelWithNameAndOptions(entities, modelName, optionsCreate);
        NSArray a = NSArray.componentsSeparatedByString(s, "/");
        StringBuffer buf = new StringBuffer(s.length());
        Pattern pattern = Pattern.compile(".*ALTER TABLE .* ADD CONSTRAINT (.*) FOREIGN KEY .* REFERENCES .* \\(.*\\) DEFERRABLE INITIALLY DEFERRED.*");
        Pattern pattern2 = Pattern.compile("(.*ALTER TABLE .* ADD CONSTRAINT ).*( FOREIGN KEY .* REFERENCES .* \\(.*\\) DEFERRABLE INITIALLY DEFERRED.*)");
        String lineSeparator = System.getProperty("line.separator");

        for (Enumeration e = a.objectEnumerator(); e.hasMoreElements();) {
            String statementLine = (String)e.nextElement();
            NSArray b = NSArray.componentsSeparatedByString(statementLine, lineSeparator);
            for (Enumeration e1 = b.objectEnumerator(); e1.hasMoreElements();) {
                String statement = (String)e1.nextElement();
                if (!pattern.matcher(statement).matches()) {
                    buf.append(statement);
                    buf.append(lineSeparator);
                    continue;
                }
                
                String constraintName = pattern.matcher(statement).replaceAll("$1");
                if (constraintName.length() <= 30) {
                    buf.append(statement);
                    buf.append(lineSeparator);
                    continue;
                }
                
                constraintName = "fk" + System.currentTimeMillis() + new NSTimestamp().getNanos();
                String newConstraintName = i == 0 ? constraintName : constraintName + "_" + i; 

                if (oldConstraintName == null) {
                    oldConstraintName = constraintName;
                } else if (oldConstraintName.equals(newConstraintName)) {
                    constraintName += "_" + ++i;
                } else {
                    i = 0;
                }
                oldConstraintName = constraintName;
                
                String newConstraint = pattern2.matcher(statement).replaceAll("$1"+constraintName+"$2");
                buf.append(newConstraint);
                buf.append(lineSeparator);
            }
            if (e.hasMoreElements())
                buf.append("/");
        }
        System.out.println("finished!");
        return buf.toString();
    }
    
    /**
     * creates SQL to create tables for the specified Entities. This can be used
     * with EOUtilities rawRowsForSQL method to create the tables.
     * 
     * @param entities
     *            a NSArray containing the entities for which create table
     *            statements should be generated or null if all entitites in the
     *            model should be used.
     * @param modelName
     *            the name of the EOModel
     * @param optionsCreate
     *            a NSDictionary containing the different options. Possible keys
     *            are
     *            <ol>
     *            <li>EOSchemaGeneration.DropTablesKey</li>
     *            <ol>
     *            <li>EOSchemaGeneration.DropPrimaryKeySupportKey</li>
     *            <ol>
     *            <li>EOSchemaGeneration.CreateTablesKey</li>
     *            <ol>
     *            <li>EOSchemaGeneration.CreatePrimaryKeySupportKey</li>
     *            <ol>
     *            <li>EOSchemaGeneration.PrimaryKeyConstraintsKey</li>
     *            <ol>
     *            <li>EOSchemaGeneration.ForeignKeyConstraintsKey</li>
     *            <ol>
     *            <li>EOSchemaGeneration.CreateDatabaseKey</li>
     *            <ol>
     *            <li>EOSchemaGeneration.DropDatabaseKey</li>
     *            <br/><br>
     *            Possible values are <code>YES</code> and <code>NO</code>
     * 
     * @return a <code>String</code> containing SQL statements to create
     *         tables
     */
    public static String createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray entities, String modelName,
            NSDictionary optionsCreate) {
        // get the JDBCAdaptor
        EODatabaseContext dc = EOUtilities.databaseContextForModelNamed(ERXEC.newEditingContext(), modelName);
        EOAdaptorContext ac = dc.adaptorContext();
        // ak: stupid trick to get around having to link to JDBCAdaptor
        EOSynchronizationFactory sf = (EOSynchronizationFactory) NSKeyValueCodingAdditions.Utility.valueForKeyPath(ac,
                "adaptor.plugIn.createSynchronizationFactory");
        EOModel m = modelGroup(null).modelNamed(modelName);
        Enumeration e = m.entities().objectEnumerator();
        entities = entities == null ? new NSMutableArray() : entities;

        if (entities == null) {
            NSMutableArray ar = new NSMutableArray();
            while (e.hasMoreElements()) {
                EOEntity currentEntity = (EOEntity) e.nextElement();
                if (!(currentEntity.name().startsWith("EO") && currentEntity.name().endsWith("Prototypes"))) {
                    // we do not want to add EOXXXPrototypes entities
                    ar.addObject(currentEntity);
                }
            }
            entities = ar;
        }
        return sf.schemaCreationScriptForEntities(entities, optionsCreate);
    }

    /**
     * creates SQL to create tables for the specified Entities. This can be used
     * with EOUtilities rawRowsForSQL method to create the tables.
     * 
     * @param entities
     *            a NSArray containing the entities for which create table
     *            statements should be generated or null if all entitites in the
     *            model should be used.
     * @param modelName
     *            the name of the EOModel <br/><br/>This method uses the
     *            following defaults options:
     *            <ol>
     *            <li>EOSchemaGeneration.DropTablesKey=YES</li>
     *            <ol>
     *            <li>EOSchemaGeneration.DropPrimaryKeySupportKey=YES</li>
     *            <ol>
     *            <li>EOSchemaGeneration.CreateTablesKey=YES</li>
     *            <ol>
     *            <li>EOSchemaGeneration.CreatePrimaryKeySupportKey=YES</li>
     *            <ol>
     *            <li>EOSchemaGeneration.PrimaryKeyConstraintsKey=YES</li>
     *            <ol>
     *            <li>EOSchemaGeneration.ForeignKeyConstraintsKey=YES</li>
     *            <ol>
     *            <li>EOSchemaGeneration.CreateDatabaseKey=NO</li>
     *            <ol>
     *            <li>EOSchemaGeneration.DropDatabaseKey=NO</li>
     *            <br/><br>
     *            Possible values are <code>YES</code> and <code>NO</code>
     * 
     * @return a <code>String</code> containing SQL statements to create
     *         tables
     */
    public static String createSchemaSQLForEntitiesInModelWithName(NSArray entities, String modelName) {
        NSMutableDictionary optionsCreate = new NSMutableDictionary();
        optionsCreate.setObjectForKey("YES", EOSchemaGeneration.DropTablesKey);
        optionsCreate.setObjectForKey("YES", EOSchemaGeneration.DropPrimaryKeySupportKey);
        optionsCreate.setObjectForKey("YES", EOSchemaGeneration.CreateTablesKey);
        optionsCreate.setObjectForKey("YES", EOSchemaGeneration.CreatePrimaryKeySupportKey);
        optionsCreate.setObjectForKey("YES", EOSchemaGeneration.PrimaryKeyConstraintsKey);
        optionsCreate.setObjectForKey("YES", EOSchemaGeneration.ForeignKeyConstraintsKey);
        optionsCreate.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
        optionsCreate.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
        return createSchemaSQLForEntitiesInModelWithNameAndOptions(entities, modelName, optionsCreate);
    }

    public static String createIndexSQLForEntitiesForOracle(NSArray entities) {
        NSMutableArray a = new NSMutableArray();
        a.addObject("BLOB");
        a.addObject("CLOB");
        return createIndexSQLForEntities(entities, a);
    }

    public static String createIndexSQLForEntities(NSArray entities) {
        return createIndexSQLForEntities(entities, null);
    }
    public static String createIndexSQLForEntities(NSArray entities, NSArray externalTypesToIgnore) {
        if (externalTypesToIgnore == null) externalTypesToIgnore = NSArray.EmptyArray;
        if (entities == null || entities.count() == 0) return "";
        int i = 0;
        String oldIndexName = null;
        String lineSeparator = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        EOEntity ent = (EOEntity)entities.objectAtIndex(0);
        String commandSeparator = null;
        String modelName = ent.model().name();
        String plugin= ERXSystem.getProperty(modelName + ".DBPlugin");
        plugin= plugin ==null ? ERXSystem.getProperty("dbConnectPluginGLOBAL") : plugin;
        if ("Oracle".equals(plugin)) {
            commandSeparator = lineSeparator + "/" + lineSeparator; 
        } else {
            commandSeparator = ";" + lineSeparator;
        }
        for (Enumeration entitiesEnum = entities.objectEnumerator(); entitiesEnum.hasMoreElements();) {
            EOEntity entity = (EOEntity) entitiesEnum.nextElement();
            // only use this entity if it has its own table
            if (!entityUsesSeparateTable(entity)) continue;
            
            NSDictionary d = entity.userInfo();
            NSMutableArray usedColumns = new NSMutableArray();
            for (Enumeration keys = d.keyEnumerator(); keys.hasMoreElements();) {
                String key = (String) keys.nextElement();
                if (key.startsWith("index")) {
                    String numbers = key.substring("index".length());
                    if (ERXStringUtilities.isDigitsOnly(numbers)) {
                        String attributeNames = (String) d.objectForKey(key);
                        if (ERXStringUtilities.stringIsNullOrEmpty(attributeNames)) continue;
                        String indexName = "c" + System.currentTimeMillis() + new NSTimestamp().getNanos();
                        String newIndexName = i == 0 ? indexName : indexName + "_" + i; 
                        if (oldIndexName == null) {
                            oldIndexName = indexName;
                        } else if (oldIndexName.equals(newIndexName)) {
                            indexName += "_" + ++i;
                        } else {
                            i = 0;
                        }
                        oldIndexName = indexName;
                        StringBuffer localBuf = new StringBuffer();
                        StringBuffer columnBuf = new StringBuffer();
                        boolean validIndex = false;
                        localBuf.append("create index " + indexName + " on " + entity.externalName() + "(");
                        for (Enumeration attributes = NSArray.componentsSeparatedByString(attributeNames, ",")
                                .objectEnumerator(); attributes.hasMoreElements();) {
                            String attributeName = (String) attributes.nextElement();
                            attributeName = attributeName.trim();
                            EOAttribute attribute = entity.attributeNamed(attributeName);
                            if (attribute == null) {
                                attribute = attributeWithColumnNameFromEntity(attributeName, entity);
                            }
                            if (attribute != null && externalTypesToIgnore.indexOfObject(attribute.externalType()) != NSArray.NotFound) {
                                continue;
                            }
                            validIndex = true;
                            String columnName = attribute == null ? attributeName : attribute.columnName();
                            columnBuf.append(columnName);
                            if (attributes.hasMoreElements()) {
                                columnBuf.append(", ");
                            }
                        }
                        if (validIndex) {
                            String l = columnBuf.toString();
                            if (l.endsWith(", ")) l = l.substring(0, l.length() - 2);
                            if (usedColumns.indexOfObject(l) == NSArray.NotFound) {
                                buf.append(localBuf).append(l);
                                usedColumns.addObject(l);
                                buf.append(")").append(commandSeparator).append(lineSeparator);
                            }
                        }
                    }
                } else if (key.equals("additionalIndexes")) {
                    // this is a space separated list of column or attribute
                    // names
                    String value = (String) d.objectForKey(key);
                    for (Enumeration indexes = NSArray.componentsSeparatedByString(value, " ").objectEnumerator(); indexes
                            .hasMoreElements();) {
                        String indexValues = (String) indexes.nextElement();
                        if (ERXStringUtilities.stringIsNullOrEmpty(indexValues)) continue;

                        // this might be a comma separate list
                        String indexName = "c"+System.currentTimeMillis()+new NSTimestamp().getNanos();
                        String newIndexName = i == 0 ? indexName : indexName + "_" + i; 
                        if (oldIndexName == null) {
                            oldIndexName = indexName;
                        } else if (oldIndexName.equals(newIndexName)) {
                            indexName += "_" + ++i;
                        } else {
                            i = 0;
                        }
                        oldIndexName = indexName;
                        
                        StringBuffer localBuf = new StringBuffer();
                        StringBuffer columnBuf = new StringBuffer();
                        boolean validIndex = false;
                        localBuf.append("create index "+indexName+" on "+entity.externalName()+"(");
                        for (Enumeration e = NSArray.componentsSeparatedByString(indexValues, ",").objectEnumerator(); e.hasMoreElements();) {
                            String attributeName = (String)e.nextElement();
                            attributeName = attributeName.trim();
                            EOAttribute attribute = entity.attributeNamed(attributeName);

                            if (attribute == null) {
                                attribute = attributeWithColumnNameFromEntity(attributeName, entity);
                            }
                            if (attribute != null && externalTypesToIgnore.indexOfObject(attribute.externalType()) != NSArray.NotFound) {
                                continue;
                            }
                            validIndex = true;
                            
                            
                            String columnName = attribute == null ? attributeName : attribute.columnName();
                            columnBuf.append(columnName);
                            if (e.hasMoreElements()) {
                                columnBuf.append(", ");
                            }
                        }
                        if (validIndex) {
                            String l = columnBuf.toString();
                            if (l.endsWith(", ")) l = l.substring(0, l.length() - 2);
                            if (usedColumns.indexOfObject(l) == NSArray.NotFound) {
                                buf.append(localBuf).append(l);
                                usedColumns.addObject(l);
                                buf.append(")").append(commandSeparator).append(lineSeparator);
                            }
                        }
                    }
                }
            }
        }
        return buf.toString();
    }

    public static boolean entityUsesSeparateTable(EOEntity entity) {
        if (entity.parentEntity() == null) return true;
        EOEntity parent = entity.parentEntity();
        while (parent != null) {
            if (!entity.externalName().equals(parent.externalName())) return true;
            entity = parent;
            parent = entity.parentEntity();
        }
        return false;
    }

    public static EOAttribute attributeWithColumnNameFromEntity(String columnName, EOEntity entity) {
        for (Enumeration e = entity.attributes().objectEnumerator(); e.hasMoreElements();) {
            EOAttribute att = (EOAttribute)e.nextElement();
            if (columnName.equalsIgnoreCase(att.columnName())) {
                return att;
            }
        }
        return null;
    }

    /**
     * Tries to recover from a {@link EOGeneralAdaptorException}.
     * 
     * @param exception
     *            the exception as recieved from saveChanges()
     * @param editingContext
     *            editing context that created the error
     * @return true if the error could be handled.
     */
    public static boolean recoverFromAdaptorException(EOEditingContext editingContext, EOGeneralAdaptorException exception) {
        boolean wasHandled = false;
        NSDictionary userInfo = (NSDictionary) exception.userInfo();
        if (userInfo != null) {
            String failureKey = (String) userInfo.objectForKey("EOAdaptorFailureKey");
            if ("EOAdaptorOptimisticLockingFailure".equals(failureKey)) {
                EOAdaptorOperation adaptorOperation = (EOAdaptorOperation) userInfo.objectForKey("EOFailedAdaptorOperationKey");
                EODatabaseOperation databaseOperation = (EODatabaseOperation) userInfo.objectForKey("EOFailedDatabaseOperationKey");
                if (adaptorOperation != null && databaseOperation != null) {
                    NSDictionary changedValues = adaptorOperation.changedValues();
                    NSDictionary snapshot = databaseOperation.dbSnapshot();

                    if (log.isDebugEnabled()) log.debug("snapshot" + snapshot);

                    EOEntity entity = adaptorOperation.entity();
                    String entityName = entity.name();

                    if (log.isDebugEnabled()) log.debug("entityName: " + entityName);

                    NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
                    EOQualifier qualifier = ERXTolerantSaver.qualifierWithSnapshotAndPks(primaryKeyAttributes, snapshot);
                    EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, null);
                    fs.setRefreshesRefetchedObjects(true);

                    NSArray objects = editingContext.objectsWithFetchSpecification(fs);
                    editingContext.revert();
                    wasHandled = true;
                } else {
                    log.error("Missing EOFailedAdaptorOperationKey or EOFailedDatabaseOperationKey in " + exception + ": "
                            + exception.userInfo());
                }
            }
        }
        return wasHandled;
    }

    /**
     * Given an array of EOs, returns snapshot dictionaries for the given
     * related objects.
     */
    //CHECKME ak is this description correct?
    public static NSArray snapshotsForObjectsFromRelationshipNamed(NSArray eos, String relKey) {
    	NSMutableArray result = new NSMutableArray();
    	if (eos.count() > 0) {
    		EOEnterpriseObject eo = (EOEnterpriseObject)eos.lastObject();
    		String entityName = eo.entityName();
    		EOEditingContext ec = eo.editingContext();
    		EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
    		EORelationship relationship = entity.relationshipNamed(relKey);
    		if(relationship.sourceAttributes().count() == 1) {
    			EOAttribute attribute = (EOAttribute) relationship.sourceAttributes().lastObject();
    			EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec, entity.model().name());
    			String name = attribute.name();
    			for (Enumeration e = eos.objectEnumerator(); e.hasMoreElements();) {
    				EOEnterpriseObject target = (EOEnterpriseObject) e.nextElement();
    				Object value = (context.snapshotForGlobalID(ec.globalIDForObject(target))).valueForKey(name);
    				result.addObject(value);
    			}
    		} else {
    			throw new IllegalArgumentException("Has more than one relationship attribute: " + relKey);
    		}
    	}
    	return result;
    }

    /**
     * Utility method to generate a new primary key dictionary using the adaptor
     * for a given entity. This is can be handy if you need to have a primary
     * key for an object before it is saved to the database. This method uses
     * the same method that EOF uses by default for generating primary keys. See
     * {@link ERXGeneratesPrimaryKeyInterface}for more information about using
     * a newly created dictionary as the primary key for an enterprise object.
     * 
     * @param ec
     *            editing context
     * @param entityName
     *            name of the entity to generate the primary key dictionary for.
     * @return a dictionary containing a new primary key for the given entity.
     */
    public static NSDictionary primaryKeyDictionaryForEntity(EOEditingContext ec, String entityName) {
    	EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
    	EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
    	NSDictionary primaryKey = null;
    	dbContext.lock();
    	try {
    		EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
    		if (!adaptorChannel.isOpen()) {
    			adaptorChannel.openChannel();
    		}
    		NSArray arr = adaptorChannel.primaryKeysForNewRowsWithEntity(1, entity);
    		if (arr != null) {
    			primaryKey = (NSDictionary) arr.lastObject();
    		} else {
    			log.warn("Could not get primary key for entity: " + entityName + " exception");
    		}
    	} catch (Exception e) {
    		log.error("Caught exception when generating primary key for entity: " + entityName + " exception: " + e, e);
    	} finally {
    		dbContext.unlock();
    	}
    	return primaryKey;
    }
    
    /**
     * Creates an array containing all of the primary keys of the given objects.
     * 
     * @param eos
     *            array of enterprise objects
     */
    public static NSArray primaryKeysForObjects(NSArray eos) {
        NSMutableArray result = new NSMutableArray();
        if (eos.count() > 0) {
            for (Enumeration e = eos.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject target = (EOEnterpriseObject) e.nextElement();
                NSDictionary pKey = EOUtilities.primaryKeyForObject(target.editingContext(), target);
                result.addObject(pKey.allValues().objectAtIndex(0));
            }
        }
        return result;
    }

    /**
     * Crude hack to get at the end of a relationship path.
     * @param relationship
     * @return
     */
    public static EORelationship lastRelationship(EORelationship relationship) {
        return (EORelationship) NSKeyValueCoding.Utility.valueForKey(relationship, "lastRelationship");
    }

    /**
     * Creates an array of relationships and attributes from the given keypath to give to the
     * EOSQLExpression method <code>sqlStringForAttributePath</code>. If the last element is a
     * relationship, then the relationship's source attribute will get chosen. As such, this can only 
     * work for single-value relationships in the last element.
     * @param entity
     * @param keyPath
     * @return
     */
    public static NSArray attributePathForKeyPath(EOEntity entity, String keyPath) {
        NSMutableArray result = new NSMutableArray();
        String[] parts = keyPath.split("\\.");
        String part;
        for (int i = 0; i < parts.length - 1; i++) {
            part = parts[i];
            EORelationship relationship = entity.anyRelationshipNamed(part);
            entity = relationship.destinationEntity();
            result.addObject(relationship);
        }
        part = parts[parts.length-1];
        EOAttribute attribute = entity.anyAttributeNamed(part);
        if(attribute == null) {
            EORelationship relationship = entity.anyRelationshipNamed(part);
            if(relationship == null) {
                throw new IllegalArgumentException("Last element is not an attribute nor a relationship: " + keyPath);
            }
            if (relationship.isFlattened()) {
                //FIXME!
            } else {
                attribute = ((EOJoin) relationship.joins().lastObject()).sourceAttribute();
            }
         }
        result.addObject(attribute);
        return result;
    }

    /**
     * Creates a where clause string " someKey IN ( someValue1,...)". Can migrate keyPaths.
     */
    public static String sqlWhereClauseStringForKey(EOSQLExpression e, String key, NSArray valueArray) {
        if(valueArray.count() == 0) {
            return "0=1";
        }
        StringBuffer sb = new StringBuffer();
        
        NSArray attributePath = attributePathForKeyPath(e.entity(), key);
        EOAttribute attribute = (EOAttribute) attributePath.lastObject();
        if(attributePath.count() > 1) {
            sb.append(e.sqlStringForAttributePath(attributePath));
        } else {
            sb.append(e.sqlStringForAttribute(attribute));
        }
        sb.append(" IN ");
        sb.append("(");
        for (int i = 0; i < valueArray.count(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object value = valueArray.objectAtIndex(i);
            // The Postgres Expression has a problem using bind variables so we have to get the formatted
            // SQL string for a value instead.  All Apple provided plugins must use the bind variables
            // however.  Frontbase can go either way
            boolean isPostgres = e.getClass().getName().equals("com.webobjects.jdbcadaptor.PostgresqlExpression");
            value = isPostgres ? e.formatValueForAttribute(value, attribute) : e.sqlStringForValue(value, key);
            sb.append(value);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Returns the database context for the given entity in the given
     * EOObjectStoreCoordinator
     * 
     * @param entityName
     * @param osc
     * @return
     */
    public static EODatabaseContext databaseContextForEntityNamed(EOObjectStoreCoordinator osc, String entityName) {
        EOModel model = EOModelGroup.modelGroupForObjectStoreCoordinator(osc).entityNamed(entityName).model();
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, osc);
        return dbc;
    }

    /**
     * Closes the (JDBC) Connection from all database channels for the specified
     * EOObjectStoreCoordinator
     * 
     * @param osc,
     *            the EOObjectStoreCoordinator from which the (JDBC)Connections
     *            should be closed
     */
    public static boolean closeDatabaseConnections(EOObjectStoreCoordinator osc) {
        boolean couldClose = true;
        try {
            int i, contextCount, j, channelCount;
            NSArray databaseContexts;
            databaseContexts = osc.cooperatingObjectStores();
            contextCount = databaseContexts.count();
            for (i = contextCount; i-- > 0;) {
                NSArray channels = ((EODatabaseContext) databaseContexts.objectAtIndex(i)).registeredChannels();
                channelCount = channels.count();
                for (j = channelCount; j-- > 0;) {
                    EODatabaseChannel dbch = (EODatabaseChannel) channels.objectAtIndex(j);
                    if (!dbch.adaptorChannel().adaptorContext().hasOpenTransaction()) {
                        dbch.adaptorChannel().closeChannel();
                        
                    } else {
                        log.warn("could not close Connection from " + dbch + " because its EOAdaptorContext "
                                + dbch.adaptorChannel().adaptorContext() + " had open Transactions");
                        couldClose = false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("could not close all Connections, reason:", e);
            couldClose = false;
        }
        return couldClose;
    }

    /**
     * Returns the last entity for the given key path. If the path is empty or null, returns the given entity.
     * @param entity
     * @param keyPath
     * @return
     */
    private static Set _keysWithWarning = Collections.synchronizedSet(new HashSet());
    
    public static EOEntity destinationEntityForKeyPath(EOEntity entity, String keyPath) {
        if(keyPath == null || keyPath.length() == 0) {
            return entity;
        }
        NSArray keyArray = NSArray.componentsSeparatedByString(keyPath, ".");
        for(Enumeration keys = keyArray.objectEnumerator(); keys.hasMoreElements(); ) {
            String key = (String)keys.nextElement();
            EORelationship rel = entity.anyRelationshipNamed(key);
            if(rel == null) {
                if(entity.anyAttributeNamed(key) == null) {
                	if(key.indexOf("@") != 0) {
                		if(!_keysWithWarning.contains(key + "-" + entity)) {
                			_keysWithWarning.add(key + "-" + entity);
                			log.warn("No relationship or attribute <" + key + "> in entity: " + entity);
                		}
                	}
                }
                return null;
            }
            entity = rel.destinationEntity();
        }
        return entity;
    }

    /** Returns the EOEntity for the provided EOEnterpriseObject if one exists
     * 
     * @param eo the EOEnterpriseObject
     * @return the EOEntity from the EOEnterpriseObject
     */
    public static EOEntity entityForEo(EOEnterpriseObject eo) {
        EOClassDescription classDesc = eo.classDescription();
        
        if (classDesc instanceof EOEntityClassDescription)
            return ((EOEntityClassDescription)classDesc).entity();
        return null;
    }

    public static NSArray classPropertiesNotInParent(EOEntity entity, boolean includeAttributes, boolean includeToOneRelationships, boolean includeToManyRelationships) {
        Object parent = entity.parentEntity();
        if (parent == null) { return NSArray.EmptyArray; }
        NSMutableArray ret = new NSMutableArray();
        NSArray parentAttributeNames = (NSArray) entity.parentEntity().attributes().valueForKey("name");
        NSArray attributes = entity.attributes();
        NSArray cpNames = entity.classPropertyNames();

        if (includeAttributes) {
	        for (int i = attributes.count(); i-- > 0;) {
	            EOAttribute att = (EOAttribute) attributes.objectAtIndex(i);
	            String name = att.name();
	            if (cpNames.containsObject(name) && !parentAttributeNames.containsObject(name)) {
	                ret.addObject(att);
	            }
	        }
        }
        
        NSArray parentRelationships = (NSArray) entity.parentEntity().relationships().valueForKey("name");
        NSArray relationships = entity.relationships();
        
        for (int i = relationships.count(); i-- > 0;) {
            EORelationship element = (EORelationship) relationships.objectAtIndex(i);
            if ((element.isToMany() && includeToManyRelationships)
                    || (!element.isToMany() && includeToOneRelationships)) {
                String name = element.name();
                if (cpNames.containsObject(name) && !parentRelationships.containsObject(name)) {
                    ret.addObject(element);
                }
            }
        }
        return ret;
    }

    public static void writeAdaptorOperationsToDisk(NSArray adaptorOps, File file) throws IOException {
        adaptorOperationsLock.lock();
        try {
            ByteArrayOutputStream bous = new ByteArrayOutputStream();
            ObjectOutputStream os;
            NSMutableArray ops = new NSMutableArray();
            for (int i = 0; i < adaptorOps.count(); i++) {
                EOAdaptorOperation a = (EOAdaptorOperation) adaptorOps.objectAtIndex(i);
                ERXAdaptorOperationWrapper wrapper = new ERXAdaptorOperationWrapper(a);
                ops.addObject(wrapper);
            }
            os = new ObjectOutputStream(bous);
            os.writeObject(ops);
            os.flush();
            os.close();
            file.createNewFile();
            ERXFileUtilities.writeInputStreamToFile(new ByteArrayInputStream(bous.toByteArray()), file);
        } finally {
            adaptorOperationsLock.unlock();
        }
    }

    public static NSArray readAdaptorOperationsFromDisk(File dir) throws IOException {
        NSMutableArray ops = new NSMutableArray();

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            try {
                NSArray sops = (NSArray) ois.readObject();
                ops.addObjectsFromArray((NSArray) sops.valueForKey("operation"));
                ois.close();
            } catch (ClassNotFoundException e) {
                throw new IOException("cannot read object, reason "+e.getMessage()); 
            }
        }
        return ops;
    }

    public static void insertAdaptorOperations(NSArray ops) {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec, "TestModel");
        context.lock();
        adaptorOperationsLock.lock();
        EODatabaseChannel dchannel = context.availableChannel();
        EOAdaptorChannel achannel = dchannel.adaptorChannel();
        achannel.adaptorContext().beginTransaction();
        try {
            boolean wasOpen = achannel.isOpen();
            if (!wasOpen) {
                achannel.openChannel();
            }
            achannel.performAdaptorOperations(ops);
            if (!wasOpen) {
                achannel.closeChannel();
            }
        } finally {
            achannel.adaptorContext().commitTransaction();
            context.unlock();
            adaptorOperationsLock.unlock();
            ec.unlock();
        }
    }

    public static NSArray externalNamesForEntity(EOEntity entity, boolean includeParentEntities) {
        if (includeParentEntities) { 
            entity = rootEntityForEntity(entity);
        }
        NSMutableArray entityNames = new NSMutableArray();
        if (entity.subEntities().count() > 0) {
            for (Enumeration it = entity.subEntities().objectEnumerator(); it.hasMoreElements();) {
                EOEntity entity1 = (EOEntity) it.nextElement();
                NSArray names = externalNamesForEntity(entity1, includeParentEntities);
                entityNames.addObjectsFromArray(names);
            }
        }
        entityNames.addObject(entity.externalName()); 
        return ERXArrayUtilities.arrayWithoutDuplicates(entityNames);
    }

    public static NSArray externalNamesForEntityNamed(String entityName, boolean includeParentEntities) {
        return externalNamesForEntity(EOModelGroup.defaultGroup().entityNamed(entityName), includeParentEntities);
    }

    public static EOEntity rootEntityForEntity(EOEntity entity) {
        while (entity.parentEntity() != null) {
            entity = entity.parentEntity();
        }
        return entity;
    }

    public static EOEntity rootEntityForEntityNamed(String entityName) {
        return rootEntityForEntity(EOModelGroup.defaultGroup().entityNamed(entityName));
    }

}
