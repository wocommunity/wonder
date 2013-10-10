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

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRemoteNotificationCenter;
import er.extensions.remoteSynchronizer.ERXRemoteSynchronizer.RefByteArrayOutputStream;

/**
 * NSNotificationCenter that can post simple notifications to other apps.
 * Serializes the NSNotification, so be sure not to put EOs or similar stuff
 * into it. Also the <code>object</code> is sent into the stream, so you must
 * override <code>equals()</code> so it also gets the notification on the
 * other side (totally untested).<br >
 * Note: you must specifically register here, not at <code>NSNotificationCenter.defaultCenter()</code>.
 * 
 * @author ak
 */

public class ERJGroupsNotificationCenter extends ERXRemoteNotificationCenter {

    private static final Logger log = Logger.getLogger(ERJGroupsNotificationCenter.class);

    private String _groupName;

    private boolean _postLocal;

    private JChannel _channel;

    private static volatile ERJGroupsNotificationCenter _sharedInstance;

    protected ERJGroupsNotificationCenter() throws ChannelException {
        String jgroupsPropertiesFile = ERXProperties.stringForKey("er.extensions.jgroupsNotificationCenter.properties");
        String jgroupsPropertiesFramework = null;
        if (jgroupsPropertiesFile == null) {
            jgroupsPropertiesFile = "jgroups-default.xml";
            jgroupsPropertiesFramework = "ERJGroupsSynchronizer";
        }
        _groupName = ERXProperties.stringForKeyWithDefault("er.extensions.jgroupsNotificationCenter.groupName", "ERJGroupsNotificationCenter");

        String localBindAddressStr = ERXProperties.stringForKey("er.extensions.jgroupsNotificationCenter.localBindAddress");
        if (localBindAddressStr == null) {
            System.setProperty("bind.address", WOApplication.application().hostAddress().getHostAddress());
        } else {
            System.setProperty("bind.address", localBindAddressStr);
        }

        URL propertiesUrl = WOApplication.application().resourceManager().pathURLForResourceNamed(jgroupsPropertiesFile, jgroupsPropertiesFramework, null);
        _channel = new JChannel(propertiesUrl);
        _postLocal = ERXProperties.booleanForKeyWithDefault("er.extensions.jgroupsNotificationCenter.postLocal", false);
        _channel.setOpt(Channel.LOCAL, Boolean.FALSE);
        _channel.connect(_groupName);
        _channel.setReceiver(new ExtendedReceiverAdapter() {

            @Override
            public void receive(Message message) {
                try {
                    byte[] buffer = message.getBuffer();
                    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                    ObjectInputStream dis = new ObjectInputStream(bais);
                    String name = (String) dis.readObject();
                    Object object = dis.readObject();
                    NSDictionary userInfo = (NSDictionary) dis.readObject();
                    NSNotification notification = new NSNotification(name, object, userInfo);
                    if (log.isDebugEnabled()) {
                        log.debug("Received notification: " + notification);
                    } else if (log.isInfoEnabled()) {
                        log.info("Received " + notification.name() + " notification.");
                    }
                    postLocalNotification(notification);
                } catch (IOException e) {
                    log.error("Failed to read notification: " + e, e);
                } catch (ClassNotFoundException e) {
                    log.error("Failed to find class: " + e, e);
                }
            }

            @Override
            public void viewAccepted(View view) {
                // System.out.println(".viewAccepted: " + view);
            }
        });
    }

    public static void install() {
        if (_sharedInstance == null) {
            synchronized (ERJGroupsNotificationCenter.class) {
                if (_sharedInstance == null) {
                    try {
                        _sharedInstance = new ERJGroupsNotificationCenter();
                        setDefaultCenter(_sharedInstance);
                    } catch (ChannelException e) {
                        throw NSForwardException._runtimeExceptionForThrowable(e);
                    }
                }
            }
        }
    }

    @Override
    public void postRemoteNotification(NSNotification notification) {
        try {
            writeNotification(notification);
            if (_postLocal) {
                postLocalNotification(notification);
            }
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
        if (log.isDebugEnabled()) {
            log.debug("Sending notification: " + notification);
        } else if (log.isInfoEnabled()) {
            log.info("Sending " + notification.name() + " notification.");
        }
        Message message = new Message(null, null, baos.buffer(), 0, baos.size());
        _channel.send(message);
    }
}
