//
//  ERXEOAccessUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Sat Feb 22 2003.
//
package er.extensions.eof;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOAdaptorChannel;
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
import com.webobjects.eoaccess.EOProperty;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
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
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSDelegate;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

import er.extensions.appserver.ERXSession;
import er.extensions.eof.listener.ERXEOExecutionListenerDumbImpl;
import er.extensions.eof.listener.IERXEOExecutionListener;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXThreadStorage;
import er.extensions.foundation.ERXUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.jdbc.ERXSQLHelper;
import er.extensions.statistics.ERXStats;
import er.extensions.statistics.ERXStats.Group;

/**
 * Collection of EOAccess related utilities.
 * 
 * EOAccess provides the data access mechanisms for the Enterprise Objects technology.
 */
public class ERXEOAccessUtilities {
    /** logging support */
    public static final Logger log = Logger.getLogger(ERXEOAccessUtilities.class);
    
    /** SQL logger */
    private static Logger sqlLoggingLogger = null;

    private static final AtomicReference<IERXEOExecutionListener> listener = new AtomicReference<IERXEOExecutionListener>(new ERXEOExecutionListenerDumbImpl());

    public static void setListener(IERXEOExecutionListener aListener) {
        listener.set(aListener);
    }

    /**
     * Finds an entity that is contained in a string. This is used a lot in
     * DirectToWeb. Example: "ListAllStudios"=>Studio
     * 
     * @param ec
     *            editing context
     * @param string
     *            string to look into
     * @return found entity or <code>null</code>
     */
    public static EOEntity entityMatchingString(EOEditingContext ec, String string) {
        EOEntity result = null;
        if (string != null) {
            String lowerCaseName = string.toLowerCase();
            EOModelGroup group = modelGroup(ec);
            NSArray<String> entityNames = (NSArray<String>) ERXUtilities.entitiesForModelGroup(group).valueForKeyPath("name.toLowerCase");
            NSMutableArray<String> possibleEntities = new NSMutableArray<String>();
            for (String lowercaseEntityName : entityNames) {
                if (lowerCaseName.indexOf(lowercaseEntityName) != -1) possibleEntities.addObject(lowercaseEntityName);
            }
            if (possibleEntities.count() == 1) {
                result = ERXUtilities.caseInsensitiveEntityNamed(possibleEntities.lastObject());
            } else if (possibleEntities.count() > 1) {
                ERXArrayUtilities.sortArrayWithKey(possibleEntities, "length");
                if (possibleEntities.objectAtIndex(0).length() == possibleEntities.lastObject().length())
                        log.warn("Found multiple entities of the same length for string: " + string + " possible entities: "
                                + possibleEntities);
                result = ERXUtilities.caseInsensitiveEntityNamed(possibleEntities.lastObject());
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
     * @return found entity or <code>null</code>
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
                        while (root.parentEntity() != null && 
                        	   lowercaseTableName.equalsIgnoreCase(root.parentEntity().externalName()))
                            root = root.parentEntity();
                        if (! possibleEntities.containsObject(root))
                        	possibleEntities.addObject(root);
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
     * 
     * @param ec
     *            editing context
     * @param entityName
     *            name of the entity
     * @return if the entity is a shared entity
     * @throws IllegalStateException if the entityName provided is null
     */
    public static boolean entityWithNamedIsShared(EOEditingContext ec, String entityName) {
        if (entityName == null)
            throw new IllegalStateException("Entity name argument is null for method: entityWithNamedIsShared");
        EOEntity entity = entityNamed(ec, entityName);
        return entity != null && entity.sharedObjectFetchSpecificationNames().count() > 0;
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
     * @deprecated
     */
    // ENHANCEME: Need a non-oracle specific way of doing this. Should poke
    // around at
    //		the adaptor level and see if we can't find something better.
    @Deprecated
    public static Number getNextValFromSequenceNamed(EOEditingContext ec, String modelName, String sequenceName) {
    	EODatabaseContext dbContext = EOUtilities.databaseContextForModelNamed(ec, modelName);
    	return ERXSQLHelper.newSQLHelper(dbContext).getNextValFromSequenceNamed(ec, modelName, sequenceName);
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
        dbContext.lock();
        try {
	        EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
	        if (!adaptorChannel.isOpen()) {
	        	adaptorChannel.openChannel();
	        }
	        EOSQLExpressionFactory factory = adaptorChannel.adaptorContext().adaptor().expressionFactory();
			if (ERXEOAccessUtilities.log.isInfoEnabled()) {
				ERXEOAccessUtilities.log.info("Executing " + exp);
			}
	        // If channel.evaluateExpression throws when committing, it won't close the JDBC transaction
	        // Probably a bug in JDBCChannel, but we must take care of it
	        boolean contextHadOpenTransaction = adaptorChannel.adaptorContext().hasOpenTransaction();
			try {
				adaptorChannel.evaluateExpression(factory.expressionForString(exp));        
			}
			catch (EOGeneralAdaptorException e) {
				if (adaptorChannel.adaptorContext().hasOpenTransaction() && ! contextHadOpenTransaction) {
					adaptorChannel.adaptorContext().rollbackTransaction();
				}
				throw e;
			}
        }
        finally {
        	dbContext.unlock();
        }
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
     * Returns the raw rows for the given EOSQLExpression.  When possible,
     * you should use the variant of this method that requires you to pass
     * in the array of EOAttributes you are fetching. If you do not pass in
     * attributes, this will use channel.describeResults(), which can produce
     * attributes that may not be able to be faulted back into EO's because
     * of case mismatches.
     * 
     * @param ec
     *            the EOEditingContext
     * @param modelName
     *            the name of the model in question
     * @param expression
     *            the EOSQLExpression in question
     * 
     * @return array of dictionaries
     */
    public static NSArray<NSDictionary> rawRowsForSQLExpression(EOEditingContext ec, String modelName, EOSQLExpression expression) {
    	EOModelGroup modelGroup = EOUtilities.modelGroup(ec);
        EOModel model = modelGroup.modelNamed(modelName);
    	return ERXEOAccessUtilities.rawRowsForSQLExpression(ec, model, expression, null);
    }

    /**
     * Returns the raw rows for the given EOSQLExpression.
     * 
     * @param ec
     *            the EOEditingContext
     * @param model
     *            the model in question
     * @param expression
     *            the EOSQLExpression to fetch with
     * @param attributes the attributes to fetch
     * 
     * @return array of dictionaries
     */
    public static NSArray rawRowsForSQLExpression(EOEditingContext ec, EOModel model, EOSQLExpression expression, NSArray<EOAttribute> attributes) {
        NSArray results = NSArray.EmptyArray;
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
        
        dbc.lock();
        try {
            results = _rawRowsForSQLExpression(dbc, expression, attributes);
        } 
        catch (Exception localException) {
            if (dbc._isDroppedConnectionException(localException)) {
                try {
                    dbc.database().handleDroppedConnection();
                    results = _rawRowsForSQLExpression(dbc, expression, attributes);
                }
                catch(Exception ex) {
                    throw NSForwardException._runtimeExceptionForThrowable(ex);
                }
            }
            else {
            	throw NSForwardException._runtimeExceptionForThrowable(localException);
            }
        }
        finally {
            dbc.unlock();
        }
        return results;
    }
    
    private static NSArray _rawRowsForSQLExpression(EODatabaseContext dbc, EOSQLExpression expression, NSArray<EOAttribute> attributes) {
        NSMutableArray results = null;
        EOAdaptorChannel channel = dbc.availableChannel().adaptorChannel();
        
        if (!channel.isOpen()) 
            channel.openChannel();
        // If channel.evaluateExpression throws when committing, it won't close the JDBC transaction
        // Probably a bug in JDBCChannel, but we must take care of it
        boolean contextHadOpenTransaction = channel.adaptorContext().hasOpenTransaction();
		try {
			channel.evaluateExpression(expression);       
		}
		catch (EOGeneralAdaptorException e) {
			if (channel.adaptorContext().hasOpenTransaction() && ! contextHadOpenTransaction) {
				channel.adaptorContext().rollbackTransaction();
			}
			throw e;
		}
        
        if (attributes == null) {
            channel.setAttributesToFetch(channel.describeResults());
        }
        else {
            channel.setAttributesToFetch(attributes);
        }
        
        try {
            results = new NSMutableArray();
            NSDictionary row;
            while ((row = channel.fetchRow()) != null)
                results.addObject(row);
        }
        catch (EOGeneralAdaptorException ex) {
            channel.cancelFetch();
            throw ex;
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
    	EOSQLExpression expression = null;
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        EOModel model = entity.model();
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
        dbc.lock();
        try {
        	ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(ec, model.name());
        	expression = sqlHelper.sqlExpressionForFetchSpecification(ec, spec, start, end);
        }
        catch (Exception e) {
        	throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
        finally {
        	dbc.unlock();
        }
        
        return expression;
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
    	EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        if (entity == null)
            throw new java.lang.IllegalStateException("entity could not be found for name \""+spec.entityName()+"\". Checked EOModelGroup for loaded models.");

    	EOModel model = entity.model();

    	EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
    	int results = 0;

    	dbc.lock();
    	try {
    		results = ERXSQLHelper.newSQLHelper(ec, model.name()).rowCountForFetchSpecification(ec, spec);
    	}
    	catch (Exception localException) {
    		if (dbc._isDroppedConnectionException(localException)) {
    			try {
    				dbc.database().handleDroppedConnection();
    				results = ERXSQLHelper.newSQLHelper(ec, model.name()).rowCountForFetchSpecification(ec, spec);
    			}
    			catch (Exception ex) {
    				throw NSForwardException._runtimeExceptionForThrowable(ex);
    			}
    		}
    		else {
    			throw NSForwardException._runtimeExceptionForThrowable(localException);
    		}
    	}
    	finally {
    		dbc.unlock();
    	}

    	return results;
    }

    /**
     * Similar to the helper in EOUtilities, but allows for <code>null</code> editingContext.
     * If ec is <code>null</code>, it will try to get at the session via thread storage and
     * use its defaultEditingContext. This is here now so we can remove the
     * delegate in ERXApplication.
     * 
     * @param ec
     *            editing context used to locate the model group (can be <code>null</code>)
     * @return the model group associated with the editing context's root object store
     */
    public static EOModelGroup modelGroup(EOEditingContext ec) {
    	if (ec == null && !ERXThreadStorage.wasInheritedFromParentThread()) {
			// this can be problematic, if called from a background thread with ERXThreadStorage.useInheritableThreadLocal=true, 
    		// which is the default. In my case it was called indirectly from ERXEntityClassDescription.Factory.classDescriptionNeededForEntityName 
    		// resulting in locking problems
    		
            WOSession s = ERXSession.anySession();
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
     * Similar to the helper in EOUtilities, but allows for <code>null</code> editingContext.
     * 
     * @param ec
     *            editing context used to locate the model group (can be <code>null</code>)
     * @param entityName
     *            entity name
     * @return the entity with the specified name
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
    	return ERXEOAccessUtilities.createAggregateAttribute(ec, function, attributeName, entityName, valueClass, valueType, null, null);
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
     * @param aggregateName the name to assign to the aggregate column in the query
     * @param valueClass the java class of this attribute's values
     * @param valueType the EOAttribute value type
     * @return aggregate function attribute
     */
    public static EOAttribute createAggregateAttribute(EOEditingContext ec, String function, String attributeName, String entityName, Class valueClass, String valueType, String aggregateName) {
    	return ERXEOAccessUtilities.createAggregateAttribute(ec, function, attributeName, entityName, valueClass, valueType, aggregateName, null);
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
     * @param aggregateName the name to assign to the aggregate column in the query
     * @param valueClass the java class of this attribute's values
     * @param valueType the EOAttribute value type
     * @param entityTableAlias the "t0"-style name of the attribute in this query (or null for "t0")  
     * @return aggregate function attribute
     */
    public static EOAttribute createAggregateAttribute(EOEditingContext ec, String function, String attributeName, String entityName, Class valueClass, String valueType, String aggregateName, String entityTableAlias) {
        return createAggregateAttribute(ec, function, attributeName, entityName, valueClass, valueType, aggregateName, entityTableAlias, false);
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
     * @param aggregateName
     *            the name to assign to the aggregate column in the query
     * @param valueClass
     *            the java class of this attribute's values
     * @param valueType
     *            the EOAttribute value type
     * @param entityTableAlias
     *            the "t0"-style name of the attribute in this query (or null for "t0")
	 * @param usesDistinct
	 *            <code>true</code> if function should be used on distinct values
     * @return aggregate function attribute
     */
    public static EOAttribute createAggregateAttribute(EOEditingContext ec, String function, String attributeName, String entityName, Class valueClass, String valueType, String aggregateName, String entityTableAlias, boolean usesDistinct) {
        if (function == null) {
        	throw new IllegalStateException("Function is null.");
        }
        if (attributeName == null) {
        	throw new IllegalStateException("Attribute name is null.");
        }
        if (entityName == null) {
        	throw new IllegalStateException("Entity name is null.");
        }

        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        if (entity == null) {
        	throw new IllegalStateException("Unable find entity named: " + entityName);
        }

        EOAttribute attribute = entity.attributeNamed(attributeName);
        if (attribute == null) {
        	throw new IllegalStateException("Unable find attribute named: " + attributeName + " for entity: " + entityName);
        }

        EOAttribute aggregate = new EOAttribute();
        if (aggregateName != null) {
        	aggregate.setName(aggregateName);
        	aggregate.setColumnName(aggregateName);
        } else {
	        aggregate.setName("p_object" + function + "Attribute");
	        aggregate.setColumnName("p_object" + function + "Attribute");
        }
        aggregate.setClassName(valueClass.getName());
        if (valueType != null) {
        	aggregate.setValueType(valueType);
        } else {
        	aggregate.setValueType(attribute.valueType());
        }
        
        // MS: This "t0." is totally wrong, but it is required. It should be dynamically
        // generated, but this function doesn't have an EOSQLExpression to operate on
        if (entityTableAlias == null) {
        	entityTableAlias = "t0";
        }
        aggregate.setReadFormat(ERXSQLHelper.newSQLHelper(entity.model()).readFormatForAggregateFunction(function, entityTableAlias + "." + attribute.columnName(), aggregateName, usesDistinct));
        return aggregate;
    }

    /** 
     * Oracle 9 has a maximum length of 30 characters for table names, column names and constraint names.
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
     * This method checks each foreign key constraint name and if it is longer than 30 characters it is replaced
     * with a unique name.
     * @param entities
	 *            a NSArray containing the entities for which create table
	 *            statements should be generated or <code>null</code> if all entities in the
	 *            model should be used.
     * @param modelName
	 *            the name of the EOModel
     * @param optionsCreate 
     * @return a <code>String</code> containing SQL statements to create
	 *         tables
     * 
     * @see er.extensions.jdbc.ERXSQLHelper#createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray, String, NSDictionary)
     */
    public static String createSchemaSQLForEntitiesInModelWithNameAndOptionsForOracle9(NSArray entities, String modelName, NSDictionary optionsCreate) {
        EODatabaseContext dc = EOUtilities.databaseContextForModelNamed(ERXEC.newEditingContext(), modelName);
        return ERXSQLHelper.newSQLHelper(dc).createSchemaSQLForEntitiesInModelWithNameAndOptions(entities, modelName, optionsCreate);
    }
    
    /**
     * creates SQL to create tables for the specified Entities. This can be used
     * with EOUtilities rawRowsForSQL method to create the tables.
     * 
     * @param entities
     *            a NSArray containing the entities for which create table
     *            statements should be generated or null if all entities in the
     *            model should be used.
     * @param modelName
     *            the name of the EOModel
     * @param optionsCreate
     *            a NSDictionary containing the different options. Possible keys
     *            are
     *            <ul>
     *            <li>EOSchemaGeneration.DropTablesKey</li>
     *            <li>EOSchemaGeneration.DropPrimaryKeySupportKey</li>
     *            <li>EOSchemaGeneration.CreateTablesKey</li>
     *            <li>EOSchemaGeneration.CreatePrimaryKeySupportKey</li>
     *            <li>EOSchemaGeneration.PrimaryKeyConstraintsKey</li>
     *            <li>EOSchemaGeneration.ForeignKeyConstraintsKey</li>
     *            <li>EOSchemaGeneration.CreateDatabaseKey</li>
     *            </ul>
     *            <li>EOSchemaGeneration.DropDatabaseKey</li>
     *            <br/><br>
     *            Possible values are <code>YES</code> and <code>NO</code>
     * 
     * @return a <code>String</code> containing SQL statements to create
     *         tables
     * @deprecated
     */
    @Deprecated
    public static String createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray entities, String modelName, NSDictionary optionsCreate) {
        EODatabaseContext dc = EOUtilities.databaseContextForModelNamed(ERXEC.newEditingContext(), modelName);
        return ERXSQLHelper.newSQLHelper(dc).createSchemaSQLForEntitiesInModelWithNameAndOptions(entities, modelName, optionsCreate);
    }
    
    /**
     * Creates the schema sql for a set of entities.
     * 
     * @param entities the entities to create sql for
     * @param databaseContext the database context to use
     * @param optionsCreate the options (@see createSchemaSQLForEntitiesInModelWithNameAndOptions)
     * @return a sql script
     * @deprecated
     */
    @Deprecated
    public static String createSchemaSQLForEntitiesWithOptions(NSArray entities, EODatabaseContext databaseContext, NSDictionary optionsCreate) {
    	return ERXSQLHelper.newSQLHelper(databaseContext).createSchemaSQLForEntitiesWithOptions(entities, databaseContext, optionsCreate);
    }
    
    /**
     * creates SQL to create tables for the specified Entities. This can be used
     * with EOUtilities rawRowsForSQL method to create the tables.
     * 
     * @param entities
     *            a NSArray containing the entities for which create table
     *            statements should be generated or null if all entities in the
     *            model should be used.
     * @param modelName
     *            the name of the EOModel <br/><br/>This method uses the
     *            following defaults options:
     *            <ul>
     *            <li>EOSchemaGeneration.DropTablesKey=YES</li>
     *            <li>EOSchemaGeneration.DropPrimaryKeySupportKey=YES</li>
     *            <li>EOSchemaGeneration.CreateTablesKey=YES</li>
     *            <li>EOSchemaGeneration.CreatePrimaryKeySupportKey=YES</li>
     *            <li>EOSchemaGeneration.PrimaryKeyConstraintsKey=YES</li>
     *            <li>EOSchemaGeneration.ForeignKeyConstraintsKey=YES</li>
     *            <li>EOSchemaGeneration.CreateDatabaseKey=NO</li>
     *            <li>EOSchemaGeneration.DropDatabaseKey=NO</li>
     *            </ul>
     *            <br/><br>
     *            Possible values are <code>YES</code> and <code>NO</code>
     * 
     * @return a <code>String</code> containing SQL statements to create
     *         tables
     * @deprecated
     */
    @Deprecated
    public static String createSchemaSQLForEntitiesInModelWithName(NSArray entities, String modelName) {
    	EODatabaseContext databaseContext = EOUtilities.databaseContextForModelNamed(ERXEC.newEditingContext(), modelName);
    	return ERXSQLHelper.newSQLHelper(databaseContext).createSchemaSQLForEntitiesInModelWithName(entities, modelName);
    }

    /**
     * creates SQL to create tables for the specified Entities. This can be used
     * with EOUtilities rawRowsForSQL method to create the tables.
     * 
     * @param entities
     *            a NSArray containing the entities for which create table
     *            statements should be generated or null if all entities in the
     *            model should be used.
     * @param databaseContext
     *            the databaseContext
     *
     * @param create if true, tables and keys are created
     * @param drop if true, tables and keys are dropped
     * @return a <code>String</code> containing SQL statements to create
     *         tables
     * @deprecated
     */
    @Deprecated
    public static String createSchemaSQLForEntitiesInDatabaseContext(NSArray entities, EODatabaseContext databaseContext, boolean create, boolean drop) {
    	return ERXSQLHelper.newSQLHelper(databaseContext).createSchemaSQLForEntitiesInDatabaseContext(entities, databaseContext, create, drop);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static String createIndexSQLForEntitiesForOracle(NSArray entities) {
        NSMutableArray a = new NSMutableArray();
        a.addObject("BLOB");
        a.addObject("CLOB");
        return createIndexSQLForEntities(entities, a);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static String createIndexSQLForEntities(NSArray entities) {
        return createIndexSQLForEntities(entities, null);
    }
    
    /**
     * @deprecated
     */
    @Deprecated
    public static String createIndexSQLForEntities(NSArray entities, NSArray externalTypesToIgnore) {
        EOEntity ent = (EOEntity)entities.objectAtIndex(0);
        String modelName = ent.model().name();
        return ERXSQLHelper.newSQLHelper(ERXEC.newEditingContext(), modelName).createIndexSQLForEntities(entities, externalTypesToIgnore);
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
   * <span class="ja">
   * 例外エラーが重複エラーの場合は true を戻します。
   * 
   * @param e - saveChanges() から受けた例外エラーそのまま
   * 
   * @return エラーが処理できた場合は true
   * </span>
   */
  public static boolean isUniqueFailure(EOGeneralAdaptorException e) {
    boolean wasHandled = false;
    NSDictionary userInfo = e.userInfo();
    if(userInfo != null) {
      EOAdaptorOperation adaptorOp = (EOAdaptorOperation) userInfo.objectForKey(EOAdaptorChannel.FailedAdaptorOperationKey);

      wasHandled = adaptorOp.toString().contains("UNIQUE");
      if (!wasHandled) {
        log.error("UNIQUE Integrity constraint violation  " + e + ": " + userInfo);
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
    			EOAttribute attribute = relationship.sourceAttributes().lastObject();
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
     * @return array of primary keys
     */
    public static NSArray primaryKeysForObjects(NSArray<? extends EOEnterpriseObject> eos) {
        NSMutableArray result = new NSMutableArray();
        if (eos != null) {
            for (EOEnterpriseObject target : eos) {
                NSDictionary pKey = EOUtilities.primaryKeyForObject(target.editingContext(), target);
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
     * @param entity base entity
     * @param keyPath key path
     * @return array of EOProperties that make up the given key path
     */
    public static NSArray<EOProperty> attributePathForKeyPath(EOEntity entity, String keyPath) {
        NSMutableArray<EOProperty> result = new NSMutableArray<EOProperty>();
        String[] parts = keyPath.split("\\.");
        String part;
        for (int i = 0; i < parts.length - 1; i++) {
            part = parts[i];
            EORelationship relationship = entity.anyRelationshipNamed(part);
            if(relationship == null) {
            	// CHECKME AK:  it would probably be better to return null 
            	// to indicate that this is not a valid path?
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
            	NSArray<EOProperty> path = attributePathForKeyPath(entity, relationship.definition());
            	result.addObjectsFromArray(path);
            	return result;
            }
            attribute = relationship.joins().lastObject().sourceAttribute();
         }
        result.addObject(attribute);
        return result;
    }

    /**
     * Creates a where clause string " someKey IN ( someValue1,...)". Can migrate keyPaths.
     * @deprecated
     */
    @Deprecated
    public static String sqlWhereClauseStringForKey(EOSQLExpression e, String key, NSArray valueArray) {
    	return ERXSQLHelper.newSQLHelper(e).sqlWhereClauseStringForKey(e, key, valueArray);
    }

    /**
     * Returns the database context for an EO.
     * 
     * @param eo the EO to get a database context for
     * @return the eo's database context
     */
    public static EODatabaseContext databaseContextForObject(EOEnterpriseObject eo) {
        EOEditingContext editingContext = eo.editingContext();
        EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) editingContext.rootObjectStore();
        EODatabaseContext databaseContext = (EODatabaseContext) osc.objectStoreForObject(eo);
        return databaseContext;
    }
    
    /**
     * Returns the database context for the given entity in the given
     * EOObjectStoreCoordinator
     * 
     * @param entityName entity name
     * @param osc the object store coordinator
     * @return database context
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
     * @param osc
     *            the EOObjectStoreCoordinator from which the (JDBC)Connections
     *            should be closed
     * @return <code>true</code> if all connections have been closed
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

    private static Set<String> _keysWithWarning = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * Returns the last entity for the given key path. If the path is empty or <code>null</code>, returns the given entity.
     * @param entity
     * @param keyPath
     * @return entity object
     */
    public static EOEntity destinationEntityForKeyPath(EOEntity entity, String keyPath) {
        if(keyPath == null || keyPath.length() == 0) {
            return entity;
        }
        NSArray<String> keyArray = NSArray.componentsSeparatedByString(keyPath, ".");
        for(String key : keyArray) {
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

    /**
     * Walks all of the parentEntity relationships to
     * find the root entity.
     * @param entity to find the root parent
     * @return root parent entity
     */
    public static EOEntity rootEntityForEntity(EOEntity entity) {
        while (entity.parentEntity() != null) {
            entity = entity.parentEntity();
        }
        return entity;
    }

    /**
     * Walks all of the parentEntity relationships to
     * find the root entity.
     * @param entityName to find the root parent
     * @return root parent entity
     */
    public static EOEntity rootEntityForEntityNamed(String entityName) {
        return rootEntityForEntity(EOModelGroup.defaultGroup().entityNamed(entityName));
    }

    public static void logExpression(EOAdaptorChannel channel, EOSQLExpression expression, long startTime) {
        if (sqlLoggingLogger == null) {
            sqlLoggingLogger = Logger.getLogger("er.extensions.ERXAdaptorChannelDelegate.sqlLogging");
        }
        // sqlLoggingLogger.setLevel(Level.DEBUG);
        String entityMatchPattern = ERXProperties.stringForKeyWithDefault(
                "er.extensions.ERXAdaptorChannelDelegate.trace.entityMatchPattern", ".*");
        long millisecondsNeeded = System.currentTimeMillis() - startTime;
        String entityName = (expression.entity() != null ? expression.entity().name() : "Unknown");
        if (entityName.matches(entityMatchPattern)) {
            long debugMilliseconds = ERXProperties.longForKeyWithDefault(
                    "er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.debug", 0);
            long infoMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.info",
                    100);
            long warnMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.warn",
                    1000);
            long errorMilliseconds = ERXProperties.longForKeyWithDefault(
                    "er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.error", 5000);
            int maxLength = ERXProperties.intForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.maxLength", 3000);
            boolean needsLog = false;
            if (millisecondsNeeded > errorMilliseconds) {
                needsLog = true;
            } else if (millisecondsNeeded > warnMilliseconds) {
                needsLog = true;
            } else if (millisecondsNeeded > infoMilliseconds) {
                if (sqlLoggingLogger.isInfoEnabled()) {
                    needsLog = true;
                }
            } else if (millisecondsNeeded > debugMilliseconds) {
                if (sqlLoggingLogger.isDebugEnabled()) {
                    needsLog = true;
                }
            }
            if (ERXStats.isTrackingStatistics()) {
            	String statement = expression.statement();
            	// AK: special postgres data PK handling
            	statement = statement.replaceAll("decode\\(.*?\\)", "?");
            	// IN's can be quite long and are normally not bound
               	statement = statement.replaceAll(" IN \\(.*?\\)", " IN ([removed])");
               	statement = statement.replaceAll("([a-zA-Z0-9_\\.]+)\\s+IN \\(.*?\\)(\\s+OR\\s+\\1\\s+IN \\(.*?\\))+", " IN ([multi removed])");
               	// the cols are always the same, replace with *
               	statement = statement.replaceAll("((t0|T0)\\.[a-zA-Z0-9_]+\\,\\s*)*(t0|T0)\\.[a-zA-Z0-9_\\.]+\\s+FROM\\s+", "t0.* FROM ");
            	ERXStats.addDurationForKey(millisecondsNeeded, Group.SQL, entityName + ": " +statement);
            }
            listener.get().log(millisecondsNeeded, entityName);
            if (needsLog) {
                String logString = createLogString(channel, expression, millisecondsNeeded);
        		if (logString.length() > maxLength) {
        		    logString = logString.substring(0, maxLength);
        		}

                if (millisecondsNeeded > errorMilliseconds) {
        		    sqlLoggingLogger.error(logString, new RuntimeException("Statement running too long"));
        		} else if (millisecondsNeeded > warnMilliseconds) {
        		    sqlLoggingLogger.warn(logString);
        		} else if (millisecondsNeeded > infoMilliseconds) {
        		    if (sqlLoggingLogger.isInfoEnabled()) {
        		        sqlLoggingLogger.info(logString);
        		    }
        		} else if (millisecondsNeeded > debugMilliseconds) {
        		    if (sqlLoggingLogger.isDebugEnabled()) {
        		        sqlLoggingLogger.debug(logString);
        		    }
        		}

            }
        }
    }

	public static String createLogString(EOAdaptorChannel channel, EOSQLExpression expression, long millisecondsNeeded) {
		StringBuilder sb = new StringBuilder();
        String entityName = (expression.entity() != null ? expression.entity().name() : "Unknown");
        sb.append("\"").append(entityName).append("\"@").append(channel.adaptorContext().hashCode());
        sb.append(" expression took ").append(millisecondsNeeded).append(" ms: ").append(expression.statement());
		NSArray<NSDictionary<String, ? extends Object>> variables = expression.bindVariableDictionaries();
		int cnt = variables != null ? variables.count() : 0;
		if (cnt > 0) {
		    sb.append(" withBindings: ");
		    for (int i = 0; i < cnt; i++) {
		        NSDictionary<String, ? extends Object> nsdictionary = variables.objectAtIndex(i);
		        Object obj = nsdictionary.valueForKey("BindVariableValue");
		        String attributeName = (String) nsdictionary.valueForKey("BindVariableName");
		        if (obj instanceof String) {
		            obj = EOSQLExpression.sqlStringForString((String) obj);
		        } else if (obj instanceof Number) {
		        	obj = EOSQLExpression.sqlStringForNumber((Number) obj);
		        } else if (obj instanceof NSTimestamp) {
		            obj = obj.toString();
		        } else if (obj instanceof NSData) {
		            // ak: this is just for logging, however we would
		            // like to get readable data
		            // in particular for PKs and with postgres this
		            // works.
		            // plain EOF is broken, though
		            try {
		                if (((NSData) obj).length() < 50) {
		                    obj = expression.sqlStringForData((NSData) obj);
		                }
		            } catch (ArrayIndexOutOfBoundsException ex) {
		                // ignore, this is a bug in EOF
		            }
		            if (obj instanceof NSData) {
		                // produces very yucky output
		                obj = obj.toString();
		            }
		        } else {
		            if (expression.entity() != null) {
		                EOAttribute attribute = expression.entity().anyAttributeNamed(attributeName);
		                if (attribute != null) {
		                    obj = expression.formatValueForAttribute(obj, attribute);
		                }
		            }
		        }
		        if (i != 0)
		            sb.append(", ");
		        sb.append(i + 1);
		        sb.append(':');
		        sb.append(obj);
		        sb.append('[');
		        sb.append(attributeName);
		        sb.append(']');
		    }
		}

		return sb.toString();
	}
    
    
    /**
     * Creates an AND qualifier of EOKeyValueQualifiers for every keypath in the given array of attributes.
     *
     * @author ak
     * @param attributes 
     * @param values 
     * @return qualifier. EOAndQualifier for 2 or more elements in <code>attributes</code>, EOKeyValueQualifier for a single element <code>attributes</code> array, <code>null</code> when <code>attributes</code> is null or empty.
     */
    public static EOQualifier qualifierFromAttributes(NSArray<EOAttribute> attributes, NSDictionary values) {
        EOQualifier result = null;
        if (attributes != null && attributes.count() > 0) {
        	NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
            for (EOAttribute key : attributes) {
                Object value = values.objectForKey(key.name());
                qualifiers.addObject(new EOKeyValueQualifier(key.name(), EOQualifier.QualifierOperatorEqual, value));
            }
            // Don't wrap in an AND qualifier if there is only one qualifier
            result = (qualifiers.count() == 1 ? qualifiers.objectAtIndex(0) : new EOAndQualifier(qualifiers));
        }
        return result;
    }

    /**
     * Filters a list of relationships for only the ones that
     * have a given EOAttribute as a source attribute. 
     * @param entity 
     * @param attrib EOAttribute to filter source attributes of
     *      relationships.
     * @return filtered array of EORelationship objects that have
     *      the given attribute as the source attribute.
     */
    public static NSArray<EORelationship> relationshipsForAttribute(EOEntity entity, EOAttribute attrib) {
        NSMutableArray<EORelationship> arr = new NSMutableArray<EORelationship>();
        for (EORelationship rel : entity.relationships()) {
            NSArray<EOAttribute> attribs = rel.sourceAttributes();
            if (attribs.containsObject(attrib)) {
                arr.addObject(rel);
            }
        }
        return arr;
    }
    
    /**
     * Returns the source attribute for the relationship named relationshipName on entity.  Assumes there is only one join.
     *
     * @param entity EOEntity to find relationship on
     * @param relationshipName name of relationship on entity
     * @return source attribute for the relationship
     */
    public static EOAttribute sourceAttributeForRelationship(EOEntity entity, String relationshipName)
    {
        EORelationship relationship = entity.relationshipNamed(relationshipName);
        return sourceAttributeForRelationship(relationship);
    }

    /**
     * Returns the source attribute for the relationship.  Assumes there is only one join.
     *
     * @param relationship relationship on entity to return source attribute for
     * @return source attribute for the relationship
     */
    public static EOAttribute sourceAttributeForRelationship(EORelationship relationship)
    {
        EOJoin join = relationship.joins().objectAtIndex(0);
        return join.sourceAttribute();
    }

    /**
     * Returns the external column name for the source attribute for the relationship named relationshipName on entity.  Assumes there is only one join.
     * This is intend to support the *RowsDescribedByQualifier methods when used with relationships.
     *
     * @param entity EOEntity to find relationship on
     * @param relationshipName name of relationship on entity
     * @return the external column name for the source attribute for the relationship
     */
    public static String sourceColumnForRelationship(EOEntity entity, String relationshipName)
    {
        EORelationship relationship = entity.relationshipNamed(relationshipName);
        return sourceColumnForRelationship(relationship);
    }

    /**
     * Returns the external column name for the source attribute for the relationship named relationshipName on entity.  Assumes there is only one join.
     * This is intend to support the *RowsDescribedByQualifier methods when used with relationships.
     *
     * @param relationship relationship on entity to return the external column name for the source attribute for
     * @return the external column name for the source attribute for the relationship
     */
    public static String sourceColumnForRelationship(EORelationship relationship)
    {
        return sourceAttributeForRelationship(relationship).columnName();
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
     * @param e 
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
                if(ERXValueUtilities.isNull(val)) {
                	val = null;
                }
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
                    if (!wasOpen && channel != null) {
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
            @Override
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
            @Override
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
            @Override
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
            final Collection<? extends NSDictionary<String, ?>> newValues) {
        final EOEntity entity = entityNamed(ec, entityName);
        ChannelAction action = new ChannelAction() {
            @Override
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
			@Override
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
	 * @param model model name
     * @return name of the JDBC plugin or <code>null</code>
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
	                if (pluginName.startsWith("_")) {
	                	pluginName = pluginName.substring(1);
	                }
	            }
	        }
	    }
        if (pluginName != null && pluginName.trim().length() == 0) {
          pluginName = null;
        }
        return pluginName;
    }
     
     /**
      * Returns a new fetch spec by morphing sort orderings containing the keys <code>foo.name</code>
      * returning <code>foo.name_de</code> where appropriate.
      * @param ec
      * @param fetchSpecification
      * @return localized fetch specification
      */
     public static EOFetchSpecification localizeFetchSpecification(EOEditingContext ec, EOFetchSpecification fetchSpecification) {
         if(fetchSpecification != null && fetchSpecification.sortOrderings().count() > 0) {
             fetchSpecification = (EOFetchSpecification) fetchSpecification.clone();
             NSMutableArray sortOrderings = new NSMutableArray(fetchSpecification.sortOrderings().count());
             for (Enumeration enumerator = fetchSpecification.sortOrderings().objectEnumerator(); enumerator.hasMoreElements();) {
                 EOSortOrdering item = (EOSortOrdering) enumerator.nextElement();
                 String key = item.key();
                 NSArray path = NSArray.componentsSeparatedByString(key, ".");
                 EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, fetchSpecification.entityName());
                 String attributeName = (String) path.lastObject();
            	 String prefix = "";
                 if(path.count() > 1) {
                     for (int i = 0; i < path.count() - 1; i++) {
                         String part = (String) path.objectAtIndex(i);
                         EORelationship rel = entity.anyRelationshipNamed(part);
                         entity = rel.destinationEntity();
                         prefix += part + ".";
                     }
                 }
                     if(entity.attributeNamed(attributeName) == null) {
                    	 EOClassDescription cd = entity.classDescriptionForInstances();
                         if(cd instanceof ERXEntityClassDescription) {
                        	 String localizedKey = ((ERXEntityClassDescription)cd).localizedKey(attributeName);
                        	 if(localizedKey != null) {
                                 item = new EOSortOrdering(prefix + localizedKey, item.selector());
                        	 }
                         }
                     }
                 sortOrderings.addObject(item);
             }
             fetchSpecification.setSortOrderings(sortOrderings);
         }
         return fetchSpecification;
     }
     
     /**
      * Batch fetch a relationship, optionally skipping any relationship that has already faulted in its to-many relationship.
      * 
      * @param databaseContext the database context to fetch in
      * @param entityName the name of the entity to fetch on
      * @param relationshipName the name of the relationship in that entity to fetch
      * @param objects the objects to fetch the relationship on
      * @param editingContext the editingContext to fetch in
      * @param skipFaultedRelationships if true, skip any object whose relationship has already been faulted
      */
     public static void batchFetchRelationship(EODatabaseContext databaseContext, String entityName, String relationshipName, NSArray objects, EOEditingContext editingContext, boolean skipFaultedRelationships) {
    	 EOEntity entity = ERXEOAccessUtilities.entityNamed(editingContext, entityName);
    	 EORelationship relationship = entity.relationshipNamed(relationshipName);
    	 ERXEOAccessUtilities.batchFetchRelationship(databaseContext, relationship, objects, editingContext, skipFaultedRelationships);
     }
     
 	/**
 	 * Batch fetch a relationship, optionally skipping any relationship that has
 	 * already faulted in its to-many relationship.
 	 * 
 	 * @param databaseContext
 	 *            the database context to fetch in
 	 * @param relationship
 	 *            the relationship to fetch
 	 * @param objects
 	 *            the objects to fetch the relationship on
 	 * @param editingContext
 	 *            the editingContext to fetch in
 	 * @param skipFaultedRelationships
 	 *            if true, skip any object whose relationship has already been
 	 *            faulted
 	 */
 	public static void batchFetchRelationship(EODatabaseContext databaseContext, EORelationship relationship, NSArray objects, EOEditingContext editingContext, boolean skipFaultedRelationships) {
 		NSArray objectsToBatchFetch;

		if (relationship.isToMany()) {
	 		if (skipFaultedRelationships) {
 				NSMutableArray objectsWithUnfaultedRelationships = new NSMutableArray();
 				String relationshipName = relationship.name();
 				Enumeration objectsEnum = objects.objectEnumerator();
 				while (objectsEnum.hasMoreElements()) {
 					EOEnterpriseObject object = (EOEnterpriseObject) objectsEnum.nextElement();
 					Object relationshipValue = object.storedValueForKey(relationshipName);
 					if (EOFaultHandler.isFault(relationshipValue)) {
 						objectsWithUnfaultedRelationships.addObject(object);
 					}
 				}
 				objectsToBatchFetch = objectsWithUnfaultedRelationships;
	 		}
	 		else {
	 			objectsToBatchFetch = objects;
	 		}
		}
		else {
			NSMutableSet<EOGlobalID> gids = new NSMutableSet<EOGlobalID>();
	 		
			NSMutableArray objectsWithUnfaultedRelationships = new NSMutableArray();
			EOEntity destinationEntity = relationship.destinationEntity();
			String relationshipName = relationship.name();
			Enumeration objectsEnum = objects.objectEnumerator();
			while (objectsEnum.hasMoreElements()) {
				EOEnterpriseObject object = (EOEnterpriseObject) objectsEnum.nextElement();
				// This seems a bit odd.  Why not just use object.isFault()?
				NSDictionary sourceSnapshot = databaseContext.snapshotForGlobalID(editingContext.globalIDForObject(object));
				if (sourceSnapshot == null) {
					// object here is an unfaulted object, not an object with an unfaulted relationship
					objectsWithUnfaultedRelationships.addObject(object);
				}
				else {
					NSDictionary destinationPK = relationship._foreignKeyForSourceRow(sourceSnapshot);
					EOGlobalID destinationGID = destinationEntity.globalIDForRow(destinationPK);
					if (destinationGID != null) {
 						NSDictionary destinationSnapshot = databaseContext.snapshotForGlobalID(destinationGID, editingContext.fetchTimestamp());
 						if (destinationSnapshot == null || ! skipFaultedRelationships) {
 	 						gids.addObject(destinationGID);
 	 						
 	 						// The ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs below will fetch the row and register 
 	 						// the snapshot.  BUT as the fault is deferred, it will not get fired.  This means there is no
 							// strong reference to the fetched object in the EC.  If the garbage collector runs, it will detect
 							// this and place the EO on the EC's cleanup queue (_referenceQueue()).  This is processed from 
 							// _processReferenceQueue() which is called at various points in EOEditingContext, most problematically
 							// from unlockObjectStore().  If this happens before the deferred fault is fired, the snapshot will be discarded.
 							// The object will get re-fetched when next referenced.  This makes batch fetching of to-one relationships of
 							// little use.  To avoid this, the call to storedValueForKey below will call (via the _LazyGenericRecordBinding) 
 							// willReadRelationship() on the deferred fault.  This fires the deferred fault (calls createFaultForDeferredFault() 
 							// defined on EOAccessDeferredFaultHandler), making it a regular fault.  When the EO is batch fetched below, 
 							// the regular fault resolves and there is a strong reference to the EO.  This is what EOFetchSpecification 
 							// pre-fetching does for to-one relationships.
 							// References:
 							// http://developer.apple.com/documentation/developertools/reference/wo541reference/com/webobjects/eocontrol/EODeferredFaulting.html
 							// http://developer.apple.com/DOCUMENTATION/WebObjects/Enterprise_Objects/Fetching/chapter_6_section_8.html
 	 						object.storedValueForKey(relationshipName);
 						}
					}
				}
			}
			objectsToBatchFetch = objectsWithUnfaultedRelationships;

			// MS: This is split out into a separate fetch because EOF does not handle batch
			// fetching of abstract entities very effectively.  We instead want to create
			// our own GID and batch fetch the GIDs ourselves.
			if (gids.count() > 0) {
				ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(editingContext, gids.allObjects(), ! skipFaultedRelationships);
			}
		}

 		if (objectsToBatchFetch.count() > 0) {
 			databaseContext.batchFetchRelationship(relationship, objectsToBatchFetch, editingContext);
 		}
 	}
 	
 	/**
 	 * In a multi-OSC or multi-instance scenario, when there are bugs, it is possible for the snapshots to become out of
 	 * sync with the database. It is useful to have some way to determine when exactly this condition occurs for writing
 	 * test cases.
 	 * 
 	 * @return a set of strings that describe the mismatches that occurred
 	 */
 	public static NSSet verifyAllSnapshots() {
 		NSMutableSet mismatches = new NSMutableSet();
 		NSMutableSet verifiedDatabases = new NSMutableSet();
 		EOEditingContext editingContext = ERXEC.newEditingContext();
 		EOModelGroup modelGroup = EOModelGroup.defaultGroup();
 		Enumeration modelsEnum = modelGroup.models().objectEnumerator();
 		while (modelsEnum.hasMoreElements()) {
 			EOModel model = (EOModel) modelsEnum.nextElement();
 			EODatabaseContext databaseContext = null;
 			try {
 				databaseContext = EODatabaseContext.registeredDatabaseContextForModel(model, editingContext);
 			}
 			catch (IllegalStateException e) {
 				log.warn("Model " + model.name() + " failed: " + e.getMessage());
 			}
 			if (databaseContext != null) {
 				databaseContext.lock();
 				try {
 					EODatabase database = databaseContext.database();
 					if (!verifiedDatabases.containsObject(database)) {
 						Enumeration gidEnum = database.snapshots().keyEnumerator();
 						while (gidEnum.hasMoreElements()) {
 							EOGlobalID gid = (EOGlobalID) gidEnum.nextElement();
 							if (gid instanceof EOKeyGlobalID) {
 								EOEnterpriseObject eo = null;
 								EOKeyGlobalID keyGID = (EOKeyGlobalID) gid;
 								String entityName = keyGID.entityName();
 								EOEntity entity = modelGroup.entityNamed(entityName);
 								NSDictionary snapshot = database.snapshotForGlobalID(gid);
 								if (snapshot != null) {
 									EOQualifier gidQualifier = entity.qualifierForPrimaryKey(entity.primaryKeyForGlobalID(gid));
 									EOFetchSpecification gidFetchSpec = new EOFetchSpecification(entityName, gidQualifier, null);

 									NSMutableDictionary databaseSnapshotClone;
 									NSMutableDictionary memorySnapshotClone = snapshot.mutableClone();
 									EOAdaptorChannel channel = databaseContext.availableChannel().adaptorChannel();
 									channel.openChannel();
 									channel.selectAttributes(entity.attributesToFetch(), gidFetchSpec, false, entity);
 									try {
 										databaseSnapshotClone = channel.fetchRow().mutableClone();
 									}
 									finally {
 										channel.cancelFetch();
 									}
 									// gidFetchSpec.setRefreshesRefetchedObjects(true);
 									// NSArray databaseEOs = editingContext.objectsWithFetchSpecification(gidFetchSpec);
 									if (databaseSnapshotClone == null) {
 										mismatches.addObject(gid + " was deleted in the database, but the snapshot still exists: " + memorySnapshotClone);
 									}
 									else {
 										// NSMutableDictionary refreshedSnapshotClone =
 										// database.snapshotForGlobalID(gid).mutableClone();
 										ERXDictionaryUtilities.removeMatchingEntries(memorySnapshotClone, databaseSnapshotClone);
 										if (databaseSnapshotClone.count() > 0 || memorySnapshotClone.count() > 0) {
 											mismatches.addObject(gid + " doesn't match the database: original = " + memorySnapshotClone + "; database = " + databaseSnapshotClone);
 										}
 										eo = (EOEnterpriseObject) editingContext.objectsWithFetchSpecification(gidFetchSpec).objectAtIndex(0);
 									}
 								}

 								if (eo != null) {
 									Enumeration relationshipsEnum = entity.relationships().objectEnumerator();
 									while (relationshipsEnum.hasMoreElements()) {
 										EORelationship relationship = (EORelationship) relationshipsEnum.nextElement();
 										String relationshipName = relationship.name();
 										NSArray originalDestinationGIDs = database.snapshotForSourceGlobalID(keyGID, relationshipName);
 										if (originalDestinationGIDs != null) {
 											NSMutableArray newDestinationGIDs = new NSMutableArray();
 											EOQualifier qualifier = relationship.qualifierWithSourceRow(database.snapshotForGlobalID(keyGID));
 											EOFetchSpecification relationshipFetchSpec = new EOFetchSpecification(entityName, qualifier, null);
 											EOAdaptorChannel channel = databaseContext.availableChannel().adaptorChannel();
 											channel.openChannel();
 											try {
 												channel.selectAttributes(relationship.destinationEntity().attributesToFetch(), relationshipFetchSpec, false, relationship.destinationEntity());
 												NSDictionary destinationSnapshot = null;
 												do {
 													destinationSnapshot = channel.fetchRow();
 													if (destinationSnapshot != null) {
 														EOGlobalID destinationGID = relationship.destinationEntity().globalIDForRow(destinationSnapshot);
 														newDestinationGIDs.addObject(destinationGID);
 													}
 												}
 												while (destinationSnapshot != null);
 											}
 											finally {
 												channel.cancelFetch();
 											}

 											NSArray objectsNotInDatabase = ERXArrayUtilities.arrayMinusArray(originalDestinationGIDs, newDestinationGIDs);
 											if (objectsNotInDatabase.count() > 0) {
 												mismatches.addObject(gid + "." + relationshipName + " has entries not in the database: " + objectsNotInDatabase);
 											}
 											NSArray objectsNotInMemory = ERXArrayUtilities.arrayMinusArray(newDestinationGIDs, originalDestinationGIDs);
 											if (objectsNotInMemory.count() > 0) {
 												mismatches.addObject(gid + "." + relationshipName + " is missing entries in the database: " + objectsNotInMemory);
 											}
 										}
 									}
 								}
 							}
 						}
 						verifiedDatabases.addObject(database);
 					}
 				}
 				finally {
 					databaseContext.unlock();
 				}
 			}
 		}
 		return mismatches;
 	}

 	/**
   * Utility method to make a shared entity editable. This
   * can be useful if you want to have an administration
   * application that can edit shared enterprise objects
   * and need a way at start up to disable the sharing
   * constraints.
   * @param entityName name of the shared entity to make
   *    shareable.
   */
  // FIXME: Should have to pass in an editing context so that the
  //    correct model group and shared ec will be used.
  // FIXME: Should also dump all of the currently shared eos from
  //    the shared context.
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

	/**
	 * Programatically adds a toOne or toMany relationship to an entity at runtime.
	 * 
	 * @param relationshipName name of the relationship to be created on the source entity
	 * @param sourceEntityName name of the source entity
	 * @param sourceAttributeName name of the attribute in the source entity to be used by the join
	 * @param destinationEntityName name of the destination entity
	 * @param destinationAttributeName name of the attribute in the destination entity to be used by the join
	 * @param toMany if true, the relationship will be toMany, otherwise it will be toOne
	 * @param deleteRule
	 *            EOClassDescription.DeleteRuleCascade ||
	 *            EOClassDescription.DeleteRuleDeny ||
	 *            EOClassDescription.DeleteRuleNoAction ||
	 *            EOClassDescription.DeleteRuleNullify
	 * @param isMandatory mandatory or not
	 * @param isClassProperty class property or not
	 * @param shouldPropagatePrimaryKey propagate primary key or not
	 * @return the newly created relationship
	 * 
	 * @author th
	 */
	public static EORelationship createRelationship(String relationshipName, String sourceEntityName, String sourceAttributeName, String destinationEntityName, String destinationAttributeName, boolean toMany, int deleteRule, boolean isMandatory, boolean isClassProperty, boolean shouldPropagatePrimaryKey) {
		EOEntity sourceEntity = EOModelGroup.defaultGroup().entityNamed(sourceEntityName);
		if(sourceEntity.isAbstractEntity())
			log.warn("If you programatically add relationships to an abstract entity, make sure you also add it to child entities");
		EOEntity destinationEntity = EOModelGroup.defaultGroup().entityNamed(destinationEntityName);
		EOAttribute sourceAttribute = sourceEntity.attributeNamed(sourceAttributeName);
		EOAttribute destinationAttribute = destinationEntity.attributeNamed(destinationAttributeName);
		EOJoin join = new EOJoin(sourceAttribute, destinationAttribute);
		EORelationship relationship = new EORelationship();

		relationship.setName(relationshipName);
		sourceEntity.addRelationship(relationship);
		relationship.addJoin(join);
		relationship.setToMany(toMany);
		relationship.setJoinSemantic(EORelationship.InnerJoin);
		relationship.setDeleteRule(deleteRule);
		relationship.setIsMandatory(isMandatory);
		relationship.setPropagatesPrimaryKey(shouldPropagatePrimaryKey);
		NSMutableArray<EOProperty> classProperties = sourceEntity.classProperties().mutableClone();
		if (isClassProperty && !classProperties.containsObject(relationship)) {
			classProperties.addObject(relationship);
			sourceEntity.setClassProperties(classProperties);
		} else if (!isClassProperty && classProperties.containsObject(relationship)) {
			classProperties.removeObject(relationship);
			sourceEntity.setClassProperties(classProperties);
		}
		if(log.isDebugEnabled())
			log.debug(relationship);

		return relationship;
	}

	/**
	 * Programatically adds a flattened relationship to an entity at runtime.
	 * 
	 * @param relationshipName name of the relationship to be created on the source entity
	 * @param sourceEntityName name of the source entity
	 * @param definition data path of the relationship (e.g. department.facility)
	 * @param deleteRule
	 *            EOClassDescription.DeleteRuleCascade ||
	 *            EOClassDescription.DeleteRuleDeny ||
	 *            EOClassDescription.DeleteRuleNoAction ||
	 *            EOClassDescription.DeleteRuleNullify
	 * @param isMandatory mandatory or not
	 * @param isClassProperty class property or not
	 * @return the newly created relationship
	 * 
	 * @author th
	 */
	public static EORelationship createFlattenedRelationship(String relationshipName, String sourceEntityName, String definition, int deleteRule, boolean isMandatory, boolean isClassProperty) {
		EOEntity sourceEntity = EOModelGroup.defaultGroup().entityNamed(sourceEntityName);
		if(sourceEntity.isAbstractEntity())
			log.warn("If you programatically add relationships to an abstract entity, make sure you also add it to child entities");

		// order is important here!
		// first create relationship and set name
		EORelationship relationship = new EORelationship();
		relationship.setName(relationshipName);
		// then add to entity (will only work if name is set!)
		sourceEntity.addRelationship(relationship);
		relationship.setDeleteRule(deleteRule);
		relationship.setIsMandatory(isMandatory);
		// finally set definition (only will work if assigned to entity!)
		relationship.setDefinition(definition);
		if (isClassProperty) {
			NSMutableArray<EOProperty> classProperties = sourceEntity.classProperties().mutableClone();
			classProperties.addObject(relationship);
			sourceEntity.setClassProperties(classProperties);
		}
		if(log.isDebugEnabled())
				log.debug(relationship + ", flattened:" + relationship.isFlattened() + ", definition:" + relationship.definition() + ", relationshipPath:" + relationship.relationshipPath());
		return relationship;
	}

    /**
     * Executes the given database context operation within a database context lock, retrying up to maxRetryCount times if the database connection dropped.
     * 
     * @param <T> the type of the results of this operation
     * @param databaseContext the (possibly unlocked) database context to operate on
     * @param maxAttempts the maximum number of times to reattempt the operation before failing
     * @param operation the database context operation to perform
     */
    public static <T> T executeDatabaseContextOperation(final EODatabaseContext databaseContext, final int maxAttempts, final DatabaseContextOperation<T> operation) {
    	T results = null;
    	boolean succeeded = false;
    	for (int attempt = 0; !succeeded && attempt < maxAttempts; attempt ++) {
        	databaseContext.lock();
	    	try {
	    		results = operation.execute(databaseContext);
	    		succeeded = true;
	        }
	        catch (Exception e) {
	            boolean didHandleException = false;
	            Object delegate = databaseContext.delegate();
	            if (delegate instanceof _NSDelegate) {
	                if (((_NSDelegate)delegate).respondsTo("databaseContextShouldHandleDatabaseException")) {
	                    didHandleException = ((Boolean) ((_NSDelegate)delegate).perform("databaseContextShouldHandleDatabaseException", new Object[] { databaseContext, e })).booleanValue() == false;
	                }
	            } else if (delegate instanceof ERXDatabaseContextDelegate) {
	                try {
	                    didHandleException = (((ERXDatabaseContextDelegate)delegate).databaseContextShouldHandleDatabaseException(databaseContext, e) == false);
	                } catch (Throwable t) {
	                    // fucking pointless.
	                    ERXEOAccessUtilities.log.error("caught an exception while trying to handle a database exception.", t);
	                }
	            }
	            if (didHandleException) {
	                // delegate handled exception and clean up, just try again
                    succeeded = false;
	            }
	            else if (databaseContext._isDroppedConnectionException(e)) {
	                // start clean up at the database and let it work down the stack, then try again
	                try {
	                    databaseContext.database().handleDroppedConnection();
	                    succeeded = false;
	                } catch (Exception ex) {
	                    throw NSForwardException._runtimeExceptionForThrowable(ex);
	                }
	            }
	            else {
	                throw NSForwardException._runtimeExceptionForThrowable(e);
	            }
	            NSLog._conditionallyLogPrivateException(e);
	        }
	        finally {
	            databaseContext.unlock();
	        }
    	}
    	return results;
    }
    
    /**
     * Executes the given adaptor channel operation within a database context lock and with a requested open channel, retrying up to maxRetryCount times if the database connection dropped.
     * 
     * @param <T> the type of the results of this operation
     * @param databaseContext the (possibly unlocked) database context to operate on
     * @param maxAttempts the maximum number of times to reattempt the operation before failing
     * @param operation the adaptor channel operation to perform
     */
    public static <T> T executeAdaptorChannelOperation(final EODatabaseContext databaseContext, final int maxAttempts, final AdaptorChannelOperation<T> operation) {
    	return ERXEOAccessUtilities.executeDatabaseContextOperation(databaseContext, maxAttempts, new DatabaseContextOperation<T>() {
    		public T execute(EODatabaseContext databaseContext) throws Exception {
    	        EOAdaptorChannel adaptorChannel = databaseContext.availableChannel().adaptorChannel();
    	        if (!adaptorChannel.isOpen()) {
    	            adaptorChannel.openChannel();
    	        }
    			return operation.execute(databaseContext, adaptorChannel);
    		}
    	});
    }
    
    /**
     * Executes the given adaptor channel operation within a database context lock and with a requested open channel, retrying up to maxRetryCount times if the database connection dropped.
     * 
     * @param <T> the type of the results of this operation
     * @param modelName the name of the model to lookup a database context for
     * @param maxAttempts the maximum number of times to reattempt the operation before failing
     * @param operation the adaptor channel operation to perform
     */
    public static <T> T executeAdaptorChannelOperation(final String modelName, final int maxAttempts, final AdaptorChannelOperation<T> operation) {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		EODatabaseContext databaseContext;
		try {
			databaseContext = EOUtilities.databaseContextForModelNamed(editingContext, modelName);
		}
		finally {
			editingContext.dispose();
		}
		return ERXEOAccessUtilities.executeAdaptorChannelOperation(databaseContext, maxAttempts, operation);
    }
    
    /**
     * Implemented by any database operation that needs to run inside of a database context lock.
     * 
     * @author mschrag
     *
     * @param <T> the type of the results of this operation
     */
    public static interface DatabaseContextOperation<T> {
    	/**
    	 * Executes the given operation.
    	 * 
    	 * @param databaseContext the database context to operate on
    	 * @throws Exception if anything goes wrong
    	 */
    	public T execute(EODatabaseContext databaseContext) throws Exception;
    }

    /**
     * Implemented by any adaptor channel operation that needs to run with an open adaptor channel.
     * 
     * @author mschrag
     *
     * @param <T> the type of the results of this operation
     */
    public static interface AdaptorChannelOperation<T> {
    	/**
    	 * Executes the given operation with the given channel.
    	 * 
    	 * @param databaseContext the locked database context to operate on
    	 * @param channel the open adaptor channel to operate on
    	 * @throws Exception if anything goes wrong
    	 */
    	public T execute(EODatabaseContext databaseContext, EOAdaptorChannel channel) throws Exception;
    }
    
    /**
     * Add or remove a class property from its entity.
     * @param property The property to add or remove from the class properties array
     * @param isClassProperty true adds the property to the list of class properties,
     * false removes it.
     */
    public static void setIsClassProperty(EOProperty property, boolean isClassProperty) {
    	EOEntity entity = null;
    	if(property instanceof EOAttribute) {
    		entity = ((EOAttribute)property).entity();
    	} else if(property instanceof EORelationship) {
    		entity = ((EORelationship)property).entity();
    	} else {
    		throw new IllegalArgumentException("property must be an EOAttribute or an EORelationship.");
    	}
    	NSArray<EOProperty> classProperties = entity.classProperties();
    	if(isClassProperty && !classProperties.contains(property)) {
    		classProperties = classProperties.arrayByAddingObject(property);
    		entity.setClassProperties(classProperties);
    	} else if(!isClassProperty && classProperties.contains(property)) {
    		classProperties = ERXArrayUtilities.arrayMinusObject(classProperties, property);
    		entity.setClassProperties(classProperties);
    	}
    }
    
    /**
     * Add or remove an attribute from the entities list of attributes used for locking
     * @param attr the attribute to add or remove
     * @param isUsedForLocking true adds the attribute, false removes it
     */
    public static void setIsAttributeUsedForLocking(EOAttribute attr, boolean isUsedForLocking) {
    	EOEntity entity = attr.entity();
    	NSArray<EOAttribute> atts = entity.attributesUsedForLocking();
    	if(isUsedForLocking && !atts.contains(attr)) {
    		atts = atts.arrayByAddingObject(attr);
    		entity.setAttributesUsedForLocking(atts);
    	} else if(!isUsedForLocking && atts.contains(attr)) {
    		atts = ERXArrayUtilities.arrayMinusObject(atts, attr);
    		entity.setAttributesUsedForLocking(atts);
    	}
    }

	/**
	 * @param ec editing context
	 * @param rootEntityName
	 * @return a list of all concrete entity names that inherit from
	 *         rootEntityName, including rootEntityName itself if it is
	 *         concrete.
	 */
	public static NSArray<String> entityHierarchyNamesForEntityNamed(EOEditingContext ec, String rootEntityName) {
		NSMutableArray<String> names = new NSMutableArray<String>();
		EOEntity rootEntity = entityNamed(ec, rootEntityName);
		NSArray<EOEntity> entities = entityHierarchyForEntity(ec, rootEntity);
	
		for (EOEntity entity : entities) {
			names.add(entity.name());
		}
		return names.immutableClone();
	}

	/**
	 * Utility method used to find all of the non-abstract sub entities
	 * for a given entity including itself.
	 * @param ec editing context
	 * @param rootEntity to walk all of the <code>subEntities</code>
	 *            relationships
	 * @return a list of all concrete entities that inherit from rootEntity,
	 *         including rootEntity itself if it is concrete.
	 */
	public static NSArray<EOEntity> entityHierarchyForEntity(EOEditingContext ec, EOEntity rootEntity) {
		NSMutableArray<EOEntity> entities = new NSMutableArray<EOEntity>();
	
		if (!rootEntity.isAbstractEntity()) {
			entities.add(rootEntity);
		}
		@SuppressWarnings("unchecked")
		NSArray<EOEntity> subEntities = rootEntity.subEntities();
		for (EOEntity subEntity : subEntities) {
			entities.addAll(entityHierarchyForEntity(ec, subEntity));
		}
		return entities.immutableClone();
	}
	

	/**
	 * Utility method used to find all of the sub entities
	 * for a given entity.
	 * @param rootEntity to walk all of the <code>subEntities</code>
	 *            relationships
	 * @param includeAbstracts determines if abstract entities should
	 *            be included in the returned array
	 * @return all of the sub-entities for a given entity.
	 */
	public static NSArray<EOEntity> allSubEntitiesForEntity(EOEntity rootEntity, boolean includeAbstracts) {
		NSMutableArray<EOEntity> entities = new NSMutableArray<EOEntity>();
		if (rootEntity != null) {
			for (EOEntity subEntity : rootEntity.subEntities()) {
				if (!subEntity.isAbstractEntity() || includeAbstracts) {
					entities.addObject(subEntity);
				}
				if (subEntity.subEntities().count() > 0) {
					entities.addAll(allSubEntitiesForEntity(subEntity, includeAbstracts));
				}
			}
		}
		return entities.immutableClone();
	}
}
