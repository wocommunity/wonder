//
//  ERXEOAccessUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Sat Feb 22 2003.
//
package er.extensions;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAdaptorOperation;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

/**
 * Collection of EOAccess related utilities.
 */
public class ERXEOAccessUtilities {
    /** logging support */
    public static final Logger log = Logger.getLogger(ERXEOAccessUtilities.class);

    /** SQL logger */
    private static Logger sqlLoggingLogger = null;

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
     * Finds an entity that is associated with the table name. When inheritance is used,
     * will return the least derived entity using that table.  This can be used to deal 
     * with database exceptions where you only have the table name to go on. As multiple
     * entities can map to a single table, the results of this method are inexact.
     * 
     * @param ec
     *            editing context
     * @param tableName
     *            table (external) name to find an entity for
     * @return found entity or null
     */
    public static EOEntity entityUsingTable(EOEditingContext ec, String tableName) {
        EOEntity result = null;
        NSMutableArray possibleEntities = new NSMutableArray();
        
        if (tableName != null) {
            NSArray entities = ERXUtilities.entitiesForModelGroup(modelGroup(ec));
            tableName = tableName.toLowerCase();

            for (Enumeration e = entities.objectEnumerator(); e.hasMoreElements();) {
            	EOEntity entity = (EOEntity)e.nextElement();
            	if (entity.externalName() != null)
            	{
                	String lowercaseTableName = entity.externalName().toLowerCase();
                    if (tableName.equals(lowercaseTableName))
                	{
                    	// Prefer the parent entity as long as it is using the same table
                        EOEntity root = entity;
                        while (root != null && root.parentEntity() != null && 
                        	   lowercaseTableName.equals(root.parentEntity().externalName().toLowerCase()))
                            root = root.parentEntity();
                        if ( ! possibleEntities.containsObject(entity))
                        	possibleEntities.addObject(entity);
                	}
            	}
            }

            if (possibleEntities.count() > 0) {
                result = (EOEntity) possibleEntities.lastObject();
            }
            
            if (log.isEnabledFor(Level.WARN) && possibleEntities.count() > 1) 
                log.warn("Found multiple entities: " + possibleEntities.valueForKey("name") + " for table name: " + tableName);

            if (log.isDebugEnabled())
                log.debug("Found possible entities: " + possibleEntities.valueForKey("name") + " for table name: " + tableName + " result: " + result);
        }
        return result;
    }

    /**
     * Method used to determine if a given entity is a shared entity.
     * @param ec editing context
     * @param entityName name of the entity
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
     * @param ec editing context
     * @param modelName name of the model which connects to the database
     *			that has the sequence in it
     * @param sequenceName name of the sequence
     * @return next value in the sequence
     */
    // ENHANCEME: Need a non-oracle specific way of doing this. Should poke around at
    //		the adaptor level and see if we can't find something better.
    public static Number getNextValFromSequenceNamed(EOEditingContext ec,
                                                     String modelName,
                                                     String sequenceName) {
        String sqlString = "select "+sequenceName+".nextVal from dual";
        NSArray array = EOUtilities.rawRowsForSQL(ec, modelName, sqlString);
        if (array.count() == 0) {
            throw new RuntimeException("Unable to generate value from sequence named: " + sequenceName + " in model: " + modelName);            
        }
        NSDictionary dictionary = (NSDictionary)array.objectAtIndex(0);
        NSArray valuesArray = dictionary.allValues();
        return (Number)valuesArray.objectAtIndex(0);
    }

