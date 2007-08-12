//
//  ERXEOAccessUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Sat Feb 22 2003.
//
package er.extensions;

import java.util.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

/**
 * Collection of EOAccess related utilities.
 */
public class ERXEOAccessUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEOAccessUtilities.class);


    /**
     * Finds an entity that is contained in a string. This is used a lot in DirectToWeb.
     * Example: "ListAllStudios"=>Studio
     * @param ec editing context
     * @param string string to look into
     * @return found entity or null
     */
    protected static NSArray entityNames;
    public static EOEntity entityMatchingString(EOEditingContext ec, String string) {
        EOEntity result = null;
        if(string != null) {
            String lowerCaseName = string.toLowerCase();
            if (entityNames == null) {
                EOModelGroup group = modelGroup(ec);
                entityNames = (NSArray)ERXUtilities.entitiesForModelGroup(group).valueForKeyPath("name.toLowerCase");
            }
            NSMutableArray possibleEntities = new NSMutableArray();
            for (Enumeration e = entityNames.objectEnumerator(); e.hasMoreElements();) {
                String lowercaseEntityName = (String)e.nextElement();
                if (lowerCaseName.indexOf(lowercaseEntityName) != -1)
                    possibleEntities.addObject(lowercaseEntityName);
            }
            if (possibleEntities.count() == 1) {
                result = ERXUtilities.caseInsensitiveEntityNamed((String)possibleEntities.lastObject());
            } else if (possibleEntities.count() > 1) {
                ERXArrayUtilities.sortArrayWithKey(possibleEntities, "length");
                if (((String)possibleEntities.objectAtIndex(0)).length() == ((String)possibleEntities.lastObject()).length())
                    log.warn("Found multiple entities of the same length for string: " + string
                             + " possible entities: " + possibleEntities);
                result = ERXUtilities.caseInsensitiveEntityNamed((String)possibleEntities.lastObject());
            }
            if (log.isDebugEnabled())
                log.debug("Found possible entities: " + possibleEntities + " for string: " + string
                          + " result: " + result);
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
     * If ec is null, it will try to get at the session via thread storage and use
     * its defaultEditingContext. This is here now so we can remove the delgate in
     * ERXApplication.
     * @param ec editing context used to locate the model group (can be null)
     */
    
    public static EOModelGroup modelGroup(EOEditingContext ec) {
        if(ec == null) {
            ERXSession s = ERXExtensions.session();
            if(s != null) {
                ec = s.defaultEditingContext();
            }
        }
        EOModelGroup group;
        if(ec == null) {
            group = EOModelGroup.defaultGroup();
        } else {
            group = EOModelGroup.modelGroupForObjectStoreCoordinator((EOObjectStoreCoordinator)ec.rootObjectStore());
        }
        return group;
    }

    /**
     * Similar to the helper in EUUtilities, but allows for null editingContext.
     * @param ec editing context used to locate the model group (can be null)
     * @param entityName entity name
     */
     public static EOEntity entityNamed(EOEditingContext ec, String entityName) {
        EOModelGroup modelGroup = modelGroup(ec);
        return modelGroup.entityNamed(entityName);
    }

    /**
     * Creates an aggregate attribute for a given function name. These can then be
     * used to query on when using raw rows.
     * @param ec editing context used to locate the model group
     * @param function name of the function MAX, MIN, etc
     * @param attributeName name of the attribute
     * @param entityName name of the entity
     * @return aggregate function attribute
     */
    public static EOAttribute createAggregateAttribute(EOEditingContext ec,
                                                       String function,
                                                       String attributeName,
                                                       String entityName) {
        if (function == null)
            throw new IllegalStateException("Function is null.");
        if (attributeName == null)
            throw new IllegalStateException("Attribute name is null.");
        if (entityName == null)
            throw new IllegalStateException("Entity name is null.");
        
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);

        if (entity == null)
            throw new IllegalStateException("Unable find entity named: " + entityName);
        
        EOAttribute attribute = entity.attributeNamed(attributeName);

        if (attribute == null)
            throw new IllegalStateException("Unable find attribute named: " + attributeName
                                            + " for entity: " + entityName);
        
        EOAttribute aggregate = new EOAttribute();
        aggregate.setName("p_object" + function + "Attribute");
        aggregate.setColumnName("p_object" + function + "Attribute");
        aggregate.setClassName("java.lang.Number");
        aggregate.setValueType("i");
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

    /** Given an array of EOs, returns snapshot dictionaries for the given related objects. */
    //CHECKME ak is this description correct?
    public static NSArray snapshotsForObjectsFromRelationshipNamed(NSArray eos,String relKey) {
        NSMutableArray result=new NSMutableArray();
        if (eos.count()>0) {
            String entityName=((EOEnterpriseObject)eos.objectAtIndex(0)).entityName();
            EOEditingContext ec = ((EOEnterpriseObject)eos.objectAtIndex(0)).editingContext();
            EOEntity entity=EOUtilities.entityNamed(ec,entityName);
            EORelationship relationship = entity.relationshipNamed(relKey);
            EOAttribute attribute = (EOAttribute)relationship.sourceAttributes().objectAtIndex(0);
            EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec, entity.model().name());
            String name=attribute.name();
            for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject target=(EOEnterpriseObject)e.nextElement();
                Object value = (context.snapshotForGlobalID(ec.globalIDForObject(target))).valueForKey(name);
                result.addObject(value);
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
