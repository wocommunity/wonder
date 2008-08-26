package er.extensions.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXThreadStorage;

/**
 * ERXConsoleAppender is just like ConsoleAppender except that it display stack
 * traces using ERXExceptionUtilities. Additionally, it will not log the same
 * exception twice in a row, preventing the annoying problem where you may log
 * from multiple places in your code and produce multiple copies of the same
 * exception trace.
 * 
 * @author mschrag
 */
public class ERXConsoleAppender extends ConsoleAppender {
	private static final String LAST_THROWABLE_KEY = "er.extensions.logging.ERXConsoleAppender.lastThrowable";

	public ERXConsoleAppender() {
		super();
	}

	@SuppressWarnings("hiding")
	public ERXConsoleAppender(Layout layout) {
		super(layout);
	}

	@SuppressWarnings("hiding")
	public ERXConsoleAppender(Layout layout, String target) {
		super(layout, target);
	}

	@Override
	protected void subAppend(LoggingEvent event) {
		qw.write(super.layout.format(event));

		if (super.layout.ignoresThrowable()) {
			ThrowableInformation throwableInfo = event.getThrowableInformation();
			if (throwableInfo != null) {
				Throwable throwable = throwableInfo.getThrowable();
				Throwable lastThrowable = (Throwable) ERXThreadStorage.valueForKey(ERXConsoleAppender.LAST_THROWABLE_KEY);
				if (throwable != null) {
					if (lastThrowable != throwable) {
						StringWriter exceptionStringWriter = new StringWriter();
						ERXExceptionUtilities.printStackTrace(throwable, new PrintWriter(exceptionStringWriter, true));
						String exceptionStr = exceptionStringWriter.toString();
						if (exceptionStr.length() > 0) {
							for (String line : exceptionStr.split("[\r\n]+")) {
								qw.write(line);
								qw.write(Layout.LINE_SEP);
							}
						}
						ERXThreadStorage.takeValueForKey(throwable, ERXConsoleAppender.LAST_THROWABLE_KEY);
					}
				}
			}
		}

		if (immediateFlush) {
			qw.flush();
		}
	}
}
