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
import java.util.Vector;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOModelGroup;
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

	private Vector _coordinators;
	private ProcessChangesQueue _queueThread;
	private MulticastSynchronizer _multicastSynchronizer;

	private ERXObjectStoreCoordinatorSynchronizer() {
		_coordinators = new Vector();
		_queueThread = new ProcessChangesQueue();
		new Thread(_queueThread).start();
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("objectStoreWasAdded", new Class[] { NSNotification.class }), EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification, null);
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("objectStoreWasRemoved", new Class[] { NSNotification.class }), EOObjectStoreCoordinator.CooperatingObjectStoreWasRemovedNotification, null);

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
			LocalChange changes = new LocalChange((EOObjectStoreCoordinator) n.object(), n.userInfo());
			_queueThread.addChange(changes);
		}
	}

	private Enumeration coordinators() {
		return _coordinators.elements();
	}

	/**
	 * Thread and locking safe implementation to propagate the changes from one EOF stack to another.
	 */
	private class ProcessChangesQueue implements Runnable, IChangeListener {
		private abstract class SnapshotProcessor {
			public abstract void processSnapshots(EODatabase database, NSDictionary snapshots);
		}

		private class DeleteSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabase database, NSDictionary snapshots) {
				database.forgetSnapshotsForGlobalIDs(snapshots.allKeys());
				if (log.isDebugEnabled()) {
					log.debug("forget: " + snapshots);
				}
			}
		}

		private class UpdateSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabase database, NSDictionary snapshots) {
				database.forgetSnapshotsForGlobalIDs(snapshots.allKeys());
				database.recordSnapshots(snapshots);
				if (log.isDebugEnabled()) {
					log.debug("update: " + snapshots);
				}
			}
		}

		private class InsertSnapshotProcessor extends SnapshotProcessor {
			public void processSnapshots(EODatabase database, NSDictionary snapshots) {
				database.recordSnapshots(snapshots);
				if (log.isDebugEnabled()) {
					log.debug("insert: " + snapshots);
				}
			}
		}

		private abstract class SnapshotGIDProcessor {
			public abstract void processSnapshots(EODatabase database, NSArray gids);
		}

		private class DeleteSnapshotGIDProcessor extends SnapshotGIDProcessor {
			public void processSnapshots(EODatabase database, NSArray gids) {
				database.forgetSnapshotsForGlobalIDs(gids);
				if (log.isDebugEnabled()) {
					log.debug("forget gids: " + gids);
				}
			}
		}

		private class UpdateSnapshotGIDProcessor extends SnapshotGIDProcessor {
			public void processSnapshots(EODatabase database, NSArray gids) {
				NSMutableArray existingGIDs = new NSMutableArray();
				Enumeration gidsEnum = gids.objectEnumerator();
				while (gidsEnum.hasMoreElements()) {
					EOGlobalID gid = (EOGlobalID)gidsEnum.nextElement();
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
				//database.forgetSnapshotsForGlobalIDs(gids);
				if (log.isDebugEnabled()) {
					log.debug("update gids: " + gids);
				}
			}
		}

		private class InsertSnapshotGIDProcessor extends SnapshotGIDProcessor {
			public void processSnapshots(EODatabase database, NSArray gids) {
				// database.recordSnapshots(snapshots);
				if (log.isDebugEnabled()) {
					log.debug("insert: " + gids);
				}
			}
		}

		private List _elements = new LinkedList();
		private SnapshotProcessor _deleteProcessor = new DeleteSnapshotProcessor();
		private SnapshotProcessor _insertProcessor = new InsertSnapshotProcessor();
		private SnapshotProcessor _updateProcessor = new UpdateSnapshotProcessor();
		private SnapshotGIDProcessor _deleteGIDProcessor = new DeleteSnapshotGIDProcessor();
		private SnapshotGIDProcessor _insertGIDProcessor = new InsertSnapshotGIDProcessor();
		private SnapshotGIDProcessor _updateGIDProcessor = new UpdateSnapshotGIDProcessor();

		private ProcessChangesQueue() {
			Thread.currentThread().setName("ProcessChangesQueue");
		}

		public void addChange(Change changes) {
			synchronized (_elements) {
				_elements.add(changes);
				_elements.notify();
			}
		}

		/**
		 * @param dictionary
		 * @param sender
		 */
		protected void process(EOObjectStoreCoordinator sender, SnapshotProcessor processor, NSDictionary changesByEntity) {
			NSMutableDictionary dbcs = new NSMutableDictionary();
			for (Enumeration oscs = _synchronizer.coordinators(); oscs.hasMoreElements();) {
				EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
				if (osc != sender) {
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
								processor.processSnapshots(database, snapshotsByGlobalID);
							}
							finally {
								dbc.unlock();
							}
						}
					}
				}
			}
		}

		/**
		 * @param dictionary
		 * @param sender
		 */
		protected void process(SnapshotGIDProcessor processor, NSArray changesByEntity) {
			if (changesByEntity.count() > 0) {
				NSMutableDictionary dbcs = new NSMutableDictionary();
				for (Enumeration oscs = _synchronizer.coordinators(); oscs.hasMoreElements();) {
					EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) oscs.nextElement();
					EOKeyGlobalID firstGID = (EOKeyGlobalID) changesByEntity.objectAtIndex(0);
					String entityName = firstGID.entityName();
					EOEntity entity = EOModelGroup.modelGroupForObjectStoreCoordinator(osc).entityNamed(entityName);
					EODatabaseContext dbc = ERXEOAccessUtilities.databaseContextForEntityNamed(osc, entityName);
					EODatabase database = dbc.database();
					EODatabaseContext._EOAssertSafeMultiThreadedAccess(dbc);
					dbc.lock();
					try {
						processor.processSnapshots(database, changesByEntity);
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

						process(sender, _deleteProcessor, localChanges.deleted());
						process(sender, _insertProcessor, localChanges.inserted());
						process(sender, _updateProcessor, localChanges.updated());

						if (_multicastSynchronizer != null) {
							try {
								_multicastSynchronizer.writeChanges(localChanges);
							}
							catch (Throwable t) {
								t.printStackTrace();
								ERXObjectStoreCoordinatorSynchronizer.log.error("Failed to send multicast notification.", t);
							}
						}
					}
					else if (changes instanceof RemoteChange) {
						RemoteChange remoteChanges = (RemoteChange) changes;
						process(_deleteGIDProcessor, remoteChanges.deletedGIDs());
						process(_insertGIDProcessor, remoteChanges.insertedGIDs());
						process(_updateGIDProcessor, remoteChanges.updatedGIDs());
					}
				}
			}
		}
	}

	public static class Change {
		private NSArray _deletedGIDs;
		private NSArray _updatedGIDs;
		private NSArray _insertedGIDs;

		public Change(NSArray deletedGIDs, NSArray updatedGIDs, NSArray insertedGIDs) {
			_deletedGIDs = deletedGIDs;
			_updatedGIDs = updatedGIDs;
			_insertedGIDs = insertedGIDs;
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
	}

	public static class RemoteChange extends Change {
		public RemoteChange(NSArray deletedGIDs, NSArray updatedGIDs, NSArray insertedGIDs) {
			super(deletedGIDs, updatedGIDs, insertedGIDs);
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

		public LocalChange(EOObjectStoreCoordinator osc, NSDictionary userInfo) {
			super((NSArray) userInfo.objectForKey(EOObjectStore.DeletedKey), (NSArray) userInfo.objectForKey(EOObjectStore.UpdatedKey), (NSArray) userInfo.objectForKey(EOObjectStore.InsertedKey));
			_coordinator = osc;
			_deleted = snapshotsGroupedByEntity(deletedGIDs(), _coordinator);
			_updated = snapshotsGroupedByEntity(updatedGIDs(), _coordinator);
			_inserted = snapshotsGroupedByEntity(insertedGIDs(), _coordinator);
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
				EOKeyGlobalID globalID = (EOKeyGlobalID) gids.nextElement();
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
		private int _maxPacketSize;

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
			_maxPacketSize = maxPacketSize;

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
			byte[] buffer = new byte[_maxPacketSize];
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
							NSArray gids = readGIDs(receivePacket, dis);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") inserted " + gids);
							}
							RemoteChange changes = new RemoteChange(NSArray.EmptyArray, NSArray.EmptyArray, gids);
							_listener.addChange(changes);
						}
						else if (messageType == MulticastSynchronizer.UPDATE) {
							NSArray gids = readGIDs(receivePacket, dis);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") updated " + gids);
							}
							RemoteChange changes = new RemoteChange(NSArray.EmptyArray, gids, NSArray.EmptyArray);
							_listener.addChange(changes);
						}
						else if (messageType == MulticastSynchronizer.DELETE) {
							NSArray gids = readGIDs(receivePacket, dis);
							if (log.isDebugEnabled()) {
								log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(identifier) + " (" + receivePacket.getAddress() + ") deleted " + gids);
							}
							RemoteChange changes = new RemoteChange(gids, NSArray.EmptyArray, NSArray.EmptyArray);
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

		public void writeChanges(LocalChange changes) throws IOException {
			writeGIDs(MulticastSynchronizer.DELETE, changes.deletedGIDs());
			writeGIDs(MulticastSynchronizer.INSERT, changes.insertedGIDs());
			writeGIDs(MulticastSynchronizer.UPDATE, changes.updatedGIDs());
		}

		protected void writeGIDs(int messageType, NSArray gids) throws IOException {
			int maxPacketSize = _maxPacketSize - 64; // MS: Give ourselves a little leg room so we don't overrun ...
														// it should be smarter about this
			int count = 0;
			RefByteArrayOutputStream baos = new RefByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			boolean packetHeaderWritten = false;
			boolean firstPacket = true;
			NSDictionary gidsByEntity = globalIDsGroupedByEntity(gids);
			Enumeration entityNamesEnum = gidsByEntity.keyEnumerator();
			while (entityNamesEnum.hasMoreElements()) {
				String entityName = (String) entityNamesEnum.nextElement();
				NSSet entityGids = (NSSet) gidsByEntity.objectForKey(entityName);
				Enumeration entityGidsEnum = entityGids.objectEnumerator();
				while (entityGidsEnum.hasMoreElements()) {
					EOKeyGlobalID gid = (EOKeyGlobalID) entityGidsEnum.nextElement();
					boolean gidInPacket = false;
					while (!gidInPacket) {
						if (count > 0) {
							dos.flush();
							_multicastSocket.send(baos.createDatagramPacket());
							baos.reset();
							count = 0;
							packetHeaderWritten = false;
						}
						if (!packetHeaderWritten) {
							dos.write(_identifier);
							dos.writeByte(messageType);
							dos.writeUTF(entityName);
							packetHeaderWritten = true;
							firstPacket = false;
						}
						Object[] values = gid._keyValuesNoCopy();
						dos.writeByte(values.length);
						gidInPacket = true;
						for (int keyNum = 0; keyNum < values.length; keyNum++) {
							int bytesWritten = writeKey(dos, values[keyNum], maxPacketSize - count);
							if (bytesWritten == 0) {
								packetHeaderWritten = false;
								gidInPacket = false;
							}
							else {
								count += bytesWritten;
							}
						}
					}
				}
			}
			if (count > 0) {
				dos.flush();
				_multicastSocket.send(baos.createDatagramPacket());
			}
		}

		protected int writeKey(DataOutputStream dos, Object key, int bytesLeft) throws IOException {
			int bytesWritten = 0;
			if (key instanceof Byte) {
				int size = 1 + 1;
				if (bytesLeft >= size) {
					dos.writeByte(MulticastSynchronizer.BYTE_TYPE);
					dos.writeShort(((Byte) key).byteValue());
					bytesWritten = size;
				}
			}
			else if (key instanceof Short) {
				int size = 2 + 1;
				if (bytesLeft >= size) {
					dos.writeByte(MulticastSynchronizer.SHORT_TYPE);
					dos.writeShort(((Short) key).shortValue());
					bytesWritten = size;
				}
			}
			else if (key instanceof Integer) {
				int size = 4 + 1;
				if (bytesLeft >= size) {
					dos.writeByte(MulticastSynchronizer.INT_TYPE);
					dos.writeInt(((Integer) key).intValue());
					bytesWritten = size;
				}
			}
			else if (key instanceof Long) {
				int size = 8 + 1;
				if (bytesLeft >= size) {
					dos.writeByte(MulticastSynchronizer.LONG_TYPE);
					dos.writeLong(((Long) key).longValue());
					bytesWritten = size;
				}
			}
			else if (key instanceof NSData) {
				NSData data = (NSData) key;
				int size = data.length() + 2;
				if (bytesLeft >= size) {
					dos.writeByte(MulticastSynchronizer.DATA_TYPE);
					dos.writeByte(data.length());
					data.writeToStream(dos);
					bytesWritten = size;
				}
			}
			else {
				throw new IllegalArgumentException("MulticastSynchronizer can't handle key '" + key + "'.");
			}
			return bytesWritten;
		}

		protected NSArray readGIDs(DatagramPacket packet, DataInputStream dis) throws IOException {
			NSMutableArray gids = new NSMutableArray();
			String entityName = dis.readUTF();
			while (dis.available() > 0) {
				EOGlobalID gid = readGID(entityName, dis);
				gids.addObject(gid);
			}
			return gids;
		}

		protected EOGlobalID readGID(String entityName, DataInputStream dis) throws IOException {
			int keyCount = dis.readByte();
			Object[] keys = new Object[keyCount];
			for (int i = 0; i < keyCount; i++) {
				keys[i] = readKey(dis);
			}
			EOKeyGlobalID gid = EOEntityClassDescription.classDescriptionForEntityName(entityName)._globalIDWithEntityName(entityName, keys);
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