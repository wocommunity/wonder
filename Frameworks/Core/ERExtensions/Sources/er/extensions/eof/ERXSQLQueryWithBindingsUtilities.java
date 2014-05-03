package er.extensions.eof;


import java.util.Enumeration;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;


/**
 * This class allows SQL queries with binded variables to be run against the database. It's possible
 * to obtain EOs or raw rows, depending on the used method.
 */
public class ERXSQLQueryWithBindingsUtilities {

    /**
     * Runs a select query against the database, obtaining the resulting EOs.
     * <p>
     * Please read the {@link #selectObjectsOfEntityForSqlWithBindings(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)}
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
     * @see #selectObjectsOfEntityForSqlWithBindings(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)
     * @return array of objects obtained by the query
     */
    public static NSArray selectObjectsOfEntityForSqlWithBindings( EOEditingContext ec, String entityName, String query, ERXSQLBinding... bindings ) {
        return selectObjectsOfEntityForSqlWithBindings(ec, entityName, query, false, null, bindings);
    }
    
    /**
     * Runs a select query against the database, obtaining the resulting EOs.
     * <p>
     * Please read the {@link #selectObjectsOfEntityForSqlWithBindings(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)}
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
     * @see #selectObjectsOfEntityForSqlWithBindings(EOEditingContext, String, String, boolean, Integer, ERXSQLBinding...)
     * @return array of objects obtained by the query
     */
    public static NSArray selectObjectsOfEntityForSqlWithBindings( EOEditingContext ec, String entityName, String query, boolean refreshesCache, ERXSQLBinding... bindings ) {
        return selectObjectsOfEntityForSqlWithBindings(ec, entityName, query, refreshesCache, null, bindings);
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
    public static NSArray selectObjectsOfEntityForSqlWithBindings( EOEditingContext ec, String entityName, String query, boolean refreshesCache, Integer fetchLimit, ERXSQLBinding... bindings ) {
        EODatabaseContext context = databaseContextForEntityName(ec,entityName);
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        EOSQLExpression expression = context.adaptorContext().adaptor().expressionFactory().expressionForEntity( entity );

        String proceccedQuery = processedQueryString(query, expression, bindings);
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
     * The requirements for the passed in SQL query are the same as {@link #selectObjectsOfEntityForSqlWithBindings(EOEditingContext, String, String, boolean, ERXSQLBinding...)},
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
    public static ERXFetchSpecificationBatchIterator batchIteratorForObjectsWithSqlWithBindings( EOEditingContext ec, String entityName, String query, boolean refreshesCache, int batchSize, NSArray sortOrderings, ERXSQLBinding... bindings ) {
        EODatabaseContext databaseContext = databaseContextForEntityName(ec,entityName);
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        if( entity.primaryKeyAttributes().count() > 1 ) {
            throw new RuntimeException("Multiple primary keys not supported.");
        }

        EOSQLExpression expression = databaseContext.adaptorContext().adaptor().expressionFactory().expressionForEntity( entity );
        expression.setStatement(processedQueryString(query, expression, bindings));

        EOFetchSpecification pkSpec = new EOFetchSpecification( entityName, null, null );
        pkSpec.setRefreshesRefetchedObjects(refreshesCache);
        pkSpec.setFetchesRawRows(true);
        pkSpec.setRawRowKeyPaths(entity.primaryKeyAttributeNames());
        pkSpec.setHints( new NSDictionary( expression, EODatabaseContext.CustomQueryExpressionHintKey ) );

        NSArray pkDicts = ec.objectsWithFetchSpecification(pkSpec);
        NSMutableArray pks = new NSMutableArray();
        String pkAtttributeName = entity.primaryKeyAttributes().lastObject().name();

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
     * The requirements for the passed in SQL query are the same as {@link #selectObjectsOfEntityForSqlWithBindings(EOEditingContext, String, String, boolean, ERXSQLBinding...)},
     * except for the fact it should not return any objects. This method is intended to be used with non-SELECT
     * queries, like UPDATE, INSERT or DELETE.
     * </p>
     * @param ec
     *          The current editing context (used to obtain the correct OSC)
     * @param modelName
     *          The name of a model affected by the query. This does not necessarily have to be accurate,
     *          as this value is only used to find out the correct database that the query will be run against.
     * @param query
     *          The SQL query
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     */
    public static void runSqlQueryWithBindings( EOEditingContext ec, String modelName, String query, ERXSQLBinding... bindings ) {
        EOObjectStore osc = ec.rootObjectStore();
        EODatabaseChannel databaseChannel = databaseContextForModelName(ec,modelName).availableChannel();
        osc.lock();

        try {
            EOAdaptorChannel adaptorChannel = databaseChannel.adaptorChannel();
            
            if (!adaptorChannel.isOpen()) {
                adaptorChannel.openChannel();
            }

            EOSQLExpression expression = adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForString( query );

            String proceccedQuery = processedQueryString(query, expression, bindings);
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
     * The requirements for the passed in SQL query are the same as {@link #selectObjectsOfEntityForSqlWithBindings(EOEditingContext, String, String, boolean, ERXSQLBinding...)},
     * except for the fact you are free to return whatever columns you want.
     * </p>
     * @param ec
     *          The current editing context (used to obtain the correct OSC)
     * @param entityName
     *          The name of an entity affected by the query. This does not necessarily have to be accurate,
     *          as this value is only used to find out the correct database that the query will be run against.
     * @param query
     *          The SQL query
     * @param bindings
     *          The variable bindings, wrapped in {@link ERXSQLBinding} objects
     * @return The requested raw rows
     */
    public static NSArray rawRowsForSqlWithBindings( EOEditingContext ec, String entityName, String query, ERXSQLBinding... bindings ) {
        EODatabaseChannel databaseChannel = databaseContextForEntityName(ec,entityName).availableChannel();
        EOAdaptorChannel adaptorChannel = databaseChannel.adaptorChannel();
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        EOSQLExpression expression = adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForEntity( entity );
        expression.setStatement( processedQueryString( query, expression, bindings ) );

        EOFetchSpecification spec = new EOFetchSpecification( entityName, null, null );
        spec.setFetchesRawRows(true);
        spec.setHints( new NSDictionary( expression, EODatabaseContext.CustomQueryExpressionHintKey ) );

        return ec.objectsWithFetchSpecification(spec);
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
    protected static String processedQueryString(String query, EOSQLExpression expression, ERXSQLBinding... bindings) {
        int currentOffset = 0;
        int bindingCount = 0;
        StringBuilder processedQueryBuffer = new StringBuilder();
        NSArray positions = varPositionsForQuery( query );

        if( positions.count() != bindings.length ) {
            throw new RuntimeException("Binding placeholders count (" + positions.count() + ") does not match binding wrappers count (" + bindings.length + ").");
        }

        for ( Enumeration positionEnumerator = positions.objectEnumerator(); positionEnumerator.hasMoreElements(); ) {
            Integer position = (Integer) positionEnumerator.nextElement();
            if( position > currentOffset ) {
                processedQueryBuffer.append( query.substring( currentOffset, position ) );
            }
            ERXSQLBinding binding = bindings[bindingCount];
            // The call to sqlStringForValue adds a binding to the expression binding list...
            processedQueryBuffer.append( binding.sqlStringForBindingOnExpression(expression) );
            currentOffset = position+1;
            bindingCount++;
        }

        if( currentOffset < query.length() ) {
            processedQueryBuffer.append( query.substring( currentOffset, query.length() ) );
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
        if (model == null) {
            throw new RuntimeException("Entity named " + entityName + " not found in the model group.");
        }
        return EODatabaseContext.registeredDatabaseContextForModel(model, ec);
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
        if (model == null) {
            throw new RuntimeException("Model " + modelName + " not found in the model group.");
        }
        return EODatabaseContext.registeredDatabaseContextForModel(model, ec);
    }
}
