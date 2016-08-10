//
// ERCNNotificationCoordinator.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import javax.jms.Topic;
import javax.jms.TopicConnection;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

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
public class ERCNNotificationCoordinator {

    static final String LOG_HEADER = "ERChangeNotification: ";

    private static final ERCNNotificationCoordinator _coordinator = new ERCNNotificationCoordinator();

    private ERCNConfiguration _configuration;
    private ERCNConnectionKeeper _connectionKeeper;
    private ERCNPublisher _publisher;
    private ERCNSubscriber _subscriber;

    private boolean _isInitialized = false;
    private boolean _isTerminated = false;

    static {
        NSLog.debug.appendln(LOG_HEADER + "Registering the observer to initialize ERChangeNotification Framework");

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

    protected String id() {
        WOApplication app = WOApplication.application();
        String host = app.host();
        Number port = app.port();
        String appName = app.name();
        return host + ":" + port + "/" + appName;
    }

    public synchronized void initialize(NSNotification notification) {
        if (_isInitialized)  return;

        NSLog.out.appendln(LOG_HEADER + "Initializing ERChangeNotification framework");

        // Create the notification publisher object and register it as the observer for
        // the EOObjectStoreCoordinator changes. 
        _publisher = new ERCNPublisher(this);

        NSNotificationCenter.defaultCenter().addObserver(
            this,
            new NSSelector("publishChange", new Class[] { NSNotification.class } ), 
            EOObjectStoreCoordinator.ObjectsChangedInStoreNotification, 
            EOObjectStoreCoordinator.defaultCoordinator());

        // Create the notification subscriber object and register it as the observer for 
        // the distributed change notifications.  
        _subscriber = new ERCNSubscriber(this);

        // Create the connection keeper object and initiate connection.
        _connectionKeeper = new ERCNConnectionKeeper(this);

        _connectionKeeper.openConnection(ERCNConnectionKeeper.VERBOSE_LOGGING);
        if (! _connectionKeeper.isConnected())
            _connectionKeeper.initiateRecoveryTask();

        NSLog.out.appendln(LOG_HEADER + "Finished initializing ERChangeNotificationJMS framework");

        _isInitialized = true;
    }

    void didConnect(TopicConnection connection) {
        _subscriber.subscribe(connection);
    }

    void didDisconnect(TopicConnection connection) {
        _subscriber.unsubscribe();
    }

    /**
     * releases JMS resouces, including closing the connection. 
     * <p>
     * This method is supposed to be called by the application's 
     * terminate method. 
     */ 
     // ENHANCEME: Should remove observers as well. 
    public synchronized void terminate() {
        if (_isTerminated)   return;

        _connectionKeeper.stopConnection(ERCNConnectionKeeper.VERBOSE_LOGGING);

        _publisher.terminate();
   	    _subscriber.terminate();
        
        NSLog.out.appendln(LOG_HEADER + "Closing the JMS connection.");
        _connectionKeeper.closeConnection(ERCNConnectionKeeper.VERBOSE_LOGGING);
        _connectionKeeper.terminate();

        _isTerminated = true;
    }

    @Override
    public void finalize() throws Throwable {
        if (! _isTerminated)   terminate();
        super.finalize();
    }

    public void publishChange(NSNotification notification) {
        _publisher.publishChange(notification);
    }

    public ERCNConfiguration configuration() {
        if (_configuration == null)
            _configuration = ERCNConfiguration.getInstance();
        return _configuration;
    }

    public ERCNSubscriberDelegate subscriberDelegate() {
        return ERCNSubscriber.delegate();
    }

    public void setSubscriberDelegate(ERCNSubscriberDelegate delegate) {
        ERCNSubscriber.setDelegate(delegate);
    }

    Topic topic() {
        return _connectionKeeper.topic();
    }

    TopicConnection connection() {
        return _connectionKeeper.connection();
    }

    public boolean isConnected() {
        return _connectionKeeper.isConnected();
    }

    public static EODatabaseContext databaseContextForEntityNamed(String entityName, EOEditingContext editingContext) {
        return EOUtilities.databaseContextForModelNamed(editingContext,
                        EOModelGroup.defaultGroup().entityNamed(entityName).model().name());
    }

}
