package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;

/**
This class implements EOF stack pooling including EOF stack synchronizing. It provides a special ERXEC.Factory in order to work without any changes in existing applications. The number of EOObjectStoreCoordinators can be set with the System Property <code>er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators</code>. Each Session will become one EOObjectStoreCoordinator and the method <code>newEditingContext</code> will always return an <code>EOEditingContext</code> with the same <code>EOObjectStoreCoordinator</code> for the same <code>WOSession</code>. This first release uses round-robin pooling, future versions might use better algorithms to decide which <code>EOObjectStoreCoordinator</code> will be used for the next new <code>WOSession</code>.<br>The code is tested in a heavy  multithreaded application and afawk no deadlock occures, neither in EOF nor directly in Java.

 @author David Teran, Frank Caputo @ cluster9
 */
public class ERXObjectStoreCoordinatorPool {
    private static ERXLogger log = ERXLogger.getERXLogger(ERXObjectStoreCoordinatorPool.class);

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

/** Observer class that stores EOObjectStoreCoordinators per session. Registers for sessionDidCreate and sessionDidTimeout to maintain its array.
  *
  *
  */
public static class Observer {
        protected Observer() {
            NSSelector sel = new NSSelector("sessionDidCreate", ERXConstant.NotificationClassArray);
            NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidCreateNotification, null);
            sel = new NSSelector("sessionDidTimeout", ERXConstant.NotificationClassArray);
            NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidTimeOutNotification, null);
        }

