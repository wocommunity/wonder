package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

/**
 * Tracks and logs the SQL that gets sent to the database. If the milliseconds used exceed 
 * the time specified in the system property
 * <code>er.extensions.ERXSQLExpressionTracker.trace.milliSeconds.[debug|info|warn|error]</code>, and the entity name
 * matches the regular expression <code>er.extensions.ERXAdaptorChannelDelegate.trace.entityMatchPattern</code> then the SQL 
 * expression is logged together with the time used and the parameters. <br />
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
        String entityMatchPattern = ERXProperties.stringForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.entityMatchPattern", ".*");
        long millisecondsNeeded = System.currentTimeMillis() - _lastMilliseconds;
        String entityName = (expression.entity() != null ? expression.entity().name() : "Unknown");
        if(entityName.matches(entityMatchPattern)) {
            long debugMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.debug", 5);
            long infoMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.info", 100);
            long warnMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.warn", 500);
            long errorMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.error", 5000);
            int maxLength = ERXProperties.intForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.maxLength", 3000);

            String description = "\"" + entityName  + "\"@" + channel.adaptorContext().hashCode() + " expression took " + millisecondsNeeded + " ms: " + expression.statement();
            if(description.length() > maxLength) {
                description = description.substring(0, maxLength);
            }
            if(millisecondsNeeded > errorMilliseconds) {
                log.error(description, new RuntimeException("Statement running too long"));
            } else if(millisecondsNeeded > warnMilliseconds) {
                log.warn(description);
            } else if(millisecondsNeeded > infoMilliseconds) {
                log.info(description);
            } else if(millisecondsNeeded > debugMilliseconds) {
                log.debug(description);
            }
        }
    }

    public boolean adaptorChannelShouldEvaluateExpression(EOAdaptorChannel channel,  EOSQLExpression expression) {
        _lastMilliseconds = System.currentTimeMillis();
        return true;
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
