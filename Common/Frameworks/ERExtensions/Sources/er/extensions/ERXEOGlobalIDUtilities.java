/*
 * Created on 24.01.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import java.util.Enumeration;

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
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Utilities that help with batch loading sets of global IDs. 
 * 
 * @author ak
 */
public class ERXEOGlobalIDUtilities {
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEOGlobalIDUtilities.class);
    
    /**
     * Groups an array of global IDs by their entity name.
     * @param globalIDs
     * @return
     */
    public static NSDictionary globalIDsGroupedByEntityName(NSArray globalIDs) {
        return ERXArrayUtilities.arrayGroupedByKeyPath(globalIDs, "entityName");
    }

    /**
     * Translates an array of {@link EOGlobalID} to primary key values. Returns null if the given
     * global IDs are not EOKeyValueGlobalIDs and the primary keys are not single values.
     * @param globalIDs
     * @return
     */
    public static NSArray primaryKeyValuesWithGlobalIDs(NSArray globalIDs) {
    	NSMutableArray result = new NSMutableArray();
    	if(globalIDs.count() > 0) {
    		NSDictionary gidsByEntity = globalIDsGroupedByEntityName(globalIDs);
    		for(Enumeration e = gidsByEntity.keyEnumerator(); e.hasMoreElements();) {
    			String entityName = (String) e.nextElement();
    			NSArray gidsForEntity = (NSArray) gidsByEntity.objectForKey(entityName);
    			
    			NSMutableArray primaryKeys = new NSMutableArray();
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
     * @return
     */
    public static NSArray globalIDsWithPrimaryKeyValues(String entityName, NSArray values) {
        NSMutableArray result = new NSMutableArray();
        if(values.count() > 0) {
            for (Enumeration pks = values.objectEnumerator(); pks.hasMoreElements();) {
                Object value = (Object) pks.nextElement();
                EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entityName, new Object[] {value});
                result.addObject(gid);
            }
        }
        return result;
    }
    
    /**
     * Fetches an array of objects defined by the globalIDs in a single fetch per entity.
     * @param ec
     * @param globalIDs
     * @return
     */
    public static NSArray fetchObjectsWithGlobalIDs(EOEditingContext ec, NSArray globalIDs) {
    	NSDictionary gidsByEntity = globalIDsGroupedByEntityName(globalIDs);
    	NSMutableArray result = new NSMutableArray();
    	for(Enumeration e = gidsByEntity.keyEnumerator(); e.hasMoreElements();) {
    		String entityName = (String) e.nextElement();
    		NSArray gidsForEntity = (NSArray) gidsByEntity.objectForKey(entityName);
    		
    		NSMutableArray qualifiers = new NSMutableArray();
        	EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
    		for (Enumeration gids = gidsForEntity.objectEnumerator(); gids.hasMoreElements();) {
    			EOGlobalID g = (EOGlobalID) gids.nextElement();
    			EOQualifier qualifier = entity.qualifierForPrimaryKey(entity.primaryKeyForGlobalID(g));
    			qualifiers.addObject(qualifier);
    		}
    		EOQualifier qualifier = new EOOrQualifier(qualifiers);
    		EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName, qualifier, null);
    		NSArray details = ec.objectsWithFetchSpecification(fetchSpec);
    		result.addObjectsFromArray(details);
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
     * @return
     */
    public static NSArray fireFaultsForGlobalIDs(EOEditingContext ec, 
    		NSArray globalIDs, NSArray prefetchingKeypaths) {
    	NSMutableArray result = new NSMutableArray();
    	if(globalIDs.count() > 0) {
    		NSMutableArray faults = new NSMutableArray();
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
     * @return
     */
    public static NSArray fireFaultsForGlobalIDs(EOEditingContext ec, NSArray globalIDs) {
        return fireFaultsForGlobalIDs(ec, globalIDs, NSArray.EmptyArray);
    }
}
