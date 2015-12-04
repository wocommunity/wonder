package er.extensions.eof;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.eocontrol._EOIntegralKeyGlobalID;
import com.webobjects.eocontrol._EOVectorKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXArrayUtilities;

/**
 * Utilities that help with batch loading sets of global IDs. 
 * 
 * @author ak
 */
public class ERXEOGlobalIDUtilities {
	
    /** logging support */
    public static final Logger log = Logger.getLogger(ERXEOGlobalIDUtilities.class);
    
    /**
     * Decrypts the byte array of NSData PKs so you get the process number or port, 
     * host and timestamp.
     *
     * @author ak
     */
    public static class Info {

    	private byte _data[];
    	
    	private static final int _HostIdentificationStartIndex = 0;

    	private static final int _ProcessIdentificationStartIndex = 6;

    	private static final int _CounterStartIndex = 10;

    	private static final int _TimestampStartIndex = 12;

    	private static final int _RandomStartIndex = 20;


    	public Info(EOGlobalID gid) {
    		if(gid instanceof EOTemporaryGlobalID) {
    			_data = ((EOTemporaryGlobalID)gid)._rawBytes();
    		} else if (gid instanceof EOKeyGlobalID) {
    			EOKeyGlobalID keyGid = (EOKeyGlobalID)gid;
    			Object value = keyGid.keyValues()[0];
    			if(value instanceof NSData && keyGid.keyValues().length == 1) {
    				_data = ((NSData)value)._bytesNoCopy();
    			}
    		} 
    		
    		if(_data == null) {
    			throw new IllegalArgumentException("This class only works with EOTemporaryGlobalID or EOKeyGlobalID with a single 24-byte data PK");
    		}
    	}

    	private byte extractByte(int offset) {
    		return _data[offset + 0];
    	}

    	@SuppressWarnings("cast")
		private short extractShort(int offset) {
    		short result = 0;
    		result |= ((int)extractByte(offset + 0)) & 255;
    		result <<= 8;
    		result |= ((int)extractByte(offset + 1)) & 255;
    		return result;
    	}

    	private int extractInt(int offset) {
    		int result = 0;
    		result |= extractShort(offset + 0) & 65535;
    		result <<= 16;
    		result |= extractShort(offset + 2) & 65535;
    		return result;
    	}

    	private long extractLong(int offset) {
    		long result = 0;
    		result |= extractInt(offset + 0) & 4294967295L;
    		result <<= 32;
    		result |= extractInt(offset + 4) & 4294967295L;
    		return result;
    	}

    	public NSTimestamp timestamp() {
    		return new NSTimestamp(milliseconds());
    	}
    	
    	public long milliseconds() {
     		return extractLong(_TimestampStartIndex);
    	}
    	
    	public InetAddress host() {
    		try {
    			byte data[] = new byte[4];
    			System.arraycopy(_data, _HostIdentificationStartIndex + 2, data, 0, 4);
				return InetAddress.getByAddress(data);
			} catch (UnknownHostException e) {
				return null;
			}
    	}
     	
    	public long port() {
			return extractInt(_ProcessIdentificationStartIndex);
    	}
    	
    	public short counter() {
    		return extractShort(_CounterStartIndex);
    	}
    	
    	public long random() {
    		return extractLong(_RandomStartIndex);
    	}
    	
    	@Override
		public String toString() {
    		return host().getHostAddress() + ":" + port() + " " + counter() + "@" + timestamp();
    	}
    }

    /**
     * Groups an array of global IDs by their entity name.
     * 
     * @param <T> subclass of EOGlobalID
     * @param globalIDs list of global IDs
     * @return dictionary with global IDs grouped by their entity name
     */
    public static <T extends EOGlobalID> NSDictionary<String, NSArray<T>> globalIDsGroupedByEntityName(Collection<T> globalIDs) {
        return ERXArrayUtilities.arrayGroupedByKeyPath(globalIDs, "entityName");
    }

