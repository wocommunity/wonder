//
// ERXLogger.java
// Project ERExtensions
//
// Created by ak on Tue Apr 02 2002
//
package er.extensions;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

/**
 * Custom subclass of Category. The main reason for this class
 * is to isolate the log4j dependency to only this class. This
 * gives us the freedom in the future to switch logging systems
 * and this should be the only effected class .. in theory.
 */
public class ERXLogger extends org.apache.log4j.Category {

    /** logging supprt */
    public static Category cat;
    
    /**
     * Sets the default category factory so that ERXLogger
     * classes are created instead of Category classes. Also
     * configures the log4j logging system.
     */
    static {
        // (ak): Why, just why do we need this
        Category.defaultHierarchy.setCategoryFactory(new ERXLogger.Factory());
        ERXLog4j.configureLogging();
        cat = Category.getInstance("er.utilities.log4j.ERXLogger");
    }

    /**
     * CategoryFactory subclass that creates ERXLogger objects
     * instead of the default Category classes.
     */
    public static class Factory implements org.apache.log4j.spi.CategoryFactory {

        /**
         * Overriden method used to create new ERXLogger classes.
         * @param name to create the new category instance for
         * @return new ERXlogger object for the given name
         */
        public Category makeNewCategoryInstance(String name) {
            if (cat != null && cat.isDebugEnabled())
                cat.debug("makeNewCategoryInstance: " + name);
            return new ERXLogger(name);
        }
    }

    /**
     * Main entry point for getting an ERXLogger for a given name.
     * This works identical to {@link org.apache.log4j.Category$getInstance}
     * except that an ERXLogger is returned instead of a Category.
     * Note that if the log4j system has not been setup correctly, meaning
     * the LoggerFactory subclass has not been correctly put in place, then
     * RuntimeException will be thrown.
     * @param name to create the logger for
     * @return ERXlogger for the given name.
     */
    public static ERXLogger getLogger(String name) {
        org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance(name);
        if(logger != null && !(logger instanceof ERXLogger))
            throw new RuntimeException("Can't load Logger for \""+name+"\" because it is not of class ERXLogger but \""+logger.getClass().getName()+"\". Let your Application class inherit from ERXApplication or call ERXLog4j.configureLogging() statically the first thing in your app. \nAlso check if there is a \"log4j.categoryFactory=er.extensions.ERXLogger$Factory\" line in your properties.");
        return (ERXLogger)logger;
    }

    /**
     * Creates a logger for a given class object. Gets a logger
     * for the fully qualified class name of the given class.
     * @param clazz Class object to create the logger for
     * @return logger for the given class name
     */
    public static ERXLogger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Creates a logger for the given class object plus a restricting
     * subtopic. For instance if you had the class <code>a.b.Foo</code>
     * and you wanted to create a logger for the subtopic 'utilities' for
     * the class Foo then the created logging logger would have the path:
     * <code>a.b.Foo.utilities</code>.
     * @param clazz Class object to create the logger for
     * @param subTopic to restrict the current logger to
     * @return logger for the given class and subtopic
     */
    //ENHANCEME: We could do something more useful here...
    public static ERXLogger getLogger(Class clazz, String subTopic) {
        return getLogger(clazz.getName() + (subTopic != null && subTopic.length() > 0 ? "."+ subTopic : null));
    }
    
    /**
     * Default constructor. Constructs a logger
     * for the given name.
     * @param name of the logging logger
     */
    public ERXLogger(String name) {
        super(name);
    }
}
