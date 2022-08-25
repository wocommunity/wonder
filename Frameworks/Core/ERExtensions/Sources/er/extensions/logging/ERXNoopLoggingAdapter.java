package er.extensions.logging;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Properties;

class ERXNoopLoggingAdapter implements ERXLoggingAdapter {
    @Override
    public void setLevel(Logger aLogger, Level level) {
        // Do nothing
    }

    @Override
    public void reset() {
        // Do nothing
    }

    @Override
    public void configureLogging(Properties properties) {
        // Do nothing
    }
}
