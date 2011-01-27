package er.extensions.eof;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOModelSQLParser;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol._EOMutableKnownKeyDictionary;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation._NSDelegate;
import com.webobjects.jdbcadaptor.JDBCChannel;
import com.webobjects.jdbcadaptor.JDBCColumn;
import com.webobjects.jdbcadaptor.JDBCContext;

/**
 * This class allows SQL queries with binded variables to be run against the database. It's possible
 * to obtain EOs, raw rows or a batch iterator, depending on the used method.
 * <p>
 * Binded values are passed in using {@link ERXSQLBinding} implementations. Currently, there are two available
 * implementations: {@link ERXKeyValueBinding} and {@link ERXObjectBinding}. The first one should be used if
 * the binding matches a column modeled in an EOModel. This way, any data type conversions that would happen
 * on normal EOF usage are also applied when generating the SQL query. An example of this can be converting a
 * boolean value to a string or an integer. {@link ERXObjectBinding} can be used for non-modeled columns.
 * Please read the {@link ERXObjectBinding} class documentation for important notes regarding some databases
 * that expect SQL queries with binding typification.
 * </p>
 * <p>
 * This class is used by calling the most appropriate static method for the intended usage. Specific requirements
 * for the SQL query resulting columns are detailed on each of the method's documentation. Generally, you need
 * to provide a query returning the appropriate columns and using the '?' character for each binded value.
 * Depending on the database plug-in being used, the '?' character will be automatically replaced by a different
 * expression if the {@link ERXKeyValueBinding} class is used to wrap the binded value. In PostgreSQL, for instance,
 * the '?' character could be replaced by '?::varchar(1000)'.
 * </p>
 * <p>
 * Queries may be written using tokens described in {@link EOModelSQLParser}. This simplifies writing SQL queries,
 * as entity, attribute and relationship names can be used instead of table and column names.
 * </p>
 * <p>
 * Here are some sample uses of this class:
 * </p>
 * <pre>
 *   // Obtains EOs that match the query
 *   ERXSQLQuery.objectsForQuery(editingContext(), Song.ENTITY_NAME,
 *     "SELECT * FROM SONG WHERE FAVORITE = ? AND DURATION > ? ORDER BY NUMBER ASC", true,
 *     new ERXKeyValueBinding("favorite", true), new ERXKeyValueBinding("duration", 120));
 *     
 *   // Obtains raw rows for query
 *   ERXSQLQuery.rowsForQueryOnEntity(editingContext(), Song.ENTITY_NAME,
 *     "SELECT t0.NAME, t0.DURATION FROM SONG t0 WHERE COMPOSER = ?", new ERXKeyValueBinding("composer", "Mozart"));
 *     
 *   // Runs a query that returns no objects, using EOModelSQLParser-style tokens
 *   ERXSQLQuery.runQuery(editingContext(), "SongsModel",
 *     "DELETE FROM {Song} WHERE {favorite} = ?", new ERXKeyValueBinding("Song", "favorite", false));
 *   
 *   // Obtains ERXFetchSpecificationBatchIterator
 *   // Note the query must obtain the primary key!
 *   ERXSQLQuery.batchIteratorForQuery(editingContext(), Employee.ENTITY_NAME,
 *     "SELECT ID FROM EMPLOYEE WHERE HEIGHT < ? AND FIRST_NAME = ? ORDER BY NUMBER DESC", false, 100,
 *     new NSArray<EOSortOrdering>(new EOSortOrdering[] {new EOSortOrdering("number", EOSortOrdering.CompareDescending)}),
 *     new ERXObjectBinding(190), new ERXKeyValueBinding("firstName", "John"));
 * </pre>
 */
public class ERXSQLQuery {
    
    /**
     * Runs a select query against the database, obtaining the resulting EOs.
     * <p>
     * Please read the {@link #objectsForQuery(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)}
     * documentation for the necessary details on how to use both methods.
     * </p>
     * @param ec
     *          The editing context where the objects will be placed
     * @param entityName
     *          The name of the entity whose objects are returned by the query
     * @param query
     *          The SQL query
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * 
     * @see #objectsForQuery(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)
     * @return array of objects obtained by the query
     */
    public static NSArray objectsForQuery( EOEditingContext ec, String entityName, String query, ERXSQLBinding... bindings ) {
        return objectsForQuery(ec, entityName, query, false, null, bindings);
    }
    
