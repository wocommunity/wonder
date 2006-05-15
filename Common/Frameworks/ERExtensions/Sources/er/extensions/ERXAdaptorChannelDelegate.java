package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

/**
 * Tracks and logs the SQL that gets sent to the database. If the milliseconds
 * used exceed the time specified in the system property
 * <code>er.extensions.ERXSQLExpressionTracker.trace.milliSeconds.[debug|info|warn|error]</code>,
 * and the entity name matches the regular expression
 * <code>er.extensions.ERXSQLExpressionTracker.trace.entityMatchPattern</code>
 * then the SQL expression is logged together with the time used and the
 * parameters. <br />
 * NOTE: to get patched into EOF, this class registers itself for the
 * <code>EODatabaseContext.DatabaseChannelNeededNotification</code>
 * notification and creates a new channel. If you would like to handle creation
 * of the channel yourself *and* you need the logging feature, you need to:
 * <ul>
 * <li>set the er.extensions.ERXAdaptorChannelDelegate.enabled=false in your
 * properties, which will prevent creation of the channel here
 * <li>create the channel yourself and set the delegate to
 * {@link ERXAdaptorChannelDelegate.defaultDelegate();}
 * </ul>
 * otherwise you just need to set
 * er.extensions.ERXAdaptorChannelDelegate.enabled=true
 * 
 * @author ak
 */
public class ERXAdaptorChannelDelegate {

	private static ERXLogger log = ERXLogger.getERXLogger(ERXAdaptorChannelDelegate.class);

	private static ERXAdaptorChannelDelegate _delegate;

    private long _lastMilliseconds;
	

	public static void setupDelegate() {
		_delegate = new ERXAdaptorChannelDelegate();
		NSNotificationCenter.defaultCenter().addObserver(_delegate,
				new NSSelector("dataBaseChannelNeeded", ERXConstant.NotificationClassArray),
				EODatabaseContext.DatabaseChannelNeededNotification, null);
	}

	public static ERXAdaptorChannelDelegate delegate() {
		return _delegate;
	}

	public void adaptorChannelDidEvaluateExpression(EOAdaptorChannel channel, EOSQLExpression expression) {
		ERXEOAccessUtilities.logExpression(channel, expression, _lastMilliseconds);
	}

	public boolean adaptorChannelShouldEvaluateExpression(EOAdaptorChannel channel, EOSQLExpression expression) {
		_lastMilliseconds = System.currentTimeMillis();
		return true;
	}

    /**
     * Answers to the EODataBaseChannelNeeded notification. 
     * Creates a new EODatabaseChannel and sets its adaptorChannel delegate to this instance,
     * @param n
     */
	public void dataBaseChannelNeeded(NSNotification n) {
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.enabled", false)) {
			EODatabaseContext context = (EODatabaseContext) n.object();
			EODatabaseChannel channel = new EODatabaseChannel(context);
			context.registerChannel(channel);
			channel.adaptorChannel().setDelegate(this);
		}
	}
}
