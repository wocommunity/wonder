package er.extensions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSet;

/**
 * <p>
 * ERXStats provides a simple interface for logging statistics information like
 * WOEvent, but also tracked on a per-thread basis (so you can dump stats just
 * for a particular thread). DO PROBABLY DO NOT WANT TO TURN THIS ON IN
 * PRODUCTION.
 * </p>
 * 
 * <p>
 * As an example, you may want to track stats on keypaths in your components.  In your
 * base components, you could add:
 * </p>
 * <code>
 * public Object valueForKeyPath(String keyPath) {
 *   Object value;
 *   if (_shouldTrackStats) {
 *     String logName = ERXStringUtilities.getSimpleClassName(getClass()) + ": " + keyPath;
 *     ERXStats.markStart(logName);
 *     try {
 *       value = super.valueForKeyPath(keyPath);
 *     }
 *     finally {
 *       ERXStats.markEnd(logName);
 *     }
 *   }
 *   else {
 *     value = super.valueForKeyPath(keyPath);
 *   }
 *   return value;
 * }
 * </code>
 * 
 * @author anjo
 * @author mschrag
 * 
 * @property er.extensions.erxStats.enabled if true, stats will be initialized on each for each request
 * @property er.extensions.erxStats.max the maximum historical stats to collect (defaults to 1000) 
 */
public class ERXStats {
	private static final String STATS_INITIALIZED_KEY = "er.extensions.erxStats.initialized";
	private static final String STATS_KEY = "er.extensions.erxStats.statistics";

	public static final Logger log = Logger.getLogger(ERXStats.class);

	private static NSMutableArray<NSMutableDictionary<String, LogEntry>> _allStatistics = new NSMutableArray<NSMutableDictionary<String, LogEntry>>();

	/**
	 * Initializes the logging system if the property
	 * er.extensions.erxStats.enabled is true. ERXApplication.dispatchRequest
	 * will automatically call this.
	 */
	public static synchronized void initStatisticsIfNecessary() {
		if (ERXProperties.booleanForKey("er.extensions.erxStats.enabled")) {
			ERXStats.initStatistics();
		}
	}

	/**
	 * Initializes the logging stats manually. You can all this if you want to
	 * turn on thread logging just for a particular area of your application.
	 */
	public static synchronized void initStatistics() {
		ERXThreadStorage.takeValueForKey(Boolean.TRUE, ERXStats.STATS_INITIALIZED_KEY);
		ERXThreadStorage.removeValueForKey(ERXStats.STATS_KEY);
	}

	/**
	 * Returns true if the current thread is tracking statistics.
	 * 
	 * @return true if the current thread is tracking statistics
	 */
	public static boolean isTrackingStatistics() {
		Boolean statsInitialized = (Boolean) ERXThreadStorage.valueForKey(ERXStats.STATS_INITIALIZED_KEY);
		return statsInitialized != null && statsInitialized.booleanValue();
	}

	/**
	 * Returns the statistics for the current thread.
	 * 
	 * @return the statistics for the current thread
	 */
	@SuppressWarnings("unchecked")
	public static NSMutableDictionary<String, LogEntry> statistics() {
		NSMutableDictionary<String, LogEntry> statistics = (NSMutableDictionary<String, LogEntry>) ERXThreadStorage.valueForKey(ERXStats.STATS_KEY);
		if (statistics == null) {
			statistics = new NSMutableDictionary<String, LogEntry>();
			ERXThreadStorage.takeValueForKey(statistics, ERXStats.STATS_KEY);
			ERXStats._allStatistics.addObject(statistics);

			int maxStatistics = ERXProperties.intForKeyWithDefault("er.extensions.erxStats.max", 1000);
			if (ERXStats._allStatistics.count() > maxStatistics) {
				ERXStats._allStatistics.removeObjectAtIndex(0);
			}
		}
		return statistics;
	}

	/**
	 * Returns the log entry for the given key.
	 * 
	 * @param key
	 *            the key to lookup
	 * @return the log entry for the given key
	 */
	public static LogEntry logEntryForKey(String key) {
		LogEntry entry = null;
		NSMutableDictionary<String, LogEntry> statistics = ERXStats.statistics();
		if (statistics != null) {
			synchronized (statistics) {
				entry = (LogEntry) statistics.objectForKey(key);
				if (entry == null) {
					entry = new LogEntry(key);
					statistics.setObjectForKey(entry, key);
				}
			}
		}
		return entry;
	}

	/**
	 * Returns the aggregate key names for all of the threads that have been
	 * recorded.
	 * 
	 * @return the aggregate key names for all of the threads that have been
	 *         recorded
	 */
	public static NSSet<String> aggregateKeys() {
		NSMutableSet<String> keys = new NSMutableSet<String>();
		for (NSMutableDictionary<String, LogEntry> statistics : ERXStats._allStatistics) {
			keys.addObjectsFromArray(statistics.allKeys());
		}
		return keys;
	}