    /**
     * Runs a select query against the database, obtaining the resulting EOs.
     * <p>
     * Please read the {@link #objectsForQuery(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)}
     * documentation for the necessary details on how to use both methods.
     * </p>
     * @param ec
     *          The editing context where the objects will be placed
     * @param entityName
     *          The name of the entity whose objects are returned by the query
     * @param query
     *          The SQL query
     * @param refreshesCache
     *          If true, the cached snapshots will be refreshed. See {@link EOFetchSpecification#setRefreshesRefetchedObjects(boolean)}. 
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * 
     * @see #objectsForQuery(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)
     * @return array of objects obtained by the query
     */
    public static NSArray objectsForQuery( EOEditingContext ec, String entityName, String query, boolean refreshesCache, ERXSQLBinding... bindings ) {
        return objectsForQuery(ec, entityName, query, refreshesCache, null, bindings);
    }

    /**
     * Runs a select query against the database, obtaining the resulting EOs.
     * <p>
     * This method allows a SELECT SQL query to be run, using binded variables. The method will
     * process the query result and return EOs of the requested entity.
     * </p>
     * <p>
     * There are some important requirements regarding the passed in query:
     * </p>
     * <ul>
     *  <li>The query must return all the columns of the affected table that are mapped in the modeled
     *  Entity. 'SELECT * FROM Table ...' is the easiest way to achieve this. Alias are supported, like
     *  'SELECT t0.* FROM Table t0 ...'.</li>
     *  <li>The placeholder for binded variables must be the '?' character. This method will process the
     *  query before executing it, replacing the generic placeholder character by the one appropriate to
     *  the used database. Ex: when using PostgreSQL, '... WHERE FIRST_NAME = ?' may be replaced by
     *  '... WHERE FIRST_NAME = ?::varchar(1000)'.</li>
     *  <li>The '?' character does not need to be quoted if it appears on the query inside a string literal.
     *  This situation is automatically detected and the substitution is not performed.</li>
     *  <li>The number of binding wrappers passed in as last arguments of this method must match the number of
     *  binding placeholders in the query.</li>
     * </ul>
     * @param ec
     *          The editing context where the objects will be placed
     * @param entityName
     *          The name of the entity whose objects are returned by the query
     * @param query
     *          The SQL query
     * @param refreshesCache
     *          If true, the cached snapshots will be refreshed. See {@link EOFetchSpecification#setRefreshesRefetchedObjects(boolean)}.
     * @param fetchLimit
     *          Fetch limit, or null for no limit
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * 
     * @return array of objects obtained by the query
     */
    public static NSArray objectsForQuery( EOEditingContext ec, String entityName, String query, boolean refreshesCache, Integer fetchLimit, ERXSQLBinding... bindings ) {
        EODatabaseContext context = databaseContextForEntityName(ec,entityName);
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        EOSQLExpression expression = context.adaptorContext().adaptor().expressionFactory().expressionForEntity( entity );

        String proceccedQuery = processedQueryString(query, expression, ec, bindings);
        expression.setStatement(proceccedQuery);

        EOFetchSpecification spec = new EOFetchSpecification( entityName, null, null );
        spec.setRefreshesRefetchedObjects(refreshesCache);
        if( fetchLimit != null ) {
            spec.setFetchLimit( fetchLimit );
        }
        spec.setHints( new NSDictionary( expression, EODatabaseContext.CustomQueryExpressionHintKey ) );

        return ec.objectsWithFetchSpecification(spec);
    }

