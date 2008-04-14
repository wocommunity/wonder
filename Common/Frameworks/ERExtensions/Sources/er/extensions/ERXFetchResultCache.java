package er.extensions;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;

/**
 * Transparent cache for fetch results, uses ERXFetchSpecifictation.identifier() as a key.
 * @author ak
 *
 */
public class ERXFetchResultCache {

	private ERXExpiringCache<String, NSArray<EOGlobalID>> cache = new ERXExpiringCache<String, NSArray<EOGlobalID>>();
	
	/**
	 * Returns an array of EOs that where cached for the given fetch specification or null.
	 * @param ec
	 * @param fs
	 * @return
	 */
	public NSArray<? extends EOEnterpriseObject> objectsForFetchSpecification(EOEditingContext ec, EOFetchSpecification fs) {
		String identifier = ERXFetchSpecification.identifierForFetchSpec(fs);
		synchronized (cache) {
			NSArray<EOGlobalID> objects = cache.objectForKey(identifier);
			NSArray result = null;
			
			if(objects != null) {
				result = ERXEOControlUtilities.faultsForGlobalIDs(ec, objects);
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
	protected boolean isExcluded(EOFetchSpecification fs) {
		return excludedEntities().containsObject(fs.entityName()) || true;
	}

	/**
	 * Registers eos for a given fetch spec.
	 * @param eos
	 * @param fs
	 */
	public void setObjectsForFetchSpecification(NSArray<?> eos, EOFetchSpecification fs) {
		String identifier = ERXFetchSpecification.identifierForFetchSpec(fs);
		synchronized (cache) {
			boolean isSafe = true;
			for (Object object : eos) {
				if (!(object instanceof EOEnterpriseObject)) {
					return;
				}
				if (excludedEntities().containsObject(((EOEnterpriseObject)object).entityName())) {
					return;
				}
			}
		}

		if(isExcluded(fs)) {
			NSArray<EOGlobalID> gids = ERXEOControlUtilities.globalIDsForObjects(eos);
			cache.setObjectForKeyWithVersion(gids, identifier, null, 100000L);
		}
	}
}
