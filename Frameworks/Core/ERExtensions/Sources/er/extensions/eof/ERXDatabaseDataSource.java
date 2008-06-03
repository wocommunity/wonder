package er.extensions.eof;

import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;


/**
 * @author david teran
 *
 * enhanced version which allows the user to 
 * 1) set refreshesRefetchedObjects
 * 2) set prefetchingRelationshipKeyPaths
 * 
 * which modifies the datasource's fetchSpecification
 * 
 */
public class ERXDatabaseDataSource extends EODatabaseDataSource {

    public boolean _refreshRefetchedObjects;
    public NSArray _prefetchingRelationshipKeyPaths;
    
    /**
     * @see com.webobjects.eoaccess.EODatabaseDataSource  
	*/
    public ERXDatabaseDataSource(EOEditingContext arg0, String arg1) {
        super(arg0, arg1);
    }

    /**
     * @see com.webobjects.eoaccess.EODatabaseDataSource  
	*/
   public ERXDatabaseDataSource(EOEditingContext arg0, String arg1, String arg2) {
        super(arg0, arg1, arg2);
    }

   /**
    * @see com.webobjects.eoaccess.EODatabaseDataSource  
	*/
   public ERXDatabaseDataSource(EOEditingContext arg0, String arg1, String arg2, boolean refresh) {
       super(arg0, arg1, arg2);
       _refreshRefetchedObjects = refresh;
   }

   public void setRefreshesRefetchedObjects(boolean v) {
       _refreshRefetchedObjects = v;
   }
   public boolean refreshesRefetchedObjects() {
       return _refreshRefetchedObjects;
   }
   public NSArray prefetchingRelationshipKeyPaths() {
       return _prefetchingRelationshipKeyPaths;
   }
   public void setPrefetchingRelationshipKeyPaths(NSArray relationshipKeyPaths) {
       _prefetchingRelationshipKeyPaths = relationshipKeyPaths;
   }
   
   
    /** Enhanced version which uses the refreshesRefetchedObjects value
     * @see ERXDatabaseDataSource.setRefreshedRefetchedObjects
     */
    public EOFetchSpecification fetchSpecificationForFetch() {
        EOFetchSpecification spec = super.fetchSpecificationForFetch();
        spec.setRefreshesRefetchedObjects(refreshesRefetchedObjects());
        spec.setPrefetchingRelationshipKeyPaths(_prefetchingRelationshipKeyPaths);
        return spec;
    }
    
    

    /** Enhanced version which uses the refreshesRefetchedObjects value
     * @see ERXDatabaseDataSource.setRefreshedRefetchedObjects
     */
    public EOFetchSpecification fetchSpecification() {
        EOFetchSpecification spec = super.fetchSpecification();
        spec.setRefreshesRefetchedObjects(refreshesRefetchedObjects());
        spec.setPrefetchingRelationshipKeyPaths(_prefetchingRelationshipKeyPaths);
        return spec;
    }
}
