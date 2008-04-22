package er.extensions;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

/**
 * When this request handler is set via <code>registerRequestHandlerForKey(new
 * ERXDelayedRequestHandler(), ERXDelayedRequestHandler.KEY)</code>,
 * then a request that takes too long is automatically detached and a poor man's
 * long response is returned. It is pretty cool in that:
 * <ul>
 * <li>the users don't get the adaptor timeout and won't get redirected to an
 * instance that doesn't know anything about the session.</li>
 * <li>the users get immediate feedback with no code changes on your part.</li>
 * <li>the handler tries to cancels active requests that are over
 * maxRequestTime*5, which should mean no more session deadlocks.</li>
 * <li>you can subclass this handler to provide for better responses.</li>
 * <li>you can provide a simple style sheet for the default refresh page.</li>
 * </ul>
 * 
 * @author ak
 * 
 */
public class ERXDelayedRequestHandler extends WORequestHandler {

	protected static final Logger log = Logger.getLogger(ERXDelayedRequestHandler.class);

	public static String KEY = "_edr_";

	private ERXExpiringCache<String, DelayedRequest> _futures;
	private ERXExpiringCache<String, String> _urls;
	private ExecutorService _executor;
	private String _cssUrl;

	private int _refresh;
	private int _maxRequestTime;

	/**
	 * Helper to wrap a future and the accompanying request.
	 * 
	 * @author ak
	 * 
	 */
	public class DelayedRequest implements Callable<WOResponse> {

		protected WORequest _request;
		protected Future<WOResponse> _future;
		protected String _id;
		protected NSTimestamp _start;

		public DelayedRequest(WORequest request) {
			super();
			_request = request;
			_future = _executor.submit(this);
			_id = ERXRandomGUID.newGid();
			_start = new NSTimestamp();
		}

		public WOResponse call() throws Exception {
			final ERXApplication app = ERXApplication.erxApplication();
			WOResponse response = app.dispatchRequestImmediately(request());
			// testing
			// Thread.sleep(6000);
			// log.info("done: " + this);
			
			return response;
		}

		public WORequest request() {
			return _request;
		}

		public WOResponse response(long millis) throws InterruptedException, ExecutionException, TimeoutException {
			return future().get(millis, TimeUnit.MILLISECONDS);
		}

		public String id() {
			return _id;
		}

		public NSTimestamp start() {
			return _start;
		}

		public Future<WOResponse> future() {
			return _future;
		}
		
		public boolean isDone() {
			return future().isDone();
		}
		
		public boolean cancel() {
			return future().cancel(true);
		}

		@Override
		public String toString() {
			return "<DelayedRequest: " + request().uri() + " id: " + id() + " isDone: " + future().isDone() + " start: " + start() + ">";
		}
	}

	/**
	 * Creates a request handler instance.
	 * 
	 * @param refresh
	 *            time for the refresh of the page
	 * @param maxRequestTime
	 *            time that a request can take at most before the delayed page
	 *            is returned.
	 * @param cssUrl
	 *            url for a style sheet for the message page
	 */
	public ERXDelayedRequestHandler(int refresh, int maxRequestTime, String cssUrl) {
		_cssUrl = cssUrl;
		_refresh = refresh;
		_maxRequestTime = maxRequestTime;
		_executor = Executors.newCachedThreadPool();
		_futures = new ERXExpiringCache<String, DelayedRequest>(refresh() * 5) {
			@Override
			protected synchronized void removeEntryForKey(Entry<DelayedRequest> entry, String key) {
				Future future = entry.object().future();
				if (!future.isDone()) {
					if (!future.cancel(true)) {
						log.error("Delayed was running, but couldn't be cancelled: " + entry.object());
					}
					else {
						log.info("Stopped delayed request that was still running: " + entry.object());
					}
				}
				super.removeEntryForKey(entry, key);
			}
		};
		_urls = new ERXExpiringCache(refresh() * 50);
	}
	
