package er.jgroups;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.ERXProperties;
import er.extensions.ERXObjectStoreCoordinatorSynchronizer.RemoteChange;
import er.extensions.remoteSynchronizer.ERXRemoteSynchronizer.RefByteArrayOutputStream;

/**
 * NSNotificationCenter that can post simple notifications to other apps.
 * Currently just serializes the NSNotification, so be sure not to put EOs or stuff into it.
 * Also posts back to your own app, so you might need to be carefull...
 * @author ak
 */

// TODO configure per notification if to post back or not?
// TODO subclass of NSNotification that custom-serialize itself

public class ERJGroupsNotificationCenter extends NSNotificationCenter {

    private static final Logger log = Logger.getLogger(ERJGroupsNotificationCenter.class);

    private String _groupName;

    private JChannel _channel;
    private JChannel _backchannel;

    private static ERJGroupsNotificationCenter _sharedInstance;

    protected ERJGroupsNotificationCenter() throws ChannelException {
        String jgroupsPropertiesFile = ERXProperties.stringForKey("er.extensions.jgroupsNotificationCenter.properties");
        String jgroupsPropertiesFramework = null;
        if (jgroupsPropertiesFile == null) {
            jgroupsPropertiesFile = "jgroups-default.xml";
            jgroupsPropertiesFramework = "ERJGroupsSynchronizer";
        }
        _groupName = ERJGroupsNotificationCenter.class.getName();

        String localBindAddressStr = ERXProperties.stringForKey("er.extensions.jgroupsNotificationCenter.localBindAddress");
        if (localBindAddressStr == null) {
            System.setProperty("bind.address", WOApplication.application().hostAddress().getHostAddress());
        } else {
            System.setProperty("bind.address", localBindAddressStr);
        }

        URL propertiesUrl = WOApplication.application().resourceManager().pathURLForResourceNamed(jgroupsPropertiesFile, jgroupsPropertiesFramework, null);
        _channel = new JChannel(propertiesUrl);
        _channel.setOpt(Channel.LOCAL, Boolean.TRUE);
        _channel.connect(_groupName);
        _channel.setReceiver(new ExtendedReceiverAdapter() {
            // @Override
            public void receive(Message message) {
                try {
                    byte[] buffer = message.getBuffer();
                    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                    ObjectInputStream dis = new ObjectInputStream(bais);
                    String name = (String) dis.readObject();
                    Object object = dis.readObject();
                    NSDictionary userInfo = (NSDictionary) dis.readObject();
                    NSNotification notification = new NSNotification(name, object, userInfo);
                     if (log.isInfoEnabled()) {
                        log.info("Received " + name + " notification from " + message.getSrc());
                    }
                    if (log.isDebugEnabled()) {
                        log.info("  Details = " + notification);
                    }
                    NSNotificationCenter.defaultCenter().postNotification(notification);
                } catch (IOException e) {
                    log.error("Failed to read notification: " + e, e);
                } catch (ClassNotFoundException e) {
                    log.error("Failed to find class: " + e, e);
                }
            }

            // @Override
            public void viewAccepted(View view) {
                // System.out.println(".viewAccepted: " + view);
            }
        });
    }

    public static ERJGroupsNotificationCenter defaultCenter() {
        if (_sharedInstance == null) {
            try {
                _sharedInstance = new ERJGroupsNotificationCenter();
            } catch (ChannelException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
        }
        return _sharedInstance;
    }

    public void postNotification(NSNotification notification) {
        try {
            writeNotification(notification);
        } catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    protected void writeNotification(NSNotification notification) throws ChannelNotConnectedException, ChannelClosedException, IOException {
        RefByteArrayOutputStream baos = new RefByteArrayOutputStream();
        ObjectOutputStream dos = new ObjectOutputStream(baos);
        dos.writeObject(notification.name());
        dos.writeObject(notification.object());
        dos.writeObject(notification.userInfo());
        dos.flush();
        dos.close();
        if (log.isInfoEnabled()) {
            log.info("Sending " + notification.name() + " notification.");
        }
        if (log.isDebugEnabled()) {
            log.info("  Details = " + notification);
        }
        Message message = new Message(null, null, baos.buffer(), 0, baos.size());
        _channel.send(message);
    }
}
