/*
 * Created on 22.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import java.util.*;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;


/** 
 * Synchronizes different EOF stacks inside an instance. This supplements the
 * change notification frameworks that sync different instances and should help
 * you to run multithreaded.
 * After calling initialize(), every ObjectStoreCoordinator
 * that gets created will be added to the list of stacks to sync.
 * You will need to add any stack created before initalization manually.
 */
public class ERXObjectStoreCoordinatorSynchronizer {
    private static Logger log = Logger.getLogger(ERXObjectStoreCoordinatorSynchronizer.class);
   
    private static ERXObjectStoreCoordinatorSynchronizer _synchronizer;
    
    public static void initialize() {
        if(_synchronizer == null) {
            _synchronizer = new ERXObjectStoreCoordinatorSynchronizer();
        }
    }
    
    public static ERXObjectStoreCoordinatorSynchronizer synchronizer() {
        if(_synchronizer == null) {
            initialize();
        }
        return _synchronizer;
    }
    
    private Vector _coordinators;
    private ProcessChangesQueue _queueThread;
    
    private ERXObjectStoreCoordinatorSynchronizer() {
        _coordinators = new Vector();
        _queueThread = new ProcessChangesQueue();
        new Thread(_queueThread).start();
        NSNotificationCenter.defaultCenter().addObserver(
                this,
                new NSSelector("objectStoreWasAdded", new Class[] { NSNotification.class } ),
                EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification,
                null);
        NSNotificationCenter.defaultCenter().addObserver(
                this,
                new NSSelector("objectStoreWasRemoved", new Class[] { NSNotification.class } ),
                EOObjectStoreCoordinator.CooperatingObjectStoreWasRemovedNotification,
                null);
    }
    
    public void objectStoreWasRemoved(NSNotification n) {
        removeObjectStore((EOObjectStoreCoordinator)n.object());
    }      
    
    public void objectStoreWasAdded(NSNotification n) {
        addObjectStore((EOObjectStoreCoordinator)n.object());
    }
    
    public void addObjectStore(EOObjectStoreCoordinator osc) {
        if(!_coordinators.contains(osc)) {
            _coordinators.add(osc);
            NSSelector sel = new NSSelector("publishChange", new Class[] { NSNotification.class } );
            NSNotificationCenter.defaultCenter().addObserver(this, sel, EOObjectStore.ObjectsChangedInStoreNotification, osc);
        } else {
            log.error("Adding same coodinator twice!");
        }
    }
    
    public void removeObjectStore(EOObjectStoreCoordinator osc) {
        synchronized(_coordinators) {
            if(_coordinators.contains(osc)) {
                _coordinators.remove(osc);
                NSNotificationCenter.defaultCenter().removeObserver(this, EOObjectStore.ObjectsChangedInStoreNotification, osc);
            } else {
                log.error("Coordinator not found!");
            }
        }
    }
    
    public void publishChange(NSNotification n) {
        if (_coordinators.size() >= 2) {
            Change changes = new Change((EOObjectStoreCoordinator)n.object(), n.userInfo());
            _queueThread.addChange(changes);
        }
    }
    
    private Enumeration coordinators() {
        return _coordinators.elements();
    }
    
    /** 
     * Thread and locking safe implementation to propagate 
     * the changes from one EOF stack to another.
     */
    private class ProcessChangesQueue implements Runnable {
        private abstract class SnapshotProcessor {
            public abstract void processSnapshots(EODatabase database,  NSDictionary snapshots);
        }
        
        private class DeleteProcessor extends SnapshotProcessor {
        	public void processSnapshots(EODatabase database,  NSDictionary snapshots) {
        		database.forgetSnapshotsForGlobalIDs(snapshots.allKeys());
        		if(log.isDebugEnabled()) {
        			log.debug("forget: " + snapshots);
        		}
        	}
        }
        private class UpdateProcessor extends SnapshotProcessor {
        	public void processSnapshots(EODatabase database,  NSDictionary snapshots) {
        		database.forgetSnapshotsForGlobalIDs(snapshots.allKeys());
        		database.recordSnapshots(snapshots);
        		if(log.isDebugEnabled()) {
        			log.debug("update: " + snapshots);
        		}
        	}
        }
        private class InsertProcessor extends SnapshotProcessor {
        	public void processSnapshots(EODatabase database,  NSDictionary snapshots) {
        		database.recordSnapshots(snapshots);
        		if(log.isDebugEnabled()) {
        			log.debug("insert: " + snapshots);
        		}
        	}
        }
        
        private List _elements = new LinkedList();
        
        private SnapshotProcessor _deleteProcessor = new DeleteProcessor();
        private SnapshotProcessor _insertProcessor = new InsertProcessor();
        private SnapshotProcessor _updateProcessor = new UpdateProcessor();
        
        private ProcessChangesQueue() {
            Thread.currentThread().setName("ProcessChangesQueue");
        }
        
        private void addChange(Change changes) {
            synchronized (_elements) {
                _elements.add(changes);
                _elements.notify();
            }
        }
        