	/**
	 * Creates a handler with the supplied values for refresh and maxRequestTime.
	 * @param refresh
	 * @param maxRequestTime
	 */
	public ERXDelayedRequestHandler(int refresh, int maxRequestTime) {
		this(refresh, maxRequestTime, null);
	}
	
	/**
	 * Creates a handler with the default values of 5 second refresh and 5
	 * seconds maxRequestTime.
	 */
	public ERXDelayedRequestHandler() {
		this(5, 5000, null);
	}

	/**
	 * Handles the request and returns the applicable response.
	 */
	@Override
	public WOResponse handleRequest(final WORequest request) {
		ERXApplication app = ERXApplication.erxApplication();
		String key = request.requestHandlerKey();
		WOResponse response = null;
		if (canHandleRequest(key)) {
			String uri = request.uri();
			DelayedRequest delayedRequest;
			String id;
			log.debug("Handling: " + uri);

			if (KEY.equals(key)) {
				id = request.stringFormValueForKey("id");
				delayedRequest = _futures.objectForKey(id);
				if (delayedRequest == null) {
					String url = _urls.objectForKey(id);
					if (url == null) {
						return createErrorResponse(request);
					}
					response = new WOResponse();
					response.setStatus(302);
					response.setHeader(url, "location");
					// refresh entry, so it doesn't time out
					_urls.setObjectForKey(url, id);
					return response;
				}
				// refresh entry, so it doesn't time out
				_futures.setObjectForKey(delayedRequest, id);
			}
			else {
				delayedRequest = new DelayedRequest(request);
				id = delayedRequest.id();
				_futures.setObjectForKey(delayedRequest, id);
			}
			response = handle(request, delayedRequest, id);
		}
		else {
			// not handled
			response = app.dispatchRequestImmediately(request);
		}
		return response;
	}

	/**
	 * Returns true if the request handler key can be handled.
	 * 
	 * @param key
	 * @return
	 */
	protected boolean canHandleRequest(String key) {
		ERXApplication app = ERXApplication.erxApplication();
		return key == null || KEY.equals(key) || app.componentRequestHandlerKey().equals(key) || app.directActionRequestHandlerKey().equals(key);
	}

	/**
	 * Override to handle specific actions for the current future.
	 * 
	 * @param request
	 * @param delayedRequest
	 * @param id
	 * @return
	 */
	protected WOResponse handle(WORequest request, DelayedRequest delayedRequest, String id) {
		final ERXApplication app = ERXApplication.erxApplication();
		WOResponse response = null;
		try {
			String action = request.stringFormValueForKey("action");
			if (!delayedRequest.isDone()) {
				if ("stop".equals(action)) {
					if (delayedRequest.cancel()) {
						_futures.removeObjectForKey(id);
						_urls.setObjectForKey(delayedRequest.request().uri(), id);
						response = createStoppedResponse(request);
						return response;
					}
				}
				else {
					String url = request.uri();
					if (!KEY.equals(request.requestHandlerKey())) {
						String args = "id=" + id;
						String sessionID = request.sessionID();
						if (sessionID != null) {
							args += "&wosid=" + sessionID;
						}
						args += "&__start=" + delayedRequest.start().getTime();
						args += "&__time=" + System.currentTimeMillis();
						url = app.createContextForRequest((WORequest) request.clone()).urlWithRequestHandlerKey(KEY, "wait", args);
					}
					log.debug("Delaying: " + request.uri());
					response = createRefreshResponse(request, url);
				}
			}
			// AK: this double assignment is not an error. The future will try
			// to get the value. When we time out, the old value from above will
			// be returned. If we don't, then the real response is used.
			response = delayedRequest.response(maxRequestTime());
			_futures.removeObjectForKey(id);
			_urls.setObjectForKey(delayedRequest.request().uri(), id);
		}
		catch (InterruptedException e1) {
			throw NSForwardException._runtimeExceptionForThrowable(e1.getCause());
		}
		catch (ExecutionException e1) {
			throw NSForwardException._runtimeExceptionForThrowable(e1.getCause());
		}
		catch (TimeoutException e) {
			log.debug("Timed out, redirecting: " + request.uri());
		}
		return response;
	}

