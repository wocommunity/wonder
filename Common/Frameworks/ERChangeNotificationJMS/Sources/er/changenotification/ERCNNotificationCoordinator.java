//
// ERCNNotificationCoordinator.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Properties;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.Context;
//import org.exolab.jms.client.JmsTopicConnectionFactory;
//import org.exolab.jms.jndi.JndiConstants;
import org.exolab.jms.jndi.rmi.RmiJndiInitialContextFactory;

/**
 * ERCNNotificationCoordinator is the primary controller of the change 
 * notification for enterprise objects. It manages the single connection  
 * to the JMS server, and creates ERCNPublisher and ERCNSubscriber objects  
 * and registers them as the observers to handle change notifications. 
 * <p>
 * When the application finishes launching, it checks properties and 
 * initializes the framework. It can also perform necessary clean-up 
 * operations when the application is about to terminate. 
 * <p>
 * The framework works transparently and you will not have to call any 
 * methods on the framework. Just add this framework to application project as 
 * an external framework and put necessary properties to your property file. 
 * <p>
 * The current implementation supports concurrent request handling (multi 
 * threaded operations.)  It only supports the changes in the default 
 * EOObjectStoreCoordinator. 
 * <p>
 * Properties: <br>
 * Put the following properties into WebObjects.properties file under 
 * your home directory, or into Properties file and register it under 
 * the applications project's Resources group. 
 * <p>
 * <pre>
 * 
 * # The host name that the JMS server is running on
 * er.changenotification.jms.serverHostName = localhost
 * 
 * # Entities *not* to synchronize
 * #er.changenotification.entitiesNotToSynchronize = (TalentPhoto)
 * er.changenotification.entitiesNotToSynchronize = ()
 * 
 * # Change types to track; Can contain inserted, updated and deleted. 
 * er.changenotification.changeTypesToTrack = (inserted, updated, deleted)
 * 
 * # JMS topic name (Destination object) to pass the notifications. 
 * # Specify one and register it from the OpenJMS administration tool or 
 * # configuration file. 
 * er.changenotification.jms.topicName = business logic group 1
 * 
 * # Whether or not the JMS subscriber is durable; 
 * # prevents to miss change notifications by temporaly 
 * # network disruptions. 
 * # 
 * # false - suggested for both development and deployment
 * #
 * #         Please do *not* set it true, otherwise OpenJMS 0.7.3.1 server 
 * #         will fail with some databases (PostgreSQL, FrontBase, etc.)
 * # 
 * # If it's set to true, you need properly to shut down the applications 
 * # (e.g. shut down it from JavaMonitor or calling application's 
 * # terminate() method), otherwise JMS provider will try to keep  
 * # all changes even after application is shut down. 
 * #
 * er.changenotification.jms.durableSubscribers = false
 * 
 * </pre>
 */
public class ERCNNotificationCoordinator implements ExceptionListener {

    private static final ERCNNotificationCoordinator _coordinator = new ERCNNotificationCoordinator();

    private ERCNPublisher _publisher;
    private ERCNSubscriber _subscriber;

    private NSArray _entitiesNotToSynchronize;
    private NSArray _changeTypesToTrack;

    private Context _context;
    private TopicConnection _connection;
    private Topic _topic;

    private String _topicName;
    private boolean _isSubscriberDurable;

    private boolean _isInitialized = false;
    private boolean _isTerminated = false;

    static {
        NSLog.debug.appendln("Registering the observer to initialize ERChangeNotification Framework");
        
        NSNotificationCenter.defaultCenter().addObserver(
            _coordinator, 
            new NSSelector("initialize", new Class[] { NSNotification.class } ), 
            WOApplication.ApplicationDidFinishLaunchingNotification, 
            null);
    }

    private ERCNNotificationCoordinator() {
        super();
    }
    
    public static ERCNNotificationCoordinator coordinator() {
        return _coordinator;
    }

    public NSArray entitiesNotToSynchronize() {
        return _entitiesNotToSynchronize;
    }
    
    public void setEntitiesNotToSynchronize(NSArray newEntitiesNotToSynchronize) {
        if (newEntitiesNotToSynchronize != null) 
            _entitiesNotToSynchronize = newEntitiesNotToSynchronize;
        else 
            _entitiesNotToSynchronize = NSArray.EmptyArray;
    } 
    
    public NSArray changeTypesToTrack() {
        return _changeTypesToTrack;
    }

    public void setChangeTypesToTrack(NSArray newChangeTypesToTrack) {
        if (newChangeTypesToTrack != null) 
            _changeTypesToTrack = newChangeTypesToTrack;
        else 
            _changeTypesToTrack = NSArray.EmptyArray;
    } 
    
    protected Topic topic() {
        return _topic;
    }

    protected TopicConnection connection() {
        return _connection;
    }

