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

// ATTENTION: to use ERXLogger.getLogger(), you must add to your config file
//    log4j.categoryFactory=er.extensions.ERXLog4j$Factory

public class ERXLogger extends org.apache.log4j.ERXLog4JCategory {
    public ERXLogger(String name) {
        super(name);
    }

    public static ERXLogger getLogger(String name) {
        return (ERXLogger)org.apache.log4j.Category.getInstance(name);
    }

    public static ERXLogger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    // we could do something more useful here...
    public static ERXLogger getLogger(Class clazz, String subTopic) {
        return getLogger(clazz.getName() +"."+ subTopic);
    }
}
