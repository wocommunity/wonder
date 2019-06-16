package er.extensions.eof;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import er.extensions.foundation.ERXExpiringCache;

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
	
	private static final Logger log = LoggerFactory.getLogger(ERXFetchResultCache.class);
	
	/**
	 * Returns an array of EOs that where cached for the given fetch specification or null.
	 * @param ec
	 * @param fs
	 */
	public NSArray<? extends EOEnterpriseObject> objectsForFetchSpecification(EODatabaseContext dbc, EOEditingContext ec, EOFetchSpecification fs) {
		String identifier = ERXFetchSpecification.identifierForFetchSpec(fs);
		synchronized (cache) {
			currentDatabase = dbc.database();
			NSArray<EOGlobalID> gids = cache.objectForKey(identifier);
			NSArray result = null;
			
			if(gids != null) {
				NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<>(gids.count());
				EODatabase database = dbc.database();
				for (EOGlobalID gid : gids) {
					NSDictionary snapshotForGlobalID = database.snapshotForGlobalID(gid);
					if(snapshotForGlobalID == null || dbc.snapshotForGlobalID(gid, ec.fetchTimestamp()) == null) {
						// not found with recent timestamp
						return null;
					}
					database.recordSnapshotForGlobalID(snapshotForGlobalID, gid);
		            EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
		            eos.addObject(eo);
				}
				result = eos;
			}
			currentDatabase = null;
			if(log.isDebugEnabled()) {
				boolean hit = result != null;
				log.info("Cache: {} on {}", hit ? "HIT" : "MISS", fs.entityName());
			}
			return result;			
		}
	}
	
	/**
	 * Returns a list of entities that should not be cached.
	 */
	protected NSArray<String> excludedEntities() {
		return NSArray.EmptyArray;
	}

	/**
	 * Returns the time the result should stay in the cache. Less or equal than zero means don't cache.
	 * @param fs
	 */
	protected long cacheTime(NSArray eos, EOFetchSpecification fs) {
		if(fs.fetchesRawRows() || fs.refreshesRefetchedObjects()) {
			return 0;
		}
		for (Object object : eos) {
			if (!(object instanceof EOEnterpriseObject)) {
				return 0;
			}
			if (EOFaultHandler.isFault(object)) {
				return 0;
			}
			if (excludedEntities().containsObject(((EOEnterpriseObject)object).entityName())) {
				return 0;
			}
		}
		return 100L;
	}

	/**
	 * Registers eos for a given fetch spec.
	 * @param ec 
	 * @param dbc 
	 * @param eos
	 * @param fs
	 */
	public void setObjectsForFetchSpecification(EODatabaseContext dbc, EOEditingContext ec, NSArray<? extends EOEnterpriseObject> eos, EOFetchSpecification fs) {
		String identifier = ERXFetchSpecification.identifierForFetchSpec(fs);
		synchronized (cache) {
			currentDatabase = dbc.database();

			long cacheTime = cacheTime(eos, fs);
			if(cacheTime > 0) {
				NSArray<EOGlobalID> gids = ERXEOControlUtilities.globalIDsForObjects(eos);
				cache.setObjectForKeyWithVersion(gids, identifier, null, cacheTime);
			}
			if(log.isDebugEnabled()) {
				log.debug("Cache: {} on {}", cacheTime > 0 ? "SET" : "DROP", fs.entityName());
			}
			currentDatabase = null;
		}
	}
}
