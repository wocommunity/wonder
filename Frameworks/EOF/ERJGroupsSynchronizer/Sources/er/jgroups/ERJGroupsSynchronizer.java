package er.jgroups;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.appserver.ERXShutdownHook;
import er.extensions.eof.ERXDatabase;
import er.extensions.eof.ERXObjectStoreCoordinatorSynchronizer.IChangeListener;
import er.extensions.eof.ERXObjectStoreCoordinatorSynchronizer.RemoteChange;
import er.extensions.foundation.ERXProperties;
import er.extensions.remoteSynchronizer.ERXRemoteSynchronizer;

/**
 * A multicast synchronizer built on top of the JGroups library.  This is a much
 * more robust implementation than the default synchronizer used by ERXObjectStoreCoordinatorSynchronizer.
 * 
 * @property er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators you should set this property to at least "1" to trigger
 *   ERXObjectStoreCoordinatorSynchronizer to turn on
 * @property er.extensions.jgroupsSynchronizer.applicationWillTerminateNotificationName the name of the NSNotification that is sent when
 *   the application is terminating. Leave blank to disable this feature.
 * @property er.extensions.jgroupsSynchronizer.autoReconnect whether to auto reconnect when shunned (defaults to false)
 * @property er.extensions.jgroupsSynchronizer.groupName the JGroups group name to use (defaults to WOApplication.application.name)
 * @property er.extensions.jgroupsSynchronizer.localBindAddress
 * @property er.extensions.jgroupsSynchronizer.multicastAddress the multicast address to use (defaults to 230.0.0.1,
     and only necessary if you are using multicast)
 * @property er.extensions.jgroupsSynchronizer.multicastPort the multicast port to use (defaults to 9753, and only necessary if you are using multicast)
 * @property er.extensions.jgroupsSynchronizer.properties an XML JGroups configuration file (defaults to jgroups-default.xml in this framework)
 * @property er.extensions.jgroupsSynchronizer.useShutdownHook whether to register a JVM shutdown hook to clean up the JChannel (defaults to true)
 * @property er.extensions.remoteSynchronizer "er.jgroups.ERJGroupsSynchronizer" for this implementation
 * @property er.extensions.remoteSynchronizer.enabled if true, remote synchronization is enabled
 * @property er.extensions.remoteSynchronizer.excludeEntities the list of entities to NOT synchronize (none by default)
 * @property er.extensions.remoteSynchronizer.includeEntities the list of entities to synchronize (all by default)
 *
 * @author mschrag
 */
public class ERJGroupsSynchronizer extends ERXRemoteSynchronizer {
	private static final Logger log = LoggerFactory.getLogger(ERXRemoteSynchronizer.class);

	private String _groupName;
	private JChannel _channel;

	public ERJGroupsSynchronizer(IChangeListener listener) throws Exception {
		super(listener);
		String jgroupsPropertiesFile = ERXProperties.stringForKey("er.extensions.jgroupsSynchronizer.properties");
		String jgroupsPropertiesFramework = null;
		if (jgroupsPropertiesFile == null) {
			jgroupsPropertiesFile = "jgroups-default.xml";
			jgroupsPropertiesFramework = "ERJGroupsSynchronizer";
		}
		_groupName = ERXProperties.stringForKeyWithDefault("er.extensions.jgroupsSynchronizer.groupName", WOApplication.application().name());

		String localBindAddressStr = ERXProperties.stringForKey("er.extensions.jgroupsSynchronizer.localBindAddress");
		if (localBindAddressStr == null) {
			System.setProperty("bind.address", WOApplication.application().hostAddress().getHostAddress());
		}
		else {
			System.out.println("localBindAddressStr = " + localBindAddressStr);
			System.setProperty("bind.address", localBindAddressStr);
		}

		URL propertiesUrl = WOApplication.application().resourceManager().pathURLForResourceNamed(jgroupsPropertiesFile, jgroupsPropertiesFramework, null);
		_channel = new JChannel(propertiesUrl);
		_channel.setDiscardOwnMessages(Boolean.FALSE);

		_registerForCleanup();
	}

