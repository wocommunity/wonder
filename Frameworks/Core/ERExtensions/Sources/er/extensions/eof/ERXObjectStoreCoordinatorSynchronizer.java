/*
 * Created on 22.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions.eof;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXDatabase.CacheChange;
import er.extensions.remoteSynchronizer.ERXRemoteSynchronizer;

/**
 * Synchronizes different EOF stacks inside an instance. This supplements the
 * change notification frameworks that sync different instances and should help
 * you to run multithreaded. After calling initialize(), every
 * ObjectStoreCoordinator that gets created will be added to the list of stacks
 * to sync. You will need to add any stack created before initalization
 * manually.
 */
public class ERXObjectStoreCoordinatorSynchronizer {
	public static final Logger log = Logger.getLogger(ERXObjectStoreCoordinatorSynchronizer.class);

	public static final String SYNCHRONIZER_KEY = "_synchronizer";

	private static ERXObjectStoreCoordinatorSynchronizer _synchronizer;
	private static ThreadLocal _processingRemoteNotifications = new ThreadLocal();

	public static void initialize() {
		if (_synchronizer == null) {
			_synchronizer = new ERXObjectStoreCoordinatorSynchronizer();
		}
	}

	public static ERXObjectStoreCoordinatorSynchronizer synchronizer() {
		if (_synchronizer == null) {
			initialize();
		}
		return _synchronizer;
	}

	private static NSMutableDictionary _cacheChanges = new NSMutableDictionary();
	private NSMutableArray _coordinators;
	private ProcessChangesQueue _queue;
	protected Thread _queueThread;
	private ERXRemoteSynchronizer _remoteSynchronizer;
	private SynchronizerSettings _defaultSettings;
	private NSMutableDictionary<EOObjectStoreCoordinator, SynchronizerSettings> _settings;

