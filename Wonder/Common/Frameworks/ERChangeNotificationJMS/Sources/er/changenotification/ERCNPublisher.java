//
// ERCNPublisher.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import er.extensions.ERXLogger;

import javax.jms.*;

/** 
 * ERCNPublisher sends change notifications to other application instances via 
 * JMS server. It observes changes in the default EOObjectStoreCoordinator and 
 * creates an ERCNSnapshot object when EOEditingContext saveChanges is performed. 
 * <p>
 * There must be exactly one instance of the ERCNPublisher on the application 
 * and registered to the NSNotificationCenter, but its publishChange method   
 * will be executed by application's multiple worker threads concurrently.
 * Since JMS session object is limited for serial use (single threaded use), 
 * ERCNPublisher prepares a session object per thread so that each worker threads 
 * can perform the operation concurrently. 
 */ 
public class ERCNPublisher {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCNPublisher.class);

    private ERCNNotificationCoordinator _coordinator; 
    private final ThreadLocal _topicSessionCollection = new ThreadLocal();
    private final ThreadLocal _topicPublisherCollection = new ThreadLocal();

    private ERCNPublisher() {
        super();
    }
    
    public ERCNPublisher(ERCNNotificationCoordinator coordinator) {
        super();
        _coordinator = coordinator;
    }

    private TopicSession _topicSession() {
        TopicSession topicSession = (TopicSession)_topicSessionCollection.get();
        if (topicSession == null) {
            try {
                topicSession = _coordinator.connection().createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
                _topicSessionCollection.set(topicSession);
            } catch (JMSException ex) {
                log.error("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        return topicSession;
    }

    private TopicPublisher _topicPublisher() {
        TopicPublisher topicPublisher = (TopicPublisher)_topicPublisherCollection.get();
        if (topicPublisher == null) {
            try {
                topicPublisher = _topicSession().createPublisher(_coordinator.topic());
                _topicPublisherCollection.set(topicPublisher);
            } catch (JMSException ex) {
                log.error("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            }
        }
        return topicPublisher;
    }

    public void publishChange(NSNotification notification) {
        ERCNSnapshot snapshot = new ERCNSnapshot(notification);

        if (snapshot.shouldPostChange()) {
            int deliveryMode = DeliveryMode.PERSISTENT;
            try {
                ObjectMessage message = _topicSession().createObjectMessage(snapshot);
                int priority = 4; // 0:Low -- 9:High  
                _topicPublisher().publish(_coordinator.topic(), message, deliveryMode, priority, 0);
                if (log.isDebugEnabled())
                    log.debug("Posted a message with snapshot: " + snapshot);
            } catch (JMSException ex) {
                log.error("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            }
        }
    }

    public synchronized void terminate() {
        ;
    }

}
