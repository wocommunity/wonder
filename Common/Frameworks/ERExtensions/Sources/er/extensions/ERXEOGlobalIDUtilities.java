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
 * Utilities that help with batch loading sets of global IDs. Unless otherwise noted, the 
 * supplied global ids must all be from the same entity.
 * 
 * @author ak
 */
public class ERXEOGlobalIDUtilities {
    //  ===========================================================================
    //  Class Constants
    //  ---------------------------------------------------------------------------

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
            EOGlobalID gid = (EOGlobalID) globalIDs.lastObject();
            if((gid instanceof EOKeyGlobalID) && (((EOKeyGlobalID)gid).keyCount() == 1)) {
                EOKeyGlobalID keyGID = (EOKeyGlobalID)gid;
                NSMutableArray primaryKeys = new NSMutableArray();
                String entityName = keyGID.entityName();
                for (Enumeration gids = globalIDs.objectEnumerator(); gids.hasMoreElements();) {
                    keyGID = (EOKeyGlobalID) gids.nextElement();
                    result.addObject(keyGID.keyValues()[0]);
                }
            } else {
                return null;
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
     * Fetches an array of objects defined by the globalIDs in a single fetch. This is very useful
     * as a replacement for batch-faulting.
     * @param ec
     * @param globalIDs
     * @return
     */
    public static NSArray fetchObjectsWithGlobalIDs(EOEditingContext ec, NSArray globalIDs) {
        NSMutableArray result = new NSMutableArray();
        EOGlobalID gid = (EOGlobalID) globalIDs.lastObject();
        if(gid instanceof EOKeyGlobalID) {
            EOKeyGlobalID keyGID = (EOKeyGlobalID)gid;
            NSMutableArray qualifiers = new NSMutableArray();
            String entityName = keyGID.entityName();
            
            EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
            for (Enumeration gids = globalIDs.objectEnumerator(); gids.hasMoreElements();) {
                EOGlobalID g = (EOGlobalID) gids.nextElement();
                EOQualifier qualifier = entity.qualifierForPrimaryKey(entity.primaryKeyForGlobalID(g));
                qualifiers.addObject(qualifier);
            }
            EOQualifier qualifier = new EOOrQualifier(qualifiers);
            EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName, qualifier, null);
            NSArray details = ec.objectsWithFetchSpecification(fetchSpec);
            result.addObjectsFromArray(details);
        } else {
            if(gid != null) {
                throw new IllegalArgumentException("Can't fetch because GIDs are not EOKeyValueGlobalIDs: " + gid);
            }
        }
        return result;
    }
    
    
    
    /**
     * If needed, batch fetches a set of global IDs together with a set of relationships. This is much more efficient than
     * triggering the individual faults and relationships later on. The method should use only 1 fetch for all objects 
     * and one for each relationship.
     * @param ec
     * @param globalIDs
     * @param prefetchingKeypaths
     * @return
     */
    public static NSArray objectsForGlobalIDs(EOEditingContext ec, 
            NSArray globalIDs, NSArray prefetchingKeypaths) {
        NSArray result = new NSArray();
        if(globalIDs.count() > 0) {
            result = objectsForGlobalIDs(ec, globalIDs);
            String entityName = ((EOEnterpriseObject)result.lastObject()).entityName();
            EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
            for (Enumeration keyPaths = prefetchingKeypaths.objectEnumerator(); keyPaths.hasMoreElements();) {
                String keypath = (String) keyPaths.nextElement();
                EORelationship relationship = entity.relationshipNamed(keypath);
                EODatabaseContext dbc = ERXEOAccessUtilities.databaseContextForEntityNamed((EOObjectStoreCoordinator) ec.rootObjectStore(), entityName);
                dbc.batchFetchRelationship(relationship, result, ec);
            }
        }
        return result;
        
    }
    
    /**
     * If needed, batch fetches a set of global IDs. This is much more efficient than
     * triggering the individual faults later on. The method should use only 1 fetch for all objects.
     * @param ec
     * @param globalIDs
     * @return
     */
    public static NSArray objectsForGlobalIDs(EOEditingContext ec, NSArray globalIDs) {
        NSMutableArray result = new NSMutableArray();
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
        return result;
    }
}