	@Override
	public void join() throws Exception {
		_channel.connect(_groupName);
	}

	@Override
	public void leave() {
		_channel.disconnect();
	}

	@Override
	public void listen() {
		_channel.setReceiver(new ReceiverAdapter() {

			public void receive(Message message) {
				try {
					byte[] buffer = message.getBuffer();
					ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
					DataInputStream dis = new DataInputStream(bais);
					int transactionCount = dis.readInt();
					RemoteChange remoteChange = new RemoteChange("AnotherInstance", -1, transactionCount);
					for (int transactionNum = 0; transactionNum < transactionCount; transactionNum++) {
						_readCacheChange(remoteChange, dis);
					}
					addChange(remoteChange);
					log.info("Received {} changes from {}", transactionCount, message.getSrc());
					if (log.isDebugEnabled()) {
						log.debug("  Changes = {}", remoteChange.remoteCacheChanges());
					}
				}
				catch (IOException e) {
					log.error("Failed to apply remote changes.  This is bad.", e);
				}
			}

			public void viewAccepted(View view) {
				// System.out.println(".viewAccepted: " + view);
			}
		});
	}

	@Override
	protected void _writeCacheChanges(int transactionID, NSArray cacheChanges) throws Exception, IOException {
		if (!_channel.isConnected()) {
			log.info("Channel not connected: Not Sending {} changes.", cacheChanges.count());
			log.debug("Channel not connected: Changes = {}", cacheChanges);
			return;
		}
		if (cacheChanges.count() == 0) {
			log.info("No changes to send!");
			return;
		}
		RefByteArrayOutputStream baos = new RefByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(cacheChanges.count());
		for (Enumeration cacheChangesEnum = cacheChanges.objectEnumerator(); cacheChangesEnum.hasMoreElements();) {
			ERXDatabase.CacheChange cacheChange = (ERXDatabase.CacheChange) cacheChangesEnum.nextElement();
			_writeCacheChange(dos, cacheChange);
		}
		dos.flush();
		dos.close();
		log.info("Sending {} changes.", cacheChanges.count());
		log.debug("  Changes = {}", cacheChanges);
		Message message = new Message(null, null, baos.buffer(), 0, baos.size());
		_channel.send(message);
	}

	private void _registerForCleanup() {
		String notificationName = ERXProperties.stringForKey("er.extensions.jgroupsSynchronizer.applicationWillTerminateNotificationName");
		if (notificationName != null && notificationName.length() > 0) {
			NSSelector applicationLaunchedNotification = new NSSelector("_applicationWillTerminateNotification", new Class[] { NSNotification.class });
			NSNotificationCenter.defaultCenter().addObserver(this, applicationLaunchedNotification, notificationName, null);
		}

		if (ERXProperties.booleanForKeyWithDefault("er.extensions.jgroupsSynchronizer.useShutdownHook", true)) {
			new ERJGroupsCleanupTask(_channel);
		}
	}

	private static void cleanUpJChannel(JChannel channel) {
		try {
			if (channel == null || !channel.isOpen()) {
				return;
			}

			if (channel.isConnected()) {
				channel.disconnect();
			}

			channel.close();
		}
		catch (Throwable e) {
			log.error("Error closing JChannel: {}", channel, e);
		}
	}

	public void _applicationWillTerminateNotification(NSNotification notification) {
		cleanUpJChannel(_channel);
	}

	private static class ERJGroupsCleanupTask extends ERXShutdownHook {
		private final JChannel channel;

		public ERJGroupsCleanupTask(JChannel channel) {
			super("JGroups Cleanup");
			this.channel = channel;
		}

		@Override
		public void hook() {
			cleanUpJChannel(channel);
		}
	}
}