	private ERXObjectStoreCoordinatorSynchronizer() {
		_coordinators = new NSMutableArray();
		_queue = new ProcessChangesQueue();
		_defaultSettings = new SynchronizerSettings(true, true, true, true);
		_settings = new NSMutableDictionary<EOObjectStoreCoordinator, SynchronizerSettings>();

		_queueThread = new Thread(_queue);
		_queueThread.setName("ERXOSCProcessChanges");
		_queueThread.setDaemon(true);
		_queueThread.start();
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("objectStoreWasAdded", ERXConstant.NotificationClassArray), EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification, null);
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("objectStoreWasRemoved", ERXConstant.NotificationClassArray), EOObjectStoreCoordinator.CooperatingObjectStoreWasRemovedNotification, null);
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("startRemoteSynchronizer", ERXConstant.NotificationClassArray), WOApplication.ApplicationDidFinishLaunchingNotification, null);
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("stopRemoteSynchronizer", ERXConstant.NotificationClassArray), ERXApplication.ApplicationWillTerminateNotification, null);
	}

	public void setDefaultSettings(SynchronizerSettings defaultSettings) {
		_defaultSettings = defaultSettings;
	}

	public void setSettingsForCoordinator(SynchronizerSettings settings, EOObjectStoreCoordinator coordinator) {
		if (settings == null) {
			_settings.removeObjectForKey(coordinator);
		}
		else {
			_settings.setObjectForKey(settings, coordinator);
		}
	}

	public SynchronizerSettings settingsForCoordinator(EOObjectStoreCoordinator coordinator) {
		SynchronizerSettings settings = _settings.objectForKey(coordinator);
		if (settings == null) {
			settings = _defaultSettings;
		}
		return settings;
	}

	public void initializeRemoteSynchronizer() {
		boolean remoteSynchronizerEnabled = ERXRemoteSynchronizer.remoteSynchronizerEnabled();
		if (remoteSynchronizerEnabled) {
			try {
				_remoteSynchronizer = ERXRemoteSynchronizer.newRemoteSynchronizer(_queue);
				_remoteSynchronizer.join();
				_remoteSynchronizer.listen();
			}
			catch (Throwable e) {
				throw new RuntimeException("Failed to configure remote synchronization.", e);
			}
		}
	}

	public void startRemoteSynchronizer(NSNotification n) {
		initializeRemoteSynchronizer();
	}
	
	public void stopRemoteSynchronizer(NSNotification n) {
		_queue.stop();
	}

	public void objectStoreWasRemoved(NSNotification n) {
		removeObjectStore((EOObjectStoreCoordinator) n.object());
	}

	public void objectStoreWasAdded(NSNotification n) {
		addObjectStore((EOObjectStoreCoordinator) n.object());
	}

	public void addObjectStore(EOObjectStoreCoordinator osc) {
		synchronized (_coordinators) {
			if (!_coordinators.containsObject(osc)) {
				_coordinators.addObject(osc);
				NSSelector sel = new NSSelector("publishChange", new Class[] { NSNotification.class });
				NSNotificationCenter.defaultCenter().addObserver(this, sel, EOObjectStore.ObjectsChangedInStoreNotification, osc);
				NSSelector snapshotCacheChanged = new NSSelector("snapshotCacheChanged", new Class[] { NSNotification.class });
				NSNotificationCenter.defaultCenter().addObserver(this, snapshotCacheChanged, ERXDatabase.SnapshotCacheChanged, null);
			}
		}
	}

	public void removeObjectStore(EOObjectStoreCoordinator osc) {
		synchronized (_coordinators) {
			if (_coordinators.containsObject(osc)) {
				_coordinators.removeObject(osc);
				NSNotificationCenter.defaultCenter().removeObserver(this, EOObjectStore.ObjectsChangedInStoreNotification, osc);
			}
			else {
				log.error("Coordinator not found!");
			}
		}
	}

	protected static void setProcessingRemoteNotifications(boolean processingRemoteNotifications) {
		_processingRemoteNotifications.set(Boolean.valueOf(processingRemoteNotifications));
	}

	protected static boolean isProcessingRemoteNotifications() {
		Boolean processingRemoteNotifications = (Boolean) _processingRemoteNotifications.get();
		return processingRemoteNotifications != null && processingRemoteNotifications.booleanValue();
	}

	public void publishChange(NSNotification n) {
		boolean processingMulticastNotifications = ERXObjectStoreCoordinatorSynchronizer.isProcessingRemoteNotifications();
		if (_coordinators.count() > 1 || (_remoteSynchronizer != null && !processingMulticastNotifications)) {
			EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) n.object();
			NSDictionary userInfo = n.userInfo();
			if (userInfo == null || userInfo.valueForKey(ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY) == null) {
				LocalChange changes = new LocalChange(osc, userInfo, processingMulticastNotifications);
				_queue.addChange(changes);
			}
		}
	}

	public void snapshotCacheChanged(NSNotification n) {
		boolean processingMulticastNotifications = ERXObjectStoreCoordinatorSynchronizer.isProcessingRemoteNotifications();
		if (_remoteSynchronizer != null && !processingMulticastNotifications) {
			ERXDatabase database = (ERXDatabase) n.object();
			ERXDatabase.CacheChange cacheChange = (ERXDatabase.CacheChange) n.userInfo().objectForKey(ERXDatabase.CacheChangeKey);
			if (cacheChange != null) {
				ERXObjectStoreCoordinatorSynchronizer._enqueueCacheChange(database, cacheChange);
			}
		}
	}

	public synchronized static void _enqueueCacheChange(EODatabase database, CacheChange cacheChange) {
		// System.out.println("ERXObjectStoreCoordinatorSynchronizer._enqueueCacheChange:
		// " + cacheChange);
		NSMutableArray cacheChanges = (NSMutableArray) ERXObjectStoreCoordinatorSynchronizer._cacheChanges.objectForKey(database);
		if (cacheChanges == null) {
			cacheChanges = new NSMutableArray();
			ERXObjectStoreCoordinatorSynchronizer._cacheChanges.setObjectForKey(cacheChanges, database);
		}
		cacheChanges.addObject(cacheChange);
	}

	public synchronized static NSArray dequeueCacheChanges(EODatabase database) {
		NSMutableArray cacheChanges = (NSMutableArray) ERXObjectStoreCoordinatorSynchronizer._cacheChanges.removeObjectForKey(database);
		return cacheChanges;
	}

	private Enumeration coordinators() {
		return _coordinators.objectEnumerator();
	}

	private NSArray _coordinators() {
		return _coordinators.immutableClone();
	}

	/**
	 * Thread and locking safe implementation to propagate the changes from one
	 * EOF stack to another.
	 */
	private class ProcessChangesQueue implements Runnable, IChangeListener {
		private boolean _running;
		
		private abstract class SnapshotProcessor {
			public SnapshotProcessor() {}
			public abstract void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots, SynchronizerSettings settings);
		}

		private abstract class RelationshipSnapshotProcessor extends SnapshotProcessor {
			public RelationshipSnapshotProcessor() {}

			public void processRelationships(EODatabase database, NSArray gids, NSDictionary snapshots, Object context, SynchronizerSettings settings) {
				Enumeration gidsEnum = gids.objectEnumerator();
				while (gidsEnum.hasMoreElements()) {
					EOKeyGlobalID gid = (EOKeyGlobalID) gidsEnum.nextElement();
					processGID(database, gid, context, settings);
					EOEntity entity = database.entityNamed(gid.entityName());
					NSDictionary snapshot = (NSDictionary) snapshots.objectForKey(gid);
					Enumeration relationshipsEnum = entity.relationships().objectEnumerator();
					while (relationshipsEnum.hasMoreElements()) {
						EORelationship relationship = (EORelationship) relationshipsEnum.nextElement();
						if (!relationship.isToMany()) {
							EORelationship inverseRelationship = relationship.inverseRelationship();
							if (inverseRelationship != null && inverseRelationship.isToMany()) {
								EOEntity destEntity = inverseRelationship.entity();
								NSDictionary destPK = relationship._foreignKeyForSourceRow(snapshot);
								EOGlobalID destGID = destEntity.globalIDForRow(destPK);
								if (destGID != null) {
									processRelationship(database, gid, relationship, destGID, inverseRelationship, context, settings);
								}
							}
						}
					}
				}
			}

			public abstract void processGID(EODatabase database, EOGlobalID sourceGID, Object context, SynchronizerSettings settings);

			public abstract void processRelationship(EODatabase database, EOGlobalID sourceGID, EORelationship sourceRelationship, EOGlobalID destGID, EORelationship inverseRelationship, Object context, SynchronizerSettings settings);
		}

		private class DeleteSnapshotProcessor extends SnapshotProcessor {
			public DeleteSnapshotProcessor() {}

			@Override
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots, SynchronizerSettings settings) {
				if (settings.broadcastDeletes()) {
					NSArray gids = snapshots.allKeys();
					database.forgetSnapshotsForGlobalIDs(gids);
					if (log.isDebugEnabled()) {
						log.debug("forget: " + snapshots);
					}
				}
			}
		}

		private class UpdateSnapshotProcessor extends SnapshotProcessor {
			public UpdateSnapshotProcessor() {}

			@Override
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots, SynchronizerSettings settings) {
				if (settings.broadcastUpdates()) {
					NSArray gids = snapshots.allKeys();
					// database.forgetSnapshotsForGlobalIDs(gids);
					database.recordSnapshots(snapshots);

					NSMutableDictionary userInfo = new NSMutableDictionary(gids, EODatabaseContext.UpdatedKey);
					userInfo.setObjectForKey(Boolean.TRUE, ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
					NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification, dbc, userInfo);

					if (log.isDebugEnabled()) {
						log.debug("update: " + snapshots);
					}
				}
			}
		}

		private class InsertSnapshotProcessor extends RelationshipSnapshotProcessor {
			public InsertSnapshotProcessor() {}

			@Override
			public void processGID(EODatabase database, EOGlobalID sourceGID, Object context, SynchronizerSettings settings) {
			}

			@Override
			public void processRelationship(EODatabase database, EOGlobalID sourceGID, EORelationship sourceRelationship, EOGlobalID destGID, EORelationship inverseRelationship, Object context, SynchronizerSettings settings) {
				String inverseRelationshipName = inverseRelationship.name();
				NSArray inverseRelationshipGIDs = database.snapshotForSourceGlobalID(destGID, inverseRelationshipName);
				if (inverseRelationshipGIDs != null) {
					database.recordSnapshotForSourceGlobalID(inverseRelationshipGIDs.arrayByAddingObject(sourceGID), destGID, inverseRelationshipName);
				}
			}

			@Override
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots, SynchronizerSettings settings) {
				NSArray gids = snapshots.allKeys();
				if (settings.broadcastInserts()) {
					database.recordSnapshots(snapshots);
				}

				if (settings.broadcastRelationships()) {
					processRelationships(database, gids, snapshots, null, settings);
				}

				if (settings.broadcastInserts() || settings.broadcastRelationships()) {
					NSMutableDictionary userInfo = new NSMutableDictionary(gids, EODatabaseContext.InsertedKey);
					userInfo.setObjectForKey(Boolean.TRUE, ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
					NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification, dbc, userInfo);

					if (log.isDebugEnabled()) {
						log.debug("insert: " + snapshots);
					}
				}
			}
		}

		private class InvalidateSnapshotProcessor extends SnapshotProcessor {
			public InvalidateSnapshotProcessor() {}

			@Override
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots, SynchronizerSettings settings) {
				if (log.isDebugEnabled()) {
					log.debug("invalidate: " + snapshots);
				}
			}
		}

		private abstract class CacheChangeProcessor {
			public CacheChangeProcessor() {}
			public abstract void processCacheChange(EODatabaseContext dbc, EODatabase database, ERXDatabase.CacheChange cacheChange);
		}

		private class DeleteCacheChangeProcessor extends CacheChangeProcessor {
			public DeleteCacheChangeProcessor() {}

			@Override
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.SnapshotDeleted deleteChange = (ERXDatabase.SnapshotDeleted) cacheChange;
				EOGlobalID gid = deleteChange.gid();
				ERXObjectStoreCoordinatorSynchronizer.setProcessingRemoteNotifications(true);
				try {
					database.forgetSnapshotForGlobalID(gid);
				}
				finally {
					ERXObjectStoreCoordinatorSynchronizer.setProcessingRemoteNotifications(false);
				}
				if (log.isDebugEnabled()) {
					log.debug("multicast delete: " + deleteChange);
				}
			}
		}

		private class UpdateCacheChangeProcessor extends CacheChangeProcessor {
			public UpdateCacheChangeProcessor() {}

			@Override
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.SnapshotUpdated updateChange = (ERXDatabase.SnapshotUpdated) cacheChange;
				EOGlobalID gid = updateChange.gid();
				NSDictionary snapshot = database.snapshotForGlobalID(gid);
				if (snapshot != null) {
					ERXObjectStoreCoordinatorSynchronizer.setProcessingRemoteNotifications(true);
					try {
						EOEditingContext editingContext = ERXEC.newEditingContext();
						editingContext.lock();
						try {
							ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(editingContext, new NSArray(gid), true);
						}
						finally {
							editingContext.unlock();
						}
					}
					finally {
						ERXObjectStoreCoordinatorSynchronizer.setProcessingRemoteNotifications(false);
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("multicast update: " + updateChange);
				}
			}
		}

		private class InsertCacheChangeProcessor extends CacheChangeProcessor {
			public InsertCacheChangeProcessor() {}

			@Override
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.SnapshotInserted insertChange = (ERXDatabase.SnapshotInserted) cacheChange;
				// NSMutableDictionary userInfo = new NSMutableDictionary(gids,
				// EODatabaseContext.InsertedKey);
				// userInfo.setObjectForKey(destGIDs,
				// EODatabaseContext.UpdatedKey);
				// userInfo.setObjectForKey(Boolean.TRUE,
				// ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
				// NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification,
				// dbc, userInfo);

				if (log.isDebugEnabled()) {
					log.debug("multicast insert: " + insertChange);
				}
			}
		}

		private class ToManyUpdateCacheChangeProcessor extends CacheChangeProcessor {
			public ToManyUpdateCacheChangeProcessor() {}

			@Override
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.ToManySnapshotUpdated toManyUpdateChange = (ERXDatabase.ToManySnapshotUpdated) cacheChange;
				ERXObjectStoreCoordinatorSynchronizer.setProcessingRemoteNotifications(true);
				try {
					EOGlobalID sourceGID = toManyUpdateChange.gid();
					database.recordSnapshotForSourceGlobalID(null, sourceGID, toManyUpdateChange.name());
					// MS: Technically we can add and remove the GIDs that
					// changed in the
					// remote instances, but without a unified counter across
					// instances, it's hard to
					// know the ordering of changes, so it's possible to end up
					// with an inconsistent
					// state under high concurrency.
					/*
					 * if (toManyUpdateChange.removeAll()) {
					 * database.recordSnapshotForSourceGlobalID(null, sourceGID,
					 * toManyUpdateChange.name()); } else { NSArray
					 * toManySnapshots =
					 * database.snapshotForSourceGlobalID(sourceGID,
					 * toManyUpdateChange.name()); if (toManySnapshots != null) {
					 * NSMutableArray mutableToManySnapshots =
					 * toManySnapshots.mutableClone(); NSArray addedGIDs =
					 * toManyUpdateChange.addedGIDs(); if (addedGIDs != null) {
					 * mutableToManySnapshots.addObjectsFromArray(addedGIDs); }
					 * NSArray removedGIDs = toManyUpdateChange.removedGIDs();
					 * if (removedGIDs != null) {
					 * mutableToManySnapshots.removeObjectsInArray(removedGIDs); }
					 * database.recordSnapshotForSourceGlobalID(removedGIDs,
					 * sourceGID, toManyUpdateChange.name()); } }
					 */
					NSMutableDictionary userInfo = new NSMutableDictionary(new NSArray(sourceGID), EODatabaseContext.UpdatedKey);
					userInfo.setObjectForKey(Boolean.TRUE, ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
					NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification, dbc, userInfo);
				}
				finally {
					ERXObjectStoreCoordinatorSynchronizer.setProcessingRemoteNotifications(false);
				}

				if (log.isDebugEnabled()) {
					log.debug("multicast insert: " + toManyUpdateChange);
				}
			}
		}

		private int _transactionID;
		private List _elements = new LinkedList();
		private SnapshotProcessor _deleteProcessor = new DeleteSnapshotProcessor();
		private SnapshotProcessor _insertProcessor = new InsertSnapshotProcessor();
		private SnapshotProcessor _updateProcessor = new UpdateSnapshotProcessor();
		private SnapshotProcessor _invalidateProcessor = new InvalidateSnapshotProcessor();
		private CacheChangeProcessor _insertCacheChangeProcessor = new InsertCacheChangeProcessor();
		private CacheChangeProcessor _updateCacheChangeProcessor = new UpdateCacheChangeProcessor();
		private CacheChangeProcessor _deleteCacheChangeProcessor = new DeleteCacheChangeProcessor();
		private CacheChangeProcessor _toManyUpdateCacheChangeProcessor = new ToManyUpdateCacheChangeProcessor();

		protected ProcessChangesQueue() {
		}

		public void addChange(Change changes) {
			synchronized (_elements) {
				_elements.add(changes);
				_elements.notify();
			}
		}

		protected void _process(EOObjectStoreCoordinator sender, EOObjectStoreCoordinator osc, NSMutableDictionary dbcs, SnapshotProcessor processor, NSDictionary changesByEntity, String userInfoKey) {
			SynchronizerSettings settings = settingsForCoordinator(osc);
			EOModelGroup modelGroup = EOModelGroup.modelGroupForObjectStoreCoordinator(sender);
			for (Enumeration entityNames = changesByEntity.allKeys().objectEnumerator(); entityNames.hasMoreElements();) {
				String entityName = (String) entityNames.nextElement();
				String key = entityName + "/" + System.identityHashCode(osc);
				EOEntity entity = modelGroup.entityNamed(entityName);
				NSArray snapshots = (NSArray) changesByEntity.objectForKey(entityName);
				EODatabaseContext dbc = (EODatabaseContext) dbcs.objectForKey(key);
				if (dbc == null) {
					dbc = ERXEOAccessUtilities.databaseContextForEntityNamed(osc, entityName);
					dbcs.setObjectForKey(dbc, key);
				}
				EODatabase database = dbc.database();
				NSMutableDictionary snapshotsByGlobalID = new NSMutableDictionary();
				for (Enumeration snapshotsEnumerator = snapshots.objectEnumerator(); snapshotsEnumerator.hasMoreElements();) {
					NSDictionary snapshot = (NSDictionary) snapshotsEnumerator.nextElement();
					EOGlobalID globalID = entity.globalIDForRow(snapshot);
					snapshotsByGlobalID.setObjectForKey(snapshot, globalID);
				}
				if (snapshotsByGlobalID.count() > 0) {
					dbc.lock();
					try {
						processor.processSnapshots(dbc, database, snapshotsByGlobalID, settings);
					}
					finally {
						dbc.unlock();
					}
				}
			}
		}

		/**
		 * @param dictionary
		 * @param sender
		 */
		protected void process(EOObjectStoreCoordinator sender, SnapshotProcessor processor, NSDictionary changesByEntity, String userInfoKey) {
			NSMutableDictionary dbcs = new NSMutableDictionary();
			for (Enumeration oscs = _synchronizer.coordinators(); oscs.hasMoreElements();) {
				EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
				if (osc != sender) {
					_process(sender, osc, dbcs, processor, changesByEntity, userInfoKey);
				}
			}
		}

		protected void processRemoteChange(RemoteChange remoteChange) {
			for (Enumeration oscs = _synchronizer.coordinators(); oscs.hasMoreElements();) {
				EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
				Enumeration cacheChangeEnum = remoteChange.remoteCacheChanges().objectEnumerator();
				while (cacheChangeEnum.hasMoreElements()) {
					ERXDatabase.CacheChange cacheChange = (ERXDatabase.CacheChange) cacheChangeEnum.nextElement();
					EOKeyGlobalID gid = (EOKeyGlobalID) cacheChange.gid();
					EODatabaseContext dbc = ERXEOAccessUtilities.databaseContextForEntityNamed(osc, gid.entityName());
					EODatabase database = dbc.database();
					dbc.lock();
					try {
						if (cacheChange instanceof ERXDatabase.SnapshotInserted) {
							_insertCacheChangeProcessor.processCacheChange(dbc, database, cacheChange);
						}
						else if (cacheChange instanceof ERXDatabase.SnapshotUpdated) {
							_updateCacheChangeProcessor.processCacheChange(dbc, database, cacheChange);
						}
						else if (cacheChange instanceof ERXDatabase.SnapshotDeleted) {
							_deleteCacheChangeProcessor.processCacheChange(dbc, database, cacheChange);
						}
						else if (cacheChange instanceof ERXDatabase.ToManySnapshotUpdated) {
							_toManyUpdateCacheChangeProcessor.processCacheChange(dbc, database, cacheChange);
						}
					}
					finally {
						dbc.unlock();
					}
				}
			}
		}

		protected void publishRemoteChanges(int transactionID, LocalChange localChange) {
			if (_remoteSynchronizer != null && !localChange.causedByRemoteUpdate()) {
				try {
					NSArray cacheChanges = localChange.localCacheChanges();
					if (cacheChanges != null && cacheChanges.count() > 0) {
						_remoteSynchronizer.writeCacheChanges(transactionID, cacheChanges);
					}
				}
				catch (Throwable e) {
					log.error("Failed to send remote changes: " + localChange + ".", e);
				}
			}
		}

		public void run() {
			try {
				_running = true;
				while (_running) {
					Change changes = null;
					synchronized (_elements) {
						try {
							if (_elements.isEmpty()) {
								_elements.wait();
							}
							if (!_elements.isEmpty()) {
								changes = (Change) _elements.remove(0);
							}
						}
						catch (InterruptedException e) {
							if (_running) {
								log.warn("Interrupted: " + e, e);
							}
							_running = false;
						}
					}
					if (changes != null) {
						if (changes instanceof LocalChange) {
							LocalChange localChange = (LocalChange) changes;
							EOObjectStoreCoordinator sender = localChange.coordinator();
							process(sender, _deleteProcessor, localChange.deleted(), EODatabaseContext.DeletedKey);
							process(sender, _insertProcessor, localChange.inserted(), EODatabaseContext.InsertedKey);
							process(sender, _updateProcessor, localChange.updated(), EODatabaseContext.UpdatedKey);
							process(sender, _invalidateProcessor, localChange.invalidated(), EODatabaseContext.InvalidatedKey);
							publishRemoteChanges(_transactionID++, localChange);
						}
						else if (changes instanceof RemoteChange) {
							processRemoteChange((RemoteChange) changes);
						}
					}
				}
			} catch (Throwable e) {
				log.error(e, e);
			}
		}
		
		public void stop() {
			if (_queueThread != null && _queueThread.isAlive()) {
				_running = false;
				_queueThread.interrupt();
			}
			else {
				throw new IllegalStateException("Attempted to stop the " + getClass().getSimpleName() + " when it wasn't already running");
			}
		}
	}

	public static class Change {
		public Change() {
		}
	}

	public static class RemoteChange extends Change {
		private NSMutableArray _remoteCacheChanges;
		private String _identifier;
		private int _transactionID;
		private int _transactionSize;
		private long _creationDate;

		public RemoteChange(String identifier, int transactionID, int transactionSize) {
			_creationDate = System.currentTimeMillis();
			_remoteCacheChanges = new NSMutableArray();
			_identifier = identifier;
			_transactionID = transactionID;
			_transactionSize = transactionSize;
		}

		public String identifier() {
			return _identifier;
		}

		public NSArray remoteCacheChanges() {
			return _remoteCacheChanges;
		}

		public void addRemoteCacheChange(ERXDatabase.CacheChange cacheChange) {
			_remoteCacheChanges.addObject(cacheChange);
		}

		public boolean isComplete() {
			return _remoteCacheChanges.count() <= _transactionSize;
		}

		public long creationDate() {
			return _creationDate;
		}
	}

	/**
	 * Holds a change notification (one transaction).
	 */
	public static class LocalChange extends Change {
		private NSMutableArray _localCacheChanges;
		private EOObjectStoreCoordinator _coordinator;
		private NSDictionary _inserted;
		private NSDictionary _updated;
		private NSDictionary _deleted;
		private NSDictionary _invalidated;
		private NSArray _deletedGIDs;
		private NSArray _updatedGIDs;
		private NSArray _insertedGIDs;
		private NSArray _invalidatedGIDs;
		private boolean _causedByRemoteUpdate;

		public LocalChange(EOObjectStoreCoordinator osc, NSDictionary userInfo, boolean causedByRemoteUpdate) {
			_coordinator = osc;
			_deletedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.DeletedKey);
			_updatedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.UpdatedKey);
			_insertedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.InsertedKey);
			_invalidatedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.InvalidatedKey);
			_deleted = snapshotsGroupedByEntity(_deletedGIDs, _coordinator);
			_updated = snapshotsGroupedByEntity(_updatedGIDs, _coordinator);
			_inserted = snapshotsGroupedByEntity(_insertedGIDs, _coordinator);
			_invalidated = snapshotsGroupedByEntity(_invalidatedGIDs, _coordinator);
			_causedByRemoteUpdate = causedByRemoteUpdate;
			_localCacheChanges = new NSMutableArray();
			if (!causedByRemoteUpdate) {
				Enumeration cosEnum = osc.cooperatingObjectStores().objectEnumerator();
				while (cosEnum.hasMoreElements()) {
					EOCooperatingObjectStore cos = (EOCooperatingObjectStore) cosEnum.nextElement();
					if (cos instanceof ERXDatabaseContext) {
						EODatabaseContext dbc = (EODatabaseContext) cos;
						ERXDatabase db = (ERXDatabase) dbc.database();
						NSArray cacheChanges = ERXObjectStoreCoordinatorSynchronizer.dequeueCacheChanges(db);
						if (cacheChanges != null) {
							_localCacheChanges.addObjectsFromArray(cacheChanges);
						}
					}
				}
			}
		}

		public boolean causedByRemoteUpdate() {
			return _causedByRemoteUpdate;
		}

		public NSArray localCacheChanges() {
			return _localCacheChanges;
		}

		public NSArray deletedGIDs() {
			return _deletedGIDs;
		}

		public NSArray updatedGIDs() {
			return _updatedGIDs;
		}

		public NSArray insertedGIDs() {
			return _insertedGIDs;
		}

		public NSArray invalidatedGIDs() {
			return _invalidatedGIDs;
		}

		/**
		 * Returns a dictionary of snapshots where the key is the entity name
		 * and the value an array of snapshots.
		 * 
		 * @param objects
		 * @param osc
		 */
		protected NSDictionary snapshotsGroupedByEntity(NSArray objects, EOObjectStoreCoordinator osc) {
			if (objects == null || objects.count() == 0) {
				return NSDictionary.EmptyDictionary;
			}

			NSMutableDictionary result = new NSMutableDictionary();
			NSMutableDictionary dbcs = new NSMutableDictionary();

			for (Enumeration gids = objects.objectEnumerator(); gids.hasMoreElements();) {
				EOGlobalID gid = (EOGlobalID) gids.nextElement();
				if (gid instanceof EOKeyGlobalID) {
					EOKeyGlobalID globalID = (EOKeyGlobalID) gid;
					String entityName = globalID.entityName();
					String key = entityName + "/" + System.identityHashCode(osc);
					EODatabaseContext dbc = (EODatabaseContext) dbcs.objectForKey(key);
					if (dbc == null) {
						dbc = ERXEOAccessUtilities.databaseContextForEntityNamed(osc, entityName);
						dbcs.setObjectForKey(dbc, key);
					}
					NSMutableArray snapshotsForEntity = (NSMutableArray) result.objectForKey(entityName);
					if (snapshotsForEntity == null) {
						snapshotsForEntity = new NSMutableArray();
						result.setObjectForKey(snapshotsForEntity, entityName);
					}
					synchronized (snapshotsForEntity) {
						Object o = dbc.snapshotForGlobalID(globalID);
						if (o != null) {
							snapshotsForEntity.addObject(o);
						}
					}
				}
			}
			return result;
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

		public NSDictionary invalidated() {
			return _invalidated;
		}

		public EOObjectStoreCoordinator coordinator() {
			return _coordinator;
		}
	}

	public static interface IChangeListener {
		public void addChange(Change changes);
	}

	public static class SynchronizerSettings {
		private boolean _broadcastInserts;
		private boolean _broadcastUpdates;
		private boolean _broadcastDeletes;
		private boolean _broadcastRelationships;

		public SynchronizerSettings(boolean broadcastInserts, boolean broadcastUpdates, boolean broadcastDeletes, boolean broadcastRelationships) {
			_broadcastInserts = broadcastInserts;
			_broadcastUpdates = broadcastUpdates;
			_broadcastDeletes = broadcastDeletes;
			_broadcastRelationships = broadcastRelationships;
		}

		public boolean broadcastInserts() {
			return _broadcastInserts;
		}

		public boolean broadcastUpdates() {
			return _broadcastUpdates;
		}

		public boolean broadcastDeletes() {
			return _broadcastDeletes;
		}

		public boolean broadcastRelationships() {
			return _broadcastRelationships;
		}
	}
}
