package er.extensions.logging;

import er.extensions.foundation.ERXSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Properties;

/**
 * Custom subclass of Logger. The main reason for this class is to isolate the
 * log4j dependency to only this class. This gives us the freedom in the future
 * to switch logging systems and this should be the only effected class .. in
 * theory.
 */
public class ERXLogger implements org.slf4j.Logger {

    public static final String CONFIGURE_LOGGING_WITH_SYSTEM_PROPERTIES = "configureLoggingWithSystemProperties";

    /**
     * Main entry point for getting a Logger for a given name. Calls getLogger
     * to return the instance of Logger.
     *
     * @param name to create the logger for
     * @return Logger for the given name.
     */
    public static ERXLogger getERXLogger(String name) {
        ERXLogger logger = new ERXLogger(name);
        ERXLoggingUtilities.configureLoggingWithSystemProperties();

        return logger;
    }

    /**
     * Overrides method of superclass to return a logger using the {@code org.slf4j.LoggerFactory} class.
     * This works identical to {@link org.slf4j.LoggerFactory#getLogger(String)}
     *
     * @param name to create the logger for
     * @return Logger for the given name.
     */
    public static Logger getLogger(String name) {
        return ERXLogger.getERXLogger(name);
    }

    /**
     * Creates a logger for a given class object. Gets a logger for the fully
     * qualified class name of the given class.
     *
     * @param clazz Class object to create the logger for
     * @return logger for the given class name
     */
    public static ERXLogger getERXLogger(Class clazz) {
        return ERXLogger.getERXLogger(clazz.getName());
    }

    public static Logger getLogger(Class clazz) {
        return ERXLogger.getERXLogger(clazz);
    }

    /**
     * Creates a logger for the given class object plus a restricting subtopic.
     * For instance if you had the class <code>a.b.Foo</code> and you wanted to
     * create a logger for the subtopic 'utilities' for the class Foo then the
     * created logging logger would have the path:
     * <code>a.b.Foo.utilities</code>.
     *
     * @param clazz    Class object to create the logger for
     * @param subTopic to restrict the current logger to
     * @return logger for the given class and subtopic
     */
    // ENHANCEME: We could do something more useful here...
    public static ERXLogger getERXLogger(Class clazz, String subTopic) {
        return ERXLogger.getERXLogger(clazz.getName() + (subTopic != null && subTopic.length() > 0 ? "." + subTopic : null));
    }

    public static void configureLoggingWithSystemProperties() {
        ERXLoggingUtilities.configureLogging(ERXSystem.getProperties());
    }

    /**
     * Sets up the logging system with the given configuration in
     * {@link java.util.Properties} format.
     *
     * @param properties with the logging configuration
     */
    public static void configureLogging(Properties properties) {
        ERXLoggingUtilities.configureLogging(properties);
    }

    private final Logger logger;

    /**
     * Default constructor. Constructs a logger for the given name.
     *
     * @param name of the logging logger
     */
    public ERXLogger(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
    }

    /**
     * Dumps a Throwable's Stack trace on the appender if debugging is enabled.
     *
     * @param throwable throwable to dump
     */
    public void debugStackTrace(Throwable throwable) {
        if (isDebugEnabled()) {
            logger.debug("", throwable);
        }
    }
}
