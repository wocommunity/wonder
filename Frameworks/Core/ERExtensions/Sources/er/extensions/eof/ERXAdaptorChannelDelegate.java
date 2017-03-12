package er.extensions.eof;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRuntimeUtilities;

/**
 * Tracks and logs the SQL that gets sent to the database. If the milliseconds
 * used exceeds the time specified in the system property
 * <code>er.extensions.ERXSQLExpressionTracker.trace.milliSeconds.[debug|info|warn|error]</code>,
 * and the entity name matches the regular expression
 * <code>er.extensions.ERXSQLExpressionTracker.trace.entityMatchPattern</code>
 * then the SQL expression is logged together with the time used and the
 * parameters.
 * <p>
 * NOTE: To get patched into EOF, this class registers itself for the
 * <code>EODatabaseContext.DatabaseChannelNeededNotification</code>
 * notification and creates a new channel. If you would like to handle creation
 * of the channel yourself *and* you need the logging feature, you need to:
 * <ul>
 * <li>set the er.extensions.ERXAdaptorChannelDelegate.enabled=false in your
 * properties, which will prevent creation of the channel here</li>
 * <li>create the channel yourself and set the delegate to
 * <code>new ERXAdaptorChannelDelegate()</code></li>
 * </ul>
 * otherwise you just need to set
 * er.extensions.ERXAdaptorChannelDelegate.enabled=true
 * 
 * @author ak
 */
public class ERXAdaptorChannelDelegate {
	private static final Logger log = LoggerFactory.getLogger(ERXAdaptorChannelDelegate.class);

    private long _lastMilliseconds;
    
    private LinkedList _lastStatements;
    
    private Boolean _collectLastStatements;

	private Integer _numberOfStatementsToCollect;  
	
	public static void setupDelegate() {
		NSNotificationCenter.defaultCenter().addObserver(ERXAdaptorChannelDelegate.class,
				new NSSelector("dataBaseChannelNeeded", ERXConstant.NotificationClassArray),
				EODatabaseContext.DatabaseChannelNeededNotification, null);
	}

	/**
	 * Implemented so the the thread checks if it should get interrupted.
	 * @param eoadaptorchannel
	 * @param nsmutabledictionary
	 */
    public void adaptorChannelDidFetchRow(EOAdaptorChannel eoadaptorchannel, NSMutableDictionary nsmutabledictionary) {
    	ERXRuntimeUtilities.checkThreadInterrupt();
    }

	public void adaptorChannelDidEvaluateExpression(EOAdaptorChannel channel, EOSQLExpression expression) {
		if (collectLastStatements()) {
			// this collects the last 10 statements executed for dumping them  
			if (_lastStatements == null) {
				_lastStatements = new LinkedList<>();
			}
			_lastStatements.addLast(ERXEOAccessUtilities.createLogString(channel, expression, System.currentTimeMillis() - _lastMilliseconds));
			
			while (_lastStatements.size() > numberOfStatementsToCollect()) {
				_lastStatements.removeFirst();
			}
		}
		ERXEOAccessUtilities.logExpression(channel, expression, _lastMilliseconds);
	}
	
	private int numberOfStatementsToCollect () {
		if (_numberOfStatementsToCollect == null) {
			_numberOfStatementsToCollect = Integer.valueOf(ERXProperties.intForKeyWithDefault("er.extensions.ERXSQLExpressionTracker.numberOfStatementsToCollect", 10));
		}
		return _numberOfStatementsToCollect.intValue();
	}
	
	private boolean collectLastStatements () {
		if (_collectLastStatements == null) {
			_collectLastStatements = new Boolean (ERXProperties.booleanForKeyWithDefault("er.extensions.ERXSQLExpressionTracker.collectLastStatements", false));
		}
		return _collectLastStatements.booleanValue();
	}

	public boolean adaptorChannelShouldEvaluateExpression(EOAdaptorChannel channel, EOSQLExpression expression) {
		_lastMilliseconds = System.currentTimeMillis();
		return true;
	}

    /**
     * Answers to the EODataBaseChannelNeeded notification. 
     * Creates a new EODatabaseChannel and sets its adaptorChannel delegate 
     * to a new instance of ERXAdaptorChannelDelegate.
     * @param n
     */
	static public void dataBaseChannelNeeded(NSNotification n) {
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.enabled", false)) {
			EODatabaseContext context = (EODatabaseContext) n.object();
			EODatabaseChannel channel = new EODatabaseChannel(context);
			context.registerChannel(channel);
			channel.adaptorChannel().setDelegate(new ERXAdaptorChannelDelegate());
		}
	}

	/**
	 * Dump the last collected statements to the log. Use the property
	 * <code>er.extensions.ERXSQLExpressionTracker.collectLastStatements</code>
	 * set to true to collect executed statements.
	 */
	public synchronized void dumpLastStatements() {
		log.info("******* dumping collected SQL statements *******");
		if (_lastStatements != null) {
			for (int i = 0; i < _lastStatements.size(); i++) {
				log.info("{}", _lastStatements.get(i));
			}
		}
		else {
			log.info("No collected statements available.");
			if (!_collectLastStatements.booleanValue()) {
				log.info("You have to set the property 'er.extensions.ERXSQLExpressionTracker.collectLastStatements = true'. to make this feature work.");
			}
		}
		_lastStatements = new LinkedList<>();
		log.info("************************************************");
	}
	
	/**
	 * Return the last collected SQL statements
	 * @author cug - Jun 20, 2007
	 * @return The last collected SQL statements.
	 */
	public LinkedList<String> lastStatements() {
		return _lastStatements;
	}
}