    /**
     * Utility method used to execute arbitrary SQL. This
     * has the advantage over the
     * {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * <code>rawRowsForSQL</code> in that it can be used with
     * other statements besides just SELECT without throwing
     * exceptions.
     * @param ec editing context that determines which model group
     *		and database context to use.
     * @param entityName name of an entity in the model connected
     *		to the database you wish to execute SQL against
     * @param exp SQL expression
     */
    // ENHANCEME: Should support the use of bindings
    // ENHANCEME: Could also support the option of using a seperate EOF stack so as to execute
    //		sql in a non-blocking fashion.
    public static void evaluateSQLWithEntityNamed(EOEditingContext ec, String entityName, String exp) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        evaluateSQLWithEntity(ec, entity, exp);
    }

    /**
    * Utility method used to execute arbitrary SQL. This
     * has the advantage over the
     * {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * <code>rawRowsForSQL</code> in that it can be used with
     * other statements besides just SELECT without throwing
     * exceptions.
     * @param ec editing context that determines which model group
     *		and database context to use.
     * @param entity an entity in the model connected
     *		to the database you wish to execute SQL against
     * @param exp SQL expression
     */
    // ENHANCEME: Should support the use of bindings
    // ENHANCEME: Could also support the option of using a seperate EOF stack so as to execute
    //		sql in a non-blocking fashion.
    public static void evaluateSQLWithEntity(EOEditingContext ec, EOEntity  entity, String exp) {
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
        if (!adaptorChannel.isOpen())
            adaptorChannel.openChannel();
        EOSQLExpressionFactory factory=adaptorChannel.adaptorContext().adaptor().expressionFactory();
        adaptorChannel.evaluateExpression(factory.expressionForString(exp));
    }

    /**
     * Creates the SQL which is used by the provides EOFetchSpecification.
     *
     * @param ec the EOEditingContext
     * @param spec the EOFetchSpecification in question
     *
     * @return the SQL which the EOFetchSpecification would use
     */
    public static String sqlForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec) {
       return sqlExpressionForFetchSpecification(ec,spec,0,-1).statement();
    }

    /**
     * Returns the raw rows for the given EOSQLExpression.
     *
     * @param ec the EOEditingContext
     * @param spec the EOFetchSpecification in question
     * @param modelName the name of the model in question
     * @param expression the EOSQLExpression in question
     *
     * @return array of dictionaries
     */
    public static NSArray rawRowsForSQLExpression(EOEditingContext ec, String modelName, EOSQLExpression expression) {
        EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, modelName);
        dbc.lock();
        NSMutableArray results = null;
        try {
            EOAdaptorChannel channel = dbc.availableChannel().adaptorChannel();
            if (!channel.isOpen())
                channel.openChannel();
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
     * Creates the SQL which is used by the provided EOFetchSpecification, limited by the given range.
     *
     * @param ec the EOEditingContext
     * @param spec the EOFetchSpecification in question
     * @param start start of rows to fetch
     * @param end end of rows to fetch (-1 if not used)
     *
     * @return the EOSQLExpression which the EOFetchSpecification would use
     */
    public static EOSQLExpression sqlExpressionForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec, long start, long end) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        EOModel model = entity.model();
        EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
        EODatabase db = new EODatabase(adaptor);
        EOSQLExpressionFactory sqlFactory = adaptor.expressionFactory();

        NSArray attributes = spec.rawRowKeyPaths();
        if(attributes == null || attributes.count() == 0)
            attributes = entity.attributesToFetch();
        EOSQLExpression sqlExpr = sqlFactory.selectStatementForAttributes(attributes, false, spec, entity);
        String sql = sqlExpr.statement();
        if(end >= 0) {
            String url = (String)model.connectionDictionary().objectForKey("URL");
            if(url != null) {
                if(url.toLowerCase().indexOf("frontbase") != -1) {
                    //add TOP(start, (end - start)) after the SELECT word
                    int index = sql.indexOf("select");
                    if (index == -1) {
                        index = sql.indexOf("SELECT");
                    }
                    index += 6;

                    //FIXME: this works for frontbase, might need to be adjusted for other db servers!
                    StringBuffer buf = new StringBuffer();
                    buf.append(sql.substring(0, index)).append(" TOP(").append(start).append(",").append(end - start).append(") ").append(sql.substring(index + 1, sql.length()));
                    sql = buf.toString();

                } else if(url.toLowerCase().indexOf("mysql") != -1) {
                    sql += " LIMIT " + start + ", " + (end - start);
                }
            }
            sqlExpr.setStatement(sql);
        }

        return sqlExpr;
    }

    /**
     * Returns the number of rows the supplied EOFetchSpecification would return.
     *
     * @param ec the EOEditingContext
     * @param spec the EOFetchSpecification in question
     * @return the number of rows
     */
    public static int rowCountForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec) {
        int rowCount = -1;
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        EOModel model = entity.model();
        EOSQLExpression sql = ERXEOAccessUtilities.sqlExpressionForFetchSpecification(ec, spec, 0, -1);
        String statement = sql.statement();
        int index = statement.toLowerCase().indexOf(" from ");
        statement = "select count(*) " + statement.substring(index, statement.length());
        sql.setStatement(statement);
        NSArray result = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, model.name(), sql);

        if (result.count() > 0) {
            NSDictionary dict = (NSDictionary)result.objectAtIndex(0);
            NSArray values = dict.allValues();
            if (values.count() > 0) {
                Object value = values.objectAtIndex(0);
                if (value instanceof Number) {
                    return ((Number)value).intValue();
                } else {
                    try {
                        int c = Integer.parseInt(value.toString());
                        rowCount = c;
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("sql "+sql+" returned a wrong result, could not convert "+value+" into an int!");
                    }
                }
            } else {
                throw new IllegalStateException("sql "+sql+" returned no result!");
            }
        } else {
            throw new IllegalStateException("sql "+sql+" returned no result!");
        }
        return rowCount;
    }
    

    /**
     * Similar to the helper in EUUtilities, but allows for null editingContext.
     * If ec is null, it will try to get at the session via thread storage and
     * use its defaultEditingContext. This is here now so we can remove the
     * delegate in ERXApplication.
     * 
     * @param ec
     *            editing context used to locate the model group (can be null)
     */
    
    public static EOModelGroup modelGroup(EOEditingContext ec) {
        if (ec == null) {
            ERXSession s = ERXSession.session();
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
     * Creates an aggregate integer attribute for a given function name. These can then
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
    	return ERXEOAccessUtilities.createAggregateAttribute(ec, function, attributeName, entityName, Number.class, "i");
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
     * @param valueClass the java class of this attribute's values
     * @param valueType the EOAttribute value type
     * @return aggregate function attribute
     */
    public static EOAttribute createAggregateAttribute(EOEditingContext ec, String function, String attributeName, String entityName, Class valueClass, String valueType) {
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
        aggregate.setClassName(valueClass.getName());
        if (valueType != null) {
        	aggregate.setValueType(valueType);
        }
        aggregate.setReadFormat(function + "(t0." + attribute.columnName() + ")");
        return aggregate;
    }

    /** creates SQL to create tables for the specified Entities. This can be used with EOUtilities rawRowsForSQL method to
    * create the tables.
    *
    * @param entities a NSArray containing the entities for which create table statements should be generated or null
    * if all entitites in the model should be used.
    * @param modelName the name of the EOModel
    * @param optionsCreate a NSDictionary containing the different options. Possible keys are
    * <ol><li>DropTablesKey</li>
    * <ol><li>DropPrimaryKeySupportKey</li>
    * <ol><li>CreateTablesKey</li>
    * <ol><li>CreatePrimaryKeySupportKey</li>
    * <ol><li>PrimaryKeyConstraintsKey</li>
    * <ol><li>ForeignKeyConstraintsKey</li>
    * <ol><li>CreateDatabaseKey</li>
    * <ol><li>DropDatabaseKey</li>
    *<br/><br>Possible values are <code>YES</code> and <code>NO</code>
    *
    * @return a <code>String</code> containing SQL statements to create tables
    */
    public static String createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray entities, String modelName, NSDictionary optionsCreate) {
        //get the JDBCAdaptor
        EODatabaseContext dc = EOUtilities.databaseContextForModelNamed(ERXEC.newEditingContext(),
                                                                        modelName);
        EOAdaptorContext ac = dc.adaptorContext();
        //ak: stupid trick to get around having to link to JDBCAdaptor
        EOSynchronizationFactory sf = (EOSynchronizationFactory)NSKeyValueCodingAdditions.Utility.valueForKeyPath(ac, "adaptor.plugIn.createSynchronizationFactory");
        EOModel m = modelGroup(null).modelNamed(modelName);
        Enumeration e = m.entities().objectEnumerator();
        entities = entities == null ? new NSMutableArray() : entities;

        if (entities == null) {
            NSMutableArray ar = new NSMutableArray();
            while (e.hasMoreElements()) {
                EOEntity currentEntity = (EOEntity) e.nextElement();
                if (!(currentEntity.name().startsWith("EO") && currentEntity.name().endsWith("Prototypes"))) {
                    //we do not want to add EOXXXPrototypes entities
                    ar.addObject(currentEntity);
                }
            }
            entities = ar;
        }
        return sf.schemaCreationScriptForEntities(entities, optionsCreate);
    }

    /** creates SQL to create tables for the specified Entities. This can be used with EOUtilities rawRowsForSQL method to
    * create the tables.
    *
    * @param entities a NSArray containing the entities for which create table statements should be generated or null
    * if all entitites in the model should be used.
    * @param modelName the name of the EOModel
    * <br/><br/>This method uses the following defaults options:
    * <ol><li>DropTablesKey=YES</li>
    * <ol><li>DropPrimaryKeySupportKey=YES</li>
    * <ol><li>CreateTablesKey=YES</li>
    * <ol><li>CreatePrimaryKeySupportKey=YES</li>
    * <ol><li>PrimaryKeyConstraintsKey=YES</li>
    * <ol><li>ForeignKeyConstraintsKey=YES</li>
    * <ol><li>CreateDatabaseKey=NO</li>
    * <ol><li>DropDatabaseKey=NO</li>
    *<br/><br>Possible values are <code>YES</code> and <code>NO</code>
    *
    * @return a <code>String</code> containing SQL statements to create tables
    */
    public static String createSchemaSQLForEntitiesInModelWithName(NSArray entities, String modelName) {
        NSMutableDictionary optionsCreate = new NSMutableDictionary();
        optionsCreate.setObjectForKey("YES", "DropTablesKey");
        optionsCreate.setObjectForKey("YES", "DropPrimaryKeySupportKey");
        optionsCreate.setObjectForKey("YES", "CreateTablesKey");
        optionsCreate.setObjectForKey("YES", "CreatePrimaryKeySupportKey");
        optionsCreate.setObjectForKey("YES", "PrimaryKeyConstraintsKey");
        optionsCreate.setObjectForKey("YES", "ForeignKeyConstraintsKey");
        optionsCreate.setObjectForKey("NO", "CreateDatabaseKey");
        optionsCreate.setObjectForKey("NO", "DropDatabaseKey");
        return createSchemaSQLForEntitiesInModelWithNameAndOptions(entities, modelName, optionsCreate);
    }

    /**
    * Tries to recover from a {@link EOGeneralAdaptorException}. 
    *
    * @param exception the exception as recieved from saveChanges()
    * @param editingContext editing context that created the error
    * @return true if the error could be handled.
    */
    public static boolean recoverFromAdaptorException(EOEditingContext editingContext, EOGeneralAdaptorException exception) {
        boolean wasHandled = false;
        NSDictionary userInfo = (NSDictionary)exception.userInfo();
        if(userInfo != null) {
            String failureKey = (String)userInfo.objectForKey("EOAdaptorFailureKey");
            if ("EOAdaptorOptimisticLockingFailure".equals(failureKey)) {
                EOAdaptorOperation adaptorOperation = (EOAdaptorOperation) userInfo.objectForKey("EOFailedAdaptorOperationKey");
                EODatabaseOperation databaseOperation = (EODatabaseOperation) userInfo.objectForKey("EOFailedDatabaseOperationKey");
                if (adaptorOperation != null && databaseOperation != null) {
                    NSDictionary changedValues = adaptorOperation.changedValues();
                    NSDictionary snapshot = databaseOperation.dbSnapshot();
                    
                    if (log.isDebugEnabled()) log.debug("snapshot"+ snapshot);

                    EOEntity entity = adaptorOperation.entity();
                    String entityName = entity.name();
                    
                    if (log.isDebugEnabled()) log.debug("entityName: "+ entityName);

                    NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
                    EOQualifier qualifier = ERXTolerantSaver.qualifierWithSnapshotAndPks(primaryKeyAttributes, snapshot);
                    EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, null);
                    fs.setRefreshesRefetchedObjects(true);

                    NSArray objects = editingContext.objectsWithFetchSpecification(fs);
                    editingContext.revert();
                    wasHandled = true;
                } else {
                    log.error("Missing EOFailedAdaptorOperationKey or EOFailedDatabaseOperationKey in " + exception + ": "+exception.userInfo());
                }
            }
        }
        return wasHandled;
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
     * Returns true if the exception is an optimistic locking exception.
     * 
     * @param e
     *            the exception as recieved from saveChanges()
     * @return true if the error could be handled.
     */
    public static boolean isOptimisticLockingFailure(EOGeneralAdaptorException e) {
        boolean wasHandled = false;
        NSDictionary userInfo = e.userInfo();
        if(userInfo != null) {
            String eType = (String)userInfo.objectForKey(EOAdaptorChannel.AdaptorFailureKey);
            if (EOAdaptorChannel.AdaptorOptimisticLockingFailure.equals(eType)) {
                EOAdaptorOperation adaptorOp = (EOAdaptorOperation) userInfo.objectForKey(EOAdaptorChannel.FailedAdaptorOperationKey);
                EODatabaseOperation databaseOp = (EODatabaseOperation) userInfo.objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
                wasHandled = (adaptorOp != null && databaseOp != null);
            } else {
                log.error("Missing EOFailedAdaptorOperationKey or EOFailedDatabaseOperationKey in " + e + ": " + userInfo);
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
     * Utility method to generate a new primary key dictionary using
     * the adaptor for a given entity. This is can be handy if you
     * need to have a primary key for an object before it is saved to
     * the database. This method uses the same method that EOF uses
     * by default for generating primary keys. See
     * {@link ERXGeneratesPrimaryKeyInterface} for more information
     * about using a newly created dictionary as the primary key for
     * an enterprise object.
     * @param ec editing context
     * @param entityName name of the entity to generate the primary
     *		key dictionary for.
     * @return a dictionary containing a new primary key for the given
     *		entity.
     */
    public static NSDictionary primaryKeyDictionaryForEntity(EOEditingContext ec, String entityName) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        NSDictionary primaryKey = null;
        try {
            dbContext.lock();
            EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
            if (!adaptorChannel.isOpen())
                adaptorChannel.openChannel();
            NSArray arr = adaptorChannel.primaryKeysForNewRowsWithEntity(1, entity);
            if(arr != null)
                primaryKey = (NSDictionary)arr.lastObject();
            else
                log.warn("Could not get primary key for entity: " + entityName + " exception");
            dbContext.unlock();
        } catch (Exception e) {
            dbContext.unlock();
            log.error("Caught exception when generating primary key for entity: " + entityName + " exception: " + e, e);
        }
        return primaryKey;
    }
    
    /**
     * Creates an array containing all of the primary
     * keys of the given objects.
     * @param eos array of enterprise objects
     */
    public static NSArray primaryKeysForObjects(NSArray eos) {
        NSMutableArray result=new NSMutableArray();
        if (eos.count()>0) {
            for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject target=(EOEnterpriseObject)e.nextElement();
                NSDictionary pKey=EOUtilities.primaryKeyForObject(target.editingContext(),target);
                result.addObject(pKey.allValues().objectAtIndex(0));
            }
        }
        return result;
    }

    /**
     * Crude hack to get at the end of a relationship path.
     * @param relationship
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
     */
    public static NSArray attributePathForKeyPath(EOEntity entity, String keyPath) {
        NSMutableArray result = new NSMutableArray();
        String[] parts = keyPath.split("\\.");
        String part;
        for (int i = 0; i < parts.length - 1; i++) {
            part = parts[i];
            EORelationship relationship = entity.anyRelationshipNamed(part);
            if(relationship == null) {
            	// CHECKME AK:  it would probably be better to return null 
            	// to indocate that this is not a valid path?
            	return NSArray.EmptyArray;
            }
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
            	NSArray path = attributePathForKeyPath(entity, relationship.definition());
            	result.addObjectsFromArray(path);
            	return result;
            } else {
                attribute = ((EOJoin) relationship.joins().lastObject()).sourceAttribute();
            }
         }
        result.addObject(attribute);
        return result;
    }

    /**
     * Creates a where clause string " someKey IN ( someValue1,...)".
     */
    public static String sqlWhereClauseStringForKey(EOSQLExpression e, String key, NSArray valueArray) {
        StringBuffer sb=new StringBuffer();
        sb.append(e.sqlStringForAttributeNamed(key));
        sb.append(" IN ");
        sb.append("(");
        for (int i = 0; i < valueArray.count(); i++ ) {
            if ( i > 0 )
                sb.append(", ");
            sb.append(e.sqlStringForValue(valueArray.objectAtIndex(i), key));
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
     */
    public static EODatabaseContext databaseContextForEntityNamed(EOObjectStoreCoordinator osc, String entityName) {
        EOModel model = EOModelGroup.modelGroupForObjectStoreCoordinator(osc).entityNamed(entityName).model();
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, osc);
        return dbc;
    }

    /**
     * Returns the last entity for the given key path. If the path is empty or null, returns the given entity.
     * @param entity
     * @param keyPath
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

    /**
     * Creates an AND qualifier of EOKeyValueQualifiers for every keypath in the given array of attributes.
     *
     * @author ak
     */
    public static EOQualifier qualifierFromAttributes(NSArray attributes, NSDictionary values) {
        NSMutableArray qualifiers = new NSMutableArray();
        EOQualifier result = null;
        if(attributes.count() > 0) {
            for (Enumeration i = attributes.objectEnumerator(); i.hasMoreElements();) {
                EOAttribute key = (EOAttribute) i.nextElement();
                Object value = values.objectForKey(key.name());
                qualifiers.addObject(new EOKeyValueQualifier(key.name(), EOQualifier.QualifierOperatorEqual, value));
            }
            result = new EOAndQualifier(qualifiers);
        }
        return result;
    }

    /**
     * Filters a list of relationships for only the ones that
     * have a given EOAttribute as a source attribute. 
     * @param attrib EOAttribute to filter source attributes of
     *      relationships.
     * @return filtered array of EORelationship objects that have
     *      the given attribute as the source attribute.
     */
    public static NSArray relationshipsForAttribute(EOEntity entity, EOAttribute attrib) {
        NSMutableArray arr = new NSMutableArray();
        int cnt = entity.relationships().count();
        for(int i=0; i<cnt; i++){
            EORelationship rel = (EORelationship)entity.relationships().objectAtIndex(i);
            NSArray attribs = rel.sourceAttributes();
            if(attribs.containsObject(attrib)){
                arr.addObject(rel);
            }
        }
        return arr;
    }


    public static EOEnterpriseObject refetchFailedObject(EOEditingContext ec, EOGeneralAdaptorException e) {
        EOAdaptorOperation adaptorOp = (EOAdaptorOperation) e.userInfo().objectForKey(EOAdaptorChannel.FailedAdaptorOperationKey);
        EODatabaseOperation databaseOp = (EODatabaseOperation) e.userInfo().objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
        NSDictionary dbSnapshot = databaseOp.dbSnapshot();
        EOEntity entity = adaptorOp.entity();
        String entityName = entity.name();
        EOGlobalID gid = entity.globalIDForRow(dbSnapshot);
        EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
        // EOUtilities.databaseContextForModelNamed(ec, eo.entityName()).forgetSnapshotForGlobalID(gid);
        ec.refaultObject(eo);
        // NOTE AK: I think we can just return the object here,
        // as the next time it is accessed the fault will get
        NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
        EOQualifier qualifier = ERXEOAccessUtilities.qualifierFromAttributes(primaryKeyAttributes, dbSnapshot);
        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, null);
        fs.setRefreshesRefetchedObjects(true);
        NSArray objs = ec.objectsWithFetchSpecification(fs);
        eo = null;
        if (objs.count() == 1) {
            eo = (EOEnterpriseObject) objs.objectAtIndex(0);
            if (log.isDebugEnabled()) {
                log.debug("failedEO: "+ eo);
            }
        } else if(objs.count() == 0) {
            throw new EOObjectNotAvailableException("Can't recover: Object was deleted: " + fs);
        } else {
            throw new EOUtilities.MoreThanOneException("Can't recover: More than one object found: " + objs);
        }
        return eo;
    }

    /**
     * Method used to apply a set of changes to a re-fetched eo.
     * This method is used to re-apply changes to a given eo after
     * it has been refetched.
     *
     * @param eo enterprise object to have the changes re-applied to.
     */
    public static void reapplyChanges(EOEnterpriseObject eo, EOGeneralAdaptorException e) {
        EOAdaptorOperation adaptorOp = (EOAdaptorOperation) e.userInfo().objectForKey(EOAdaptorChannel.FailedAdaptorOperationKey);
        NSDictionary changedValues = adaptorOp.changedValues();
        EOEntity entity = ERXEOAccessUtilities.entityForEo(eo);
        EOEditingContext ec = eo.editingContext();
        NSArray keys = changedValues.allKeys();
        NSMutableSet relationships = new NSMutableSet();
        
        for (int i=0; i<keys.count(); i++) {
            String key = (String)keys.objectAtIndex(i);
            EOAttribute attrib = entity.attributeNamed(key);
            if (attrib != null) {
                Object val = changedValues.objectForKey(key);
                if (entity.classProperties().containsObject(attrib)) {
                    eo.takeValueForKey(val, key);
                }
                NSArray relsUsingAttrib = ERXEOAccessUtilities.relationshipsForAttribute(entity, attrib);
                relationships.addObjectsFromArray(relsUsingAttrib);
            } else {
                log.error("Changed value found that isn't an attribute: " + key + "->" + changedValues.objectForKey(key));
            }
        }

        for (Enumeration enumerator = relationships.objectEnumerator(); enumerator.hasMoreElements();) {
            EORelationship relationship = (EORelationship) enumerator.nextElement();
            NSMutableDictionary pk = EOUtilities.destinationKeyForSourceObject(ec, eo, relationship.name()).mutableClone();
            for (int i=0; i<keys.count(); i++) {
                String key = (String)keys.objectAtIndex(i);
                if(pk.objectForKey(key) != null) {
                    Object val = changedValues.objectForKey(key);
                    pk.setObjectForKey(val, key);
                }
            }
            EOEntity destEnt = relationship.destinationEntity();
            EOGlobalID gid = destEnt.globalIDForRow(pk);
            if(gid != null) {
                EOEnterpriseObject destEO = ec.faultForGlobalID(gid, ec);
                eo.takeValueForKey(destEO, relationship.name());
            } else {
                throw new NullPointerException("Gid is null: " + pk);
            }
        }
    }

    /**
     * Deals with the nitty-gritty of direct row manipulation by
     * correctly opening, closing, locking and unlocking the 
     * needed EOF objects for direct row manipulation. Wraps the
     * actions in a transaction.
     * The API is not really finalized, if someone has a better idea
     * he's welcome to change it.
     * @author ak
     */
    public static abstract class ChannelAction {
 
        protected abstract int doPerform(EOAdaptorChannel channel);

        public int perform(EOEditingContext ec, String modelName) {
            boolean wasOpen = true;
            EOAdaptorChannel channel = null;
            int rows = 0;
            ec.lock();
            try {
                EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, modelName);
                dbc.lock();
                try {
                    channel = dbc.availableChannel().adaptorChannel();
                    wasOpen = channel.isOpen();
                    if(!wasOpen) {
                        channel.openChannel();
                    }
                    channel.adaptorContext().beginTransaction();
                    try {
                        rows = doPerform(channel);
                        channel.adaptorContext().commitTransaction();
                    } catch(RuntimeException ex) {
                        channel.adaptorContext().rollbackTransaction();
                        throw ex;
                    } 
                } finally {
                    if(!wasOpen) {
                        channel.closeChannel();
                    }
                    dbc.unlock();
                }
            } finally {
                ec.unlock();
            }
            return rows;
        }
    }

    /**
     * Deletes rows described by the qualifier. Note that the values and the qualifier need to be on an attribute 
     * and not on a relationship level. I.e. you need to give relationshipForeignKey = pk of object instead of 
     * relatedObject = object
     * @param ec
     * @param entityName
     * @param qualifier
     */
    public static int deleteRowsDescribedByQualifier(EOEditingContext ec, String entityName, 
            final EOQualifier qualifier) {
        final EOEntity entity = entityNamed(ec, entityName);
        ChannelAction action = new ChannelAction() {
            protected int doPerform(EOAdaptorChannel channel) {
                return channel.deleteRowsDescribedByQualifier(qualifier, entity);
            }
        };
        return action.perform(ec, entity.model().name());
    }

    /**
     * Updates rows described by the qualifier. Note that the values and the qualifier need to be on an attribute 
     * and not on a relationship level. I.e. you need to give relationshipForeignKey = pk of object instead of 
     * relatedObject = object. The newValues dictionaries also holds foreign keys, not objects.
     * @param ec
     * @param entityName
     * @param qualifier
     * @param newValues
     */
    public static int updateRowsDescribedByQualifier(EOEditingContext ec, String entityName, 
            final EOQualifier qualifier, final NSDictionary newValues) {
        final EOEntity entity = entityNamed(ec, entityName);
        ChannelAction action = new ChannelAction() {
            protected int doPerform(EOAdaptorChannel channel) {
                return channel.updateValuesInRowsDescribedByQualifier(newValues, qualifier, entity);
            }
        };
        return action.perform(ec, entity.model().name());
    }

    /**
     * Insert row described dictionary. 
     * @param ec
     * @param entityName
     * @param newValues
     */
    public static int insertRow(EOEditingContext ec, String entityName, 
            final NSDictionary newValues) {
        final EOEntity entity = entityNamed(ec, entityName);
        ChannelAction action = new ChannelAction() {
            protected int doPerform(EOAdaptorChannel channel) {
            	channel.insertRow(newValues, entity);
                return 1;
            }
        };
        return action.perform(ec, entity.model().name());
    }

    /**
     * Insert rows described the array of dictionaries. 
     * @param ec
     * @param entityName
     * @param newValues
     */
    public static int insertRows(EOEditingContext ec, String entityName, 
            final List<NSDictionary> newValues) {
        final EOEntity entity = entityNamed(ec, entityName);
        ChannelAction action = new ChannelAction() {
            protected int doPerform(EOAdaptorChannel channel) {
            	int insert = 0;
            	for (NSDictionary dictionary : newValues) {
                	channel.insertRow(dictionary, entity);
                	insert++;
				}
                return insert;
            }
        };
        return action.perform(ec, entity.model().name());
    }

    /**
     * Creates count new primary keys for the entity. 
     * @param ec
     * @param entityName
     * @param count
     */
	public static NSArray primaryKeysForNewRows(EOEditingContext ec, String entityName, final int count) {
		final NSMutableArray result = new NSMutableArray();
		final EOEntity entity = entityNamed(ec, entityName);
		ChannelAction action = new ChannelAction() {
			protected int doPerform(EOAdaptorChannel channel) {
				NSArray keys = channel.primaryKeysForNewRowsWithEntity(count, entity);
				result.addObjectsFromArray(keys);
				return count;
			}
		};
		action.perform(ec, entity.model().name());
		return result;
	}

    /**
	 * Tries to get the plugin name for a JDBC based model.
	 * 
	 * @param model
	 */
    public static String guessPluginName(EOModel model) {
        String pluginName = null;
        // If you don't explicitly set a prototype name, and you don't
        // declare a preferred databaseConfig,
        // then attempt to load Wonder-style prototypes with the name
        // EOJDBC(driverName)Prototypes.
        if ("JDBC".equals(model.adaptorName())) {
            NSDictionary connectionDictionary = model.connectionDictionary();
            if (connectionDictionary != null) {
            	pluginName = guessPluginNameForConnectionDictionary(connectionDictionary);
            }
        }
        return pluginName;
    }
    
    /**
     * Tries to get the plugin name for a connection dictionary.
     * @param connectionDictionary the connectionDictionary to guess a plugin name for
     * @return the plugin name
     */
    public static String guessPluginNameForConnectionDictionary(NSDictionary connectionDictionary) {
    	String pluginName = null;
	    String jdbcUrl = (String) connectionDictionary.objectForKey("URL");
	    if (jdbcUrl != null) {
	        pluginName = (String) connectionDictionary.objectForKey("plugin");
	        if (pluginName == null || pluginName.trim().length() == 0) {
	            pluginName = JDBCPlugIn.plugInNameForURL(jdbcUrl);
	            if (pluginName == null) {
	            	// AK: this is a hack that is totally bogus....
	                int firstColon = jdbcUrl.indexOf(':');
	                int secondColon = jdbcUrl.indexOf(':', firstColon + 1);
	                if (firstColon != -1 && secondColon != -1) {
	                    pluginName = jdbcUrl.substring(firstColon + 1, secondColon);
	                }
	            } else {
	                pluginName = ERXStringUtilities.lastPropertyKeyInKeyPath(pluginName);
	                pluginName = pluginName.replaceFirst("PlugIn", "");
	            }
	        }
	    }
        if (pluginName != null && pluginName.trim().length() == 0) {
          pluginName = null;
        }
        return pluginName;
    }

    /**
     * Utility method to make a shared entity editable. This
     * can be useful if you want to have an adminstration
     * application that can edit shared enterprise objects
     * and need a way at start up to disable the sharing
     * constraints.
     * @param entityName name of the shared entity to make
     *		shareable.
     */
    // FIXME: Should have to pass in an editing context so that the
    //		correct model group and shared ec will be used.
    // FIXME: Should also dump all of the currently shared eos from
    //		the shared context.
    public static void makeEditableSharedEntityNamed(String entityName) {
        EOEntity e = ERXEOAccessUtilities.entityNamed(null, entityName);
        if (e != null && e.isReadOnly()) {
            e.setReadOnly(false);
            e.setCachesObjects(false);
            // Remove all of the shared fetch specs
            for (Enumeration fetchSpecNameObjectEnumerator = e.sharedObjectFetchSpecificationNames().objectEnumerator();
                 fetchSpecNameObjectEnumerator.hasMoreElements();) {
                e.removeSharedObjectFetchSpecificationByName((String)fetchSpecNameObjectEnumerator.nextElement());
            } 
        } else if (e == null) {
            log.warn("makeEditableSharedEntityNamed: unable to find entity named: " + entityName);
        } else {
            log.warn("makeEditableSharedEntityNamed: entity already editable: " + entityName);
        }
    }
}
