package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
* Extends {@link WODisplayGroup} in order to provide real batching.
 * This is done by adding TOP(start, rows) to the select statement
 * from the {@link EOFetchSpecification} from the {@link WODisplayGroup}'s
 * {@link EODataSource} which <b>must</b> be an {@link EODatabaseDataSource}.
 * The top SQL command is recognized by FrontBase and maybe other DB servers.
 * Future versions will provide a delegate in order to modify the value
 * for lets say Oracle.
 */
public class ERXBatchingDisplayGroup extends WODisplayGroup {

    /**
    * Determines if batching is possible. 
     *
     * @return true if dataSource is an instance of EODatabaseDataSource
     */
    public boolean isBatching() {
        return dataSource() instanceof EODatabaseDataSource;
    }

    /**
    * Returns an {@link NSArray} containing the objects from the resulting rows starting
     * at start and stopping at end using a custom SQL, derived from the SQL
     * which the {@link EOFetchSpecification} would use normally {@link EOFetchSpecification.setHints}
     *
     * @param start 
     * @param end
     *
     * @return
     */
    public NSArray objectsInRange(int start, int end) {
        //uses the original fetch specification and adds a top(start,(end - start)) to the query sql
        EOFetchSpecification spec = databaseDataSource().fetchSpecificationForFetch();
        //sortOrderings from the WODisplayGroup is only used in Memory: painful slow...
        spec.setSortOrderings(sortOrderings());

        EOEditingContext ec = databaseDataSource().editingContext();
        String sql = ERXEOAccessUtilities.sqlForFetchSpecificationAndEditingContext(spec, ec);
        
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

        NSDictionary hints = new NSDictionary(sql, "EOCustomQueryExpressionHintKey");
        spec.setHints(hints);
        
        return ec.objectsWithFetchSpecification(spec);
    }

    /**
    * Overridden in order to use a custom method which determines the number of Objects / rows
     * for the existing EODatabaseDataSource.
     *
     * @return the number of rows from the EODatabaseDataSource
     */
    public int batchCount() {
        if(numberOfObjectsPerBatch() == 0) {
            return 0;
        }
        if(rowCount() == 0) {
            return 1;
        } else {
            return (rowCount() - 1) / numberOfObjectsPerBatch() + 1;
        }
    }

    /**
    * Returns the number of rows from the {@link EODatabaseDataSource}.
     *
     * @return the number of rows from the {@link EODatabaseDataSource}
     */
    public int rowCount() {
        EOFetchSpecification spec = databaseDataSource().fetchSpecificationForFetch();
        EOEditingContext ec = databaseDataSource().editingContext();
        EOModel model = ERXEOAccessUtilities.modelForFetchSpecificationAndEditingContext(spec, ec);
        String sql = ERXEOAccessUtilities.sqlForFetchSpecificationAndEditingContext(spec, ec);
        int index = sql.indexOf("from");
        if (index == -1) {
            index = sql.indexOf("FROM");
        }
        sql = "select count(*) " + sql.substring(index, sql.length());
        NSArray result = EOUtilities.rawRowsForSQL(ec, model.name(), sql);

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
                        setObjectArray(new ERFakeNSArray(c));
                        return c;
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
    }
    
    public EODatabaseDataSource databaseDataSource() {
        return (EODatabaseDataSource)dataSource();
    }

    /**
    * Overridden method in order to fetch -only- the rows that are needed. This is
     * different to the editors methods because a {@link WODisplayGroup} would always fetch
     * from the start until the end of the objects from the fetch limit.
     *
     * @return the objects that should be diplayed.
     */
    public NSArray displayedObjects() {
        if (!isBatching()) return super.displayedObjects();

        //check the start and end based on currentBatchIndex and numberOfObjectsPerBatch()
        int count = rowCount();
        int start = currentBatchIndex() * numberOfObjectsPerBatch();
        int end = start + numberOfObjectsPerBatch();

        if (numberOfObjectsPerBatch() == 0) {
            start = 0;
            end = rowCount();
        }

        NSMutableArray objects = objectsInRange(start, end).mutableClone();

        return objects;
    }

    public static class ERFakeNSArray extends NSArray {
        int count = 0;

        public ERFakeNSArray(int count) {
            this.count = count;
        }
        private ERFakeNSArray(){}

        public int count() {
            return count;
        }

        public void insertObjectAtIndex(Object o, int i) {
            //do nothing
        }
    }
}