	/**
	 * Returns a LogEntry that represents the aggregate data collected for the
	 * given key in all of the recorded threads.
	 * 
	 * @param key
	 *            the key to lookup aggregate stats for
	 * @return the aggregate log entry for the given key
	 */
	public static LogEntry aggregateLogEntryForKey(String key) {
		LogEntry aggregateLogEntry = new LogEntry(key);
		if (key != null) {
			for (NSMutableDictionary<String, LogEntry> statistics : ERXStats._allStatistics) {
				LogEntry logEntry = statistics.objectForKey(key);
				if (logEntry != null) {
					aggregateLogEntry._add(logEntry);
				}
			}
		}
		return aggregateLogEntry;
	}

	/**
	 * Returns an array of LogEntries that represents the aggregate time for
	 * all of the tracked stats in the queue, uniqued on key.
	 * 
	 * @return an aggregate set of log entries
	 */
	public static NSArray<LogEntry> aggregateLogEntries() {
		NSMutableArray<LogEntry> aggregateLogEntries = new NSMutableArray<LogEntry>();
		for (String key : aggregateKeys()) {
			LogEntry logEntry = ERXStats.aggregateLogEntryForKey(key);
			if (logEntry != null) {
				aggregateLogEntries.addObject(logEntry);
			}
		}
		return aggregateLogEntries;
	}

	/**
	 * Mark the start of a process, call markEnd when it is over to log the
	 * duration.
	 * 
	 * @param key the key log to start logging 
	 */
	public static void markStart(String key) {
		LogEntry entry = ERXStats.logEntryForKey(key);
		if (entry != null) {
			entry.start();
		}
	}

	/**
	 * Marks the end of a process, and calls addDuration(..) with the 
	 * time since markStart.
	 * 
	 * @param key the key to log under
	 */
	public static void markEnd(String key) {
		LogEntry entry = ERXStats.logEntryForKey(key);
		if (entry != null) {
			entry.end();
		}
	}

	/**
	 * Adds the specified duration in milliseconds for the given key.
	 * 
	 * @param duration the duration in milliseconds of the operation
	 * @param key the name to log the time under
	 */
	public static void addDurationForKey(long duration, String key) {
		LogEntry entry = ERXStats.logEntryForKey(key);
		if (entry != null) {
			entry.add(duration);
		}
	}

	/**
	 * Resets statistics for this thread AND the global queue.
	 */
	public static synchronized void reset() {
		_allStatistics.removeAllObjects();
		ERXThreadStorage.removeValueForKey(ERXStats.STATS_KEY);
	}

	/**
	 * Logs the messages since the last call to initStatistics() ordered by some
	 * key.
	 * 
	 * @param operation
	 *            operation to sort on ("sum", "count", "min", "max", "avg")
	 */
	public static void logStatisticsForOperation(String operation) {
		NSMutableDictionary statistics = ERXStats.statistics();
		if (statistics != null) {
			synchronized (statistics) {
				NSArray values = ERXArrayUtilities.sortedArraySortedWithKey(statistics.allValues(), operation);
				if (values.count() > 0) {
					String result = NSPropertyListSerialization.stringFromPropertyList(values);
					// result = result.replaceAll("\\n\\t", "\n\t\t");
					// result = result.replaceAll("\\n", "\n\t\t");
					ERXStats.log.info(result);
				}
			}
		}
	}

	/**
	 * 
	 * @param expression
	 * @param startTime
	 */
	public static class LogEntry {
		private float _avg;
		private long _count;
		private long _min;
		private long _max;
		private long _sum;
		private String _key;
		private Set<String> _traces = Collections.synchronizedSet(new HashSet<String>());
		private long _lastMark;

		public LogEntry(String key) {
			_key = key;
			_avg = -1.0f;
		}

		public synchronized void _add(LogEntry logEntry) {
			long originalCount = _count;
			if (logEntry._min < _min) {
				_min = logEntry._min;
			}
			if (logEntry._max > _max) {
				_max = logEntry._max;
			}
			_sum += logEntry._sum;
			_count += logEntry._count;
			_avg = -1.0f;
			_traces.addAll(logEntry._traces);
		}

		public synchronized long count() {
			return _count;
		}

		public synchronized long min() {
			return _min;
		}

		public synchronized long max() {
			return _max;
		}

		public synchronized long sum() {
			return _sum;
		}

		public synchronized void start() {
			_lastMark = System.currentTimeMillis();
		}

		public synchronized void end() {
			if (_lastMark > 0) {
				add(System.currentTimeMillis() - _lastMark);
				_lastMark = 0;
			}
			else {
				ERXStats.log.info("You called ERXStats.end before calling ERXStats.start.");
			}
		}

		public synchronized void add(long time) {
			if (time < _min) {
				_min = time;
			}
			if (time > _max) {
				_max = time;
			}
			_count++;
			_sum += time;
			_avg = -1.0f;
			if (false) {
				// Throwable t = new RuntimeException();
				// t.fillInStackTrace();
				_traces.add(ERXUtilities.stackTrace());
			}
		}

		public synchronized float avg() {
			if (_avg < 0.0f) {
				_avg = (_count == 0 ? 0.0f : (_sum / ((float) _count)));
			}
			return _avg;
		}

		public String key() {
			return _key;
		}

		@Override
		public String toString() {
			return count() + "/" + sum() + " : " + min() + "/" + max() + "/" + avg() + "|" + _traces.size() + "->" + _key;
			// + "\n" + traces.iterator().next();
		}
	}

}
