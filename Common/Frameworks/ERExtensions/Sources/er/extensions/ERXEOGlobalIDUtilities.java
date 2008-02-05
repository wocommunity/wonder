/*
 * Created on 24.01.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;

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
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

/**
 * Utilities that help with batch loading sets of global IDs. 
 * 
 * @author ak
 */
public class ERXEOGlobalIDUtilities {
	
    /** logging support */
    public static final Logger log = Logger.getLogger(ERXEOGlobalIDUtilities.class);
    
    /**
     * Unencrypts the byte array of NSData PKs so you get the process number or port, 
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
    	
    	public String toString() {
    		return host().getHostAddress() + ":" + port() + " " + counter() + "@" + timestamp();
    	}
    }
    
    /**
     * Groups an array of global IDs by their entity name.
     * @param globalIDs
     */
    public static NSDictionary globalIDsGroupedByEntityName(NSArray globalIDs) {
        return ERXArrayUtilities.arrayGroupedByKeyPath(globalIDs, "entityName");
    }

    /**
     * Translates an array of {@link EOGlobalID} to primary key values. Returns null if the given
     * global IDs are not EOKeyValueGlobalIDs and the primary keys are not single values.
     * @param globalIDs
     */
    public static NSArray primaryKeyValuesWithGlobalIDs(NSArray globalIDs) {
    	NSMutableArray result = new NSMutableArray();
    	if(globalIDs.count() > 0) {
    		NSDictionary gidsByEntity = globalIDsGroupedByEntityName(globalIDs);
    		for(Enumeration e = gidsByEntity.keyEnumerator(); e.hasMoreElements();) {
    			String entityName = (String) e.nextElement();
    			NSArray gidsForEntity = (NSArray) gidsByEntity.objectForKey(entityName);
    			
    			for (Enumeration gids = gidsForEntity.objectEnumerator(); gids.hasMoreElements();) {
    				EOKeyGlobalID keyGID = (EOKeyGlobalID) gids.nextElement();
    				if(keyGID.keyCount() == 1) {
    					result.addObject(keyGID.keyValues()[0]);
    				} else {
    					throw new IllegalArgumentException("GID has more than one key: " + keyGID);
    				}
    			}
    		}
    	}
    	return result;
    }
    
