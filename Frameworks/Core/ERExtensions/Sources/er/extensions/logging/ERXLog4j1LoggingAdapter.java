package er.extensions.logging;

import org.apache.log4j.*;
import org.apache.log4j.config.PropertyPrinter;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

class ERXLog4j1LoggingAdapter implements ERXLoggingAdapter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ERXLogger.class);

    public void setLevel(org.slf4j.Logger aLogger, org.slf4j.event.Level level) {
        Logger log4jLogger = ERXLoggingUtilities.toUnderlingLoggerOfType(aLogger, Logger.class);
        Level log4jLevel = toLog4j1Level(level);

        log4jLogger.setLevel(log4jLevel);
    }

    public void reset() {
        // ak: telling Log4J to re-init the Console appenders, so we get logging into WOOutputPath again
        for (Enumeration e = Logger.getRootLogger().getAllAppenders(); e.hasMoreElements(); ) {
            Appender appender = (Appender) e.nextElement();

            if (appender instanceof ConsoleAppender) {
                ConsoleAppender app = (ConsoleAppender) appender;
                app.activateOptions();
            }
        }
    }

    public void configureLogging(Properties properties) {
        LogManager.resetConfiguration();
        BasicConfigurator.configure();

        // AK: we re-configure the logging a few lines later from the properties,
        // but in case no config is set, we set the root level to info, install the brigde
        // which sets its own logging level to DEBUG and the output should be pretty
        // much the same as with plain WO
        Logger.getRootLogger().setLevel(Level.INFO);

        PropertyConfigurator.configure(properties);

        // AK: if the root logger has no appenders, something is really broken
        // most likely the properties didn't read correctly.
        if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
            Appender appender = new ConsoleAppender(new ERXPatternLayout("%-5p %d{HH:mm:ss} (%-20c:%L):  %m%n"), "System.out");
            Logger.getRootLogger().addAppender(appender);
            Logger.getRootLogger().setLevel(Level.DEBUG);
            Logger.getRootLogger().error("Logging prefs couldn't get read from properties, using defaults");
        }

        log.info("Updated the logging configuration with the current system properties.");

        PropertyPrinter printer = new PropertyPrinter(new PrintWriter(System.out));
        printer.print(new PrintWriter(System.out));
    }

    private Level toLog4j1Level(org.slf4j.event.Level level) {
        switch (level) {
            case TRACE:
                return Level.TRACE;
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            default:
                throw new IllegalStateException("Level " + level + " cannot be converted to " + Level.class.getName());
        }
    }
}
