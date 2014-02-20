package er.extensions.eof;

import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * enhanced version which allows the user to 
 * 1) set refreshesRefetchedObjects
 * 2) set prefetchingRelationshipKeyPaths
 * 
 * which modifies the datasource's fetchSpecification
 * 
 * @author david teran
 */
public class ERXDatabaseDataSource extends EODatabaseDataSource {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public boolean _refreshRefetchedObjects;
    public NSArray<String> _prefetchingRelationshipKeyPaths;
    
    /**
	 * Constructs a new ERXDatabaseDataSource to fetch all objects for the EOEntity specified by
	 * <code>entityName</code> into <code>ec</code>. Finds the EOObjectStoreCoordinator for
	 * <code>ec</code> and searches for a channel that services the model of the entity.
	 * If one exists, the ERXDatabaseDataSource uses it. Otherwise, a new one is created for this
	 * ERXDatabaseDataSource.
	 *
	 * @param ec
	 *            the editing context into which to fetch objects
	 * @param entityName
	 *            the EOEntity for this data source
	 */
    public ERXDatabaseDataSource(EOEditingContext ec, String entityName) {
        super(ec, entityName);
    }

    /**
	 * Constructs a new ERXDatabaseDataSource to fetch objects into <code>ec</code> for the EOEntity
	 * specified by <code>entityName</code> using the fetch specification <code>fetchSpecificationName</code>.
	 * Finds the EOObjectStoreCoordinator for <code>ec</code> and searches for a channel that services
	 * the model of the entity. If one exists, the ERXDatabaseDataSource uses it. Otherwise, a new one
	 * is created for this ERXDatabaseDataSource.
	 * <br>
	 * <code>fetchSpecificationName</code> is used to find the fetch specification in the entity.
	 * If <code>fetchSpecificationName</code> is <code>null</code>, a new fetch specification is instantiated
	 * that will fetch all objects of the entity.
	 *
	 * @param ec
	 *            the editing context into which to fetch objects
	 * @param entityName
	 *            the EOEntity for this data source
	 * @param fetchSpecificationName
	 *            the criteria to select and order a group of database records, or <code>null</code>
	 */
   public ERXDatabaseDataSource(EOEditingContext ec, String entityName, String fetchSpecificationName) {
        super(ec, entityName, fetchSpecificationName);
    }

   /**
	 * Constructs a new ERXDatabaseDataSource to fetch objects into <code>ec</code> for the EOEntity
	 * specified by <code>entityName</code> using the fetch specification <code>fetchSpecificationName</code>.
	 * Finds the EOObjectStoreCoordinator for <code>ec</code> and searches for a channel that services
	 * the model of the entity. If one exists, the ERXDatabaseDataSource uses it. Otherwise, a new one
	 * is created for this ERXDatabaseDataSource.
	 * <br>
	 * <code>fetchSpecificationName</code> is used to find the fetch specification in the entity.
	 * If <code>fetchSpecificationName</code> is <code>null</code>, a new fetch specification is instantiated
	 * that will fetch all objects of the entity.
	 *
	 * @param ec
	 *            the editing context into which to fetch objects
	 * @param entityName
	 *            the EOEntity for this data source
	 * @param fetchSpecificationName
	 *            the criteria to select and order a group of database records, or <code>null</code>
	 * @param refresh
	 *            <code>true</code> if you want to refresh refetched objects
	 */
   public ERXDatabaseDataSource(EOEditingContext ec, String entityName, String fetchSpecificationName, boolean refresh) {
       super(ec, entityName, fetchSpecificationName);
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
   
   public void setPrefetchingRelationshipKeyPaths(NSArray<String> relationshipKeyPaths) {
       _prefetchingRelationshipKeyPaths = relationshipKeyPaths;
   }

   /**
    * Sets the relationships to prefetch along with the main fetch.
    * 
    * @see #setPrefetchingRelationshipKeyPaths(NSArray)
    * @param prefetchingRelationshipKeyPaths list of keys to prefetch
    */
   public void setPrefetchingRelationshipKeyPaths(ERXKey<?>... prefetchingRelationshipKeyPaths) {
       NSMutableArray<String> keypaths = new NSMutableArray<String>();
       for (ERXKey<?> key : prefetchingRelationshipKeyPaths) {
           keypaths.addObject(key.key());
       }
       setPrefetchingRelationshipKeyPaths(keypaths);
   }

    /** 
     * Enhanced version which uses the refreshesRefetchedObjects value
     * @see #setRefreshesRefetchedObjects(boolean)
     * @see EOFetchSpecification#refreshesRefetchedObjects()
     */
    @Override
    public EOFetchSpecification fetchSpecificationForFetch() {
        EOFetchSpecification spec = super.fetchSpecificationForFetch();
        spec.setRefreshesRefetchedObjects(refreshesRefetchedObjects());
        spec.setPrefetchingRelationshipKeyPaths(_prefetchingRelationshipKeyPaths);
        return spec;
    }
    
    /**
     * Enhanced version which uses the refreshesRefetchedObjects value
     * @see #setRefreshesRefetchedObjects(boolean)
     * @see EOFetchSpecification#refreshesRefetchedObjects()
     */
    @Override
    public EOFetchSpecification fetchSpecification() {
        EOFetchSpecification spec = super.fetchSpecification();
        spec.setRefreshesRefetchedObjects(refreshesRefetchedObjects());
        spec.setPrefetchingRelationshipKeyPaths(_prefetchingRelationshipKeyPaths);
        return spec;
    }
}
