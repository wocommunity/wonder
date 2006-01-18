package er.extensions;

import java.io.*;

import org.apache.log4j.*;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

/**
 * Tracks and logs the SQL that gets sent to the database. If the milliseconds
 * used exceed the time specified in the system property
 * <code>er.extensions.ERXSQLExpressionTracker.trace.milliSeconds.[debug|info|warn|error]</code>,
 * and the entity name matches the regular expression
 * <code>er.extensions.ERXAdaptorChannelDelegate.trace.entityMatchPattern</code>
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
	/**
	 * 
	 */
	public static final String COUNTER_FILENAME = "COUNT";

	private static ERXLogger log = ERXLogger.getERXLogger(ERXAdaptorChannelDelegate.class);

	private static Boolean writeTransactionsToDisk = null;

	private static ERXAdaptorChannelDelegate _delegate;

	public static String disabledForThreadKey = "DISABLED_FOR_THREAD";
	
	/** Used to write down database transactions * */
	public static NSTimestampFormatter adaptorOperationsFormatter = new NSTimestampFormatter("d.%m.%Y-%H-%M-%S.%F");

	public static File transactionDir;

	private ERXLogger sqlLoggingLogger = null;
	private long _lastMilliseconds;
	private NSRecursiveLock transactionDirLock = new NSRecursiveLock();
	

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
		if (sqlLoggingLogger == null) {
			sqlLoggingLogger = ERXLogger.getERXLogger("er.extensions.ERXAdaptorChannelDelegate.sqlLogging");
		}
		// sqlLoggingLogger.setLevel(Level.DEBUG);
		String entityMatchPattern = ERXProperties.stringForKeyWithDefault(
				"er.extensions.ERXAdaptorChannelDelegate.trace.entityMatchPattern", ".*");
		long millisecondsNeeded = System.currentTimeMillis() - _lastMilliseconds;
		String entityName = (expression.entity() != null ? expression.entity().name() : "Unknown");
		if (entityName.matches(entityMatchPattern)) {
			long debugMilliseconds = ERXProperties.longForKeyWithDefault(
					"er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.debug", 5);
			long infoMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.info",
					100);
			long warnMilliseconds = ERXProperties.longForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.warn",
					500);
			long errorMilliseconds = ERXProperties.longForKeyWithDefault(
					"er.extensions.ERXAdaptorChannelDelegate.trace.milliSeconds.error", 5000);
			int maxLength = ERXProperties.intForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.trace.maxLength", 3000);
			boolean needsLog = false;
			if (millisecondsNeeded > errorMilliseconds) {
				needsLog = true;
			} else if (millisecondsNeeded > warnMilliseconds) {
				needsLog = true;
			} else if (millisecondsNeeded > infoMilliseconds) {
				if (sqlLoggingLogger.isInfoEnabled()) {
					needsLog = true;
				}
			} else if (millisecondsNeeded > debugMilliseconds) {
				if (sqlLoggingLogger.isDebugEnabled()) {
					needsLog = true;
				}
			}
			if (needsLog) {
				String description = "\"" + entityName + "\"@" + channel.adaptorContext().hashCode() + " expression took "
						+ millisecondsNeeded + " ms: " + expression.statement();
				StringBuffer sb = new StringBuffer();
				NSArray variables = expression.bindVariableDictionaries();
				int cnt = variables != null ? variables.count() : 0;
				if (cnt > 0) {
					sb.append(" withBindings: ");
					for (int i = 0; i < cnt; i++) {
						NSDictionary nsdictionary = (NSDictionary) variables.objectAtIndex(i);
						Object obj = nsdictionary.valueForKey("BindVariableValue");
						String attributeName = (String) nsdictionary.valueForKey("BindVariableName");
						if (obj instanceof String) {
							obj = EOSQLExpression.sqlStringForString((String) obj);
						} else if (obj instanceof NSData) {
							// ak: this is just for logging, however we would
							// like to get readable data
							// in particular for PKs and with postgres this
							// works.
							// plain EOF is broken, though
							try {
								if (((NSData) obj).length() < 50) {
									obj = expression.sqlStringForData((NSData) obj);
								}
							} catch (ArrayIndexOutOfBoundsException ex) {
								// ignore, this is a bug in EOF
							}
							if (obj instanceof NSData) {
								// produces very yucky output
								obj = obj.toString();
							}
						} else {
							if (expression.entity() != null) {
								EOAttribute attribute = expression.entity().anyAttributeNamed(attributeName);
								if (attribute != null) {
									obj = expression.formatValueForAttribute(obj, attribute);
								}
							}
						}
						if (i != 0)
							sb.append(", ");
						sb.append(i + 1);
						sb.append(":");
						sb.append(obj);
						sb.append("[");
						sb.append(attributeName);
						sb.append("]");
					}
				}
				description = description + sb.toString();

				if (description.length() > maxLength) {
					description = description.substring(0, maxLength);
				}
				if (millisecondsNeeded > errorMilliseconds) {
					sqlLoggingLogger.error(description, new RuntimeException("Statement running too long"));
				} else if (millisecondsNeeded > warnMilliseconds) {
					sqlLoggingLogger.warn(description);
				} else if (millisecondsNeeded > infoMilliseconds) {
					if (sqlLoggingLogger.isInfoEnabled()) {
						sqlLoggingLogger.info(description);
					}
				} else if (millisecondsNeeded > debugMilliseconds) {
					if (sqlLoggingLogger.isDebugEnabled()) {
						sqlLoggingLogger.debug(description);
					}
				}
			}
		}
	}

	public boolean adaptorChannelShouldEvaluateExpression(EOAdaptorChannel channel, EOSQLExpression expression) {
		_lastMilliseconds = System.currentTimeMillis();
		return true;
	}

	public void dataBaseChannelNeeded(NSNotification n) {
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.ERXAdaptorChannelDelegate.enabled", false)) {
			EODatabaseContext context = (EODatabaseContext) n.object();
			EODatabaseChannel channel = new EODatabaseChannel(context);
			context.registerChannel(channel);
			channel.adaptorChannel().setDelegate(this);
		}
	}

	public Throwable adaptorChannelDidPerformOperations(EOAdaptorChannel channel, NSArray adaptorOps, Throwable exception) {
		if (log.isDebugEnabled()) {
			log.debug("adaptorChannelDidPerformOperations: count=" + adaptorOps.count() + ", exception = " + exception);
		}
		if (exception == null) {
			if (writeTransactionsToDisk()) {
				File f = newTransactionFile();
				try {
					ERXEOAccessUtilities.writeAdaptorOperationsToDisk(adaptorOps, f);
				} catch (IOException e) {
					sqlLoggingLogger.error("could not write database operations to file " + f, e);
				}
			}

		}
		return exception;
	}

	/**
	 * @return a new <code>java.io.File</code> which can be used for
	 *         transactionlogs.
	 */
	private File newTransactionFile() {
		transactionDirLock.lock();
		try {
			File transactionDirCount = new File(transactionDir(), COUNTER_FILENAME);
			if (!transactionDirCount.exists()) {
				// we start a transaction 0
				try {
					ERXFileUtilities.stringToFile("0", transactionDirCount);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			String countString = "0";
			try {
				countString = ERXFileUtilities.stringFromFile(transactionDirCount);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int count = Integer.parseInt(countString);
			
			File f = new File(transactionDir(), count++ + ""/*"__"+adaptorOperationsFormatter.format(new NSTimestamp())*/);
			
			try {
				ERXFileUtilities.stringToFile(count + "", transactionDirCount);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return f;
		} finally {
			transactionDirLock.unlock();
		}
	}

	/**
	 * 
	 */
	public File transactionDir() {
		if (transactionDir == null) {
			transactionDir = new File(ERXSystem.getProperty("er.extensions.ERXAdaptorChannelDelegate.transactionFileLocation"));
			if (!transactionDir.exists()) {
				if (!transactionDir.mkdirs()) {
					throw new IllegalStateException("could not create directory for transaction files, please check permissions for "+
							transactionDir);
				}
			}
		}
		return transactionDir;
	}

	/**
	 * @return <code>true</code> if the system property
	 *         <code>er.extensions.ERXDatabaseContextDelegate.writeTransactionsToDisk</code>
	 *         is set to true and if ERXThreadStorage.valueForKey(disabledForThreadKey) returns false or null, false otherwise.
	 */
	private boolean writeTransactionsToDisk() {
		if (writeTransactionsToDisk == null) {
			writeTransactionsToDisk = ERXProperties.booleanForKeyWithDefault(
					"er.extensions.ERXAdaptorChannelDelegate.writeTransactionsToDisk", false) ? Boolean.TRUE : Boolean.FALSE;
		}
		Boolean b = (Boolean) ERXThreadStorage.valueForKey(disabledForThreadKey);
		
		return writeTransactionsToDisk.booleanValue() && (b == null ? true : !b.booleanValue());
	}

}