        public void run() {
            boolean run = true;
            while (true) {
                Change changes = null;
                synchronized(_elements) {
                    try {
                        if(_elements.isEmpty()) {
                            _elements.wait();
                        }
                        if(!_elements.isEmpty()) {
                            changes = (Change)_elements.remove(0);
                        }
                    } catch (InterruptedException e) {
                        run = false;
                        log.warn("Interrupted: " + e, e);
                    }
                }
                if(changes != null) {
                    EOObjectStoreCoordinator sender = changes.coordinator();
                    
                    process(sender, _deleteProcessor, changes.deleted());
                    process(sender, _insertProcessor, changes.inserted());
                    process(sender, _updateProcessor, changes.updated());
                }
            }
        }
        
        /**
         * @param dictionary
         * @param sender
         */
        private void process(EOObjectStoreCoordinator sender, SnapshotProcessor processor, NSDictionary changesByEntity) {
            NSMutableDictionary dbcs = new NSMutableDictionary();
            for (Enumeration oscs = _synchronizer.coordinators(); oscs.hasMoreElements(); ) {
                EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
                if (osc != sender) {
                    NSMutableDictionary snapshotsByGlobalID = new NSMutableDictionary();
                   for(Enumeration entityNames = changesByEntity.allKeys().objectEnumerator(); entityNames.hasMoreElements();) {
                        String entityName = (String)entityNames.nextElement();
                        String key = entityName + "/" + System.identityHashCode(osc);
                        EOEntity entity = EOModelGroup.modelGroupForObjectStoreCoordinator(sender).entityNamed(entityName);
                        NSArray snapshots = (NSArray)changesByEntity.objectForKey(entityName);
                        EODatabaseContext dbc = (EODatabaseContext) dbcs.objectForKey(key);
                        if(dbc == null) {
                            dbc = ERXEOAccessUtilities.databaseContextForEntityNamed(osc,entityName);
                            dbcs.setObjectForKey(dbc, key);
                        }
                        EODatabase database = dbc.database();
                        for(Enumeration snapshotsEnumerator = snapshots.objectEnumerator(); snapshotsEnumerator.hasMoreElements(); ) {
                        	NSDictionary snapshot = (NSDictionary)snapshotsEnumerator.nextElement();
                        	EOGlobalID globalID = entity.globalIDForRow(snapshot);
                        	snapshotsByGlobalID.setObjectForKey(snapshot, globalID);
                        }
                        if(snapshotsByGlobalID.count() > 0) {
                        	EODatabaseContext._EOAssertSafeMultiThreadedAccess(dbc);
                        	dbc.lock();
                        	
                        	try {
                        		processor.processSnapshots(database, snapshotsByGlobalID);
                        	} finally {
                        		dbc.unlock();
                        	}
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Holds a change notification (one transaction). 
     */
    private static class Change {
        private EOObjectStoreCoordinator _coordinator;
        private NSDictionary _inserted;
        private NSDictionary _updated;
        private NSDictionary _deleted;
        
        public Change(EOObjectStoreCoordinator osc, NSDictionary userInfo) {
            _coordinator = osc;
            _deleted = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("deleted"), osc);
            _updated = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("updated"), osc);
            _inserted = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("inserted"), osc);
        }
        /**
         * Returns a dictionary of snapshots where the key is the entity name and the value an
         * array of snapshots.
         * @param objects
         * @param osc
         * @return
         */
        private NSDictionary snapshotsGroupedByEntity(NSArray objects, EOObjectStoreCoordinator osc) {
            if(objects == null || objects.count() == 0) {
                return NSDictionary.EmptyDictionary;
            }
            
            NSMutableDictionary result = new NSMutableDictionary();
            NSMutableDictionary dbcs = new NSMutableDictionary();
            
            for(Enumeration gids = objects.objectEnumerator(); gids.hasMoreElements();) {
                EOKeyGlobalID globalID = (EOKeyGlobalID) gids.nextElement();
                String entityName = globalID.entityName();
                String key = entityName + "/" + System.identityHashCode(osc);
                EODatabaseContext dbc = (EODatabaseContext) dbcs.objectForKey(key);
                if(dbc == null) {
                    dbc = ERXEOAccessUtilities.databaseContextForEntityNamed(osc, entityName);
                    dbcs.setObjectForKey(dbc, key);
                }
                NSMutableArray snapshotsForEntity = (NSMutableArray)result.objectForKey(entityName);
                if(snapshotsForEntity == null) {
                    snapshotsForEntity = new NSMutableArray();
                    result.setObjectForKey(snapshotsForEntity, entityName);
                }
                synchronized(snapshotsForEntity) {
                    Object o = dbc.snapshotForGlobalID(globalID);
                    if(o != null) {
                        snapshotsForEntity.addObject(o);
                    }
                }
            }
            return result.immutableClone();
        }
        
        public NSDictionary updated() {
            return _updated;
        }
        
        public NSDictionary deleted() {
            return _deleted;
        }
        
        public NSDictionary inserted() {
            return _inserted;
        }
        
        public EOObjectStoreCoordinator coordinator() {
            return _coordinator;
        }
    }    
}