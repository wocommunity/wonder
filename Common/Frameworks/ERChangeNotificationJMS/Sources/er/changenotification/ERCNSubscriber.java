//
// ERCNSubscriber.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;
import er.extensions.ERXLogger;

import javax.jms.*;

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
public class ERCNSubscriber implements MessageListener {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCNSubscriber.class);

    private static NSKeyValueCoding _delegate;

    private ERCNNotificationCoordinator _coordinator;
    private TopicSession _topicSession;
    private TopicSubscriber _topicSubscriber;
    private boolean _isDurable = false;

    private ERCNSubscriber() {
        super();
    }
    
    public ERCNSubscriber(ERCNNotificationCoordinator coordinator, boolean isDurable) {
        super();
        _coordinator = coordinator;
        _isDurable = isDurable;
        try {
            _topicSession = _coordinator.connection().createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException ex) {
            log.error("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        String selector = null;
        boolean noLocal = true;
        try {
            if (_isDurable) {
                _topicSubscriber = _topicSession.createDurableSubscriber(_coordinator.topic(), 
            								_coordinator.id(), selector, noLocal);
            } else {
                _topicSubscriber = _topicSession.createSubscriber(_coordinator.topic(), selector, noLocal);
            }
        } catch (Exception ex) {
            log.error("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static NSKeyValueCoding delegate() { 
        return _delegate; 
    }
    
    public static void setDelegate(NSKeyValueCoding newDelegate) { 
        _delegate = newDelegate; 
    }

    protected TopicSubscriber topicSubscriber() {
        return _topicSubscriber;
    }

    public void onMessage(Message message) {
        try {
            ERCNSnapshot snapshot = (ERCNSnapshot) ((ObjectMessage)message).getObject();
            if (log.isDebugEnabled())
                log.debug("Received a message with snapshot: " + snapshot);
            processInsertions(snapshot.shapshotsForInsertionGroupedByEntity());
            processDeletions(snapshot.globalIDsForDeletionGroupedByEntity());
            processUpdates(snapshot.shapshotsForUpdateGroupedByEntity());
            message.acknowledge();
            log.debug("Finished processing changes.");
        } catch (JMSException ex) {
            log.error("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
        }
    }

    public void processInsertions(NSDictionary insertions) {
        if (delegate() != null) {
            delegate().takeValueForKey(insertions, "processInsertions");
        }
    }
    
    public void processDeletions(NSDictionary deletions) { 
        if (delegate() != null) {
            delegate().takeValueForKey(deletions, "processDeletions");
        }
    }

    public void processUpdates(NSDictionary updatedEntDict) {
        NSArray entityNames = updatedEntDict.allKeys();
        EOEditingContext ec = new EOEditingContext();
        ec.lock();
        
        Enumeration entitiesEnumerator = entityNames.objectEnumerator();
        while (entitiesEnumerator.hasMoreElements()) {
            String entityName = (String)entitiesEnumerator.nextElement();
            EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
            EODatabaseContext dbContext = ERCNNotificationCoordinator.databaseContextForEntityNamed(entityName, ec);
            EODatabase database = dbContext.database();
            NSArray snapshots = (NSArray)updatedEntDict.objectForKey(entityName);
            Enumeration snapshotsEnumerator = snapshots.objectEnumerator();
            while (snapshotsEnumerator.hasMoreElements()) {
                NSDictionary snapshot = (NSDictionary)snapshotsEnumerator.nextElement();
                if (log.isDebugEnabled())
                    log.debug("snapshot: " + snapshot);
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
    public synchronized void terminate() {
        try {
            if (_isDurable) {
                _topicSession.unsubscribe(_coordinator.id());
            }
        } catch (JMSException ex) {
            log.error("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
        }
    }

}
