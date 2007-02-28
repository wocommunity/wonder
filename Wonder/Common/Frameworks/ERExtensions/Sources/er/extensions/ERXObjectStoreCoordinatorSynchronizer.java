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
import com.webobjects.foundation.NSSet;

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
		boolean multicastEnabled = ERXProperties.booleanForKeyWithDefault("er.extensions.multicastSynchronizer.enabled", false);
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
			}
			else {
				log.error("Adding same coodinator twice!");
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

	public void publishChange(NSNotification n) {
		if (_coordinators.size() >= 2 || _multicastSynchronizer != null) {
			NSDictionary userInfo = n.userInfo();
			if (userInfo == null || userInfo.valueForKey(ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY) == null) {
				LocalChange changes = new LocalChange((EOObjectStoreCoordinator) n.object(), userInfo);
				_queueThread.addChange(changes);
			}
		}
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

		private class MulticastWriteDeleteSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
				try {
					NSArray gids = snapshots.allKeys();
					_multicastSynchronizer.writeDeleted(dbc, database, gids);
				}
				catch (Throwable t) {
					t.printStackTrace();
					ERXObjectStoreCoordinatorSynchronizer.log.error("Failed to send multicast notification.", t);
				}
			}
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

		private class MulticastWriteUpdateSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
				try {
					NSArray gids = snapshots.allKeys();
					_multicastSynchronizer.writeUpdated(dbc, database, gids);
				}
				catch (Throwable t) {
					t.printStackTrace();
					ERXObjectStoreCoordinatorSynchronizer.log.error("Failed to send multicast notification.", t);
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

		private class MulticastWriteInsertSnapshotProcessor extends RelationshipSnapshotProcessor {
			public void processGID(EODatabase database, EOGlobalID sourceGID, Object context) {
				NSMutableDictionary gidsToOneRelationships = (NSMutableDictionary) context;
				NSMutableDictionary toOneRelationships = (NSMutableDictionary) gidsToOneRelationships.objectForKey(sourceGID);
				if (toOneRelationships == null) {
					gidsToOneRelationships.setObjectForKey(new NSMutableDictionary(), sourceGID);
				}
			}

			public void processRelationship(EODatabase database, EOGlobalID sourceGID, EORelationship sourceRelationship, EOGlobalID destGID, EORelationship inverseRelationship, Object context) {
				NSMutableDictionary gidsToOneRelationships = (NSMutableDictionary) context;
				NSMutableDictionary toOneRelationships = (NSMutableDictionary) gidsToOneRelationships.objectForKey(sourceGID);
				toOneRelationships.setObjectForKey(destGID, sourceRelationship.name());
			}

			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
				try {
					NSArray gids = snapshots.allKeys();
					NSMutableDictionary gidsToOneRelationships = new NSMutableDictionary();
					processRelationships(database, gids, snapshots, gidsToOneRelationships);
					_multicastSynchronizer.writeInserted(dbc, database, gidsToOneRelationships);
				}
				catch (Throwable t) {
					t.printStackTrace();
					ERXObjectStoreCoordinatorSynchronizer.log.error("Failed to send multicast notification.", t);
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

		private class MulticastWriteInvalidateSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary snapshots) {
				try {
					NSArray gids = snapshots.allKeys();
					_multicastSynchronizer.writeInvalidated(dbc, database, gids);
				}
				catch (Throwable t) {
					t.printStackTrace();
					ERXObjectStoreCoordinatorSynchronizer.log.error("Failed to send multicast notification.", t);
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

		private abstract class SnapshotGIDProcessor {
			public abstract void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary gidsToOneRelationships);
		}

		private class MulticastReadDeleteSnapshotGIDProcessor extends SnapshotGIDProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary gidsToOneRelationships) {
				database.forgetSnapshotsForGlobalIDs(gidsToOneRelationships.allKeys());
				if (log.isDebugEnabled()) {
					log.debug("multicast delete: " + gidsToOneRelationships);
				}
			}
		}

		private class MulticastReadUpdateSnapshotGIDProcessor extends SnapshotGIDProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary gidsToOneRelationships) {
				NSMutableArray existingGIDs = new NSMutableArray();
				Enumeration gidsEnum = gidsToOneRelationships.allKeys().objectEnumerator();
				while (gidsEnum.hasMoreElements()) {
					EOGlobalID gid = (EOGlobalID) gidsEnum.nextElement();
					NSDictionary snapshot = database.snapshotForGlobalID(gid);
					if (snapshot != null) {
						existingGIDs.addObject(gid);
					}
				}
				EOEditingContext editingContext = ERXEC.newEditingContext();
				editingContext.lock();
				try {
					ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(editingContext, existingGIDs, true);
				}
				finally {
					editingContext.unlock();
				}
				// database.forgetSnapshotsForGlobalIDs(gids);
				if (log.isDebugEnabled()) {
					log.debug("multicast update: " + gidsToOneRelationships);
				}
			}
		}

		private class MulticastReadInsertSnapshotGIDProcessor extends SnapshotGIDProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary gidsToOneRelationships) {
				NSMutableArray destGIDs = new NSMutableArray();
				NSArray gids = gidsToOneRelationships.allKeys();
				Enumeration gidsEnum = gids.objectEnumerator();
				while (gidsEnum.hasMoreElements()) {
					EOKeyGlobalID sourceGID = (EOKeyGlobalID) gidsEnum.nextElement();
					EOEntity entity = database.entityNamed(sourceGID.entityName());
					NSDictionary toOneRelationships = (NSDictionary) gidsToOneRelationships.objectForKey(sourceGID);
					Enumeration relationshipNamesEnum = toOneRelationships.keyEnumerator();
					while (relationshipNamesEnum.hasMoreElements()) {
						String relationshipName = (String) relationshipNamesEnum.nextElement();
						EORelationship relationship = entity.relationshipNamed(relationshipName);
						EOGlobalID destGID = (EOGlobalID)toOneRelationships.objectForKey(relationshipName);
						EORelationship inverseRelationship = relationship.inverseRelationship();
						EOEntity destEntity = inverseRelationship.entity();
						String inverseRelationshipName = inverseRelationship.name();
						NSArray inverseRelationshipGIDs = database.snapshotForSourceGlobalID(destGID, inverseRelationshipName);
						if (inverseRelationshipGIDs != null) {
							destGIDs.addObject(destGID);
							database.recordSnapshotForSourceGlobalID(inverseRelationshipGIDs.arrayByAddingObject(sourceGID), destGID, inverseRelationshipName);
						}
					}
				}

				NSMutableDictionary userInfo = new NSMutableDictionary(gids, EODatabaseContext.InsertedKey);
				userInfo.setObjectForKey(destGIDs, EODatabaseContext.UpdatedKey);
				userInfo.setObjectForKey(Boolean.TRUE, ERXObjectStoreCoordinatorSynchronizer.SYNCHRONIZER_KEY);
				NSNotificationCenter.defaultCenter().postNotification(EODatabaseContext.ObjectsChangedInStoreNotification, dbc, userInfo);

				if (log.isDebugEnabled()) {
					log.debug("multicast insert: " + gidsToOneRelationships);
				}
			}
		}

		private class MulticastReadInvalidateSnapshotGIDProcessor extends SnapshotGIDProcessor {
			public void processSnapshots(EODatabaseContext dbc, EODatabase database, NSDictionary gidsToOneRelationships) {
				// database.recordSnapshots(snapshots);
				if (log.isDebugEnabled()) {
					log.debug("multicast invalidate: " + gidsToOneRelationships);
				}
			}
		}

		private List _elements = new LinkedList();
		private SnapshotProcessor _deleteProcessor = new DeleteSnapshotProcessor();
		private SnapshotProcessor _insertProcessor = new InsertSnapshotProcessor();
		private SnapshotProcessor _updateProcessor = new UpdateSnapshotProcessor();
		private SnapshotProcessor _invalidateProcessor = new InvalidateSnapshotProcessor();
		private SnapshotProcessor _multicastWriteDeleteProcessor = new MulticastWriteDeleteSnapshotProcessor();
		private SnapshotProcessor _multicastWriteInsertProcessor = new MulticastWriteInsertSnapshotProcessor();
		private SnapshotProcessor _multicastWriteUpdateProcessor = new MulticastWriteUpdateSnapshotProcessor();
		private SnapshotProcessor _multicastWriteInvalidateProcessor = new MulticastWriteInvalidateSnapshotProcessor();
		private SnapshotGIDProcessor _multicastReadDeleteGIDProcessor = new MulticastReadDeleteSnapshotGIDProcessor();
		private SnapshotGIDProcessor _multicastReadInsertGIDProcessor = new MulticastReadInsertSnapshotGIDProcessor();
		private SnapshotGIDProcessor _multicastReadUpdateGIDProcessor = new MulticastReadUpdateSnapshotGIDProcessor();
		private SnapshotGIDProcessor _multicastReadInvalidateGIDProcessor = new MulticastReadInvalidateSnapshotGIDProcessor();

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
			NSMutableDictionary snapshotsByGlobalID = new NSMutableDictionary();
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
		protected void process(EOObjectStoreCoordinator sender, SnapshotProcessor processor, SnapshotProcessor multicastWriteProcessor, NSDictionary changesByEntity, String userInfoKey) {
			NSMutableDictionary dbcs = new NSMutableDictionary();
			for (Enumeration oscs = _synchronizer.coordinators(); oscs.hasMoreElements();) {
				EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
				if (osc != sender) {
					_process(sender, osc, dbcs, processor, changesByEntity, userInfoKey);
				}
				else if (_multicastSynchronizer != null) {
					_process(sender, osc, dbcs, multicastWriteProcessor, changesByEntity, userInfoKey);
				}
			}
		}

		/**
		 * @param dictionary
		 * @param sender
		 */
		protected void process(SnapshotGIDProcessor processor, NSDictionary changesByEntity) {
			if (changesByEntity.count() > 0) {
				NSMutableDictionary dbcs = new NSMutableDictionary();
				for (Enumeration oscs = _synchronizer.coordinators(); oscs.hasMoreElements();) {
					EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
					EOKeyGlobalID firstGID = (EOKeyGlobalID) changesByEntity.allKeys().objectAtIndex(0);
					String entityName = firstGID.entityName();
					EOEntity entity = EOModelGroup.modelGroupForObjectStoreCoordinator(osc).entityNamed(entityName);
					EODatabaseContext dbc = ERXEOAccessUtilities.databaseContextForEntityNamed(osc, entityName);
					EODatabase database = dbc.database();
					EODatabaseContext._EOAssertSafeMultiThreadedAccess(dbc);
					dbc.lock();
					try {
						processor.processSnapshots(dbc, database, changesByEntity);
					}
					finally {
						dbc.unlock();
					}
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
						LocalChange localChanges = (LocalChange) changes;
						EOObjectStoreCoordinator sender = localChanges.coordinator();
						process(sender, _deleteProcessor, _multicastWriteDeleteProcessor, localChanges.deleted(), EODatabaseContext.DeletedKey);
						process(sender, _insertProcessor, _multicastWriteInsertProcessor, localChanges.inserted(), EODatabaseContext.InsertedKey);
						process(sender, _updateProcessor, _multicastWriteUpdateProcessor, localChanges.updated(), EODatabaseContext.UpdatedKey);
						process(sender, _invalidateProcessor, _multicastWriteInvalidateProcessor, localChanges.invalidated(), EODatabaseContext.InvalidatedKey);
					}
					else if (changes instanceof RemoteChange) {
						RemoteChange remoteChanges = (RemoteChange) changes;
						process(_multicastReadDeleteGIDProcessor, remoteChanges.deletedGIDsToOneRelationships());
						process(_multicastReadInsertGIDProcessor, remoteChanges.insertedGIDsToOneRelationships());
						process(_multicastReadUpdateGIDProcessor, remoteChanges.updatedGIDsToOneRelationships());
						process(_multicastReadInvalidateGIDProcessor, remoteChanges.invalidatedGIDsToOneRelationships());
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
		private NSDictionary _deletedGIDsToOneRelationships;
		private NSDictionary _updatedGIDsToOneRelationships;
		private NSDictionary _insertedGIDsToOneRelationships;
		private NSDictionary _invalidatedGIDsToOneRelationships;

		public RemoteChange(NSDictionary deletedGIDsToOneRelationships, NSDictionary updatedGIDsToOneRelationships, NSDictionary insertedGIDsToOneRelationships, NSDictionary invalidatedGIDsToOneRelationships) {
			_deletedGIDsToOneRelationships = deletedGIDsToOneRelationships;
			_updatedGIDsToOneRelationships = updatedGIDsToOneRelationships;
			_insertedGIDsToOneRelationships = insertedGIDsToOneRelationships;
			_invalidatedGIDsToOneRelationships = invalidatedGIDsToOneRelationships;
		}

		public NSDictionary deletedGIDsToOneRelationships() {
			return _deletedGIDsToOneRelationships;
		}

		public NSDictionary updatedGIDsToOneRelationships() {
			return _updatedGIDsToOneRelationships;
		}

		public NSDictionary insertedGIDsToOneRelationships() {
			return _insertedGIDsToOneRelationships;
		}

		public NSDictionary invalidatedGIDsToOneRelationships() {
			return _invalidatedGIDsToOneRelationships;
		}
	}

	/**
	 * Holds a change notification (one transaction).
	 */
	public static class LocalChange extends Change {
		private EOObjectStoreCoordinator _coordinator;
		private NSDictionary _inserted;
		private NSDictionary _updated;
		private NSDictionary _deleted;
		private NSDictionary _invalidated;
		private NSArray _deletedGIDs;
		private NSArray _updatedGIDs;
		private NSArray _insertedGIDs;
		private NSArray _invalidatedGIDs;

		public LocalChange(EOObjectStoreCoordinator osc, NSDictionary userInfo) {
			_coordinator = osc;
			_deletedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.DeletedKey);
			_updatedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.UpdatedKey);
			_insertedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.InsertedKey);
			_invalidatedGIDs = (NSArray) userInfo.objectForKey(EOObjectStore.InvalidatedKey);
			_deleted = snapshotsGroupedByEntity(_deletedGIDs, _coordinator);
			_updated = snapshotsGroupedByEntity(_updatedGIDs, _coordinator);
			_inserted = snapshotsGroupedByEntity(_insertedGIDs, _coordinator);
			_invalidated = snapshotsGroupedByEntity(_invalidatedGIDs, _coordinator);
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
				EOGlobalID gid = (EOGlobalID)gids.nextElement();
				if(gid instanceof EOKeyGlobalID) {
					EOKeyGlobalID globalID = (EOKeyGlobalID)gid;
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

		public MulticastSynchronizer(byte[] identifier, InetAddress localBindAddress, String multicastGroup, int multicastPort, NSArray includeEntityNames, NSArray excludeEntityNames, NSArray whitelist, int maxPacketSize, IChangeListener listener) throws IOException {
			if (identifier.length != MulticastSynchronizer.IDENTIFIER_LENGTH) {
				throw new IllegalArgumentException("Multicast identifier must be only " + MulticastSynchronizer.IDENTIFIER_LENGTH + " bytes long.");
			}
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
							NSDictionary gidToOneRelationships = readGIDs(receivePacket, dis);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") inserted " + gidToOneRelationships);
							}
							RemoteChange changes = new RemoteChange(NSDictionary.EmptyDictionary, NSDictionary.EmptyDictionary, gidToOneRelationships, NSDictionary.EmptyDictionary);
							_listener.addChange(changes);
						}
						else if (messageType == MulticastSynchronizer.UPDATE) {
							NSDictionary gidToOneRelationships = readGIDs(receivePacket, dis);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") updated " + gidToOneRelationships);
							}
							RemoteChange changes = new RemoteChange(NSDictionary.EmptyDictionary, gidToOneRelationships, NSDictionary.EmptyDictionary, NSDictionary.EmptyDictionary);
							_listener.addChange(changes);
						}
						else if (messageType == MulticastSynchronizer.DELETE) {
							NSDictionary gidToOneRelationships = readGIDs(receivePacket, dis);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") deleted " + gidToOneRelationships);
							}
							RemoteChange changes = new RemoteChange(gidToOneRelationships, NSDictionary.EmptyDictionary, NSDictionary.EmptyDictionary, NSDictionary.EmptyDictionary);
							_listener.addChange(changes);
						}
						else if (messageType == MulticastSynchronizer.INVALIDATE) {
							NSDictionary gidToOneRelationships = readGIDs(receivePacket, dis);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") invalidated " + gidToOneRelationships);
							}
							RemoteChange changes = new RemoteChange(NSDictionary.EmptyDictionary, NSDictionary.EmptyDictionary, NSDictionary.EmptyDictionary, gidToOneRelationships);
							_listener.addChange(changes);
						}
						else {
							throw new IllegalArgumentException("Unknown multicast message type #" + messageType + ".");
						}
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

		public void writeInserted(EODatabaseContext context, EODatabase database, NSDictionary gidToOneRelationships) throws IOException {
			writeGIDs(MulticastSynchronizer.INSERT, gidToOneRelationships.allKeys(), gidToOneRelationships);
			if (log.isDebugEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + ": Inserted " + gidToOneRelationships);
			}
		}

		public void writeUpdated(EODatabaseContext context, EODatabase database, NSArray gids) throws IOException {
			writeGIDs(MulticastSynchronizer.UPDATE, gids, null);
			if (log.isDebugEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + ": Updated " + gids);
			}
		}

		public void writeDeleted(EODatabaseContext context, EODatabase database, NSArray gids) throws IOException {
			writeGIDs(MulticastSynchronizer.DELETE, gids, null);
			if (log.isDebugEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + ": Deleted " + gids);
			}
		}

		public void writeInvalidated(EODatabaseContext context, EODatabase database, NSArray gids) throws IOException {
			// MS: Notify on invalidate?
			//writeGIDs(MulticastSynchronizer.INVALIDATE, gids, null);
			if (log.isDebugEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + ": Invalidated " + gids);
			}
		}

		protected void writeGIDs(int messageType, NSArray gids, NSDictionary gidsToOneRelationships) throws IOException {
			NSDictionary gidsByEntity = globalIDsGroupedByEntity(gids);
			Enumeration entityNamesEnum = gidsByEntity.keyEnumerator();
			while (entityNamesEnum.hasMoreElements()) {
				String entityName = (String) entityNamesEnum.nextElement();
				NSSet entityGids = (NSSet) gidsByEntity.objectForKey(entityName);
				Enumeration entityGidsEnum = entityGids.objectEnumerator();
				while (entityGidsEnum.hasMoreElements()) {
					RefByteArrayOutputStream baos = new RefByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);
					dos.write(_identifier);
					dos.writeByte(messageType);
					dos.writeUTF(entityName);
					while (entityGidsEnum.hasMoreElements() && baos.size() < _maxSendPacketSize) {
						EOKeyGlobalID gid = (EOKeyGlobalID) entityGidsEnum.nextElement();
						writeGIDKeys(dos, gid);
						if (gidsToOneRelationships != null) {
							NSDictionary gidToOneRelationships = (NSDictionary) gidsToOneRelationships.objectForKey(gid);
							int toOneRelationshipCount = gidToOneRelationships.count();
							dos.writeByte(toOneRelationshipCount);
							Enumeration toOneRelationshipKeyEnum = gidToOneRelationships.keyEnumerator();
							while (toOneRelationshipKeyEnum.hasMoreElements()) {
								String toOneRelationshipKey = (String) toOneRelationshipKeyEnum.nextElement();
								dos.writeUTF(toOneRelationshipKey);
								EOKeyGlobalID toOneGID = (EOKeyGlobalID) gidToOneRelationships.objectForKey(toOneRelationshipKey);
								dos.writeUTF(toOneGID.entityName());
								writeGIDKeys(dos, toOneGID);
							}
						}
						else {
							dos.writeByte(0);
						}
					}
					dos.flush();
					if (baos.size() > _maxReceivePacketSize) {
						throw new IllegalStateException("This send packet has overrun the size that other instances can receive.  Increase the packet size setting to prevent this problem.  Oh and your app is in a terrible state right now.");
					}
					_multicastSocket.send(baos.createDatagramPacket());
				}
			}
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

		protected NSDictionary readGIDs(DatagramPacket packet, DataInputStream dis) throws IOException {
			NSMutableDictionary gidToOneRelationships = new NSMutableDictionary();
			String entityName = dis.readUTF();
			EOEntityClassDescription classDescription = (EOEntityClassDescription) EOEntityClassDescription.classDescriptionForEntityName(entityName);
			while (dis.available() > 0) {
				EOGlobalID gid = readGID(classDescription, entityName, dis);
				NSMutableDictionary toOneRelationships = new NSMutableDictionary();
				gidToOneRelationships.setObjectForKey(toOneRelationships, gid);
				int toOneRelationshipCount = dis.readByte();
				for (int i = 0; i < toOneRelationshipCount; i++) {
					String toOneRelationshipKey = dis.readUTF();
					String toOneEntityName = dis.readUTF();
					EOEntityClassDescription toOneClassDescription = (EOEntityClassDescription) EOEntityClassDescription.classDescriptionForEntityName(toOneEntityName);
					EOGlobalID toOneGID = readGID(toOneClassDescription, toOneEntityName, dis);
					if (toOneGID != null) {
						toOneRelationships.setObjectForKey(toOneGID, toOneRelationshipKey);
					}
				}
			}
			return gidToOneRelationships;
		}

		protected EOGlobalID readGID(EOEntityClassDescription classDescription, String entityName, DataInputStream dis) throws IOException {
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