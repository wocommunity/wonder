package er.extensions;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;


/**
 * @author david teran
 *
 * enhanced version which allows the user to set refreshesRefetchedObjects
 * which modifies the datasource's fetchSpecification
 * 
 */
public class ERXDatabaseDataSource extends EODatabaseDataSource {

    public boolean _refreshRefetchedObjects;

    
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
   
    /** Enhanced version which uses the refreshesRefetchedObjects value
     * @see ERXDatabaseDataSource.setRefreshedRefetchedObjects
     */
    public EOFetchSpecification fetchSpecificationForFetch() {
        EOFetchSpecification spec = super.fetchSpecificationForFetch();
        spec.setRefreshesRefetchedObjects(refreshesRefetchedObjects());
        return spec;
    }
    
    

    /** Enhanced version which uses the refreshesRefetchedObjects value
     * @see ERXDatabaseDataSource.setRefreshedRefetchedObjects
     */
    public EOFetchSpecification fetchSpecification() {
        EOFetchSpecification spec = super.fetchSpecification();
        spec.setRefreshesRefetchedObjects(refreshesRefetchedObjects());
        return spec;
    }
}