/** checks if the new Session has already  a EOObjectStoreCoordinator assigned, if not it assigns a EOObjectStoreCoordinator to the 
  * session.
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
     *
     *
     * @return the sessionID from the session stored in ERXThreadStorage.
     */
    protected String sessionID() {
        WOSession session = ERXSession.session();
        String sessionID = null;
        if (session != null) {
            sessionID = session.sessionID();
        }
        return sessionID;
    }

    /** returns the session related EOObjectStoreCoordinator. If session is null then it returns the nextObjectStore.
        * This method is used to create new EOEditingContexts with the MultiOSCFactory
        *
        *
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

    /** lazy initialises the objectStores and then returns the next one, this is based on round robin.
        *
        *
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

    /** This class uses different EOF stack when  creating new EOEditingContexts.
        *
        *
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

            if (useSharedEditingContext()) {
                EOSharedEditingContext sec = os.sharedEditingContext();
                ec.lock();
                try {
                    ec.setSharedEditingContext(sec);
                } finally {
                    ec.unlock();
                }
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

    
    /** subclass to store additional stuff like the shared EC, and in future times maybe the useage count, locking etc. */

    public static class ObjectStoreCoordinator extends EOObjectStoreCoordinator {
        protected EOSharedEditingContext sharedEditingContext;
        private Hashtable databaseContextsForModels = new Hashtable();
        
        public ObjectStoreCoordinator() {
            super();
            ObjectStoreCoordinatorSynchronizer.sharedInstance(this).addObjectStoreCoordinator(this);
            databaseContextsForModels = new Hashtable();

            EOModelGroup eomodelgroup = EOModelGroup.modelGroupForObjectStoreCoordinator(this);
            for (int i = 0; i < eomodelgroup.models().count(); i++) {
                EOModel m = (EOModel)eomodelgroup.models().objectAtIndex(i);
                EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(m, this);
                databaseContextsForModels.put(m.name(), dbc);
            }
        }

        public Hashtable databaseContextsForModels() { return databaseContextsForModels; }

        public EOSharedEditingContext sharedEditingContext() {
            if (sharedEditingContext == null)
                sharedEditingContext = new EOSharedEditingContext(this);
            return sharedEditingContext;
        }

    }

    /** thread and locking safe implementation to propagate the changes from one EOF stack to another */
    private static class ProcessChangesQueue implements Runnable {
        List elements = new LinkedList();
        ObjectStoreCoordinatorSynchronizer synchronizer;
        
        private ProcessChangesQueue(ObjectStoreCoordinatorSynchronizer synchronizer) {
            this.synchronizer = synchronizer;
        }

        private void addToUpdatesQueue(NSDictionary updatedEntDict, ObjectStoreCoordinator sender) {
            synchronized (elements) {
                elements.add(new NSArray(new Object[]{updatedEntDict, sender}));
            }
        }

        public void run() {
            while (true) {
                try {
                    //FIXME: we should use wait and notify here...
                    Thread.sleep(1);
                } catch (InterruptedException e) {e.printStackTrace();}
                if (elements.size() > 0) {
                    NSArray changes = null;
                    synchronized(elements) {
                        changes = (NSArray)elements.remove(0);
                    }
                    NSDictionary updatedEntDict = (NSDictionary)changes.objectAtIndex(0);
                    ObjectStoreCoordinator sender = (ObjectStoreCoordinator)changes.objectAtIndex(1);
                    processUpdates(updatedEntDict, sender);
                }
            }
        }

        private synchronized void processUpdates(NSDictionary updatedEntDict, ObjectStoreCoordinator sender) {
            NSArray entityNames = updatedEntDict.allKeys();

            Enumeration entitiesEnumerator = entityNames.objectEnumerator();
            while (entitiesEnumerator.hasMoreElements()) {
                String entityName = (String)entitiesEnumerator.nextElement();
                EOEntity entity = EOModelGroup.modelGroupForObjectStoreCoordinator(sender).entityNamed(entityName);

                NSArray snapshots = (NSArray)updatedEntDict.objectForKey(entityName);

                for (int i = 0; i < synchronizer.oscs.count(); i++) {
                    ObjectStoreCoordinator osc = (ObjectStoreCoordinator)synchronizer.oscs.objectAtIndex(i);
                    if (osc == sender) continue;
                    EODatabaseContext dbContext = databaseContextForEntityNamed(entityName, osc);
                    EODatabase database = dbContext.database();
                    Enumeration snapshotsEnumerator = snapshots.objectEnumerator();
                    while (snapshotsEnumerator.hasMoreElements()) {
                        NSDictionary snapshot = (NSDictionary)snapshotsEnumerator.nextElement();
                        if (snapshot != null) {
                            EOGlobalID globalID = entity.globalIDForRow(snapshot);
                            //System.out.println("about to lock dbc "+dbContext.hashCode());
                            EODatabaseContext._EOAssertSafeMultiThreadedAccess(dbContext);
                            dbContext.lock();
                            database.forgetSnapshotForGlobalID(globalID);
                            database.recordSnapshotForGlobalID(snapshot, globalID);
                            dbContext.unlock();
                            //System.out.println("after unlock dbc "+dbContext.hashCode());
                        }
                    }
                }
            }
        }

        private EODatabaseContext databaseContextForEntityNamed(String entityName, ObjectStoreCoordinator osc) {
            return ObjectStoreCoordinatorSynchronizer.databaseContextForEntityNamed(entityName, osc);
        }
        
    }

    /** Synchronizes the different EOF stacks with the use of the ProcessChangesQueue
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
        }

        private void addObjectStoreCoordinator(EOObjectStoreCoordinator osc) {
            if (!(osc instanceof ObjectStoreCoordinator)) {
                throw new RuntimeException("cannot add "+osc+" because its not an instanceof ObjectStoreCoordinator");
            }
            oscs.addObject(osc);
            NSSelector sel = new NSSelector("publishChange", new Class[] { NSNotification.class } );
            NSNotificationCenter.defaultCenter().addObserver(
                                                             this,
                                                             sel,
                                                             EOObjectStoreCoordinator.ObjectsChangedInStoreNotification,
                                                             osc);
        }

        private static ObjectStoreCoordinatorSynchronizer sharedInstance(ObjectStoreCoordinator osc) {
            if (!inited) {
                inited = true;
                EOObjectStoreCoordinator.setDefaultCoordinator(osc);
                sharedInstance.addObjectStoreCoordinator(EOObjectStoreCoordinator.defaultCoordinator());
            }
            return sharedInstance;
        }
        
        public void publishChange(NSNotification n) {
            if (oscs.count() < 2) return;
            NSDictionary userInfo = n.userInfo();
            ObjectStoreCoordinator osc = (ObjectStoreCoordinator)n.object();
            NSDictionary sh = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("updated"), osc);

            queueThread.addToUpdatesQueue(sh, osc);

        }

        
        private NSDictionary snapshotsGroupedByEntity(NSArray objects, ObjectStoreCoordinator osc) {
            if (objects == null)  return NSDictionary.EmptyDictionary;

            NSMutableDictionary result = new NSMutableDictionary();

            Enumeration e = objects.objectEnumerator();
            while (e.hasMoreElements()) {
                EOKeyGlobalID globalID = (EOKeyGlobalID) e.nextElement();
                String entityName = globalID.entityName();

                EODatabaseContext dbContext = databaseContextForEntityNamed(entityName, osc);
                NSMutableArray snapshotsForEntity = (NSMutableArray)result.objectForKey(entityName);
                if (snapshotsForEntity == null) {
                    snapshotsForEntity = new NSMutableArray();
                    result.setObjectForKey(snapshotsForEntity, entityName);
                }
                synchronized (snapshotsForEntity) {
                    Object o = dbContext.snapshotForGlobalID(globalID);
                    if (o != null) {
                        snapshotsForEntity.addObject(o);
                    }
                }
            }
            return result.immutableClone();
        }

        private static synchronized EODatabaseContext databaseContextForEntityNamed(String entityName, ObjectStoreCoordinator osc) {
            String modelName = EOModelGroup.modelGroupForObjectStoreCoordinator(osc).entityNamed(entityName).model().name();
            Hashtable h = osc.databaseContextsForModels();
            EODatabaseContext dbc = (EODatabaseContext)h.get(modelName);
            return dbc;
        }
    }    
}

