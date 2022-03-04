package er.extensions.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import java.util.Properties;

class ERXLogbackLoggingAdapter implements ERXLoggingAdapter {
    @Override
    public void setLevel(org.slf4j.Logger aLogger, org.slf4j.event.Level level) {
        Logger logbackLogger = (Logger) aLogger;
        Level logbackLevel = toLogbackLevel(level);

        logbackLogger.setLevel(logbackLevel);
    }

    @Override
    public void reset() {
        // Not supported
    }

    @Override
    public void configureLogging(Properties properties) {
        // Not supported
    }

    private Level toLogbackLevel(org.slf4j.event.Level level) {
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
