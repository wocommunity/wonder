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
    public static Category cat;

    static {
        // (ak): Why, just why do we need this
        Category.defaultHierarchy.setCategoryFactory(new ERXLogger.Factory());
        ERXLog4j.configureLogging();
        cat = Category.getInstance("er.utilities.log4j.ERXLogger");
    }


    public static class Factory implements org.apache.log4j.spi.CategoryFactory {
        public Category makeNewCategoryInstance(String name) {
            if(cat != null && cat.isDebugEnabled())
                cat.debug("makeNewCategoryInstance: " + name);
            return new ERXLogger(name);
        }
    }

    public ERXLogger(String name) {
        super(name);
    }

    public static ERXLogger getLogger(String name) {
        org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance(name);
        if(logger != null && !(logger instanceof ERXLogger))
            throw new RuntimeException("Can't load Logger for \""+name+"\" because it is not of class ERXLogger but \""+logger.getClass().getName()+"\". Let your Application class inherit from ERXApplication or call ERXLog4j.configureLogging() statically the first thing in your app. \nAlso check if there is a \"log4j.categoryFactory=er.extensions.ERXLogger$Factory\" line in your properties.");
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