	/**
	 * Create an error page when the future wasn't found anymore. This happens
	 * hen the user backtracks and it is no longer in the cache. Note that the
	 * session has not been awakened.
	 * 
	 * @param request
	 * @return
	 */
	protected WOResponse createErrorResponse(WORequest request) {
		final ERXApplication app = ERXApplication.erxApplication();
		String args = (request.sessionID() != null ? "/" + request.sessionID() : "");
		// dirty trick: use a non-existing context id to get the page-expired reply.
		String url = request.applicationURLPrefix() + "/wo" + args + "/9999999999.0";
		WORequest expired = app.createRequest("GET", url, "HTTP/1.0", (Map) request.headers(), null, null);
		WOResponse result = app.dispatchRequestImmediately(expired);
		return result;
	}

	/**
	 * Create a "stopped" page. Note that the session has not been awakened yet
	 * and you probably shouldn't do it either.
	 * 
	 * @param request
	 * @param url
	 * @return
	 */
	protected WOResponse createStoppedResponse(WORequest request) {
		final ERXApplication app = ERXApplication.erxApplication();
		String args = (request.sessionID() != null ? "wosid=" + request.sessionID() : "");

		WORequest home = app.createRequest("GET", request.applicationURLPrefix() + "?" + args, "HTTP/1.0", (Map) request.headers(), null, null);
		WOResponse result = app.dispatchRequestImmediately(home);
		return result;
	}

	protected String cssUrl(WORequest request) {
		return _cssUrl;
	}

	/**
	 * Create a refresh page. Note that the session has not been awakened yet
	 * and you probably shouldn't do it either.
	 * 
	 * @param request
	 * @param timeout
	 * @param url
	 * @return
	 */
	protected WOResponse createRefreshResponse(WORequest request, String url) {
		WOResponse result = new WOResponse();
		result.setHeader(refresh() + "; url=" + url + "\"", "refresh");
		// ak: create a simple template
		result.appendContentString("<html>\n<head>\n<meta http-equiv=\"refresh\" content=\"" + refresh() + "; url=" + url + "\">\n");
		result.appendContentString("<title>Please stand by...</title>\n");
		String cssUrl = cssUrl(request);
		if (cssUrl != null) {
			result.appendContentString("<link rel=\"stylesheet\" href=\"" + cssUrl + "\"></link>\n");
		}
		result.appendContentString("</head>\n<body id=\"ERXDelayedRefreshPage\">");
		result.appendContentString("<h1>Please stand by...</h1>\n");
		result.appendContentString("<p class=\"busyMessage\">The action you selected is taking longer than " + (maxRequestTime() / 1000) + " seconds. The result will be shown as soon as it is ready.</p>\n");
		result.appendContentString("<p class=\"refreshMessage\">This page will refresh automatically in " + refresh() + " seconds.</p>\n");
		result.appendContentString("<p class=\"actions\">");
		result.appendContentString("<a href=\"" + url + "\" class=\"refreshLink\">Refresh now</a> ");
		result.appendContentString("<a href=\"" + url + "&action=stop\" class=\"stopLink\">Stop now</a>");
		result.appendContentString("</p>\n</body>\n</html>");
		return result;
	}

	/**
	 * Returns the refresh time in seconds for the message page;
	 * 
	 * @return
	 */
	protected int refresh() {
		return _refresh;
	}

	/**
	 * Returns the maximum time in milliseconds for allowed for a request before
	 * returning the message page.
	 * 
	 * @return
	 */
	protected int maxRequestTime() {
		return _maxRequestTime;
	}

	/**
	 * Returns all active delayed requests.
	 * @return
	 */
	public NSArray<DelayedRequest> activeRequests() {
		NSMutableArray<DelayedRequest> result = new NSMutableArray<DelayedRequest>();
		for (String id : _futures.allKeys()) {
			DelayedRequest request = _futures.objectForKey(id);
			if(request != null) {
				result.addObject(request);
			}
		}
		return result;
	}
}