    protected String id() {
        WOApplication app = WOApplication.application();
        String host = app.host();
        Number port = app.port(); // Don't forget to apply Max's change
        String appName = app.name();
        return host + ":" + port + "/" + appName;
    }

    public synchronized void initialize(NSNotification notification) {
        if (_isInitialized)  return;

        NSLog.out.appendln("Initializing ERChangeNotificationJMS Framework");

        setEntitiesNotToSynchronize((NSArray)NSPropertyListSerialization.propertyListFromString(
                System.getProperty("er.changenotification.entitiesNotToSynchronize")));
        setChangeTypesToTrack((NSArray)NSPropertyListSerialization.propertyListFromString(
                System.getProperty("er.changenotification.changeTypesToTrack")));
        _topicName = System.getProperty("er.changenotification.jms.topicName");
        _isSubscriberDurable = "true".equals(System.getProperty("er.changenotification.jms.durableSubscribers"));
        
        String host = System.getProperty("er.changenotification.jms.serverHostName");
        if (host == null  ||  host.length() == 0) 
            host = "localhost";
        
        // Uses OpenJMS embedded JNDI server to locate the TopicConnectionFactory 
        // and Destination objects. Of course, you can use your own JNDI servers 
        // if you want. 
        // Set RMI as the protocol. Note that OpenJMS also supports TCP, HTTP and SSL 
        // protocols as well. 
        String port = "1099";
        String jndiName = "JndiServer";
        String protocol = "rmi";
        String protocolType = RmiJndiInitialContextFactory.class.getName();

        Properties props = new Properties();
        props.put(Context.PROVIDER_URL, protocol + "://" + host + ":" + port + "/" + jndiName);
        props.put(Context.INITIAL_CONTEXT_FACTORY, protocolType);
        
        NSLog.debug.appendln("props: " + props);
        
        try {
            // Open the connection to the JMS server. 
            _context = new InitialContext(props);
            TopicConnectionFactory factory = (TopicConnectionFactory)_context.lookup("JmsTopicConnectionFactory");
            if (factory == null) 
                throw new RuntimeException("Failed to locate connection factory");
            
            _topic = (Topic)_context.lookup(_topicName);
            if (_topic == null) 
                throw new RuntimeException("Failed to locate topic \"" + _topic +"\"");

            _connection = factory.createTopicConnection();
            
            // Set itself as the exception listener. 
            _connection.setExceptionListener(this);

        } catch (Exception ex) {
            NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
    
        // Create the notification publisher object and register it as the observer for 
        // the EOObjectStoreCoordinator changes. 
        _publisher = new ERCNPublisher(this);
        NSNotificationCenter.defaultCenter().addObserver(
            _publisher, 
            new NSSelector("publishChange", new Class[] { NSNotification.class } ), 
            EOObjectStoreCoordinator.ObjectsChangedInStoreNotification, 
            EOObjectStoreCoordinator.defaultCoordinator());

        // Create the notification subscriber object and register it as the observer for 
        // the distributed change notifications.  
        _subscriber = new ERCNSubscriber(this, _isSubscriberDurable);
        try {
            _subscriber.topicSubscriber().setMessageListener(_subscriber);
        } catch (JMSException ex) {
            NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        // Start to receive notifications from the connection. 
        try {
            _connection.start();
        } catch (JMSException ex) {
            NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        NSLog.out.appendln("Finished initializing ERChangeNotificationJMS Framework");

        _isInitialized = true;
    }

    /** 
     * releases JMS resouces, including closing the connection. 
     * <p>
     * This method is supposed to be called by the applicaiton's 
     * terminate method. 
     */ 
     // ENHANCEME: Should remove observers as well. 
    public synchronized void terminate() {
        if (_isTerminated)   return;
        
        try {
            _connection.stop();
         } catch (JMSException ex) {
            NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
        }
       
        _publisher.terminate();
   	_subscriber.terminate();
        
        NSLog.out.appendln("Closing the JMS connection.");
        try {
            _connection.close();
        } catch (JMSException ex) {
            NSLog.err.appendln("An exception occured: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
        }

        _isTerminated = true;
    }

    public void finalize() throws Throwable {
        if (! _isTerminated)   terminate(); 
        super.finalize();
    }

    // ENHANCEME: Should handle connection errors; try to reconnect when the 
    //            connection is interrupted. 
    public void onException(JMSException exception) {
        NSLog.err.appendln("An exception occured: " + exception.getClass().getName() + " - " + exception.getMessage());
        exception.printStackTrace();
    }

    public static EODatabaseContext databaseContextForEntityNamed(String entityName, EOEditingContext editingContext) {
        return EOUtilities.databaseContextForModelNamed(editingContext, 
                        EOModelGroup.defaultGroup().entityNamed(entityName).model().name());
    }

}
