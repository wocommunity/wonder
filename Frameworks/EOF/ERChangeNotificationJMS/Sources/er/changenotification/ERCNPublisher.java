//
// ERCNPublisher.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSNotification;

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
class ERCNPublisher {

    private ERCNNotificationCoordinator _coordinator; 

    ERCNPublisher(ERCNNotificationCoordinator coordinator) {
        _coordinator = coordinator;
    }

    public void publishChange(NSNotification notification) {
        if (! _coordinator.isConnected())  return;

        ERCNSnapshot snapshot = new ERCNSnapshot(notification);

        if (snapshot.shouldPostChange()) {
            final int deliveryMode = DeliveryMode.NON_PERSISTENT;
            final int priority = 4; // 0:Low -- 9:High
            // set TTL to 63 minutes; little longer than the default
            // value of ec.defaultFetchTimestampLag()
            final long timeToLive = 63 * 60 * 1000;
            try {
				TopicSession topicSession = _topicSession();
				if (topicSession != null) {
					ObjectMessage message = topicSession.createObjectMessage(snapshot);
                    Topic topic = _coordinator.topic();
					_topicPublisher(topic, topicSession).publish(topic, message, deliveryMode, priority, timeToLive);
					if (NSLog.debug.isEnabled())
						NSLog.debug.appendln(ERCNNotificationCoordinator.LOG_HEADER
                                + "Posted a message with snapshot: " + snapshot);
				}

            // java.rmi.ConnectException  -- server is down (publish)
            // java.rmi.UnmarshalException -- version
			// javax.jms.IllegalStateException - Cannot perform operation - session has been closed
            } catch (JMSException ex) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "Failed to poste a snapshot: " + ex.getMessage());
            }
        }
    }

    private TopicSession _topicSession() {
        TopicSession topicSession = null;
        try {
            TopicConnection connection = _coordinator.connection();
            topicSession = connection.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
         } catch (JMSException ex) {

            // javax.jms.IllegalStateException - Cannot perform operation - session has been closed
            NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "Failed to create a JMS topic session: " + ex.getMessage());
        }
        return topicSession;
    }

    private TopicPublisher _topicPublisher(Topic topic, TopicSession topicSession) {
        TopicPublisher topicPublisher = null;
        try {
            topicPublisher = topicSession.createPublisher(topic);
        } catch (JMSException ex) {
            NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "Failed to create a JMS topic publisher: " + ex.getMessage());
        }
        return topicPublisher;
    }

    synchronized void terminate() {
        // do nothing
    }

}