    /**
     * Obtains a batch iterator for the objects resulting from the passed in SQL query with binded variables.
     * <p>
     * The requirements for the passed in SQL query are the same as {@link #objectsForQuery(EOEditingContext, String, String, boolean, ERXSQLBinding...)},
     * except for an important difference: the query must return only one column with the entity primary keys.
     * Instead of doing a query like 'SELECT * FROM Person ...', assuming ID as the name of the primary key column,
     * the query would be 'SELECT ID FROM Person ...'. Despite this, the batch iterator will provide you the
     * full initialized EOs.
     * </p>
     * <p>
     * <strong>SORTING</strong> - If you want to obtain sorted results, you'll have to add the sorting information to
     * the query (using ORDER BY clauses) <em>and</em> pass an array of EOSortOrderings to this method. The sorting
     * information in the query will be used to sort the primary key array. The EOSortOrderings are used to sort objects
     * inside each batch. To obtain sorted results as expected, both sorting criteria should be similar.
     * </p>
     * @param ec
     *          The editing context where the objects will be placed
     * @param entityName
     *          The name of the entity whose objects are returned by the query
     * @param query
     *          The SQL query
     * @param refreshesCache
     *          If true, the cached snapshots will be refreshed. See {@link EOFetchSpecification#setRefreshesRefetchedObjects(boolean)}.
     * @param batchSize
     *          The size of each batch
     * @param sortOrderings
     *          EOSortOrderings for batches (see description above)
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * 
     * @return batch iterator for the passed in query
     */
    public static ERXFetchSpecificationBatchIterator batchIteratorForQuery( EOEditingContext ec, String entityName, String query, boolean refreshesCache, int batchSize, NSArray sortOrderings, ERXSQLBinding... bindings ) {
        EODatabaseContext databaseContext = databaseContextForEntityName(ec,entityName);
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        if( entity.primaryKeyAttributes().count() > 1 ) {
            throw new RuntimeException("Multiple primary keys not supported.");
        }

        EOSQLExpression expression = databaseContext.adaptorContext().adaptor().expressionFactory().expressionForEntity( entity );
        expression.setStatement(processedQueryString(query, expression, ec, bindings));

        EOFetchSpecification pkSpec = new EOFetchSpecification( entityName, null, null );
        pkSpec.setRefreshesRefetchedObjects(refreshesCache);
        pkSpec.setFetchesRawRows(true);
        pkSpec.setRawRowKeyPaths(entity.primaryKeyAttributeNames());
        pkSpec.setHints( new NSDictionary( expression, EODatabaseContext.CustomQueryExpressionHintKey ) );

        NSArray pkDicts = ec.objectsWithFetchSpecification(pkSpec);
        NSMutableArray pks = new NSMutableArray();
        String pkAtttributeName = ((EOAttribute) entity.primaryKeyAttributes().lastObject()).name();

        for ( Enumeration rowEnumerator = pkDicts.objectEnumerator(); rowEnumerator.hasMoreElements(); ) {
            NSDictionary row = (NSDictionary) rowEnumerator.nextElement();
            pks.addObject( row.objectForKey( pkAtttributeName ));
        }

        EOFetchSpecification spec = new EOFetchSpecification(entityName, null, sortOrderings);
        spec.setRefreshesRefetchedObjects( refreshesCache );
        return new ERXFetchSpecificationBatchIterator( spec, pks, ec, batchSize);
    }


    /**
     * Runs an SQL query against the database with binded variables.
     * <p>
     * The requirements for this method are similar to {@link #objectsForQuery(EOEditingContext, String, String, boolean, ERXSQLBinding...)},
     * with the following differences:
     * </p>
     * <ul>
     *   <li>The query should not return any object. This method is intended to be used with non-SELECT
     * queries, like UPDATE, INSERT or DELETE.</li>
     *   <li>If {@link ERXKeyValueBinding} bindings are used, the entity name must be specified in the binding constructor.</li>
     * </ul>
     * 
     * @param ec
     *          The current editing context (used to obtain the correct OSC)
     * @param modelName
     *          The name of the model (used to apply the query on the correct DB)
     * @param query
     *          The SQL query
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     */
    public static void runQuery( EOEditingContext ec, String modelName, String query, ERXSQLBinding... bindings ) {
        EOObjectStore osc = ec.rootObjectStore();
        EODatabaseChannel databaseChannel = databaseContextForModelName(ec,modelName).availableChannel();
        osc.lock();

        try {
            EOAdaptorChannel adaptorChannel = databaseChannel.adaptorChannel();
            
            if (!adaptorChannel.isOpen()) {
                adaptorChannel.openChannel();
            }

            // marroz: We create the expression with the original query and then set the processed query because
            // it's the only way to create an expression without an entity.
            EOSQLExpression expression = adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForString( query );

            String proceccedQuery = processedQueryString(query, expression, ec, bindings);
            expression.setStatement(proceccedQuery);
            
            try {
                adaptorChannel.evaluateExpression( expression );
            } finally {
                databaseChannel.cancelFetch();
            }
        } finally {
            osc.unlock();
        }
    }

