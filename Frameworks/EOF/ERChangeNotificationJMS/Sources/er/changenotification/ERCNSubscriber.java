//
// ERCNSubscriber.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;

/** 
 * ERCNSubscriber handles change notifications from other application 
 * instances via JMS server, and applies the changes to the local EOF stack 
 * managed by the default EOObjectStoreCoordinator.
 * <p>
 * There must be exactly one instance of the ERCNSubscriber on the application. 
 * Its onChange method is called by JMS's own single thread which manages 
 * asynchronous receiving mechanism, and therefore, it runs concurrently with 
 * other worker threads and will not interfere request-response cycles. 
 * <p>
 * Note that the subscriber will not receive notifications from itself, but 
 * from other application instances. This is accomplished by using option flag 
 * when create the JMS topic subscriber object.
 * <p>
 * The current implementation only supports updating operations, not inserting  
 * or deleting operations. You can register a delegate object to process those 
 * operations. 
 */ 
class ERCNSubscriber implements MessageListener {

    private static ERCNSubscriberDelegate _delegate;

    private ERCNNotificationCoordinator _coordinator;
    private TopicSession _topicSession;
    private TopicSubscriber _topicSubscriber;

    ERCNSubscriber(ERCNNotificationCoordinator coordinator) {
        _coordinator = coordinator;
    }

    static ERCNSubscriberDelegate delegate() {
        return _delegate; 
    }
    
    static void setDelegate(ERCNSubscriberDelegate delegate) {
        _delegate = delegate;
    }

    void subscribe(TopicConnection connection) {
        try {
            _topicSession = connection.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException ex) {
            NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "Failed to create a JMS topic session: " + ex.getMessage());
            return;
        }

        final String selector = null;
        final boolean noLocal = true;
        try {
            if (_coordinator.configuration().isSubscriberDurable()) {
                _topicSubscriber = _topicSession.createDurableSubscriber(_coordinator.topic(),
            								_coordinator.id(), selector, noLocal);
            } else {
                _topicSubscriber = _topicSession.createSubscriber(_coordinator.topic(), selector, noLocal);
            }
        } catch (Exception ex) {
            NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "An exception occured: " + ex.getMessage());
        }

        try {
            _topicSubscriber.setMessageListener(this);
        } catch (JMSException ex) {
            NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "An exception occured: " + ex.getMessage());
        }

    }

    public void onMessage(Message message) {
        try {
            ERCNSnapshot snapshot = (ERCNSnapshot) ((ObjectMessage)message).getObject();
            if (NSLog.debug.isEnabled())
                NSLog.debug.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "Received a message with snapshot: " + snapshot);

            if (ERCNSnapshot.shouldApplyChangeFor(ERCNSnapshot.INSERTED))
                _processInsertions(snapshot);

            if (ERCNSnapshot.shouldApplyChangeFor(ERCNSnapshot.DELETED))
                _processDeletions(snapshot);

            if (ERCNSnapshot.shouldApplyChangeFor(ERCNSnapshot.UPDATED))
                _processUpdates(snapshot);

            NSLog.debug.appendln(ERCNNotificationCoordinator.LOG_HEADER + "Finished processing changes.");
        } catch (JMSException ex) {
            NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "An exception occured: " + ex.getMessage());
        } finally {
            try {
                message.acknowledge();
            } catch (JMSException ex) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "An exception occured: " + ex.getMessage());
            }
        }
    }

    void unsubscribe() {
        try {
            if (_coordinator.configuration().isSubscriberDurable()) {
                _topicSession.unsubscribe(_coordinator.id());
            }
        } catch (JMSException ex) {
            NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "An exception occured: " + ex.getMessage());
        }
        _topicSession = null;
    }

    private void _processInsertions(ERCNSnapshot ercnSnapshot) {
        if (_delegate != null)
            _delegate.processInsertions(ercnSnapshot);
    }

    private void _processDeletions(ERCNSnapshot ercnSnapshot) {
        if (_delegate != null)
            _delegate.processDeletions(ercnSnapshot);
    }

    private void _processUpdates(ERCNSnapshot ercnSnapshot) {
        if (_delegate != null) {
            boolean isProcessed = _delegate.processUpdates(ercnSnapshot);
            if (isProcessed)
                return;
        }

        NSArray entityNames = ercnSnapshot.shapshotsForUpdateGroupedByEntity().allKeys();
        EOEditingContext ec = new EOEditingContext();
        ec.lock();

        Enumeration entitiesEnumerator = entityNames.objectEnumerator();
        while (entitiesEnumerator.hasMoreElements()) {
            String entityName = (String)entitiesEnumerator.nextElement();
            if (! ERCNSnapshot.shouldSynchronizeEntity(entityName))   continue;

            EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
            EODatabaseContext dbContext = ERCNNotificationCoordinator.databaseContextForEntityNamed(entityName, ec);
            EODatabase database = dbContext.database();
            NSArray snapshots = (NSArray)ercnSnapshot.shapshotsForUpdateGroupedByEntity().objectForKey(entityName);
            Enumeration snapshotsEnumerator = snapshots.objectEnumerator();
            while (snapshotsEnumerator.hasMoreElements()) {
                NSDictionary snapshot = (NSDictionary)snapshotsEnumerator.nextElement();
                if (NSLog.debug.isEnabled())
                    NSLog.debug.appendln(ERCNNotificationCoordinator.LOG_HEADER + "Snapshot: " + snapshot);
                if (snapshot != null) {
                    EOGlobalID globalID = entity.globalIDForRow(snapshot);
                    dbContext.lock();
                    database.forgetSnapshotForGlobalID(globalID);
                    database.recordSnapshotForGlobalID(snapshot, globalID);
                    dbContext.unlock();
                }
            }
        }
        ec.unlock();
    }

    // ENHANCEME: The subscriber could be processing the last destributed notification
    //            when it's requested to terminate. Should wait *here* for the last
    //            notification to finish, then return so that the coordinator can safely
    //            close the connection to the JMS server.
    synchronized void terminate() {
        unsubscribe();
    }

}
