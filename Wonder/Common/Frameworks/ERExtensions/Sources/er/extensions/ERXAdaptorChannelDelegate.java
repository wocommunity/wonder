package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

/**
 * Tracks and logs the SQL that gets sent to the database. If the logger
 * <code>log4j.category.er.extensions.ERXAdaptorChannelDelegate.sqlLogging=DEBUG</code>
 * and the milliseconds used exceed the time specified in the system property
 * <code>er.extensions.ERXSQLExpressionTracker.maxMilliSeconds</code>, then the SQL expression is logged together with the
 * time used and the parameters.
 * NOTE: to get patched into EOF, this class registers itself for the 
 * <code>EODatabaseContext.DatabaseChannelNeededNotification</code> notification and creates a new channel. If you
 * would like to handle creation of the channel yourself *and* you need the logging feature, you need to: <ul>
 * <li>set the er.extensions.ERXAdaptorChannelDelegate.enabled=false in your properties, which will prevent creation of the channel here
 * <li>create the channel yourself and set the delegate to {@link ERXAdaptorChannelDelegate.defaultDelegate();}
 * </ul> otherwise you just need to set er.extensions.ERXAdaptorChannelDelegate.enabled=true
 * @author ak
 */
public class ERXAdaptorChannelDelegate {
    private ERXLogger log = null;
    private long _lastMilliseconds;
    private long _maxMilliseconds = Long.getLong("er.extensions.ERXAdaptorChannelDelegate.maxMilliSeconds", 0).longValue();

    private static ERXAdaptorChannelDelegate _delegate;

    public static void setupDelegate() {
        _delegate = new ERXAdaptorChannelDelegate();
        NSNotificationCenter.defaultCenter().addObserver(_delegate,
                                                         new NSSelector("dataBaseChannelNeeded", ERXConstant.NotificationClassArray),
                                                         EODatabaseContext.DatabaseChannelNeededNotification,
                                                         null);
    }

    public static ERXAdaptorChannelDelegate delegate() {
        return _delegate;
    }
    
    public void adaptorChannelDidEvaluateExpression(EOAdaptorChannel channel,  EOSQLExpression expression) {
        if(log == null) {
            log = ERXLogger.getERXLogger("er.extensions.ERXAdaptorChannelDelegate.sqlLogging");
        }
        if(log.isDebugEnabled()) {
            long millisecondsNeeded = System.currentTimeMillis() - _lastMilliseconds;
            if(millisecondsNeeded > _maxMilliseconds) {
                log.debug("Expression took " + millisecondsNeeded + " ms : " + expression);
            }
        }
    }

    public boolean adaptorChannelShouldEvaluateExpression(EOAdaptorChannel channel,  EOSQLExpression expression) {
        _lastMilliseconds = System.currentTimeMillis();
        return true;
    }

    public void setMaxMilliSeconds(long value) {
        _maxMilliseconds = value;
    }

    public void dataBaseChannelNeeded(NSNotification n) {
        if(Boolean.getBoolean("er.extensions.ERXAdaptorChannelDelegate.enabled")) {
            EODatabaseContext context = (EODatabaseContext)n.object();
            EODatabaseChannel channel = new EODatabaseChannel(context);
            context.registerChannel(channel);
            channel.adaptorChannel().setDelegate(this);
        }
    }
}
