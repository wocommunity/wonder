/*
 * Created on 22.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXDatabase.CacheChange;

/**
 * Synchronizes different EOF stacks inside an instance. This supplements the change notification frameworks that sync
 * different instances and should help you to run multithreaded. After calling initialize(), every
 * ObjectStoreCoordinator that gets created will be added to the list of stacks to sync. You will need to add any stack
 * created before initalization manually.
 */
public class ERXObjectStoreCoordinatorSynchronizer {
	private static Logger log = Logger.getLogger(ERXObjectStoreCoordinatorSynchronizer.class);

	public static final String SYNCHRONIZER_KEY = "_synchronizer";

	private static ERXObjectStoreCoordinatorSynchronizer _synchronizer;
	private static ThreadLocal _processingMulticastNotifications = new ThreadLocal();

	public static boolean multicastSynchronizationEnabled() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.multicastSynchronizer.enabled", false);
	}
	
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
	private ProcessChangesQueue _queueThread;
	private MulticastSynchronizer _multicastSynchronizer;

	private ERXObjectStoreCoordinatorSynchronizer() {
		_coordinators = new NSMutableArray();
		_queueThread = new ProcessChangesQueue();

		new Thread(_queueThread).start();
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("objectStoreWasAdded", ERXConstant.NotificationClassArray), EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification, null);
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("objectStoreWasRemoved", ERXConstant.NotificationClassArray), EOObjectStoreCoordinator.CooperatingObjectStoreWasRemovedNotification, null);
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("startMulticastListener", ERXConstant.NotificationClassArray), WOApplication.ApplicationDidFinishLaunchingNotification, null);
	}

	public void initializeMulticast() {
		boolean multicastEnabled = ERXObjectStoreCoordinatorSynchronizer.multicastSynchronizationEnabled();
		if (multicastEnabled) {
			try {
				String localBindAddressStr = ERXProperties.stringForKey("er.extensions.multicastSynchronizer.localBindAddress");
				InetAddress localBindAddress;
				if (localBindAddressStr == null) {
					localBindAddress = WOApplication.application().hostAddress();
				}
				else {
					localBindAddress = InetAddress.getByName(localBindAddressStr);
				}

				byte[] multicastIdentifier;
				String multicastIdentifierStr = ERXProperties.stringForKey("er.extensions.multicastSynchronizer.identifier");
				if (multicastIdentifierStr == null) {
					multicastIdentifier = new byte[MulticastSynchronizer.IDENTIFIER_LENGTH];
					byte[] hostAddressBytes = localBindAddress.getAddress();
					System.arraycopy(hostAddressBytes, 0, multicastIdentifier, 0, hostAddressBytes.length);
					int multicastInstance = WOApplication.application().port().shortValue();
					multicastIdentifier[4] = (byte) (multicastInstance & 0xff);
					multicastIdentifier[5] = (byte) ((multicastInstance >>> 8) & 0xff);
				}
				else {
					multicastIdentifier = ERXStringUtilities.hexStringToByteArray(multicastIdentifierStr);
				}

				String multicastGroup = ERXProperties.stringForKeyWithDefault("er.extensions.multicastSynchronizer.group", "230.0.0.1");
				int multicastPort = ERXProperties.intForKeyWithDefault("er.extensions.multicastSynchronizer.port", 9753);
				String includeEntityNames = ERXProperties.stringForKey("er.extensions.multicastSynchronizer.includeEntities");
				NSArray includeEntityNamesArray = null;
				if (includeEntityNames != null) {
					includeEntityNamesArray = NSArray.componentsSeparatedByString(includeEntityNames, ",");
				}
				NSArray excludeEntityNamesArray = null;
				String excludeEntityNames = ERXProperties.stringForKey("er.extensions.multicastSynchronizer.excludeEntities");
				if (excludeEntityNames != null) {
					excludeEntityNamesArray = NSArray.componentsSeparatedByString(excludeEntityNames, ",");
				}
				NSArray whitelistArray = null;
				String whitelist = ERXProperties.stringForKey("er.extensions.multicastSynchronizer.whitelist");
				if (whitelist != null) {
					whitelistArray = NSArray.componentsSeparatedByString(whitelist, ",");
				}
				int maxPacketSize = ERXProperties.intForKeyWithDefault("er.extensions.multicastSynchronizer.maxPacketSize", 1024);
				_multicastSynchronizer = new MulticastSynchronizer(multicastIdentifier, localBindAddress, multicastGroup, multicastPort, includeEntityNamesArray, excludeEntityNamesArray, whitelistArray, maxPacketSize, _queueThread);
				_multicastSynchronizer.join();
				_multicastSynchronizer.listen();
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to configure multicast synchronizer.", e);
			}
		}
	}

	public void startMulticastListener(NSNotification n) {
		initializeMulticast();
	}

	public void objectStoreWasRemoved(NSNotification n) {
		removeObjectStore((EOObjectStoreCoordinator) n.object());
	}

	public void objectStoreWasAdded(NSNotification n) {
		addObjectStore((EOObjectStoreCoordinator) n.object());
	}

	public void addObjectStore(EOObjectStoreCoordinator osc) {
		synchronized (_coordinators) {
			if (!_coordinators.contains(osc)) {
				_coordinators.add(osc);
				NSSelector sel = new NSSelector("publishChange", new Class[] { NSNotification.class });
				NSNotificationCenter.defaultCenter().addObserver(this, sel, EOObjectStore.ObjectsChangedInStoreNotification, osc);
				NSSelector snapshotCacheChanged = new NSSelector("snapshotCacheChanged", new Class[] { NSNotification.class });
				NSNotificationCenter.defaultCenter().addObserver(this, snapshotCacheChanged, ERXDatabase.SnapshotCacheChanged, null);
			}
			else {
				log.error("Adding same coordinator twice!");
			}
		}
	}

	public void removeObjectStore(EOObjectStoreCoordinator osc) {
		synchronized (_coordinators) {
			if (_coordinators.contains(osc)) {
				_coordinators.remove(osc);
				NSNotificationCenter.defaultCenter().removeObserver(this, EOObjectStore.ObjectsChangedInStoreNotification, osc);
			}
			else {
				log.error("Coordinator not found!");
			}
		}
	}

	protected static void setProcessingMulticastNotifications(boolean processingMulticastNotifications) {
		_processingMulticastNotifications.set(Boolean.valueOf(processingMulticastNotifications));
	}

	protected static boolean isProcessingMulticastNotifications() {
		Boolean processingMulticastNotifications = (Boolean) _processingMulticastNotifications.get();
		return processingMulticastNotifications != null && processingMulticastNotifications.booleanValue();
	}

	public void publishChange(NSNotification n) {
		boolean processingMulticastNotifications = ERXObjectStoreCoordinatorSynchronizer.isProcessingMulticastNotifications();
		if (_coordinators.size() > 1 || (_multicastSynchronizer != null && !processingMulticastNotifications)) {
			EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) n.object();
			NSDictionary userInfo = n.userInfo();
			if (userInfo == null || userInfo.valueForKey(ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY) == null) {
				LocalChange changes = new LocalChange(osc, userInfo, processingMulticastNotifications);
				_queueThread.addChange(changes);
			}
		}
	}

	public void snapshotCacheChanged(NSNotification n) {
		boolean processingMulticastNotifications = ERXObjectStoreCoordinatorSynchronizer.isProcessingMulticastNotifications();
		if (_multicastSynchronizer != null && !processingMulticastNotifications) {
			ERXDatabase database = (ERXDatabase) n.object();
			ERXDatabase.CacheChange cacheChange = (ERXDatabase.CacheChange) n.userInfo().objectForKey(ERXDatabase.CacheChangeKey);
			if (cacheChange != null) {
				ERXObjectStoreCoordinatorSynchronizer._enqueueCacheChange(database, cacheChange);
			}
		}
	}

	public synchronized static void _enqueueCacheChange(EODatabase database, CacheChange cacheChange) {
		// System.out.println("ERXObjectStoreCoordinatorSynchronizer._enqueueCacheChange: " + cacheChange);
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
	 * Thread and locking safe implementation to propagate the changes from one EOF stack to another.
	 */
	private class ProcessChangesQueue implements Runnable, IChangeListener {
		private abstract class SnapshotProcessor {
			public abstract void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots);
		}

		private abstract class RelationshipSnapshotProcessor extends SnapshotProcessor {
			public void processRelationships(EODatabase database, NSArray gids, NSDictionary snapshots, Object context) {
				Enumeration gidsEnum = gids.objectEnumerator();
				while (gidsEnum.hasMoreElements()) {
					EOKeyGlobalID gid = (EOKeyGlobalID) gidsEnum.nextElement();
					processGID(database, gid, context);
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
									processRelationship(database, gid, relationship, destGID, inverseRelationship, context);
								}
							}
						}
					}
				}
			}

			public abstract void processGID(EODatabase database, EOGlobalID sourceGID, Object context);

			public abstract void processRelationship(EODatabase database, EOGlobalID sourceGID, EORelationship sourceRelationship, EOGlobalID destGID, EORelationship inverseRelationship, Object context);
		}

		private class DeleteSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
				NSArray gids = snapshots.allKeys();
				database.forgetSnapshotsForGlobalIDs(gids);
				if (log.isDebugEnabled()) {
					log.debug("forget: " + snapshots);
				}
			}
		}

		private class UpdateSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
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

		private class InsertSnapshotProcessor extends RelationshipSnapshotProcessor {
			public void processGID(EODatabase database, EOGlobalID sourceGID, Object context) {
			}

			public void processRelationship(EODatabase database, EOGlobalID sourceGID, EORelationship sourceRelationship, EOGlobalID destGID, EORelationship inverseRelationship, Object context) {
				String inverseRelationshipName = inverseRelationship.name();
				NSArray inverseRelationshipGIDs = database.snapshotForSourceGlobalID(destGID, inverseRelationshipName);
				if (inverseRelationshipGIDs != null) {
					database.recordSnapshotForSourceGlobalID(inverseRelationshipGIDs.arrayByAddingObject(sourceGID), destGID, inverseRelationshipName);
				}
			}

			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
				NSArray gids = snapshots.allKeys();
				database.recordSnapshots(snapshots);

				processRelationships(database, gids, snapshots, null);

				NSMutableDictionary userInfo = new NSMutableDictionary(gids, EODatabaseContext.InsertedKey);
				userInfo.setObjectForKey(Boolean.TRUE, ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
				NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification, dbc, userInfo);

				if (log.isDebugEnabled()) {
					log.debug("insert: " + snapshots);
				}
			}
		}

		private class InvalidateSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
				if (log.isDebugEnabled()) {
					log.debug("invalidate: " + snapshots);
				}
			}
		}

		private abstract class CacheChangeProcessor {
			public abstract void processCacheChange(EODatabaseContext dbc, EODatabase database, ERXDatabase.CacheChange cacheChange);
		}

		private class DeleteCacheChangeProcessor extends CacheChangeProcessor {
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.SnapshotDeleted deleteChange = (ERXDatabase.SnapshotDeleted) cacheChange;
				EOGlobalID gid = deleteChange.gid();
				ERXObjectStoreCoordinatorSynchronizer.setProcessingMulticastNotifications(true);
				try {
					database.forgetSnapshotForGlobalID(gid);
				}
				finally {
					ERXObjectStoreCoordinatorSynchronizer.setProcessingMulticastNotifications(false);
				}
				if (log.isDebugEnabled()) {
					log.debug("multicast delete: " + deleteChange);
				}
			}
		}

		private class UpdateCacheChangeProcessor extends CacheChangeProcessor {
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.SnapshotUpdated updateChange = (ERXDatabase.SnapshotUpdated) cacheChange;
				EOGlobalID gid = updateChange.gid();
				NSDictionary snapshot = database.snapshotForGlobalID(gid);
				if (snapshot != null) {
					ERXObjectStoreCoordinatorSynchronizer.setProcessingMulticastNotifications(true);
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
						ERXObjectStoreCoordinatorSynchronizer.setProcessingMulticastNotifications(false);
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("multicast update: " + updateChange);
				}
			}
		}

		private class InsertCacheChangeProcessor extends CacheChangeProcessor {
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.SnapshotInserted insertChange = (ERXDatabase.SnapshotInserted) cacheChange;
				// NSMutableDictionary userInfo = new NSMutableDictionary(gids, EODatabaseContext.InsertedKey);
				// userInfo.setObjectForKey(destGIDs, EODatabaseContext.UpdatedKey);
				// userInfo.setObjectForKey(Boolean.TRUE, ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
				// NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification,
				// dbc, userInfo);

				if (log.isDebugEnabled()) {
					log.debug("multicast insert: " + insertChange);
				}
			}
		}

		private class ToManyUpdateCacheChangeProcessor extends CacheChangeProcessor {
			public void processCacheChange(EODatabaseContext dbc, EODatabase database, CacheChange cacheChange) {
				ERXDatabase.ToManySnapshotUpdated toManyUpdateChange = (ERXDatabase.ToManySnapshotUpdated) cacheChange;
				ERXObjectStoreCoordinatorSynchronizer.setProcessingMulticastNotifications(true);
				try {
					EOGlobalID sourceGID = toManyUpdateChange.gid();
					database.recordSnapshotForSourceGlobalID(null, sourceGID, toManyUpdateChange.name());
					// MS: Technically we can add and remove the GIDs that changed in the 
					// remote instances, but without a unified counter across instances, it's hard to
					// know the ordering of changes, so it's possible to end up with an inconsistent
					// state under high concurrency.
					/*
					 * if (toManyUpdateChange.removeAll()) { database.recordSnapshotForSourceGlobalID(null, sourceGID,
					 * toManyUpdateChange.name()); } else { NSArray toManySnapshots =
					 * database.snapshotForSourceGlobalID(sourceGID, toManyUpdateChange.name()); if (toManySnapshots !=
					 * null) { NSMutableArray mutableToManySnapshots = toManySnapshots.mutableClone(); NSArray addedGIDs =
					 * toManyUpdateChange.addedGIDs(); if (addedGIDs != null) {
					 * mutableToManySnapshots.addObjectsFromArray(addedGIDs); } NSArray removedGIDs =
					 * toManyUpdateChange.removedGIDs(); if (removedGIDs != null) {
					 * mutableToManySnapshots.removeObjectsInArray(removedGIDs); }
					 * database.recordSnapshotForSourceGlobalID(removedGIDs, sourceGID, toManyUpdateChange.name()); } }
					 */
					NSMutableDictionary userInfo = new NSMutableDictionary(new NSArray(sourceGID), EODatabaseContext.UpdatedKey);
					userInfo.setObjectForKey(Boolean.TRUE, ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
					NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification, dbc, userInfo);
				}
				finally {
					ERXObjectStoreCoordinatorSynchronizer.setProcessingMulticastNotifications(false);
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

		private ProcessChangesQueue() {
			Thread.currentThread().setName("ProcessChangesQueue");
		}

		public void addChange(Change changes) {
			synchronized (_elements) {
				_elements.add(changes);
				_elements.notify();
			}
		}

		protected void _process(EOObjectStoreCoordinator sender, EOObjectStoreCoordinator osc, NSMutableDictionary dbcs, SnapshotProcessor processor, NSDictionary changesByEntity, String userInfoKey) {
			for (Enumeration entityNames = changesByEntity.allKeys().objectEnumerator(); entityNames.hasMoreElements();) {
				String entityName = (String) entityNames.nextElement();
				String key = entityName + "/" + System.identityHashCode(osc);
				EOEntity entity = EOModelGroup.modelGroupForObjectStoreCoordinator(sender).entityNamed(entityName);
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
					EODatabaseContext._EOAssertSafeMultiThreadedAccess(dbc);
					dbc.lock();
					try {
						processor.processSnapshots(dbc, database, snapshotsByGlobalID);
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
					EODatabaseContext._EOAssertSafeMultiThreadedAccess(dbc);
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

		protected void multicastChanges(int transactionID, LocalChange localChange) {
			if (_multicastSynchronizer != null && !localChange.causedByMulticastUpdate()) {
				try {
					NSArray cacheChanges = localChange.localCacheChanges();
					if (cacheChanges != null && cacheChanges.count() > 0) {
						short transactionSize = (short) cacheChanges.count();
						short transactionNum = 0;
						for (Enumeration cacheChangesEnum = cacheChanges.objectEnumerator(); cacheChangesEnum.hasMoreElements(); transactionNum++) {
							ERXDatabase.CacheChange cacheChange = (ERXDatabase.CacheChange) cacheChangesEnum.nextElement();
							_multicastSynchronizer.writeCacheChange(cacheChange, transactionID, transactionNum, transactionSize);
						}
					}
				}
				catch (IOException e) {
					log.error("Failed to multicast changes: " + localChange + ".", e);
				}
			}
		}

		public void run() {
			boolean run = true;
			while (run) {
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
						run = false;
						log.warn("Interrupted: " + e, e);
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
						multicastChanges(_transactionID++, localChange);
					}
					else if (changes instanceof RemoteChange) {
						processRemoteChange((RemoteChange) changes);
					}
				}
			}
		}
	}

	public static class Change {
		public Change() {
		}
	}

	public static class RemoteChange extends Change {
		private NSMutableArray _remoteCacheChanges;
		private byte[] _identifier;
		private int _transactionID;
		private int _transactionSize;
		private long _creationDate;

		public RemoteChange(byte[] identifier, int transactionID, int transactionSize) {
			_creationDate = System.currentTimeMillis();
			_remoteCacheChanges = new NSMutableArray();
			_identifier = identifier;
			_transactionID = transactionID;
			_transactionSize = transactionSize;
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
		private boolean _causedByMulticastUpdate;

		public LocalChange(EOObjectStoreCoordinator osc, NSDictionary userInfo, boolean causedByMulticastUpdate) {
			_coordinator = osc;
			_deletedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.DeletedKey);
			_updatedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.UpdatedKey);
			_insertedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.InsertedKey);
			_invalidatedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.InvalidatedKey);
			_deleted = snapshotsGroupedByEntity(_deletedGIDs, _coordinator);
			_updated = snapshotsGroupedByEntity(_updatedGIDs, _coordinator);
			_inserted = snapshotsGroupedByEntity(_insertedGIDs, _coordinator);
			_invalidated = snapshotsGroupedByEntity(_invalidatedGIDs, _coordinator);
			_causedByMulticastUpdate = causedByMulticastUpdate;
			_localCacheChanges = new NSMutableArray();
			if (!causedByMulticastUpdate) {
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

		public boolean causedByMulticastUpdate() {
			return _causedByMulticastUpdate;
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
		 * Returns a dictionary of snapshots where the key is the entity name and the value an array of snapshots.
		 * 
		 * @param objects
		 * @param osc
		 * @return
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

	public static class MulticastSynchronizer {
		public static final int IDENTIFIER_LENGTH = 6;

		private static final int JOIN = 1;
		private static final int LEAVE = 2;
		private static final int INSERT = 3;
		private static final int UPDATE = 4;
		private static final int DELETE = 5;
		private static final int INVALIDATE = 6;
		private static final int TO_MANY_UPDATE = 6;

		private static final int BYTE_TYPE = 1;
		private static final int SHORT_TYPE = 2;
		private static final int INT_TYPE = 3;
		private static final int LONG_TYPE = 4;
		private static final int DATA_TYPE = 5;

		private IChangeListener _listener;
		private byte[] _identifier;
		private InetAddress _localBindAddress;
		private NetworkInterface _localNetworkInterface;
		private InetSocketAddress _multicastGroup;
		private int _multicastPort;
		private MulticastSocket _multicastSocket;
		private boolean _listening;
		private NSArray _includeEntityNames;
		private NSArray _excludeEntityNames;
		private NSArray _whitelist;
		private int _maxSendPacketSize;
		private int _maxReceivePacketSize;
		private NSMutableDictionary _incomingCacheChanges;

		public MulticastSynchronizer(byte[] identifier, InetAddress localBindAddress, String multicastGroup, int multicastPort, NSArray includeEntityNames, NSArray excludeEntityNames, NSArray whitelist, int maxPacketSize, IChangeListener listener) throws IOException {
			if (identifier.length != MulticastSynchronizer.IDENTIFIER_LENGTH) {
				throw new IllegalArgumentException("Multicast identifier must be only " + MulticastSynchronizer.IDENTIFIER_LENGTH + " bytes long.");
			}
			_incomingCacheChanges = new NSMutableDictionary();
			_identifier = identifier;
			_listener = listener;
			_multicastPort = multicastPort;
			_localBindAddress = localBindAddress;
			_includeEntityNames = includeEntityNames;
			_excludeEntityNames = excludeEntityNames;
			_whitelist = whitelist;
			_maxSendPacketSize = maxPacketSize;
			_maxReceivePacketSize = 2 * maxPacketSize;

			_localNetworkInterface = NetworkInterface.getByInetAddress(_localBindAddress);
			_multicastGroup = new InetSocketAddress(InetAddress.getByName(multicastGroup), _multicastPort);
			_multicastSocket = new MulticastSocket(null);
			_multicastSocket.setInterface(localBindAddress);
			_multicastSocket.setTimeToLive(4);
			_multicastSocket.setReuseAddress(true);
			_multicastSocket.bind(new InetSocketAddress(multicastPort));
		}

		public void join() throws IOException {
			if (log.isInfoEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + " joining.");
			}
			_multicastSocket.joinGroup(_multicastGroup, _localNetworkInterface);
			RefByteArrayOutputStream baos = new RefByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.write(_identifier);
			dos.writeInt(0);
			dos.writeShort(0);
			dos.writeShort(0);
			dos.writeByte(MulticastSynchronizer.JOIN);
			dos.flush();
			_multicastSocket.send(baos.createDatagramPacket());
		}

		public void leave() throws IOException {
			if (log.isInfoEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + " leaving.");
			}
			RefByteArrayOutputStream baos = new RefByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.write(_identifier);
			dos.writeInt(0);
			dos.writeShort(0);
			dos.writeShort(0);
			dos.writeByte(MulticastSynchronizer.LEAVE);
			dos.flush();
			_multicastSocket.send(baos.createDatagramPacket());
			_multicastSocket.leaveGroup(_multicastGroup, _localNetworkInterface);
			_listening = false;
		}

		public void listen() {
			Thread listenThread = new Thread(new Runnable() {
				public void run() {
					_listen();
				}
			});
			listenThread.start();
		}

		protected void _listen() {
			if (log.isInfoEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + " listening.");
			}
			_listening = true;
			byte[] buffer = new byte[_maxReceivePacketSize];
			while (_listening) {
				DatagramPacket receivePacket = new DatagramPacket(buffer, 0, buffer.length);
				try {
					_multicastSocket.receive(receivePacket);
					ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength());
					DataInputStream dis = new DataInputStream(bais);
					boolean processPacket = true;
					if (_whitelist != null) {
						InetAddress remoteAddress = receivePacket.getAddress();
						String remoteHostAddress = remoteAddress.getHostAddress();
						processPacket = _whitelist.containsObject(remoteHostAddress);
					}
					byte[] identifier = new byte[MulticastSynchronizer.IDENTIFIER_LENGTH];
					dis.readFully(identifier);
					if (processPacket && !Arrays.equals(identifier, _identifier)) {
						int transactionID = dis.readInt();
						short transactionNum = dis.readShort();
						short transactionSize = dis.readShort();
						String transactionIdentifierStr = ERXStringUtilities.byteArrayToHexString(identifier) + "-" + transactionID;
						RemoteChange remoteChange = (RemoteChange) _incomingCacheChanges.objectForKey(transactionIdentifierStr);
						if (remoteChange == null) {
							remoteChange = new RemoteChange(identifier, transactionID, transactionSize);
							_incomingCacheChanges.setObjectForKey(remoteChange, transactionIdentifierStr);
						}
						int messageType = dis.readByte();
						if (messageType == MulticastSynchronizer.JOIN) {
							if (log.isInfoEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") joined.");
							}
						}
						else if (messageType == MulticastSynchronizer.LEAVE) {
							if (log.isInfoEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") left.");
							}
						}
						else if (messageType == MulticastSynchronizer.INSERT) {
							EOGlobalID gid = readGID(receivePacket, dis);
							ERXDatabase.SnapshotInserted change = new ERXDatabase.SnapshotInserted(gid, NSDictionary.EmptyDictionary);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") inserted " + change);
							}
							remoteChange.addRemoteCacheChange(change);
						}
						else if (messageType == MulticastSynchronizer.UPDATE) {
							EOGlobalID gid = readGID(receivePacket, dis);
							ERXDatabase.SnapshotUpdated change = new ERXDatabase.SnapshotUpdated(gid, NSDictionary.EmptyDictionary);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") updated " + change);
							}
							remoteChange.addRemoteCacheChange(change);
						}
						else if (messageType == MulticastSynchronizer.DELETE) {
							EOGlobalID gid = readGID(receivePacket, dis);
							ERXDatabase.SnapshotDeleted change = new ERXDatabase.SnapshotDeleted(gid, NSDictionary.EmptyDictionary);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") deleted " + change);
							}
							remoteChange.addRemoteCacheChange(change);
						}
						else if (messageType == MulticastSynchronizer.TO_MANY_UPDATE) {
							EOGlobalID sourceGID = readGID(receivePacket, dis);
							String name = dis.readUTF();
							NSArray addedGIDs = readGIDs(receivePacket, dis);
							NSArray removedGIDs = readGIDs(receivePacket, dis);
							boolean removeAll = dis.readBoolean();
							ERXDatabase.ToManySnapshotUpdated change = new ERXDatabase.ToManySnapshotUpdated(sourceGID, name, addedGIDs, removedGIDs, removeAll);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") update to-many " + change);
							}
							remoteChange.addRemoteCacheChange(change);
						}
						else {
							throw new IllegalArgumentException("Unknown multicast message type #" + messageType + ".");
						}

						if (remoteChange.isComplete()) {
							_incomingCacheChanges.removeObjectForKey(transactionIdentifierStr);
							_listener.addChange(remoteChange);
						}

						// TODO: Sweep the _cacheChanges dictionary for expired partial cache updates. If a machine
						// crashes in the middle of a broadcast, it would leave half-open cache updates in all of the
						// multicast member _cacheChanges dictionaries.
					}
					else {
						if (log.isDebugEnabled()) {
							log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + "): skipping our own message");
						}
					}
				}
				catch (Throwable t) {
					ERXObjectStoreCoordinatorSynchronizer.log.error("Failed to read multicast notification.", t);
				}
			}
		}

		public void writeCacheChange(ERXDatabase.CacheChange cacheChange, int transactionID, short transactionNum, short transactionSize) throws IOException {
			// System.out.println("MulticastSynchronizer.writeCacheChange: Writing " + transactionID + ", " +
			// transactionNum + " of " + transactionSize);
			RefByteArrayOutputStream baos = new RefByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.write(_identifier);
			dos.writeInt(transactionID);
			dos.writeShort(transactionNum);
			dos.writeShort(transactionSize);
			if (cacheChange instanceof ERXDatabase.SnapshotInserted) {
				dos.writeByte(ERXObjectStoreCoordinatorSynchronizer.MulticastSynchronizer.INSERT);
				writeSnapshotCacheChange(dos, cacheChange);
			}
			else if (cacheChange instanceof ERXDatabase.SnapshotUpdated) {
				dos.writeByte(ERXObjectStoreCoordinatorSynchronizer.MulticastSynchronizer.UPDATE);
				writeSnapshotCacheChange(dos, cacheChange);
			}
			else if (cacheChange instanceof ERXDatabase.SnapshotDeleted) {
				dos.writeByte(ERXObjectStoreCoordinatorSynchronizer.MulticastSynchronizer.DELETE);
				writeSnapshotCacheChange(dos, cacheChange);
			}
			else if (cacheChange instanceof ERXDatabase.ToManySnapshotUpdated) {
				dos.writeByte(ERXObjectStoreCoordinatorSynchronizer.MulticastSynchronizer.TO_MANY_UPDATE);
				ERXDatabase.ToManySnapshotUpdated toManyChange = (ERXDatabase.ToManySnapshotUpdated) cacheChange;
				NSArray addedGIDs = toManyChange.addedGIDs();
				NSArray removedGIDs = toManyChange.removedGIDs();
				writeGID(dos, toManyChange.gid());
				dos.writeUTF(toManyChange.name());
				writeGIDs(dos, addedGIDs);
				if (toManyChange.removeAll()) {
					writeGIDs(dos, null);
					dos.writeBoolean(true);
				}
				else {
					writeGIDs(dos, removedGIDs);
					dos.writeBoolean(false);
				}
			}
			dos.flush();
			_multicastSocket.send(baos.createDatagramPacket());
			if (log.isDebugEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + ": Writing " + cacheChange);
			}
		}

		protected void writeSnapshotCacheChange(DataOutputStream dos, ERXDatabase.CacheChange cacheChange) throws IOException {
			writeGID(dos, cacheChange.gid());
		}

		protected void writeGIDs(DataOutputStream dos, NSArray gids) throws IOException {
			int count = (gids == null) ? 0 : gids.count();
			dos.writeByte(count);
			if (count > 0) {
				for (Enumeration gidsEnum = gids.objectEnumerator(); gidsEnum.hasMoreElements();) {
					EOGlobalID gid = (EOGlobalID) gidsEnum.nextElement();
					writeGID(dos, gid);
				}
			}
		}

		protected void writeGID(DataOutputStream dos, EOGlobalID gid) throws IOException {
			EOKeyGlobalID keyGID = (EOKeyGlobalID) gid;
			String entityName = keyGID.entityName();
			dos.writeUTF(entityName);
			writeGIDKeys(dos, keyGID);
		}

		protected void writeGIDKeys(DataOutputStream dos, EOKeyGlobalID gid) throws IOException {
			Object[] values = gid._keyValuesNoCopy();
			dos.writeByte(values.length);
			for (int keyNum = 0; keyNum < values.length; keyNum++) {
				writeKey(dos, values[keyNum]);
			}
		}

		protected void writeKey(DataOutputStream dos, Object key) throws IOException {
			if (key instanceof Byte) {
				dos.writeByte(MulticastSynchronizer.BYTE_TYPE);
				dos.writeShort(((Byte) key).byteValue());
			}
			else if (key instanceof Short) {
				dos.writeByte(MulticastSynchronizer.SHORT_TYPE);
				dos.writeShort(((Short) key).shortValue());
			}
			else if (key instanceof Integer) {
				dos.writeByte(MulticastSynchronizer.INT_TYPE);
				dos.writeInt(((Integer) key).intValue());
			}
			else if (key instanceof Long) {
				dos.writeByte(MulticastSynchronizer.LONG_TYPE);
				dos.writeLong(((Long) key).longValue());
			}
			else if (key instanceof NSData) {
				NSData data = (NSData) key;
				dos.writeByte(MulticastSynchronizer.DATA_TYPE);
				dos.writeByte(data.length());
				data.writeToStream(dos);
			}
			else {
				throw new IllegalArgumentException("MulticastSynchronizer can't handle key '" + key + "'.");
			}
		}

		protected NSArray readGIDs(DatagramPacket packet, DataInputStream dis) throws IOException {
			NSMutableArray gids = new NSMutableArray();
			int gidCount = dis.readByte();
			for (int gidNum = 0; gidNum < gidCount; gidNum++) {
				EOGlobalID gid = readGID(packet, dis);
				gids.addObject(gid);
			}
			return gids;
		}

		protected EOGlobalID readGID(DatagramPacket packet, DataInputStream dis) throws IOException {
			String entityName = dis.readUTF();
			EOEntityClassDescription classDescription = (EOEntityClassDescription) EOEntityClassDescription.classDescriptionForEntityName(entityName);
			return _readGID(classDescription, entityName, dis);
		}

		protected EOGlobalID _readGID(EOEntityClassDescription classDescription, String entityName, DataInputStream dis) throws IOException {
			EOKeyGlobalID gid;
			int keyCount = dis.readByte();
			if (keyCount == -1) {
				gid = null;
			}
			else {
				Object[] keys = new Object[keyCount];
				for (int i = 0; i < keyCount; i++) {
					keys[i] = readKey(dis);
				}
				gid = classDescription._globalIDWithEntityName(entityName, keys);
			}
			return gid;
		}

		protected Object readKey(DataInputStream dis) throws IOException {
			Object obj;
			int keyType = dis.readByte();
			if (keyType == MulticastSynchronizer.BYTE_TYPE) {
				obj = new Byte(dis.readByte());
			}
			else if (keyType == MulticastSynchronizer.SHORT_TYPE) {
				obj = new Short(dis.readShort());
			}
			else if (keyType == MulticastSynchronizer.INT_TYPE) {
				obj = new Integer(dis.readInt());
			}
			else if (keyType == MulticastSynchronizer.LONG_TYPE) {
				obj = new Long(dis.readLong());
			}
			else if (keyType == MulticastSynchronizer.DATA_TYPE) {
				int size = dis.readByte();
				byte[] data = new byte[size];
				dis.readFully(data);
				obj = new NSData(data);
			}
			else {
				throw new IllegalArgumentException("Unknown key type #" + keyType + ".");
			}
			return obj;
		}

		public boolean shouldSynchronizeEntity(String entityName) {
			boolean shouldSynchronizeEntity = true;
			if (_includeEntityNames != null) {
				shouldSynchronizeEntity = _includeEntityNames.containsObject(entityName);
			}
			if (shouldSynchronizeEntity && _excludeEntityNames != null) {
				shouldSynchronizeEntity = !_excludeEntityNames.containsObject(entityName);
			}
			return shouldSynchronizeEntity;
		}

		public NSDictionary globalIDsGroupedByEntity(NSArray gids) {
			if (gids == null) {
				return NSDictionary.EmptyDictionary;
			}
			NSMutableDictionary result = new NSMutableDictionary();
			Enumeration gidsEnum = gids.objectEnumerator();
			while (gidsEnum.hasMoreElements()) {
				EOKeyGlobalID gid = (EOKeyGlobalID) gidsEnum.nextElement();
				String entityName = gid.entityName();
				if (shouldSynchronizeEntity(entityName)) {
					NSMutableSet globalIDsForEntity = (NSMutableSet) result.objectForKey(entityName);
					if (globalIDsForEntity == null) {
						globalIDsForEntity = new NSMutableSet();
						result.setObjectForKey(globalIDsForEntity, entityName);
					}
					globalIDsForEntity.addObject(gid);
				}
			}
			return result.immutableClone();
		}

		protected class RefByteArrayOutputStream extends ByteArrayOutputStream {
			public byte[] buffer() {
				return buf;
			}

			public DatagramPacket createDatagramPacket() throws SocketException {
				return new DatagramPacket(buf, 0, count, _multicastGroup);
			}
		}
	}
}