    /**
     * Translates an array of {@link EOGlobalID} to primary key values. Throws an exception if the given
     * global IDs are not EOKeyGlobalIDs and the primary keys are not single values.
     * 
     * @param <T> subclass of EOGlobalID
     * @param globalIDs list of global IDs
     * @return list of primary key values
     */
    public static <T extends EOGlobalID> NSArray<Object> primaryKeyValuesWithGlobalIDs(Collection<T> globalIDs) {
    	if (globalIDs == null || globalIDs.isEmpty()) {
    		return NSArray.emptyArray();
    	}
    	NSMutableArray<Object> result = new NSMutableArray<>(globalIDs.size());
    	NSDictionary<String, NSArray<T>> gidsByEntity = globalIDsGroupedByEntityName(globalIDs);
    	for (String entityName : gidsByEntity.keySet()) {
    		NSArray<T> gidsForEntity = gidsByEntity.objectForKey(entityName);
    		for (T gid : gidsForEntity) {
    			if (gid instanceof EOKeyGlobalID) {
    				EOKeyGlobalID keyGID = (EOKeyGlobalID) gid;
    				if (keyGID.keyCount() == 1) {
    					result.addObject(keyGID.keyValues()[0]);
    				} else {
    					throw new IllegalArgumentException("GID has more than one key: " + keyGID);
    				}
    			} else {
    				throw new IllegalArgumentException("GID is not an EOKeyGlobalID: " + gid);
    			}
    		}
    	}
    	return result;
    }

