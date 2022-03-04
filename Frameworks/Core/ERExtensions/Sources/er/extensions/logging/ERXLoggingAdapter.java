package er.extensions.logging;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Properties;

interface ERXLoggingAdapter {
    void setLevel(Logger aLogger, Level level);

    void reset();

    void configureLogging(Properties properties);
}