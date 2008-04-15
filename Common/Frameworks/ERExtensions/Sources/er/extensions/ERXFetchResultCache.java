package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Transparent cache for fetch results, uses ERXFetchSpecifictation.identifier() as a key.
 * @author ak
 *
 */
public class ERXFetchResultCache {

	private static EODatabase currentDatabase;
	
	private ERXExpiringCache<String, NSArray<EOGlobalID>> cache = new ERXExpiringCache<String, NSArray<EOGlobalID>>() {
		@Override
		protected synchronized void removeEntryForKey(Entry<NSArray<EOGlobalID>> entry, String key) {
			for (EOGlobalID gid : entry.object()) {
				currentDatabase.decrementSnapshotCountForGlobalID(gid);
			}
			super.removeEntryForKey(entry, key);
		}
		
		@Override
		protected synchronized void setEntryForKey(Entry<NSArray<EOGlobalID>> entry, String key) {
			super.setEntryForKey(entry, key);
			for (EOGlobalID gid : entry.object()) {
				currentDatabase.incrementSnapshotCountForGlobalID(gid);
			}
		}
	};
	
	private static final Logger log = Logger.getLogger(ERXFetchResultCache.class);
	
	/**
	 * Returns an array of EOs that where cached for the given fetch specification or null.
	 * @param ec
	 * @param fs
	 * @return
	 */
	public NSArray<? extends EOEnterpriseObject> objectsForFetchSpecification(EODatabaseContext dbc, EOEditingContext ec, EOFetchSpecification fs) {
		String identifier = ERXFetchSpecification.identifierForFetchSpec(fs);
		synchronized (cache) {
			currentDatabase = dbc.database();
			NSArray<EOGlobalID> gids = cache.objectForKey(identifier);
			NSArray result = null;
			
			if(gids != null) {
				NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<EOEnterpriseObject>(gids.count());
				EODatabase database = dbc.database();
				for (EOGlobalID gid : gids) {
					NSDictionary snapshotForGlobalID = database.snapshotForGlobalID(gid);
					if(snapshotForGlobalID != null) {
						
					} else {
						log.error("Error: " + gid);
					}
					//database.recordSnapshotForGlobalID(snapshotForGlobalID, gid);
					database.incrementSnapshotCountForGlobalID(gid);
					//dbc.recordSnapshotForGlobalID(dbc.snapshotForGlobalID(gid), gid);
		            EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
		            //ec.initializeObject(eo, gid, ec);
		            eos.addObject(eo);
				}
				result = eos;
				currentDatabase = null;
			}
			boolean hit = result != null;
			if(hit) {
				log.info("Cache : " + (hit ? "HIT" : "MISS") + " on " + fs.entityName());
			}
			return result;			
		}
	}
	
	/**
	 * Returns a list of entities that should not be cached.
	 * @return
	 */
	protected NSArray<String> excludedEntities() {
		return NSArray.EmptyArray;
	}

	/**
	 * Returns true if the given fetch spec is not to be cached.
	 * @param fs
	 * @return
	 */
	protected boolean shouldCache(NSArray eos, EOFetchSpecification fs) {
		if(fs.fetchesRawRows() || fs.refreshesRefetchedObjects()) {
			return false;
		}
		for (Object object : eos) {
			if (!(object instanceof EOEnterpriseObject)) {
				return false;
			}
			if (EOFaultHandler.isFault(object)) {
				return false;
			}
			if (excludedEntities().containsObject(((EOEnterpriseObject)object).entityName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Registers eos for a given fetch spec.
	 * @param ec 
	 * @param dbc 
	 * @param eos
	 * @param fs
	 */
	public void setObjectsForFetchSpecification(EODatabaseContext dbc, EOEditingContext ec, NSArray<?> eos, EOFetchSpecification fs) {
		String identifier = ERXFetchSpecification.identifierForFetchSpec(fs);
		synchronized (cache) {
			currentDatabase = dbc.database();

			boolean shouldCache = shouldCache(eos, fs);
			if(shouldCache) {
				NSArray<EOGlobalID> gids = ERXEOControlUtilities.globalIDsForObjects(eos);
				cache.setObjectForKeyWithVersion(gids, identifier, null, 60000L);
			}
			log.debug("Cache : " + (shouldCache ? "SET" : "DROP") + " on " + fs.entityName());
			currentDatabase = null;
		}
	}
}
