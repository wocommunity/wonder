//
// ERCNNotificationCoordinator.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Mar 6 2004
//
package er.changenotification;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.ConnectionMetaData;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSTimestamp;

class ERCNConnectionKeeper implements ExceptionListener {

    public static final boolean VERBOSE_LOGGING = true;
    public static final boolean QUIET_LOGGING = false;

    private ERCNNotificationCoordinator _coordinator;
    private Timer _recoveryTimer;

    private Topic _topic;
    private TopicConnection _connection;

    private boolean _isConnected = false;

    ERCNConnectionKeeper(ERCNNotificationCoordinator coordinator) {
        _coordinator = coordinator;
    }

    // Open the connection to the JMS server.
    void openConnection(boolean verboseLogging) {
        if (_isConnected) {
            stopConnection(QUIET_LOGGING);
            closeConnection(QUIET_LOGGING);
            _isConnected = false;
        }

        Properties properties = _coordinator.configuration().jmsProperties();
        //NSLog.debug.appendln(ERCNNotificationCoordinator.LOG_HEADER + "properties: " + properties);

        TopicConnectionFactory connectionFactory = null;
        try {
            Context context = new InitialContext(properties);
            connectionFactory = (TopicConnectionFactory) context.lookup("JmsTopicConnectionFactory");
            _topic = (Topic) context.lookup(_coordinator.configuration().topicName());
        } catch (CommunicationException ex) {
            // javax.naming.CommunicationException -- no JNDI server
            if (verboseLogging) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "Cannot connect to the JNDI server. Please check if the JNDI server is available: "
                        + ex.getMessage());
            }
            return;
        } catch (NameNotFoundException ex) {
            // javax.naming.NameNotFoundException -- no topic
            throw new RuntimeException("Cannot find the topic with name \"" + _coordinator.configuration().topicName() + "\"."
                    + "Please check if the JMS server is properly configured: " + ex.getMessage());
        } catch (NamingException ex) {
            if (verboseLogging) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "An exception occurred while locating the topic with JNDI server: " + ex.getMessage());
            }
            return;
        }

        String providerName = null;
        String providerVersion = null;
        try {
            _connection = connectionFactory.createTopicConnection();
            ConnectionMetaData metaData = _connection.getMetaData();
            providerName = metaData.getJMSProviderName();
            providerVersion = metaData.getProviderVersion();

            // Set itself as the exception listener.
            _connection.setExceptionListener(this);

        } catch (JMSException ex) {
            if (verboseLogging) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "An exception occured while creating a JMS connection: " + ex.getMessage());
            }
            return;
        }

        try {
            _connection.start();
            _coordinator.didConnect(_connection);
        } catch (JMSException ex) {
            if (verboseLogging) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "An exception occured while starting the JMS connection : " + ex.getMessage());
                ex.printStackTrace();
            }
            return;
        }

        NSLog.out.appendln(ERCNNotificationCoordinator.LOG_HEADER + "Connected to the JMS server: "
                + providerName + " " + providerVersion);

        _isConnected = true;
    }

    void stopConnection(boolean verboseLogging) {
        if (_connection == null)   return;

        try {
            _connection.stop();
         } catch (JMSException ex) {
            if (verboseLogging) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "An exception occured while stopping the JMS connection: " + ex.getMessage());
            }
        }
    }

    void closeConnection(boolean verboseLogging) {
        if (_connection == null)   return;

        try {
            _connection.close();
        } catch (JMSException ex) {
            if (verboseLogging) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                    + "An exception occured while closing the JMS connection    : " + ex.getMessage());
            }
        }
    }

    void initiateRecoveryTask() {

        NSLog.out.appendln(ERCNNotificationCoordinator.LOG_HEADER + ": Trying to connect to the JMS Server... "
                + "(Recovery interval: " + _coordinator.configuration().connectionRecoveryInterval() + " seconds)");

        if (_recoveryTimer != null)
            _recoveryTimer.cancel();

        _recoveryTimer = new Timer(true);

        // This TimerTask trys to recover the JMS connection.
        TimerTask recoveryTask = new TimerTask() {

            @Override
            public void run() {
                openConnection(QUIET_LOGGING);
                if (isConnected())
                    _recoveryTimer.cancel();
            }

        };

        // This TimerTask displays warning messages.
        TimerTask warningMessageTask = new TimerTask() {

            private NSTimestamp _downTime = new NSTimestamp();
            private String _downTimeString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(_downTime);

            @Override
            public void run() {
                if (! _isConnected) {
                    double elapsTime = (new NSTimestamp().getTime() - _downTime.getTime()) / (60.0d * 60.0d * 1000.0d);
                    String elapsTimeString = new DecimalFormat("#,##0.0").format(elapsTime);

                    NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                            + "JMS connection has been down for " + elapsTimeString + " hours. "
                            + "(Since " + _downTimeString + ")");
                }
            }

        };

        long recoveryIntervalMils = _coordinator.configuration().connectionRecoveryInterval() * 1000;
        _recoveryTimer.scheduleAtFixedRate(recoveryTask, recoveryIntervalMils, recoveryIntervalMils);

        long warningIntervalMils = _coordinator.configuration().disconnectionWarningInterval() * 1000;
        _recoveryTimer.scheduleAtFixedRate(warningMessageTask, warningIntervalMils, warningIntervalMils);

    }

    public void onException(JMSException exception) {
        NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                + "Connection Keeper has detected a problem with the current JMS connection: "
                + exception.getMessage());

        _isConnected = false;

        stopConnection(QUIET_LOGGING);
        _coordinator.didDisconnect(_connection);

        closeConnection(QUIET_LOGGING);

        _topic = null;
        _connection = null;

        initiateRecoveryTask();
    }

    Topic topic() {
        return _topic;
    }

    TopicConnection connection() {
        return _connection;
    }

    boolean isConnected() {
        return _isConnected;
    }

    synchronized void terminate() {
        // do nothing
    }

}