    /**
     * Runs a select query against the database, obtaining the resulting raw rows.
     * <p>
     * This method allows a SELECT SQL query to be run, using binded variables. The
     * method will return the resulting rows without any additional processing.
     * </p>
     * <p>
     * The requirements for the passed in SQL query are the same as {@link #objectsForQuery(EOEditingContext, String, String, boolean, ERXSQLBinding...)}.
     * </p>
     * @param ec
     *          The current editing context (used to obtain the correct OSC)
     * @param entityName
     *          Entity name for fetched objects.
     * @param query
     *          The SQL query
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * @return The requested raw rows
     */
    public static NSArray rawRowsForQueryOnEntity( EOEditingContext ec, String entityName, String query, ERXSQLBinding... bindings ) {
        EODatabaseChannel databaseChannel = databaseContextForEntityName(ec,entityName).availableChannel();
        EOAdaptorChannel adaptorChannel = databaseChannel.adaptorChannel();
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        EOSQLExpression expression = adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForEntity( entity );
        expression.setStatement( processedQueryString( query, expression, ec, bindings ) );

        EOFetchSpecification spec = new EOFetchSpecification( entityName, null, null );
        spec.setFetchesRawRows(true);
        spec.setHints( new NSDictionary( expression, EODatabaseContext.CustomQueryExpressionHintKey ) );

        return ec.objectsWithFetchSpecification(spec);
    }
    
