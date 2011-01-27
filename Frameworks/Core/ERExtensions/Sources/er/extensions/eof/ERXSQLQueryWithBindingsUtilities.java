package er.extensions.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

/**
 * This class has been renamed to {@link ERXSQLQuery}, and methods got shorter names too.
 * Use {@link ERXSQLQuery} instead of this class for new code.
 */
@Deprecated
public class ERXSQLQueryWithBindingsUtilities extends ERXSQLQuery {
    
    @Deprecated
    public static NSArray selectObjectsOfEntityForSqlWithBindings( EOEditingContext ec, String entityName, String query, ERXSQLBinding... bindings ) {
        return objectsForQuery(ec, entityName, query, bindings);
    }
    
    @Deprecated
    public static NSArray selectObjectsOfEntityForSqlWithBindings( EOEditingContext ec, String entityName, String query, boolean refreshesCache, ERXSQLBinding... bindings ) {
        return objectsForQuery(ec, entityName, query, refreshesCache, bindings);
    }
    
    @Deprecated
    public static NSArray selectObjectsOfEntityForSqlWithBindings( EOEditingContext ec, String entityName, String query, boolean refreshesCache, Integer fetchLimit, ERXSQLBinding... bindings ) {
        return objectsForQuery(ec, entityName, query, refreshesCache, fetchLimit, bindings);
    }
    
    @Deprecated
    public static ERXFetchSpecificationBatchIterator batchIteratorForObjectsWithSqlWithBindings( EOEditingContext ec, String entityName, String query, boolean refreshesCache, int batchSize, NSArray sortOrderings, ERXSQLBinding... bindings ) {
        return batchIteratorForQuery(ec, entityName, query, refreshesCache, batchSize, sortOrderings, bindings);
    }
    
    @Deprecated
    public static void runSqlQueryWithBindings( EOEditingContext ec, String modelName, String query, ERXSQLBinding... bindings ) {
        runQuery(ec, modelName, query, bindings);
    }
    
    @Deprecated
    public static NSArray rawRowsForSqlWithBindings( EOEditingContext ec, String entityName, String query, ERXSQLBinding... bindings ) {
        return rawRowsForQueryOnEntity(ec, entityName, query, bindings);
    }
}
