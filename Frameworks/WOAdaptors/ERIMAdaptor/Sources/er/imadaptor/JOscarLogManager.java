package er.imadaptor;

import net.kano.joscar.logging.LogManager;
import net.kano.joscar.logging.Logger;

import org.apache.log4j.Level;

public class JOscarLogManager implements LogManager {

	public JOscarLogManager() {
	}

	public Logger getLogger(String s) {
		return new Log4JLogger(org.apache.log4j.Logger.getLogger(s));
	}

	public static class Log4JLogger implements Logger {
		private org.apache.log4j.Logger _logger;

		public Log4JLogger(org.apache.log4j.Logger logger) {
			_logger = logger;
		}

		public void logException(String s, Throwable throwable) {
			_logger.error(s, throwable);
		}
		
		public void logInfo(String s) {
			_logger.info(s);
		}

		public void logFine(String s) {
			_logger.debug(s);
		}

		public boolean logFineEnabled() {
			return _logger.isDebugEnabled();
		}

		public void logFiner(String s) {
			_logger.debug(s);
		}

		public boolean logFinerEnabled() {
			return _logger.isDebugEnabled();
		}

		public void logWarning(String s) {
			_logger.warn(s);
		}

		public boolean logWarningEnabled() {
			return _logger.isEnabledFor(Level.WARN);
		}

	}

}
