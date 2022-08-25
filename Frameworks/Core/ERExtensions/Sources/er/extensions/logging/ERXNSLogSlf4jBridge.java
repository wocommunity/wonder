package er.extensions.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.webobjects.foundation.NSLog;

import er.extensions.foundation.ERXProperties;

public class ERXNSLogSlf4jBridge extends NSLog.PrintStreamLogger {
	public static final Logger log = LoggerFactory.getLogger("NSLog");
	public static final int OUT = 1;
	public static final int ERR = 2;
	public static final int DEBUG = 3;
	private final int type;

	public ERXNSLogSlf4jBridge(int type) {
		super();

		this.type = type;
	}

	@Override
	public void appendln(Object obj) {
		if (obj == null) {
			obj = "";
		}

		if (isEnabled()) {
			switch (type) {
			case OUT:
				log.info(obj.toString());
				break;
			case ERR:
				log.error(obj.toString());
				break;
			case DEBUG:
				log.debug(obj.toString());
				break;
			}
		}
		else {
			if (type == ERR)
				log.error(obj.toString());
		}
	}

	@Override
	public void setIsEnabled(boolean enabled) {
		super.setIsEnabled(enabled);

		if (type == DEBUG && !ERXProperties.booleanForKeyWithDefault("er.extensions.ERXNSLogSlf4jBridge.ignoreNSLogSettings", false)) {
			ERXLoggingUtilities.setLevel(log, enabled ? Level.DEBUG : Level.INFO);
		}
	}

	@Override
	public void setAllowedDebugLevel(int debugLevel) {
		super.setAllowedDebugLevel(debugLevel);

		if (type == DEBUG && !ERXProperties.booleanForKeyWithDefault("er.extensions.ERXNSLogSlf4jBridge.ignoreNSLogSettings", false)) {
			ERXLoggingUtilities.setLevel(log, debugLevel != NSLog.DebugLevelOff ? Level.DEBUG : Level.INFO);
		}
	}

	@Override
	public void appendln() {
		appendln(""); // Assuming people will always put "%n" at the end of the layout pattern.
	}

	@Override
	public void flush() {
	}
}