    /**
     * Runs a select query against the database, obtaining the resulting raw rows.
     * <p>
     * This method allows a SELECT SQL query to be run, using binded variables. The
     * method will return the resulting rows without any additional processing. This is
     * a variant from {@link #rowsForQueryOnEntity(EOEditingContext, String, String, ERXSQLBinding...)}
     * that doesn't need the result rows to belong to a given entity.
     * </p>
     * <p>
     * The requirements for the passed in SQL query are similar to {@link #objectsForQuery(EOEditingContext, String, String, boolean, ERXSQLBinding...)}
     * with the following differences:
     * </p>
     * <ul>
     *   <li>The query can return any arbitrary column.</li>
     *   <li>If {@link ERXKeyValueBinding} bindings are used, the entity name must be specified in the binding constructor.</li>
     * </ul>
     * 
     * @param ec
     *          The current editing context (used to obtain the correct OSC)
     * @param modelName
     *          The name of the model (used to apply the query on the correct DB)
     * @param query
     *          The SQL query
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * @return The requested raw rows
     */
    public static NSArray rawRowsForQuery( EOEditingContext ec, String modelName, String query, ERXSQLBinding... bindings ) {
        EODatabaseContext dbContext = databaseContextForModelName(ec, modelName);
        dbContext.lock();

        try{
            EODatabaseChannel databaseChannel = dbContext.availableChannel();
            EOAdaptorChannel adaptorChannel = databaseChannel.adaptorChannel();
            adaptorChannel.openChannel();
            
            JDBCContext jdbcContext = (JDBCContext) adaptorChannel.adaptorContext();
            Connection connection = jdbcContext.connection();
            
            // marroz: We create the expression with the original query and then set the processed query because
            // it's the only way to create an expression without an entity.
            EOSQLExpression expression = adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForString(query);
            expression.setStatement(processedQueryString(query, expression, ec, bindings));
           
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            NSMutableArray<NSDictionary> resultList = new NSMutableArray<NSDictionary>(100);
            
            try {
                statement = connection.prepareStatement(expression.statement());
                
                int index = 1;
                for (NSDictionary<String, ? extends Object> binding : expression.bindVariableDictionaries()) {
                    if (bindings[index - 1].isModelAttribute()) {
                        EOAttribute attribute = (EOAttribute) binding.objectForKey(EOSQLExpression.BindVariableAttributeKey);
                        JDBCColumn column = new JDBCColumn(attribute, (JDBCChannel)adaptorChannel);
                        column.setStatement(statement);
                        column.takeInputValue(binding.objectForKey(EOSQLExpression.BindVariableValueKey), index, false);
                    } else {
                        statement.setObject(index, binding.objectForKey(EOSQLExpression.BindVariableValueKey));
                    }
                    ++index;
                }
                
                resultSet = statement.executeQuery();
                
                ResultSetMetaData metaData = resultSet.getMetaData();
                int numColumns = metaData.getColumnCount();
                NSMutableArray<String> columnNames = new NSMutableArray<String>(numColumns);

                for (int i = 0; i < numColumns; i++) {
                    columnNames.addObject(metaData.getColumnName(i + 1));
                }
                
                _EOMutableKnownKeyDictionary.Initializer dictInitializer = new _EOMutableKnownKeyDictionary.Initializer(columnNames);

                while (resultSet.next()) {
                    _EOMutableKnownKeyDictionary result = new _EOMutableKnownKeyDictionary(dictInitializer);
                    for (int i = 0; i < numColumns; i++) {
                        final Object value = resultSet.getObject(i + 1);
                        if (value != null) {
                            result.setObjectForKey(value, columnNames.objectAtIndex(i));
                        }
                        else {
                            result.setObjectForKey(NSKeyValueCoding.NullValue, columnNames.objectAtIndex(i));
                        }
                    }
                    resultList.add(result);
                }
                return resultList;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            dbContext.unlock();
        }
    }
    
    // ==================================================
    // Support methods
    // --------------------------------------------------

    /**
     * Replaces the binding placeholder characters by the appropriate token for the used database.
     * <p>
     * This method will also register each of the bindings in the given expression.
     * </p>
     *
     * @param query
     *          The original SQL query
     * @param expression
     *          EOSQLExpression to be modified
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * @return The processed query
     */
    protected static String processedQueryString(String query, EOSQLExpression expression, EOEditingContext ec, ERXSQLBinding... bindings) {
        int currentOffset = 0;
        int bindingCount = 0;
        StringBuffer processedQueryBuffer = new StringBuffer();
        String parsedQuery = EOModelSQLParser.sqlQueryForQuery(ec, query);
        NSArray positions = varPositionsForQuery( parsedQuery );

        if( positions.count() != bindings.length ) {
            throw new RuntimeException("Binding placeholders count (" + positions.count() + ") does not match binding wrappers count (" + bindings.length + ").");
        }

        for ( Enumeration positionEnumerator = positions.objectEnumerator(); positionEnumerator.hasMoreElements(); ) {
            Integer position = (Integer) positionEnumerator.nextElement();
            if( position > currentOffset ) {
                processedQueryBuffer.append( parsedQuery.substring( currentOffset, position ) );
            }
            ERXSQLBinding binding = bindings[bindingCount];
            // The call to sqlStringForValue adds a binding to the expression binding list...
            processedQueryBuffer.append( binding.sqlStringForBindingOnExpression(expression, ec) );
            currentOffset = position+1;
            bindingCount++;
        }

        if( currentOffset < parsedQuery.length() ) {
            processedQueryBuffer.append( parsedQuery.substring( currentOffset, parsedQuery.length() ) );
        }

        String proceccedQuery = processedQueryBuffer.toString();
        return proceccedQuery;
    }

    /**
     * Obtains the index positions of the binding placeholders in the query.
     * 
     * @param query 
     *          The SQL query
     * @return array of placeholder index positions
     */
    private static NSArray varPositionsForQuery(String query) {
        int position = 0;
        boolean inQuote = false;
        char quoteChar = 0;
        NSMutableArray positions = new NSMutableArray();

        while( position < query.length() ) {
            char c = query.charAt( position );
            if( c == '\\' ) {
                position += 2;
                continue;
            }
            if( inQuote == false && ( c == '\'' || c == '\"' ) ) {
                quoteChar = c;
                inQuote = true;
            } else if( inQuote && c == quoteChar ) {
                inQuote = false;
            } else if( inQuote == false && c == '?' ) {
                positions.addObject(position);
            }

            position++;
        }

        return positions;
    }

    /**
     * Obtains the database context for the given editing context and entity name.
     * 
     * @param ec
     *          Editing context
     * @param entityName
     *          The entity name
     * @return The database context for the given editing context and entity name
     */
    private static EODatabaseContext databaseContextForEntityName( EOEditingContext ec, String entityName ) {
        EOModelGroup group = EOUtilities.modelGroup( ec );
        EOModel model = group.entityNamed(entityName).model();
        if( model != null ) {
            return EODatabaseContext.registeredDatabaseContextForModel(model, ec);
        } else {
            throw new RuntimeException("Entity named " + entityName + " not found in the model group.");
        }
    }
    
    /**
     * Obtains the database context for the given editing context and model name
     * 
     * @param ec
     *          Editing context
     * @param modelName
     *          The model name
     * @return The database context for the given editing context and model name
     */
    private static EODatabaseContext databaseContextForModelName(EOEditingContext ec, String modelName) {
        EOModelGroup group = EOUtilities.modelGroup( ec );
        EOModel model = group.modelNamed(modelName);
        if( model != null ) {
            return EODatabaseContext.registeredDatabaseContextForModel(model, ec);
        } else {
            throw new RuntimeException("Model " + modelName + " not found in the model group.");
        }
    }

    
}
