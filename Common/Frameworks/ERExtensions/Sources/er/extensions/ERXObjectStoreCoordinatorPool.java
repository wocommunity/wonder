package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;

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

    public ERXObjectStoreCoordinatorPool() {
        maxOS = ERXProperties.intForKey("er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators");
        if (maxOS == 0)
            //this should work like the default implementation
            maxOS = 1;
        observer = new Observer();
        oscForSession = new Hashtable();
    }

    public static class Observer {
        protected Observer() {
            NSSelector sel = new NSSelector("sessionDidCreate", ERXConstant.NotificationClassArray);
            NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidCreateNotification, null);
            sel = new NSSelector("sessionDidTimeout", ERXConstant.NotificationClassArray);
            NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidTimeOutNotification, null);
        }

        public void sessionDidCreate(NSNotification n) {
            WOSession s = (WOSession) n.object();
            if (defaultPool.oscForSession.get(s.sessionID()) == null) {
                defaultPool.oscForSession.put(s.sessionID(), defaultPool.nextObjectStore());
            }
        }

        public void sessionDidTimeout(NSNotification n) {
            String sessionID = (String) n.object();
            defaultPool.oscForSession.remove(sessionID);
        }
    }

    protected String sessionID() {
        WOSession session = ERXExtensions.session();
        String sessionID = null;
        if (session != null) {
            sessionID = session.sessionID();
        }
        return sessionID;
    }

    public EOObjectStore currentRootObjectStore() {
        String sessionID = sessionID();
        EOObjectStore os = null;
        if (sessionID != null) {
            os = (EOObjectStore) oscForSession.get(sessionID);
        } else {
            os = nextObjectStore();
            if (sessionID != null) {
                oscForSession.put(sessionID, os);
            }
        }
        return os;
    }

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

    public static class MultiOSCFactory extends ERXEC.DefaultFactory {
        public MultiOSCFactory() {
            super();
        }
        public EOEditingContext _newEditingContext() {
            return _newEditingContext(true);
        }

        public EOEditingContext _newEditingContext(boolean validationEnabled) {
            EOObjectStore os = defaultPool.currentRootObjectStore();
            EOEditingContext ec = _newEditingContext(os, validationEnabled);
            ec.setSharedEditingContext(((ObjectStoreCoordinator) os).sharedEditingContext());
            return ec;
        }

        /** alternativ:
        public EOEditingContext _newEditingContext(EOObjectStore objectStore, boolean validationEnabled) {
            EOObjectStore os = objectStore == EOEditingContext.defaultParentObjectStore() ? defaultPool.currentRootObjectStore() : os;
            EOEditingContext ec = super._newEditingContext(os, validationEnabled);
            ec.setSharedEditingContext(((ObjectStoreCoordinator)os).sharedEditingContext());
            return ec;
        } */
    }

    public void _initObjectStores() {
        log.info("initializing Pool...");
        objectStores = new ObjectStoreCoordinator[maxOS];
        for (int i = 0; i < maxOS; i++) {
            objectStores[i] = new ObjectStoreCoordinator();
        }
        log.info("initializing Pool finished");
    }

    /** subclass to store additional stuff like the shared EC, the useage count, locking etc. */

    public static class ObjectStoreCoordinator extends EOObjectStoreCoordinator {
        protected EOSharedEditingContext sharedEditingContext;
        public ObjectStoreCoordinator() {
            super();
            ObjectStoreCoordinatorSynchronizer.sharedInstance.addObjectStoreCoordinator(this);
        }
        public EOSharedEditingContext sharedEditingContext() {
            if (sharedEditingContext == null)
                sharedEditingContext = new EOSharedEditingContext(this);
            return sharedEditingContext;
        }
    }

    public static class ObjectStoreCoordinatorSynchronizer {

        private static ObjectStoreCoordinatorSynchronizer sharedInstance = new ObjectStoreCoordinatorSynchronizer();
        private NSMutableArray oscs;

        public ObjectStoreCoordinatorSynchronizer() {
            oscs = new NSMutableArray();
            addObjectStoreCoordinator(EOObjectStoreCoordinator.defaultCoordinator());
        }

        public void addObjectStoreCoordinator(EOObjectStoreCoordinator osc) {
            oscs.addObject(osc);
            NSSelector sel = new NSSelector("publishChange", new Class[] { NSNotification.class } );
            NSNotificationCenter.defaultCenter().addObserver(
                                                             this,
                                                             sel,
                                                             EOObjectStoreCoordinator.ObjectsChangedInStoreNotification,
                                                             osc);
        }

        public static ObjectStoreCoordinatorSynchronizer sharedInstance() { return sharedInstance; }
        
        public void publishChange(NSNotification n) {
            NSDictionary userInfo = n.userInfo();
            EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator)n.object();
            NSDictionary sh = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("updated"), osc);
            processUpdates(sh, osc);
        }

        public void processUpdates(NSDictionary updatedEntDict, EOObjectStoreCoordinator sender) {
            NSArray entityNames = updatedEntDict.allKeys();

            Enumeration entitiesEnumerator = entityNames.objectEnumerator();
            while (entitiesEnumerator.hasMoreElements()) {
                String entityName = (String)entitiesEnumerator.nextElement();
                EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);

                NSArray snapshots = (NSArray)updatedEntDict.objectForKey(entityName);

                for (int i = 0; i < oscs.count(); i++) {
                    EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator)oscs.objectAtIndex(i);
                    if (osc == sender) continue;
                    EODatabaseContext dbContext = databaseContextForEntityNamed(entityName, osc);
                    EODatabase database = dbContext.database();
                    Enumeration snapshotsEnumerator = snapshots.objectEnumerator();
                    while (snapshotsEnumerator.hasMoreElements()) {
                        NSDictionary snapshot = (NSDictionary)snapshotsEnumerator.nextElement();
                        if (snapshot != null) {
                            EOGlobalID globalID = entity.globalIDForRow(snapshot);
                            dbContext.lock();
                            database.forgetSnapshotForGlobalID(globalID);
                            database.recordSnapshotForGlobalID(snapshot, globalID);
                            dbContext.unlock();
                        }
                    }
                }
            }
        }

        public NSDictionary snapshotsGroupedByEntity(NSArray objects, EOObjectStoreCoordinator osc) {
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
                snapshotsForEntity.addObject(dbContext.snapshotForGlobalID(globalID));
            }
            return result.immutableClone();
        }

        public static EODatabaseContext databaseContextForEntityNamed(String entityName, EOObjectStoreCoordinator osc) {
            String modelName = EOModelGroup.defaultGroup().entityNamed(entityName).model().name();
            EOModelGroup eomodelgroup = EOModelGroup.modelGroupForObjectStoreCoordinator(osc);
            EOModel eomodel = eomodelgroup.modelNamed(modelName);
            if(eomodel == null) {
                throw new EOObjectNotAvailableException("databaseContextForEntityNamed: cannot find model named " + modelName + " associated with this EOEditingContext");
            } else {
                EODatabaseContext eodatabasecontext = EODatabaseContext.registeredDatabaseContextForModel(eomodel, osc);
                return eodatabasecontext;
            }
        }
    }    
}

