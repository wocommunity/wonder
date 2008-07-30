package er.extensions.eof;

import java.lang.reflect.Method;

import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;


/**
 * Hack around bug in 5.3.3.
 * 
 * @see ERXDatabaseContextMulticastingDelegate
 *
 * @author chill
 */
public class ERXDatabaseContextMulticastingDelegate53 extends ERXDatabaseContextMulticastingDelegate {
	
    private Method currentEditingContext;

    public ERXDatabaseContextMulticastingDelegate53() {
        super();
        try {
            currentEditingContext = EODatabaseChannel.class.getDeclaredMethod("currentEditingContext", new Class[] {});
            currentEditingContext.setAccessible(true);
        }
        catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }
	
	/**
	  * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldUpdateCurrentSnapshot(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.foundation.NSDictionary, com.webobjects.foundation.NSDictionary, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eoaccess.EODatabaseChannel)
	  * @see EODatabase#snapshotForGlobalID(EOGlobalID, long)
	  */
	 public NSDictionary databaseContextShouldUpdateCurrentSnapshot(EODatabaseContext dbCtxt, NSDictionary existingSnapshot, NSDictionary fetchedRow, EOGlobalID gid, EODatabaseChannel dbChannel) {
		 NSDictionary resultSnapshot =  (NSDictionary)perform("databaseContextShouldUpdateCurrentSnapshot", dbCtxt, existingSnapshot, fetchedRow, gid, dbChannel, null);
		 if (resultSnapshot != null) {
			 return resultSnapshot;
		 }
		 
		 /* There is no way for this delegate method to say "do what you normally do". Our two choices for a default return value are 
		   never update the snapshot or always update it.  What we want is neither, we want the unchanged EOF behavior.  So we 
		   re-implement what EODatabaseChannel would so in the absence of this delegate method: updating of the snapshot depends 
		   whether we are refreshing and on the snapshot's age and the fetchTimestamp of the EC doing the fetching.
		  */  	
		try {
		 	// Again with the object rape.  EODatabaseChannel.currentEditingContext() is private and the underlying instance variable
		 	// is protected.  We need the ec, so we do what we have to...
		 	EOEditingContext ec = (EOEditingContext) currentEditingContext.invoke(dbChannel, new Object[] {});
		 	
		 	// Get the snapshot if it has not expired.  cachedSnapshot will be null if it has expired
		 	// If not null, it should be the same as the existingSnapshot parameter
		 	NSDictionary cachedSnapshot = dbCtxt.database().snapshotForGlobalID(gid, ec.fetchTimestamp());
		
		 	// If we are refreshing or the snapshot in the cache has timed out, but the fetched row 
		 	// matches the cached snapshot, reset the time stamp by recording the existing snapshot again.
		 	if (existingSnapshot.equals(fetchedRow) && (dbChannel.isRefreshingObjects() || cachedSnapshot == null)) {
		 		dbCtxt.database().recordSnapshotForGlobalID(existingSnapshot, gid);
		 	}
		 	
		 	// Handle refreshing fetches.  If the fetched data is the same, we return
		 	// existingSnapshot to avoid a bug in EODatabaseChannel where == is used 
		 	// instead of equals() when this delegate method is called.  Without this, 
		 	// EOObjectStore.ObjectsChangedInStoreNotification will be sent when  there are no real changes.
		 	// This should be fixed in 5.4.2
		 	if (dbChannel.isRefreshingObjects()) {
		 		return  existingSnapshot.equals(fetchedRow) ? existingSnapshot : fetchedRow;
		 	}
		
		 	// Same as for refreshing, if the snapshot has expired but is unchanged in the database,
		 	// return the existing snapshot
		 	if (cachedSnapshot == null && existingSnapshot.equals(fetchedRow)) {
		 		return existingSnapshot;
		 	}
		
		 	// Return the existing snapshot unless it has expired
		    return cachedSnapshot != null ? existingSnapshot : fetchedRow;
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
	    }
	}

}
