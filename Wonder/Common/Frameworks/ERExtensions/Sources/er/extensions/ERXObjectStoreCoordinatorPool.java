package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;

/**
 * This class implements EOF stack pooling including EOF stack synchronizing. 
 * It provides a special ERXEC.Factory in order to work without any changes in existing 
 * applications. The number of EOObjectStoreCoordinators can be set with the 
 * system Property <code>er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators</code>. 
 * Each Session will become one EOObjectStoreCoordinator and the method 
 * <code>newEditingContext</code> will always return an <code>EOEditingContext</code> 
 * with the same <code>EOObjectStoreCoordinator</code> for the same <code>WOSession</code>. 
 * This first release uses round-robin pooling, future versions might use better algorithms 
 * to decide which <code>EOObjectStoreCoordinator</code> will be used for the next new 
 * <code>WOSession</code>.<br>The code is tested in a heavy  multithreaded application 
 * and afawk no deadlock occures, neither in EOF nor directly in Java.
 * 
 * @author David Teran, Frank Caputo @ cluster9
 */
public class ERXObjectStoreCoordinatorPool {
    private static ERXLogger log = ERXLogger.getERXLogger(ERXObjectStoreCoordinatorPool.class);
    
    static {
        ObjectStoreCoordinatorSynchronizer.sharedInstance();
    }
    
    private Hashtable oscForSession;
    private int maxOS;
    private int currentObjectStoreIndex;
    private ObjectStoreCoordinator[] objectStores;
    private Observer observer;
    private Object lock = new Object();
    protected static ERXObjectStoreCoordinatorPool defaultPool;
    
    static {
        defaultPool = new ERXObjectStoreCoordinatorPool();
        log.info("setting ERXEC.factory to MultiOSCFactory");
        ERXEC.setFactory(new MultiOSCFactory());
    }
    
    /**
     * Creates a new ERXObjectStoreCoordinatorPool. This object is a singleton. 
     * This object is responsible to provide EOObjectStoreCoordinators 
     * based on the current Threads' session. 
     * It is used by MultiOSCFactory to get a rootObjectStore if the 
     * MultiOSCFactory is asked for a new EOEditingContext.
     */
    private ERXObjectStoreCoordinatorPool() {
        maxOS = ERXProperties.intForKey("er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators");
        if (maxOS == 0)
            //this should work like the default implementation
            maxOS = 1;
        observer = new Observer();
        oscForSession = new Hashtable();
    }
    