    /**
     * Translates an array of single-value raw primary key values to EOGlobalIDs.
     * 
     * @param entityName the entity name of the raw primary values
     * @param values list of raw primary key values
     * @return list of global IDs
     */
    public static NSArray<EOGlobalID> globalIDsWithPrimaryKeyValues(String entityName, Collection<Object> values) {
        if (values == null || values.isEmpty()) {
            return NSArray.emptyArray();
        }
        NSMutableArray<EOGlobalID> result = new NSMutableArray<>(values.size());
        for (Object value : values) {
            EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entityName, new Object[] {value});
            result.addObject(gid);
        }
        return result;
    }

    /**
     * Fetches an object defined by gid without refreshing refetched objects.
     * 
     * @param ec the editing context to fetch within
     * @param gid the global ID to fetch
     * @return the fetched EO
     */
    public static EOEnterpriseObject fetchObjectWithGlobalID(EOEditingContext ec, EOGlobalID gid) {
    	NSArray<EOEnterpriseObject> results = fetchObjectsWithGlobalIDs(ec, new NSArray<>(gid));
    	return ERXArrayUtilities.firstObject(results);
    }
    
    /**
     * Fetches an array of objects defined by the globalIDs in a single fetch per entity without
     * refreshing refetched objects.
     * 
     * @param <T> subclass of EOGlobalID
     * @param ec the editing context to fetch within
     * @param globalIDs the global IDs to fetch
     * @return the fetched EOs
     */
    public static <T extends EOGlobalID> NSMutableArray<EOEnterpriseObject> fetchObjectsWithGlobalIDs(EOEditingContext ec, Collection<T> globalIDs) {
    	return fetchObjectsWithGlobalIDs(ec, globalIDs, false);
    }

    /**
     * Fetches an array of objects defined by the globalIDs in a single fetch per entity.
     * 
     * @param <T> subclass of EOGlobalID
     * @param ec the editing context to fetch within
     * @param globalIDs the global IDs to fetch
     * @param refreshesRefetchedObjects whether or not to refresh refetched objects
     * @return the fetched EOs
     */
	public static <T extends EOGlobalID> NSMutableArray<EOEnterpriseObject> fetchObjectsWithGlobalIDs(EOEditingContext ec, Collection<T> globalIDs, boolean refreshesRefetchedObjects) {
    	NSMutableArray<EOEnterpriseObject> result = new NSMutableArray<>();
		ec.lock();
		ec.rootObjectStore().lock();
		try {
	    	NSDictionary<String, NSArray<T>> gidsByEntity = globalIDsGroupedByEntityName(globalIDs);
	    	for(String entityName : gidsByEntity.keySet()) {
	    		NSArray<T> gidsForEntity = gidsByEntity.objectForKey(entityName);
	    		
	    		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<>();
	        	EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
	    		for (T g : gidsForEntity) {
	    			boolean fetch = refreshesRefetchedObjects;
	    			if (!fetch) {
	    				EOEnterpriseObject eo;
						eo = ec.objectForGlobalID(g);
	    				if (eo != null && !EOFaultHandler.isFault(eo)) {
	    					result.addObject(eo);
	    				}
	    				else {
	    					NSDictionary row;
	    			        EODatabaseContext databaseContext = (EODatabaseContext) ((EOObjectStoreCoordinator) ec.rootObjectStore()).objectStoreForGlobalID(g);
	    			        databaseContext.lock();
	    			        try {
		    					row = databaseContext.snapshotForGlobalID(g, ec.fetchTimestamp());
	    			        }
	    			        finally {
	    			        	databaseContext.unlock();
	    			        }
	    					if (row == null) {
	    						fetch = true;
	    					}
	    					else {
		    					eo = ec.faultForGlobalID(g, ec);
		    					result.addObject(eo);
	    					}
	    				}
	    			}
	    			if (fetch) {
	    				EOQualifier qualifier = entity.qualifierForPrimaryKey(entity.primaryKeyForGlobalID(g));
	    				qualifiers.addObject(qualifier);
	    			}
	    		}
	    		if (!qualifiers.isEmpty()) {
		    		EOQualifier qualifier = qualifiers.size() > 1 ? ERXQ.or(qualifiers) : qualifiers.lastObject();
		    		EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName, qualifier, null);
		    		fetchSpec.setRefreshesRefetchedObjects(refreshesRefetchedObjects);
		    		NSArray<EOEnterpriseObject> details = ec.objectsWithFetchSpecification(fetchSpec);
		    		result.addObjectsFromArray(details);
	    		}
	    	}
		}
		finally {
			ec.rootObjectStore().unlock();
			ec.unlock();
		}
    	return result;
    }

    /**
     * Fires all faults in the given global IDs on one batch together with their relationships.
     * This is much more efficient than triggering the individual faults and relationships later on.
     * The method should use only 1 fetch for all objects per entity
     * and then one for each relationship per entity.
     * 
     * @param <T> subclass of EOGlobalID
     * @param ec editing context to fault into
     * @param globalIDs the global IDs to fault
     * @param prefetchingKeypaths list of key paths to prefetch or null
     * @return faulted objects
     */
    public static <T extends EOGlobalID> NSArray<EOEnterpriseObject> fireFaultsForGlobalIDs(EOEditingContext ec, Collection<T> globalIDs, Collection<String> prefetchingKeypaths) {
    	if (globalIDs == null || globalIDs.isEmpty()) {
    		return NSArray.emptyArray();
    	}
    	NSMutableArray<EOEnterpriseObject> result = new NSMutableArray<>(globalIDs.size());
    	NSMutableArray<T> faults = new NSMutableArray<>(globalIDs.size());
    	for (T gid : globalIDs) {
    		EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
    		if (EOFaultHandler.isFault(eo)) {
    			faults.addObject(gid);
    		} else {
    			result.addObject(eo);
    		}
    	}
    	NSArray<EOEnterpriseObject> loadedObjects = fetchObjectsWithGlobalIDs(ec, faults);
    	result.addObjectsFromArray(loadedObjects);
    	if (prefetchingKeypaths != null && !prefetchingKeypaths.isEmpty()) {
    		NSDictionary<String, NSArray<EOEnterpriseObject>> objectsByEntity = ERXArrayUtilities.arrayGroupedByKeyPath(result, "entityName");
    		for (String entityName : objectsByEntity.keySet()) {
    			NSArray<EOEnterpriseObject> objects = objectsByEntity.objectForKey(entityName);
    			EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
    			for (String keypath : prefetchingKeypaths) {
    				EORelationship relationship = entity.relationshipNamed(keypath);
    				EODatabaseContext dbc = ERXEOAccessUtilities.databaseContextForEntityNamed((EOObjectStoreCoordinator) ec.rootObjectStore(), entityName);
    				dbc.lock();
    				try {
    					dbc.batchFetchRelationship(relationship, objects, ec);
    				} finally {
    					dbc.unlock();
    				}
    			}
    		}
    	}
    	return result;
    }

    /**
     * Fires all faults in the given global IDs on one batch. This is much more efficient than
     * triggering the individual faults later on. The method should use only 1 fetch for all objects per entity.
     * 
     * @param <T> subclass of EOGlobalID
     * @param ec editing context to fault in
     * @param globalIDs the global IDs to fault
     * @return list of faulted objects
     */
    public static <T extends EOGlobalID> NSArray<EOEnterpriseObject> fireFaultsForGlobalIDs(EOEditingContext ec, Collection<T> globalIDs) {
        return fireFaultsForGlobalIDs(ec, globalIDs, NSArray.emptyArray());
    }

    /**
     * Creates a global ID for the given entity name and its primary key value(s).
     * 
     * @param entityName the entity name
     * @param values one or more primary key values
     * @return global ID object
     */
	public static EOKeyGlobalID createGlobalID(String entityName, Object[] values) {
		if (values != null && values.length == 1) {
			Object primaryKey = values[0];
			if (primaryKey instanceof Number) {
				return new _EOIntegralKeyGlobalID(entityName, (Number) primaryKey);
			}
		}
		return new _EOVectorKeyGlobalID(entityName, values);
	}
}
