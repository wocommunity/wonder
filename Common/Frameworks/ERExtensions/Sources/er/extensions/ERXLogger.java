//
// ERXLogger.java
// Project ERExtensions
//
// Created by ak on Tue Apr 02 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import org.apache.log4j.*;
import org.apache.log4j.spi.*;

public class ERXLogger extends org.apache.log4j.Category {
    public ERXLogger(String name) {
        super(name);
    }

    public static ERXLogger getLogger(String name) {
        org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance(name);
        if(logger != null && !(logger instanceof ERXLogger))
            throw new RuntimeException("Can't load Logger for \""+name+"\" because it is not of class ERXLogger but \""+logger.getClass().getName()+"\". Let your Application class inherit from ERXApplication or call ERXLog4j.configureLogging() statically the first thing in your app.");
        return (ERXLogger)logger;
    }

    public static ERXLogger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    // we could do something more useful here...
    public static ERXLogger getLogger(Class clazz, String subTopic) {
        return getLogger(clazz.getName() +"."+ subTopic);
    }
}