    /**
     * Translates an array of single-value raw primary values to EOGlobalIDs.
     * @param entityName
     * @param values
     */
    public static NSArray globalIDsWithPrimaryKeyValues(String entityName, NSArray values) {
        NSMutableArray result = new NSMutableArray();
        if(values.count() > 0) {
            for (Enumeration pks = values.objectEnumerator(); pks.hasMoreElements();) {
                Object value = pks.nextElement();
                EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entityName, new Object[] {value});
                result.addObject(gid);
            }
        }
        return result;
    }

    /**
     * Fetches an object defined by gid without refreshing refetched objects.
     * 
     * @param ec the editing context to fetch within
     * @param gid the global id to fetch
     * @return the fetched EO
     */
    public static EOEnterpriseObject fetchObjectWithGlobalID(EOEditingContext ec, EOGlobalID gid) {
    	NSArray results = ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(ec, new NSArray(gid));
    	EOEnterpriseObject eo;
    	if (results.count() > 0) {
    		eo = (EOEnterpriseObject) results.objectAtIndex(0);
    	}
    	else {
    		eo = null;
    	}
    	return eo;
    }
    
    /**
     * Fetches an array of objects defined by the globalIDs in a single fetch per entity without
     * refreshing refetched objects.
     * 
     * @param ec the editing context to fetch within
     * @param globalIDs the global ids to fetch
     * @return the fetched EO's
     */
    public static NSArray fetchObjectsWithGlobalIDs(EOEditingContext ec, NSArray globalIDs) {
    	return ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(ec, globalIDs, false);
    }

    /**
     * Fetches an array of objects defined by the globalIDs in a single fetch per entity.
     * 
     * @param ec the editing context to fetch within
     * @param globalIDs the global ids to fetch
     * @param refreshesRefetchedObjects whether or not to refresh refetched objects
     * @return the fetched EO's
     */
    public static NSArray fetchObjectsWithGlobalIDs(EOEditingContext ec, NSArray globalIDs, boolean refreshesRefetchedObjects) {
    	NSMutableArray result = new NSMutableArray();
		ec.lock();
		try {
	    	NSDictionary gidsByEntity = globalIDsGroupedByEntityName(globalIDs);
	    	for(Enumeration e = gidsByEntity.keyEnumerator(); e.hasMoreElements();) {
	    		String entityName = (String) e.nextElement();
	    		NSArray gidsForEntity = (NSArray) gidsByEntity.objectForKey(entityName);
	    		
	    		NSMutableArray qualifiers = new NSMutableArray();
	        	EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
	    		for (Enumeration gids = gidsForEntity.objectEnumerator(); gids.hasMoreElements();) {
	    			EOGlobalID g = (EOGlobalID) gids.nextElement();
	    			boolean fetch = refreshesRefetchedObjects;
	    			if (!fetch) {
	    				EOEnterpriseObject eo;
						eo = ec.objectForGlobalID(g);
	    				if (eo != null && !EOFaultHandler.isFault(eo)) {
	    					result.addObject(eo);
	    				}
	    				else {
	    					fetch = true;
	    				}
	    			}
	    			if (fetch) {
	    				EOQualifier qualifier = entity.qualifierForPrimaryKey(entity.primaryKeyForGlobalID(g));
	    				qualifiers.addObject(qualifier);
	    			}
	    		}
	    		EOQualifier qualifier = new EOOrQualifier(qualifiers);
	    		EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName, qualifier, null);
	    		fetchSpec.setRefreshesRefetchedObjects(refreshesRefetchedObjects);
	    		NSArray details = ec.objectsWithFetchSpecification(fetchSpec);
	    		result.addObjectsFromArray(details);
	    	}
		}
		finally {
			ec.unlock();
		}
    	return result;
    }

    /**
     * Fires all faults in the given global IDs on one batch together with their relationships.
     * This is much more efficient than triggering the individual faults and relationships later on. 
     * The method should use only 1 fetch for all objects per entity
     * and then one for each relationship per entity.
     * @param ec
     * @param globalIDs
     * @param prefetchingKeypaths
     */
    public static NSArray fireFaultsForGlobalIDs(EOEditingContext ec, 
    		NSArray globalIDs, NSArray prefetchingKeypaths) {
    	NSMutableArray result = new NSMutableArray(globalIDs.count());
    	if(globalIDs.count() > 0) {
    		NSMutableArray faults = new NSMutableArray(globalIDs.count());
    		for (Enumeration ids = globalIDs.objectEnumerator(); ids.hasMoreElements();) {
    			EOGlobalID gid = (EOGlobalID) ids.nextElement();
    			EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
    			if(EOFaultHandler.isFault(eo)) {
    				faults.addObject(gid);
    			} else {
    				result.addObject(eo);
    			}
    		}
    		NSArray loadedObjects = fetchObjectsWithGlobalIDs(ec, faults);
    		result.addObjectsFromArray(loadedObjects);
    		if(prefetchingKeypaths != null && prefetchingKeypaths.count() > 0) {
    			NSDictionary objectsByEntity = ERXArrayUtilities.arrayGroupedByKeyPath(result, "entityName");
    			for(Enumeration e = objectsByEntity.keyEnumerator(); e.hasMoreElements();) {
    				String entityName = (String) e.nextElement();
    				NSArray objects = (NSArray) objectsByEntity.objectForKey(entityName);
    				EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
    				for (Enumeration keyPaths = prefetchingKeypaths.objectEnumerator(); keyPaths.hasMoreElements();) {
    					String keypath = (String) keyPaths.nextElement();
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
    	}
    	return result;
    }
    
    /**
     * Fires all faults in the given global IDs on one batch. This is much more efficient than
     * triggering the individual faults later on. The method should use only 1 fetch for all objects per entity.
     * @param ec
     * @param globalIDs
     */
    public static NSArray fireFaultsForGlobalIDs(EOEditingContext ec, NSArray globalIDs) {
        return fireFaultsForGlobalIDs(ec, globalIDs, NSArray.EmptyArray);
    }
}
