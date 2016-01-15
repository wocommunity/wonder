package er.extensions.eof;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOAccessArrayFaultHandler;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFaulting;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Provides a cache for to-many faults. Useful when you have a lot (say a
 * few thousand) objects for which you need to access a to-many relationship
 * that hasn't been fetched yet.
 * 
 * The idea is that instead of batch-faulting you could fetch all the entries
 * for a given to-many relationship in one fetch (as raw rows), group them in
 * memory and register the faults here. Then you set up your EODatabaseContext
 * delegate to use this object to try and clear the fault first.
 * 
 * @author ak
 * 
 */
public class ERXArrayFaultCache {
    
    private NSMutableDictionary cache = new NSMutableDictionary();
    
    /**
     * Register the to-many faults by entity name and relationship name. The entries
     * are a dictionary where the key is the source EOGlobalID for the relationship
     * and the values are arrays of global IDs for the destination.
     * @param entityName
     * @param relationshipName
     * @param entries
     */
    public void registerRelationshipCacheEntries(String entityName, String relationshipName, NSDictionary entries) {
        synchronized (cache) {
            if(entries == null) {
                cache.removeObjectForKey(entityName + "\0" + relationshipName);
            } else {
                cache.setObjectForKey(entries.immutableClone(), entityName + "\0" + relationshipName);
            }
        }
    }

    private NSDictionary relationshipCacheEntriesForEntity(String entityName, String relationshipName) {
        synchronized (cache) {
            return (NSDictionary) cache.objectForKey(entityName + "\0" + relationshipName);
        }
    }

    /**
     * Attempts to clear a fault by looking up the source global id and faulting in the corresponding
     * destination values.
     * @param obj
     * @return true if the fault could be cleared or it wasn't a fault in the first place, false if not
     */
    public boolean clearFault(Object obj) {
        if(!EOFaultHandler.isFault(obj)) {
            return true;
        }
        EOFaulting fault = (EOFaulting)obj;
        if (fault.faultHandler() instanceof EOAccessArrayFaultHandler) {
            EOAccessArrayFaultHandler handler = (EOAccessArrayFaultHandler) fault.faultHandler();
            EOKeyGlobalID sourceGid = handler.sourceGlobalID();
            EOEditingContext ec = handler.editingContext();
            synchronized (cache) {
                NSDictionary entries = relationshipCacheEntriesForEntity(sourceGid.entityName(), handler.relationshipName());
                if(entries != null) {
                    NSArray gids = (NSArray) entries.objectForKey(sourceGid);
                    if(gids != null) {
                        NSMutableArray eos = new NSMutableArray(gids.count());
                        for (Enumeration enumerator = gids.objectEnumerator(); enumerator.hasMoreElements();) {
                            EOGlobalID gid = (EOGlobalID) enumerator.nextElement();
                            EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
                            eos.addObject(eo);
                        }
                        EOFaultHandler.clearFault(obj);
                        ((NSMutableArray)fault).addObjectsFromArray(eos);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
