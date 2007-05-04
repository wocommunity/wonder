//
// ERXLogger.java
// Project ERExtensions
//
// Created by ak on Tue Apr 02 2002
//
package er.extensions;

import java.util.Properties;
import org.apache.log4j.*;
import com.webobjects.foundation.NSLog;


/**
 * Custom subclass of Logger. The main reason for this class
 * is to isolate the log4j dependency to only this class. This
 * gives us the freedom in the future to switch logging systems
 * and this should be the only effected class .. in theory.
 */
public class ERXLogger extends org.apache.log4j.Logger {

    /** logging supprt */
    public static Logger log;
    public static Factory factory = null;
    static {
        String factoryClassName = System.getProperty("log4j.loggerFactory");
        if(factoryClassName == null) {
            factoryClassName = ERXLogger.Factory.class.getName();
        }
        try {
            factory = (Factory)Class.forName(factoryClassName).newInstance();
        } catch(Exception ex) {
            System.err.println("Exception while creating logger factory of class " + factoryClassName + ": " + ex);
        }
    }
    
    private static boolean _isFirstTimeConfig = true;
    
    /**
     * Sets the default Logger factory so that ERXLogger
     * classes are created instead of Logger classes. Also
     * configures the log4j logging system.
     */
    static {
        configureLogging(System.getProperties());
    }

    /**
     * LoggerFactory subclass that creates ERXLogger objects
     * instead of the default Logger classes.
     */
    public static class Factory implements org.apache.log4j.spi.LoggerFactory {

        /**
         * Overriden method used to create new ERXLogger classes.
         * @param name to create the new Logger instance for
         * @return new ERXLogger object for the given name
         */
        public Logger makeNewLoggerInstance(String name) {
            if (log != null && log.isDebugEnabled())
                log.debug("makeNewLoggerInstance: " + name);
            return new ERXLogger(name);
        }
    }

    /**
     * Main entry point for getting an ERXLogger for a given name.
     * Calls getLogger to return the instance of Logger from our custom Factory.
     * 
     * Note that if the log4j system has not been setup correctly, meaning
     * the LoggerFactory subclass has not been correctly put in place, then
     * RuntimeException will be thrown.
     * @param name to create the logger for
     * @return ERXLogger for the given name.
     */
     public static ERXLogger getERXLogger(String name) {
         Logger logger = getLogger(name);
         if(logger != null && !(logger instanceof ERXLogger))
            throw new RuntimeException("Can't load Logger for \""+name+"\" because it is not of class ERXLogger but \""+logger.getClass().getName()+"\". Let your Application class inherit from ERXApplication or call ERXLog4j.configureLogging() statically the first thing in your app. \nAlso check if there is a \"log4j.loggerFactory=er.extensions.ERXLogger$Factory\" line in your properties.");
        return (ERXLogger)logger;
    }

    /**
     *  Overrides method of superclass to return a logger using our
     *  custom ERXLogger$Factory class.
     *  This works identical to
     * {@link org.apache.log4j.Logger#getLogger log4.Logger.getLogger}
     *	@param name to create the logger for
     *  @return Logger for the given name.
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name, factory);
    }

    /**
     * Creates a logger for a given class object. Gets a logger
     * for the fully qualified class name of the given class.
     * @param clazz Class object to create the logger for
     * @return logger for the given class name
     */
    public static ERXLogger getERXLogger(Class clazz) {
        return (ERXLogger)getLogger(clazz.getName());
    }

    public static Logger getLogger(Class clazz) {
        return (ERXLogger)getERXLogger(clazz);
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
    public static ERXLogger getERXLogger(Class clazz, String subTopic) {
        return (ERXLogger)getERXLogger(clazz.getName() + (subTopic != null && subTopic.length() > 0 ? "."+ subTopic : null));
    }
    public static ERXLogger getLogger(Class clazz, String subTopic) {
        return getERXLogger(clazz, subTopic);
    }
    
    /**
     * Default constructor. Constructs a logger
     * for the given name.
     * @param name of the logging logger
     */
    public ERXLogger(String name) {
        super(name);
    }

    /** 
     * Sets up the logging system with the given configuration 
     * in {@link java.util.Properties} format. 
     * 
     * @param  properties with the logging configuration 
     */
    public static synchronized void configureLogging(Properties properties) {
        if (_isFirstTimeConfig) {
            if (!ERXProperties.webObjectsVersionIs522OrHigher()) {
                BasicConfigurator.configure();
                Logger.getRootLogger().setLevel(Level.INFO);
            }

            if(ERXProperties.webObjectsVersionIs522OrHigher()) {
                NSLog.setDebug(new ERXNSLogLog4jBridge(ERXNSLogLog4jBridge.DEBUG));
                NSLog.setOut(new ERXNSLogLog4jBridge(ERXNSLogLog4jBridge.OUT));
                NSLog.setErr(new ERXNSLogLog4jBridge(ERXNSLogLog4jBridge.ERR));
            }
            
            _isFirstTimeConfig = false;
        } else {
            LogManager.resetConfiguration();
        }
        PropertyConfigurator.configure(properties);
        
        if (log == null) 
            log = Logger.getLogger(ERXLogger.class.getName(), factory);

        log.info("Updated the logging configuration with the current system properties.");
        if(log.isDebugEnabled()) {
            log.debug("log4j.loggerFactory: " + System.getProperty("log4j.loggerFactory"));
            log.debug("Factory: " + factory);
            log.debug("", new RuntimeException());
        }
        //PropertyPrinter printer = new PropertyPrinter(new PrintWriter(System.out));
        //printer.print(new PrintWriter(System.out));


    }

    /**
     * Dumps an Throwable's Stack trace on the appender if debugging is enabled.
     * @param throwable throwable to dump
     */
    public void debugStackTrace(Throwable throwable) {
        if(isDebugEnabled())
            throwable.printStackTrace();
    }
}
