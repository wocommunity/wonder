package er.extensions.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Properties;

class ERXLog4j2LoggingAdapter implements ERXLoggingAdapter {
    @Override
    public void setLevel(org.slf4j.Logger aLogger, org.slf4j.event.Level level) {
        Logger log4j2Logger = ERXLoggingUtilities.toUnderlyingLoggerOfType(aLogger, Logger.class);
        Level log4j2Level = toLog4j2Level(level);

        Configurator.setLevel(log4j2Logger.getName(), log4j2Level);
    }

    @Override
    public void reset() {
        // Not supported
    }

    @Override
    public void configureLogging(Properties properties) {
        // Not supported
    }

    private Level toLog4j2Level(org.slf4j.event.Level level) {
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
