//
// ERCNNotificationCoordinator.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Mar 6 2004
//
package er.changenotification;

import java.util.Properties;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSPropertyListSerialization;

final class ERCNConfigurationV2 extends ERCNConfiguration {

    ERCNConfigurationV2(Properties props) {

        setEntitiesNotToSynchronize((NSArray)NSPropertyListSerialization.propertyListFromString(
                props.getProperty(PROPERTY_PREFIX + ".entitiesNotToSynchronize")));

        setChangeTypesToPublish((NSArray)NSPropertyListSerialization.propertyListFromString(
                props.getProperty(PROPERTY_PREFIX + ".changeTypesToPublish")));

        setChangeTypesToSubscribe((NSArray)NSPropertyListSerialization.propertyListFromString(
                props.getProperty(PROPERTY_PREFIX + ".changeTypesToSubscribe")));

        setTopicName(props.getProperty(PROPERTY_PREFIX + ".jms.topicName"));

        setConnectionRecoveryInterval(props.getProperty(PROPERTY_PREFIX + ".connectionRecoveryInterval"));
        setDisconnectionWarningInterval(props.getProperty(PROPERTY_PREFIX + ".disconnectionWarningInternal"));
        setIsSubscriberDurable("true".equals(props.getProperty(PROPERTY_PREFIX + ".jms.durableSubscribers")));

        setProviderURL(props.getProperty(PROPERTY_PREFIX + ".jms.providerURL", "rmi://localhost:1099/JndiServer"));


        setInitialContextFactory(props.getProperty(PROPERTY_PREFIX + ".jms.initialContextFactory",
                "org.exolab.jms.jndi.rmi.RmiJndiInitialContextFactory"));

    }

}

