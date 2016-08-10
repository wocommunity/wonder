//
// ERCNNotificationCoordinator.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Mar 6 2004
//
package er.changenotification;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;

public abstract class ERCNConfiguration {

    public static final String PROPERTY_PREFIX = "er.changenotification";

    public static final long DEFAULT_CONNECTION_RECOVERY_INTERVAL = 15;  // 15 seconds
    public static final long DEFAULT_DISCONNECTION_WARNING_INTERVAL = 60 * 60;  // 1 hour

    private NSArray _entitiesNotToSynchronize;
    private NSArray _changeTypesToPublish;
    private NSArray _changeTypesToSubscribe;

    private String _topicName;
    private boolean _isSubscriberDurable;

    private String _initialContextFactory;
    private String _providerURL;

    private long _connectionRecoveryInterval = DEFAULT_CONNECTION_RECOVERY_INTERVAL;
    private long _disconnectionWarningInterval = DEFAULT_DISCONNECTION_WARNING_INTERVAL;

    public static class Version {

        private static final Pattern _versionPattern = Pattern.compile("^c([0-9]+)\\.([.0-9]*)$");

        private final int _majorVersion;
        private final String _minorVersionString;

        public Version(String configurationVasionString) {

            Matcher versionMatcher = _versionPattern.matcher(configurationVasionString);

            if (versionMatcher.find()) {
                _majorVersion = Integer.parseInt(versionMatcher.group(1));
                _minorVersionString = versionMatcher.group(2);
            } else {
                _majorVersion = 1;
                _minorVersionString = "0";
            }

        }

        public int majorVersion() {
            return _majorVersion;
        }

        public String minorVersionString() {
            return _minorVersionString;
        }

        @Override
        public String toString() {
            return _majorVersion + "." + _minorVersionString;
        }

    }

    public static ERCNConfiguration getInstance() {
        return getInstance(System.getProperties());
    }

    public static ERCNConfiguration getInstance(Properties props) {

        ERCNConfiguration configuration = null;

        String versionString = props.getProperty("er.changenotification.confVersion", "c1.0");
        Version configVersion = new Version(versionString);

        if (configVersion.majorVersion() == 1) {
            configuration = new ERCNConfigurationV1(props);
        } else if (configVersion.majorVersion() == 2) {
            configuration = new ERCNConfigurationV2(props);
        } else {
            throw new RuntimeException(ERCNNotificationCoordinator.LOG_HEADER + "Invalid configuration version: "
                    + versionString);
        }

        return configuration;

    }

    public NSArray entitiesNotToSynchronize() {
        return _entitiesNotToSynchronize;
    }

    public NSArray changeTypesToPublish() {
        return _changeTypesToPublish;
    }

    public NSArray changeTypesToSubscribe() {
        return _changeTypesToSubscribe;
    }

    public String topicName() {
        return _topicName;
    }

    public boolean isSubscriberDurable() {
        return _isSubscriberDurable;
    }

    public String initialContextFactory() {
        return _initialContextFactory;
    }

    public String providerURL() {
        return _providerURL;
    }

    public long connectionRecoveryInterval() {
        return _connectionRecoveryInterval;
    }

    public long disconnectionWarningInterval() {
        return _disconnectionWarningInterval;
    }

    public Properties jmsProperties() {
        Properties properties = new Properties();
        properties.put(Context.PROVIDER_URL, _providerURL);
        properties.put(Context.INITIAL_CONTEXT_FACTORY, _initialContextFactory);
        return properties;
    }


    void setEntitiesNotToSynchronize(NSArray entities) {
        if (entities != null)
            _entitiesNotToSynchronize = entities;
        else
            _entitiesNotToSynchronize = NSArray.EmptyArray;
    }

    void setChangeTypesToPublish(NSArray changeTypes) {
        if (changeTypes != null)
            _changeTypesToPublish = changeTypes;
        else
            _changeTypesToPublish = NSArray.EmptyArray;
    }

    void setChangeTypesToSubscribe(NSArray changeTypes) {
        if (changeTypes != null)
            _changeTypesToSubscribe = changeTypes;
        else
            _changeTypesToSubscribe = NSArray.EmptyArray;
    }

    void setTopicName(String topicName) {
        _topicName = topicName;
    }

    void setIsSubscriberDurable(boolean isDurable) {
        _isSubscriberDurable = isDurable;
    }

    void setInitialContextFactory(String initialContextFactory) {
        _initialContextFactory = initialContextFactory;
    }

    void setProviderURL(String providerURL) {
        _providerURL = providerURL;
    }

    void setConnectionRecoveryInterval(long interval) {
         _connectionRecoveryInterval = interval;
    }

    void setConnectionRecoveryInterval(String intervalSting) {
        long interval = DEFAULT_CONNECTION_RECOVERY_INTERVAL;
        if (intervalSting != null) {
            try {
                interval = Long.parseLong(intervalSting);
            } catch (NumberFormatException ex) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "Invalid number format for connection recovery interval.");
            }
            setConnectionRecoveryInterval(interval);
        }
    }

    void setDisconnectionWarningInterval(long interval) {
        _disconnectionWarningInterval = interval;
    }

    void setDisconnectionWarningInterval(String intervalString) {
        long interval = DEFAULT_DISCONNECTION_WARNING_INTERVAL;
        if (intervalString != null) {
            try {
                interval = Long.parseLong(intervalString);
            } catch (NumberFormatException ex) {
                NSLog.err.appendln(ERCNNotificationCoordinator.LOG_HEADER
                        + "Invalid number format for connection recovery interval.");
            }
            setDisconnectionWarningInterval(interval);
        }
    }

}
