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

    /**
    * Convenience method to get the next unique ID from a sequence.
     * @param ec editing context
     * @param modelNamed name of the model which connects to the database
     *			that has the sequence in it
     * @param sequenceName name of the sequence
     * @return next value in the sequence
     */
    // ENHANCEME: Need a non-oracle specific way of doing this. Should poke around at
    //		the adaptor level and see if we can't find something better.
    public static Number getNextValFromSequenceNamed(EOEditingContext ec,
                                                     String modelNamed,
                                                     String sequenceName) {
        String sqlString = "select "+sequenceName+".nextVal from dual";
        NSArray array = EOUtilities.rawRowsForSQL(ec, modelNamed, sqlString);
        if (array.count() == 0) {
            throw new RuntimeException("Unable to generate value from sequence named: " + sequenceName
                                       + " in model: " + modelNamed);            
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

    /** creates the SQL which is used by the provides EOFetchSpecification. The EOEditingContext is needed
    * because it -could- be possible to have multiple EOF stacks, each having its own EOModelGroup and
    * each EOModel in this group could connect to different databases, Oracle, FrontBase, ...
    *
     * @param spec the EOFetchSpecification in question
     * @param ec the EOEditingContext
     *
     * @return the SQL which the EOFetchSpecification would use
     */
    public static String sqlForFetchSpecificationAndEditingContext(EOFetchSpecification spec, EOEditingContext ec) {
       return sqlExpressionForFetchSpecificationAndEditingContext(spec,ec,0,-1).statement();
    }

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

    
    /** creates the SQL which is used by the provides EOFetchSpecification. The EOEditingContext is needed
    * because it -could- be possible to have multiple EOF stacks, each having its own EOModelGroup and
    * each EOModel in this group could connect to different databases, Oracle, FrontBase, ...
    *
    * @param spec the EOFetchSpecification in question
    * @param ec the EOEditingContext
    * @param start start of rows to fetch
    * @param end end of rows to fetch (-1 if not used)

    *
    * @return the SQL which the EOFetchSpecification would use
    */
    public static EOSQLExpression sqlExpressionForFetchSpecificationAndEditingContext(EOFetchSpecification spec, EOEditingContext ec, long start, long end) {
        EOModel model = modelForFetchSpecificationAndEditingContext(spec, ec);
        EOEntity entity = model.entityNamed(spec.entityName());
        EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
        EODatabase db = new EODatabase(adaptor);
        EOSQLExpressionFactory sqlFactory = adaptor.expressionFactory();

        //NSArray attributes = spec.rawRowKeyPaths();
        NSArray attributesFromEntity = entity.attributesToFetch();
        /*EOQualifier qualifier = spec.qualifier();
        if (qualifier != null)
            qualifier = EOQualifierSQLGeneration.Support._schemaBasedQualifierWithRootEntity(qualifier, entity.rootEntity());
        if (qualifier != spec.qualifier()) {
            spec = (EOFetchSpecification) spec.clone();
            spec.setQualifier(qualifier);
        }*/

        EOSQLExpression ex = sqlFactory.expressionForEntity(entity);
        ex.setUseAliases(true);
        ex.setUseBindVariables(true);
        ex.prepareSelectExpressionWithAttributes(attributesFromEntity, false, spec);
        System.out.println(" ex.statement():" +  ex.statement());
        //EOSQLExpression expression=factory.selectStatementForAttributes(entity.primaryKeyAttributes(),
        //                                   false,
        //                                   fs,
        //                                   entity);

        EOSQLExpression sqlExpr = sqlFactory.selectStatementForAttributes(attributesFromEntity, false, spec, entity);
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

    public static EOModel modelForFetchSpecificationAndEditingContext(EOFetchSpecification spec, EOEditingContext ec) {
        EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator)ec.rootObjectStore();
        EOModelGroup modelGroup = (EOModelGroup)osc.userInfo().objectForKey("EOModelGroup");
        EOEntity entity = modelGroup.entityNamed(spec.entityName());
        EOModel model = entity.model();

        return model;
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
        if (ec == null)
            throw new IllegalStateException("EditingContext is null. Required to know which model group to use.");
        if (function == null)
            throw new IllegalStateException("Function is null.");
        if (attributeName == null)
            throw new IllegalStateException("Attribute name is null.");
        if (entityName == null)
            throw new IllegalStateException("Entity name is null.");
        
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

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
        EOModel m = EOModelGroup.defaultGroup().modelNamed(modelName);
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
}
