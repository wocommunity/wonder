//
// ERCNPublisher.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

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
 * ERCNPublisher prepares a JMS session object per thread so that each worker  
 * threads can perform the operation concurrently. 
 */ 
public class ERCNPublisher {

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
                NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
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
                NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            }
        }
        return topicPublisher;
    }

    public void publishChange(NSNotification notification) {
        ERCNSnapshot snapshot = new ERCNSnapshot(notification);

        if (snapshot.shouldPostChange()) {
            final int deliveryMode = DeliveryMode.PERSISTENT;
            final int priority = 4; // 0:Low -- 9:High  
            // set TTL to 63 minutes; little longer than the default  
            // value of ec.defaultFetchTimestampLag()
            final long timeToLive = 63 * 60 * 1000; 
            try {
                ObjectMessage message = _topicSession().createObjectMessage(snapshot);
                _topicPublisher().publish(_coordinator.topic(), message, deliveryMode, priority, timeToLive);
                if (NSLog.debug.isEnabled())
                    NSLog.debug.appendln("Posted a message with snapshot: " + snapshot);
            } catch (JMSException ex) {
                NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            }
        }
    }

    public synchronized void terminate() {
        ;
    }

}
