package er.extensions.logging;

import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSNotificationCenter;
import er.extensions.foundation.ERXSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.impl.StaticLoggerBinder;

import java.lang.reflect.Field;
import java.util.Properties;

import er.extensions.foundation.ERXConfigurationManager;

public class ERXLoggingUtilities {
    private enum Slf4jBinding {
        LOG4J_1, LOG4J_2, LOGBACK, RELOAD4J, UNKNOWN;

        public static Slf4jBinding currentBinding() {
            String loggerBinderName;

            try {
                loggerBinderName = StaticLoggerBinder.getSingleton().getLoggerFactory().getClass().getName();
            } catch (NoClassDefFoundError exception) {
                // If no binding is found on the class path, then slf4j will default to a no-operation implementation.
                return UNKNOWN;
            }

            switch (loggerBinderName) {
                case "org.slf4j.impl.Log4jLoggerFactory":
                    return LOG4J_1;
                case "org.apache.logging.slf4j.Log4jLoggerFactory":
                    return LOG4J_2;
                case "ch.qos.logback.classic.LoggerContext":
                    return LOGBACK;
                case "org.slf4j.impl.Reload4jLoggerFactory":
                    return RELOAD4J;
                default:
                    return UNKNOWN;
            }
        }
    }

    public static final String CONFIGURE_LOGGING_WITH_SYSTEM_PROPERTIES = "configureLoggingWithSystemProperties";

    private static final ERXLoggingAdapter loggingAdapter = createLoggingAdapterFor(Slf4jBinding.currentBinding());

    private static final Logger log = LoggerFactory.getLogger(ERXLoggingUtilities.class);

    public static void setLevel(Logger aLogger, Level level) {
        loggingAdapter.setLevel(aLogger, level);
    }

    public static void configureLoggingWithSystemProperties() {
        configureLogging(ERXSystem.getProperties());
    }

    /**
     * Sets up the logging system with the given configuration in
     * {@link java.util.Properties} format.
     *
     * @param properties with the logging configuration
     */
    public static synchronized void configureLogging(Properties properties) {
        int allowedLevel = NSLog.debug.allowedDebugLevel();

        if (!(NSLog.debug instanceof ERXNSLogSlf4jBridge)) {
            NSLog.setOut(new ERXNSLogSlf4jBridge(ERXNSLogSlf4jBridge.OUT));
            NSLog.setErr(new ERXNSLogSlf4jBridge(ERXNSLogSlf4jBridge.ERR));
            NSLog.setDebug(new ERXNSLogSlf4jBridge(ERXNSLogSlf4jBridge.DEBUG));
        }

        NSLog.debug.setAllowedDebugLevel(allowedLevel);

        loggingAdapter.configureLogging(properties);

        NSNotificationCenter.defaultCenter().postNotification(ERXConfigurationManager.ConfigurationDidChangeNotification, null);
    }

    public static void resetLoggingAdapter() {
        loggingAdapter.reset();
    }

    static <T> T toUnderlyingLoggerOfType(Logger logger, Class<T> loggerClass) {
        try {
            Class<? extends Logger> loggerType = logger.getClass();
            Field fields[] = loggerType.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                String fieldName = fields[i].getName();

                if (fieldName.equals("logger")) {
                    fields[i].setAccessible(true);

                    return loggerClass.cast(fields[i].get(logger));
                }
            }
        } catch (Exception exception) {
            log.error("Cannot extract a " + loggerClass.getName() + " from " + Logger.class.getName(), exception);
        }

        return null;
    }

    private static ERXLoggingAdapter createLoggingAdapterFor(Slf4jBinding binding) {
        switch (binding) {
            case LOG4J_1:
            case RELOAD4J:
                return new ERXLog4j1LoggingAdapter();
            case LOG4J_2:
                return new ERXLog4j2LoggingAdapter();
            case LOGBACK:
                return new ERXLogbackLoggingAdapter();
            default:
                return new ERXNoopLoggingAdapter();
        }
    }

    private ERXLoggingUtilities() {
        throw new UnsupportedOperationException("Cannot create an instance of " + getClass().getName());
    }
}
