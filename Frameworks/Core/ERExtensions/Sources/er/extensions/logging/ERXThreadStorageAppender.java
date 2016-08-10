/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import er.extensions.appserver.ERXSession;
import er.extensions.foundation.ERXThreadStorage;

/**
 * Appends all logging in this thread to thread storage if ERXSession.session()
 * is set. In case of an exception (or whenever you want to), you can call
 * <code>ERXThreadStorageAppender.messages()</code> to get the full log. <br>
 * This is useful because you don't clutter up your log with stuff you don't
 * really need most of the time, but still get full logging in case something
 * bad happens.
 * 
 * @author ak
 */

public class ERXThreadStorageAppender extends AppenderSkeleton {

	@Override
	public void append(LoggingEvent event) {
		if (ERXSession.session() != null) {
			StringBuffer buf = buffer();
			//buf.append(getLayout().format(event));
		}
	}

	protected static StringBuffer buffer() {
		StringBuffer buf = (StringBuffer) ERXThreadStorage.valueForKey("ERXThreadStorageAppender.buffer");
		if(buf == null) {
			buf = new StringBuffer();
			ERXThreadStorage.takeValueForKey(buf, "ERXThreadStorageAppender.buffer");
		}
		return buf;
	}

	public void close() {
		// nothing
	}

	public static String message() {
		return buffer().toString();
	}

	public boolean requiresLayout() {
		return true;
	}
}