package er.extensions.eof;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol._EOMutableKnownKeyDictionary;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.jdbcadaptor.EOKeyValueBinding;
import com.webobjects.jdbcadaptor.EOSQLBinding;
import com.webobjects.jdbcadaptor.EOSQLQuery;
import com.webobjects.jdbcadaptor.JDBCChannel;
import com.webobjects.jdbcadaptor.JDBCColumn;
import com.webobjects.jdbcadaptor.JDBCContext;

/**
 * This class extends {@link EOSQLQuery} and adds functionality.
 * <p>
 * Here are some sample uses of the added functionality:
 * </p>
 * <pre>
 *   // Obtains ERXFetchSpecificationBatchIterator
 *   // Note the query must obtain the primary key!
 *   ERXSQLQuery.batchIteratorForQuery(editingContext(), Employee.ENTITY_NAME,
 *     "SELECT ID FROM EMPLOYEE WHERE HEIGHT < ? AND FIRST_NAME = ? ORDER BY NUMBER DESC", false, 100,
 *     new NSArray<EOSortOrdering>(new EOSortOrdering[] {new EOSortOrdering("number", EOSortOrdering.CompareDescending)}),
 *     new EOObjectBinding(190), new EOKeyValueBinding("firstName", "John"));
 * </pre>
 */
public class ERXSQLQuery extends EOSQLQuery {
    
    /**
     * Obtains a batch iterator for the objects resulting from the passed in SQL query with binded variables.
     * <p>
     * The requirements for the passed in SQL query are the same as {@link #objectsForQuery(EOEditingContext, String, String, boolean, EOSQLBinding...)},
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
     *          The variable bindings, wrapped in {@link EOSQLBinding} objects
     * 
     * @return batch iterator for the passed in query
     */
    public static ERXFetchSpecificationBatchIterator batchIteratorForQuery( EOEditingContext ec, String entityName, String query, boolean refreshesCache, int batchSize, NSArray sortOrderings, EOSQLBinding... bindings ) {
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
}
