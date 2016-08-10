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

final class ERCNConfigurationV1 extends ERCNConfiguration {

    ERCNConfigurationV1(Properties props) {

        setEntitiesNotToSynchronize((NSArray)NSPropertyListSerialization.propertyListFromString(
                props.getProperty(PROPERTY_PREFIX + ".entitiesNotToSynchronize")));

        NSArray changeTypesToTrack = (NSArray)NSPropertyListSerialization.propertyListFromString(
                props.getProperty(PROPERTY_PREFIX + ".changeTypesToTrack"));

        setChangeTypesToPublish(changeTypesToTrack);
        setChangeTypesToSubscribe(changeTypesToTrack);

        setTopicName(props.getProperty(PROPERTY_PREFIX + ".jms.topicName"));

        setConnectionRecoveryInterval(props.getProperty(PROPERTY_PREFIX + ".connectionRecoveryInterval"));
        setDisconnectionWarningInterval(props.getProperty(PROPERTY_PREFIX + ".disconnectionWarningInternal"));
        setIsSubscriberDurable("true".equals(props.getProperty(PROPERTY_PREFIX + ".jms.durableSubscribers")));

        String host = System.getProperty(PROPERTY_PREFIX + ".jms.serverHostName", "localhost");

        // Uses OpenJMS embedded JNDI server to locate the TopicConnectionFactory
        // and Destination objects. Of course, you can use your own JNDI servers
        // if you want.
        // Set RMI as the protocol. Note that OpenJMS also supports TCP, HTTP and SSL
        // protocols as well.
        String protocol = "rmi";
        String port = "1099";
        String jndiName = "JndiServer";

        setProviderURL(protocol + "://" + host + ":" + port + "/" + jndiName);

        setInitialContextFactory("org.exolab.jms.jndi.rmi.RmiJndiInitialContextFactory");

    }

}
