package er.extensions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver.WOStatisticsStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Enhances the normal stats store with a bunch of useful things which get
 * displayed in the ERXStatisticsPage.
 * <ul>
 * <li>warn and debug messages when a request took too long, complete with stack traces of all threads in the state they were in at half-time.
 * </ul>
 * 
 * @author ak
 * 
 */
public class ERXStatisticsStore extends WOStatisticsStore {

	private static final Logger log = Logger.getLogger(ERXStatisticsStore.class);

	private StopWatchTimer _timer = new StopWatchTimer();

	private StopWatchTimer timer() {
		return _timer;
	}

	/**
	 * Thread that checks each second for running requests and makes a snapshot after a certain amount of tmie has expired.
	 * 
	 *
	 * @author ak
	 */
	class StopWatchTimer implements Runnable {

		long maximumRequestErrorTime;
		long maximumRequestWarnTime;

		Map<Thread, Long> _threads = Collections.synchronizedMap(new WeakHashMap<Thread, Long>());
		Map<Thread, Map<Thread, StackTraceElement[]>> _warnTraces = Collections.synchronizedMap(new WeakHashMap<Thread, Map<Thread, StackTraceElement[]>>());
		Map<Thread, Map<Thread, StackTraceElement[]>> _errorTraces = Collections.synchronizedMap(new WeakHashMap<Thread, Map<Thread, StackTraceElement[]>>());

		public StopWatchTimer() {
			new Thread(this).start();
			maximumRequestErrorTime = ERXProperties.longForKeyWithDefault("er.extensions.ERXStatisticsStore.milliSeconds.error", 10000L);
			maximumRequestWarnTime = ERXProperties.longForKeyWithDefault("er.extensions.ERXStatisticsStore.milliSeconds.warn", 2000L);
		}

		private long getTime() {
			Long time = _threads.get(Thread.currentThread());
			return time == null ? 0L : time.longValue();
		}

		private void clearTime() {
			_threads.remove(Thread.currentThread());
		}

		private void startTime() {
			_threads.put(Thread.currentThread(), new Long(System.currentTimeMillis()));
		}

		protected void endTimer(WOContext aContext, String aString) {
			try {
				long requestTime = 0;
				if (hasTimerStarted()) {
					requestTime = System.currentTimeMillis() - getTime();
				}
				Map<Thread, StackTraceElement[]> traces = _errorTraces.remove(Thread.currentThread());
				if(traces == null) {
					traces = _warnTraces.remove(Thread.currentThread());
				}
				String trace = null;
				if (traces != null) {
					StringBuffer sb = new StringBuffer();
					for (Iterator iterator = traces.keySet().iterator(); iterator.hasNext();) {
						Thread t = (Thread) iterator.next();
						StackTraceElement stack[] = (StackTraceElement[]) traces.get(t);
						String name = t.getName() != null ? t.getName() : "No name";
						String groupName = t.getThreadGroup() != null ? t.getThreadGroup().getName() : "No group";

						if (stack != null && stack.length > 2 && !name.equals("main") && !name.equals("StopWatchTimer") && !groupName.equals("system")) {
							StackTraceElement func = stack[0];
							if (func != null && func.getClassName() != null && !func.getClassName().equals("java.net.PlainSocketImpl")) {
								sb.append(t).append(":\n");
								for (int i = 0; i < stack.length; i++) {
									StackTraceElement stackTraceElement = stack[i];
									sb.append("  at ").append(stackTraceElement).append("\n");
								}
							}
						}
					}
					trace = "\n" + sb.toString();
				}
				else {
					trace = "";
				}
				clearTime();

				if (requestTime > maximumRequestErrorTime) {
					String requestDescription = aContext == null ? aString : descriptionForContext(aContext);
					log.error("Request did take too long : " + requestTime + " request was: " + requestDescription + trace);
				}
				else if (requestTime > maximumRequestWarnTime) {
					String requestDescription = aContext == null ? aString : descriptionForContext(aContext);
					log.warn("Request did take too long : " + requestTime + " request was: " + requestDescription + trace);
				}
			}
			catch (Exception ex) {
				// AK: pretty important we don't mess up here
				log.error(ex, ex);
			}
		}

		private boolean hasTimerStarted() {
			return getTime() != 0;
		}

		protected void startTimer() {
			if (!hasTimerStarted()) {
				startTime();
			}
		}

		public String descriptionForContext(WOContext aContext) {
			try {
				WOComponent component = aContext.page();
				String componentName = component != null ? component.name() : "NoNameComponent";
				String additionalInfo = "(no additional Info)";
				WORequest request = aContext.request();
				String requestHandler = request != null ? request.requestHandlerKey() : "NoRequestHandler";
				if (!requestHandler.equals("wo")) {
					additionalInfo = additionalInfo + aContext.request().uri();
				}
				return componentName + "-" + requestHandler + additionalInfo;
			}
			catch (RuntimeException e) {
				log.error("Cannot get context description since received exception " + e, e);
			}
			return "Error-during-context-description";
		}

		public void run() {
			Thread.currentThread().setName("StopWatchTimer");
			boolean done = false;
			while (!done) {
				Map<Thread, Long> map = new HashMap<Thread, Long>();
				map.putAll(_threads);
				if (!_threads.isEmpty()) {
					for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
						Thread thread = (Thread) iterator.next();
						Long time = map.get(thread);
						if (time != null && time > maximumRequestWarnTime/2 && _warnTraces.get(thread) == null) {
							Map traces = Thread.getAllStackTraces();
							_warnTraces.put(thread, traces);
						}
						if (time != null && time > maximumRequestWarnTime/2 && _errorTraces.get(thread) == null) {
							Map traces = Thread.getAllStackTraces();
							_errorTraces.put(thread, traces);
						}
					}
				}
				try {
					Thread.sleep(1000L);
				}
				catch (InterruptedException e) {
					done = true;
				}
			}
		}

	}

	protected NSMutableArray sessions = new NSMutableArray<WOSession>();

	protected void _applicationCreatedSession(WOSession wosession) {
		synchronized (this) {
			sessions.addObject(wosession);
			super._applicationCreatedSession(wosession);
		}
	}

	protected void _sessionTerminating(WOSession wosession) {
		synchronized (this) {
			super._sessionTerminating(wosession);
			sessions.removeObject(wosession);
		}
	}

	public NSArray activeSession() {
		return sessions;
	}

	private void startTimer() {
		timer().startTimer();
	}

	private void endTimer(String aString) {
		timer().endTimer(null, aString);
	}

	public void applicationWillHandleComponentActionRequest() {
		startTimer();
		super.applicationWillHandleComponentActionRequest();
	}

	public void applicationDidHandleComponentActionRequestWithPageNamed(String aString) {
		endTimer(aString);
		super.applicationDidHandleComponentActionRequestWithPageNamed(aString);
	}

	public void applicationWillHandleDirectActionRequest() {
		startTimer();
		super.applicationWillHandleDirectActionRequest();
	}

	public void applicationDidHandleDirectActionRequestWithActionNamed(String aString) {
		endTimer(aString);
		super.applicationDidHandleDirectActionRequestWithActionNamed(aString);
	}

	public void applicationWillHandleWebServiceRequest() {
		startTimer();
		super.applicationWillHandleWebServiceRequest();
	}

	public void applicationDidHandleWebServiceRequestWithActionNamed(String aString) {
		endTimer(aString);
		super.applicationDidHandleWebServiceRequestWithActionNamed(aString);
	}

}
