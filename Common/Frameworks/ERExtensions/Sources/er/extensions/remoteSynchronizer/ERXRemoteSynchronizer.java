package er.extensions.remoteSynchronizer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXDatabase;
import er.extensions.ERXProperties;
import er.extensions.ERXObjectStoreCoordinatorSynchronizer.IChangeListener;
import er.extensions.ERXObjectStoreCoordinatorSynchronizer.RemoteChange;

/**
 * The superclass of all remote EOF synchronizers.
 * 
 * @property er.extensions.remoteSynchronizer.enabled if true, remote synchronization is enabled
 * @property er.extensions.remoteSynchronizer the class name of the remote synchronizer to use (default to ERXSimpleMulticastSynchronizer)
 * 
 * @author mschrag
 */
public abstract class ERXRemoteSynchronizer {
	public static Logger log = Logger.getLogger(ERXRemoteSynchronizer.class);

	public static boolean remoteSynchronizerEnabled() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.remoteSynchronizer.enabled", false);
	}

	public static ERXRemoteSynchronizer newRemoteSynchronizer(IChangeListener changeListener) throws Throwable {
		String remoteSynchronizerClassName = ERXProperties.stringForKey("er.extensions.remoteSynchronizer");
		ERXRemoteSynchronizer remoteSynchronizer;
		if (remoteSynchronizerClassName == null) {
			remoteSynchronizer = new ERXSimpleMulticastSynchronizer(changeListener);
		}
		else {
			Class remoteSynchronizerClass = Class.forName(remoteSynchronizerClassName);
			Constructor remoteSynchronizerConstructor = remoteSynchronizerClass.getConstructor(new Class[] { IChangeListener.class });
			remoteSynchronizer = (ERXRemoteSynchronizer) remoteSynchronizerConstructor.newInstance(new Object[] { changeListener });
		}
		return remoteSynchronizer;
	}

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
	private static final int STRING_TYPE = 6;

	private IChangeListener _listener;
	private NSArray _includeEntityNames;
	private NSArray _excludeEntityNames;

	public ERXRemoteSynchronizer(IChangeListener listener) {
		_listener = listener;
		String includeEntityNames = ERXProperties.stringForKey("er.extensions.remoteSynchronizer.includeEntities");
		NSArray includeEntityNamesArray = null;
		if (includeEntityNames != null) {
			_includeEntityNames = NSArray.componentsSeparatedByString(includeEntityNames, ",");
		}
		NSArray excludeEntityNamesArray = null;
		String excludeEntityNames = ERXProperties.stringForKey("er.extensions.remoteSynchronizer.excludeEntities");
		if (excludeEntityNames != null) {
			_excludeEntityNames = NSArray.componentsSeparatedByString(excludeEntityNames, ",");
		}
	}

	protected void _readCacheChange(RemoteChange remoteChange, DataInputStream dis) throws IOException {
		int messageType = dis.readByte();
		if (messageType == ERXRemoteSynchronizer.INSERT) {
			EOGlobalID gid = readGID(dis);
			ERXDatabase.SnapshotInserted change = new ERXDatabase.SnapshotInserted(gid, NSDictionary.EmptyDictionary);
			if (log.isDebugEnabled()) {
				log.info("Remote instance (" + remoteChange.identifier() + ") inserted " + change);
			}
			remoteChange.addRemoteCacheChange(change);
		}
		else if (messageType == ERXRemoteSynchronizer.UPDATE) {
			EOGlobalID gid = readGID(dis);
			ERXDatabase.SnapshotUpdated change = new ERXDatabase.SnapshotUpdated(gid, NSDictionary.EmptyDictionary);
			if (log.isDebugEnabled()) {
				log.info("Remote instance (" + remoteChange.identifier() + ") updated " + change);
			}
			remoteChange.addRemoteCacheChange(change);
		}
		else if (messageType == ERXRemoteSynchronizer.DELETE) {
			EOGlobalID gid = readGID(dis);
			ERXDatabase.SnapshotDeleted change = new ERXDatabase.SnapshotDeleted(gid, NSDictionary.EmptyDictionary);
			if (log.isDebugEnabled()) {
				log.info("Remote instance (" + remoteChange.identifier() + ") deleted " + change);
			}
			remoteChange.addRemoteCacheChange(change);
		}
		else if (messageType == ERXRemoteSynchronizer.TO_MANY_UPDATE) {
			EOGlobalID sourceGID = readGID(dis);
			String name = dis.readUTF();
			NSArray addedGIDs = readGIDs(dis);
			NSArray removedGIDs = readGIDs(dis);
			boolean removeAll = dis.readBoolean();
			ERXDatabase.ToManySnapshotUpdated change = new ERXDatabase.ToManySnapshotUpdated(sourceGID, name, addedGIDs, removedGIDs, removeAll);
			if (log.isDebugEnabled()) {
				log.info("Remote instance (" + remoteChange.identifier() + ") update to-many " + change);
			}
			remoteChange.addRemoteCacheChange(change);
		}
		else {
			throw new IllegalArgumentException("Unknown remote message type #" + messageType + ".");
		}
	}

	protected void _writeCacheChange(DataOutputStream dos, ERXDatabase.CacheChange cacheChange) throws IOException {
		if (cacheChange instanceof ERXDatabase.SnapshotInserted) {
			dos.writeByte(ERXRemoteSynchronizer.INSERT);
			writeSnapshotCacheChange(dos, cacheChange);
		}
		else if (cacheChange instanceof ERXDatabase.SnapshotUpdated) {
			dos.writeByte(ERXRemoteSynchronizer.UPDATE);
			writeSnapshotCacheChange(dos, cacheChange);
		}
		else if (cacheChange instanceof ERXDatabase.SnapshotDeleted) {
			dos.writeByte(ERXRemoteSynchronizer.DELETE);
			writeSnapshotCacheChange(dos, cacheChange);
		}
		else if (cacheChange instanceof ERXDatabase.ToManySnapshotUpdated) {
			dos.writeByte(ERXRemoteSynchronizer.TO_MANY_UPDATE);
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
			dos.writeByte(ERXRemoteSynchronizer.BYTE_TYPE);
			dos.writeShort(((Byte) key).byteValue());
		}
		else if (key instanceof Short) {
			dos.writeByte(ERXRemoteSynchronizer.SHORT_TYPE);
			dos.writeShort(((Short) key).shortValue());
		}
		else if (key instanceof Integer) {
			dos.writeByte(ERXRemoteSynchronizer.INT_TYPE);
			dos.writeInt(((Integer) key).intValue());
		}
		else if (key instanceof Long) {
			dos.writeByte(ERXRemoteSynchronizer.LONG_TYPE);
			dos.writeLong(((Long) key).longValue());
		}
		else if (key instanceof NSData) {
			NSData data = (NSData) key;
			dos.writeByte(ERXRemoteSynchronizer.DATA_TYPE);
			dos.writeByte(data.length());
			data.writeToStream(dos);
		}
		else if (key instanceof String) {
			String str = (String)key;
			dos.writeByte(ERXRemoteSynchronizer.STRING_TYPE);
			dos.writeUTF(str);
		}
		else {
			throw new IllegalArgumentException("RemoteSynchronizer can't handle key '" + key + "'.");
		}
	}

	protected NSArray readGIDs(DataInputStream dis) throws IOException {
		NSMutableArray gids = new NSMutableArray();
		int gidCount = dis.readByte();
		for (int gidNum = 0; gidNum < gidCount; gidNum++) {
			EOGlobalID gid = readGID(dis);
			gids.addObject(gid);
		}
		return gids;
	}

	protected EOGlobalID readGID(DataInputStream dis) throws IOException {
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
		if (keyType == ERXRemoteSynchronizer.BYTE_TYPE) {
			obj = new Byte(dis.readByte());
		}
		else if (keyType == ERXRemoteSynchronizer.SHORT_TYPE) {
			obj = new Short(dis.readShort());
		}
		else if (keyType == ERXRemoteSynchronizer.INT_TYPE) {
			obj = new Integer(dis.readInt());
		}
		else if (keyType == ERXRemoteSynchronizer.LONG_TYPE) {
			obj = new Long(dis.readLong());
		}
		else if (keyType == ERXRemoteSynchronizer.DATA_TYPE) {
			int size = dis.readByte();
			byte[] data = new byte[size];
			dis.readFully(data);
			obj = new NSData(data);
		}
		else if (keyType == ERXRemoteSynchronizer.STRING_TYPE) {
			obj = dis.readUTF();
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

	protected void addChange(RemoteChange remoteChange) {
		_listener.addChange(remoteChange);
	}

	public abstract void join() throws Throwable;

	public abstract void leave() throws Throwable;

	public abstract void listen() throws Throwable;

	public abstract void writeCacheChanges(int transactionID, NSArray cacheChanges) throws Throwable;

	public static class RefByteArrayOutputStream extends ByteArrayOutputStream {
		public byte[] buffer() {
			return buf;
		}
	}
}