    /** 
     * Observer class that stores EOObjectStoreCoordinators per session. 
     * Registers for sessionDidCreate and sessionDidTimeout to maintain its array.
     */
    public static class Observer {
        protected Observer() {
            NSSelector sel = new NSSelector("sessionDidCreate", ERXConstant.NotificationClassArray);
            NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidCreateNotification, null);
            sel = new NSSelector("sessionDidTimeout", ERXConstant.NotificationClassArray);
            NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidTimeOutNotification, null);
        }
        
        /** 
         * checks if the new Session has already  a EOObjectStoreCoordinator assigned, 
         * if not it assigns a EOObjectStoreCoordinator to the session.
         * @param n {@link WOSession#SessionDidCreateNotification}
         */
        public void sessionDidCreate(NSNotification n) {
            WOSession s = (WOSession) n.object();
            if (defaultPool.oscForSession.get(s.sessionID()) == null) {
                defaultPool.oscForSession.put(s.sessionID(), defaultPool.nextObjectStore());
            }
        }
        
        /** Removes the timed out session from the internal array.
         * session.
         * @param n {@link WOSession#SessionDidTimeOutNotification}
         */
        public void sessionDidTimeout(NSNotification n) {
            String sessionID = (String) n.object();
            defaultPool.oscForSession.remove(sessionID);
        }
    }
    
    /**
     * @return the sessionID from the session stored in ERXThreadStorage.
     */
    protected String sessionID() {
        WOSession session = ERXExtensions.session();
        String sessionID = null;
        if (session != null) {
            sessionID = session.sessionID();
        }
        return sessionID;
    }
    
    /** 
     * returns the session related EOObjectStoreCoordinator. 
     * If session is null then it returns the nextObjectStore.
     * This method is used to create new EOEditingContexts with the MultiOSCFactory
     * @return an EOEditingContext
     */
    public EOObjectStore currentRootObjectStore() {
        String sessionID = sessionID();
        EOObjectStore os = null;
        if (sessionID != null) {
            os = (EOObjectStore) oscForSession.get(sessionID);
            if (os == null) {
                os = nextObjectStore();
                oscForSession.put(sessionID, os);
            }
        } else {
            os = nextObjectStore();
        }
        return os;
    }
    
    /** 
     * Lazy initialises the objectStores and then returns the next one, 
     * this is based on round robin.
     * @return the next EOObjectStore based on round robin
     */
    public EOObjectStore nextObjectStore() {
        synchronized (lock) {
            if (objectStores == null) {
                _initObjectStores();
            }
            if (currentObjectStoreIndex == maxOS) {
                currentObjectStoreIndex = 0;
            }
            return (EOObjectStore) objectStores[currentObjectStoreIndex++];
        }
    }
    
    /** 
     * This class uses different EOF stack when creating new EOEditingContexts.
     */
    public static class MultiOSCFactory extends ERXEC.DefaultFactory {
        public MultiOSCFactory() {
            super();
        }
        public EOEditingContext _newEditingContext() {
            return _newEditingContext(true);
        }
        
        public EOEditingContext _newEditingContext(boolean validationEnabled) {
            ObjectStoreCoordinator os = (ObjectStoreCoordinator)defaultPool.currentRootObjectStore();
            EOEditingContext ec = _newEditingContext(os, validationEnabled);
            ec.lock();
            try {
                ec.setSharedEditingContext(os.sharedEditingContext());
            } finally {
                ec.unlock();
            }
            return ec;
        }
    }
    
    private void _initObjectStores() {
        log.info("initializing Pool...");
        objectStores = new ObjectStoreCoordinator[maxOS];
        for (int i = 0; i < maxOS; i++) {
            objectStores[i] = new ObjectStoreCoordinator();
        }
        log.info("initializing Pool finished");
     }
    
    
    /** 
     * Subclass to store additional stuff like the shared EC, 
     * and in future times maybe the useage count, locking etc. 
     */
    //CHECKME ak: until this is not fleshed out, we do not need it....
    public static class ObjectStoreCoordinator extends EOObjectStoreCoordinator {
        protected EOSharedEditingContext sharedEditingContext;
        private Hashtable databaseContextsForModels = new Hashtable();
        
        public ObjectStoreCoordinator() {
            super();
            //ObjectStoreCoordinatorSynchronizer.sharedInstance().addObjectStoreCoordinator(this);
            databaseContextsForModels = new Hashtable();
            
            EOModelGroup eomodelgroup = EOModelGroup.modelGroupForObjectStoreCoordinator(this);
            for (int i = 0; i < eomodelgroup.models().count(); i++) {
                EOModel m = (EOModel)eomodelgroup.models().objectAtIndex(i);
                EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(m, this);
                databaseContextsForModels.put(m.name(), dbc);
            }
        }
        
        public Hashtable databaseContextsForModels() { 
            return databaseContextsForModels; 
        }
        
        public EOSharedEditingContext sharedEditingContext() {
            if (sharedEditingContext == null)
                sharedEditingContext = new EOSharedEditingContext(this);
            return sharedEditingContext;
        }
        
    }
    
    /** 
     * Thread and locking safe implementation to propagate 
     * the changes from one EOF stack to another 
     */
    private static class ProcessChangesQueue implements Runnable {
        List elements = new LinkedList();
        ObjectStoreCoordinatorSynchronizer synchronizer;
        
        private ProcessChangesQueue(ObjectStoreCoordinatorSynchronizer synchronizer) {
            this.synchronizer = synchronizer;
        }
        
        private void addChange(Change changes) {
            synchronized (elements) {
                elements.add(changes);
            }
        }
        
        public void run() {
            boolean run = true;
            while (true) {
                try {
                    //FIXME: we should use wait and notify here...
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    run = false;
                    log.info("Interrupted: " + e, e);
                }
                if (elements.size() > 0) {
                    Change changes = null;
                    synchronized(elements) {
                        changes = (Change)elements.remove(0);
                    }
                    EOObjectStoreCoordinator sender = changes.coordinator();
                    //FIXME: ak do sth useful
                    //processDeletes(changes.deleted(), sender);
                    //processInserts(changes.inserted(), sender);
                    processUpdates(changes.updated(), sender);
                }
            }
        }
        
        private synchronized void processUpdates(NSDictionary updatesByEntity, EOObjectStoreCoordinator sender) {
            for(Enumeration entityNames = updatesByEntity.allKeys().objectEnumerator(); entityNames.hasMoreElements();) {
                String entityName = (String)entityNames.nextElement();
                EOEntity entity = EOModelGroup.modelGroupForObjectStoreCoordinator(sender).entityNamed(entityName);
                NSArray snapshots = (NSArray)updatesByEntity.objectForKey(entityName);
                NSMutableDictionary dbcs = new NSMutableDictionary();
                for (Enumeration oscs = synchronizer.oscs.objectEnumerator(); oscs.hasMoreElements(); ) {
                    EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
                    if (osc == sender) {
                        continue;
                    }
                    EODatabaseContext dbc = (EODatabaseContext) dbcs.objectForKey(entityName);
                    if(dbc == null) {
                        dbc = databaseContextForEntityNamed(entityName, osc);
                        dbcs.setObjectForKey(dbc, entityName);
                    }
                    EODatabase database = dbc.database();
                    Enumeration snapshotsEnumerator = snapshots.objectEnumerator();
                    while (snapshotsEnumerator.hasMoreElements()) {
                        NSDictionary snapshot = (NSDictionary)snapshotsEnumerator.nextElement();
                        if (snapshot != null) {
                            EOGlobalID globalID = entity.globalIDForRow(snapshot);
                            EODatabaseContext._EOAssertSafeMultiThreadedAccess(dbc);
                            dbc.lock();
                            try {
                                database.forgetSnapshotForGlobalID(globalID);
                                database.recordSnapshotForGlobalID(snapshot, globalID);
                            } finally {
                                dbc.unlock();
                            }
                        }
                    }
                }
            }
        }
        
        private EODatabaseContext databaseContextForEntityNamed(String entityName, EOObjectStoreCoordinator osc) {
            return Change.databaseContextForEntityNamed(entityName, osc);
        }
        
    }
    
    /**
     * Holds a change notification (one transaction).
     * @author ak
     */
    private static class Change {
        private EOObjectStoreCoordinator _coordinator;
        private NSDictionary _inserted;
        private NSDictionary _updated;
        private NSDictionary _deleted;
        
        public Change(EOObjectStoreCoordinator osc, NSDictionary userInfo) {
            _coordinator = osc;
            _deleted = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("updated"), osc);
            _updated = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("deleted"), osc);
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
            if(objects != null) {
                return NSDictionary.EmptyDictionary;
            }
            
            NSMutableDictionary result = new NSMutableDictionary();
            NSMutableDictionary dbcs = new NSMutableDictionary();
            
            for(Enumeration gids = objects.objectEnumerator(); gids.hasMoreElements();) {
                EOKeyGlobalID globalID = (EOKeyGlobalID) gids.nextElement();
                String entityName = globalID.entityName();
                
                EODatabaseContext dbc = (EODatabaseContext) dbcs.objectForKey(entityName);
                if(dbc == null) {
                    dbc = databaseContextForEntityNamed(entityName, osc);
                    dbcs.setObjectForKey(dbc, entityName);
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
            return _updated;
        }
        
        public NSDictionary inserted() {
            return _updated;
        }
        
        public EOObjectStoreCoordinator coordinator() {
            return _coordinator;
        }
        
        private static synchronized EODatabaseContext databaseContextForEntityNamed(String entityName, EOObjectStoreCoordinator osc) {
            EOModel model = EOModelGroup.modelGroupForObjectStoreCoordinator(osc).entityNamed(entityName).model();
            EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, osc);
            return dbc;
        }
    }
    
    /** 
     * Synchronizes the different EOF stacks with the use of the ProcessChangesQueue.
     * 
     */
    public static class ObjectStoreCoordinatorSynchronizer {
        
        private static ObjectStoreCoordinatorSynchronizer sharedInstance = new ObjectStoreCoordinatorSynchronizer();
        private NSMutableArray oscs;
        private static boolean inited = false;
        private ProcessChangesQueue queueThread;
        
        private ObjectStoreCoordinatorSynchronizer() {
            oscs = new NSMutableArray();
            queueThread = new ProcessChangesQueue(this);
            new Thread(queueThread).start();
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
            EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator)n.object();
            oscs.removeObject(osc);
            NSNotificationCenter.defaultCenter().removeObserver(this, EOObjectStoreCoordinator.ObjectsChangedInStoreNotification, osc);
        }      
        
        public void objectStoreWasAdded(NSNotification n) {
            EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator)n.object();
            oscs.addObject(osc);
            
            NSSelector sel = new NSSelector("publishChange", new Class[] { NSNotification.class } );
            NSNotificationCenter.defaultCenter().addObserver(this, sel, EOObjectStoreCoordinator.ObjectsChangedInStoreNotification, osc);
        }
        
        private static ObjectStoreCoordinatorSynchronizer sharedInstance() {
            if (!inited) {
                inited = true;
                //EOObjectStoreCoordinator.setDefaultCoordinator(osc);
                //sharedInstance.addObjectStoreCoordinator(EOObjectStoreCoordinator.defaultCoordinator());
            }
            return sharedInstance;
        }
        
        public void publishChange(NSNotification n) {
            if (oscs.count() >= 2) {
                Change changes = new Change((EOObjectStoreCoordinator)n.object(), n.userInfo());
                queueThread.addChange(changes);
            }
        }
    }    
}

