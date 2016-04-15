package er.extensions.appserver;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXExpiringCache;
import er.extensions.foundation.ERXRandomGUID;
import er.extensions.foundation.ERXRuntimeUtilities;

/**
 * When this request handler is set via <code>registerRequestHandlerForKey(new
 * ERXDelayedRequestHandler(), ERXDelayedRequestHandler.KEY)</code>,
 * then a request that takes too long is automatically detached and a poor man's
 * long response is returned. It is pretty cool in that:
 * <ul>
 * <li>the users don't get the adaptor timeout and won't get redirected to an
 * instance that doesn't know anything about the session.</li>
 * <li>the users get immediate feedback with no code changes on your part.</li>
 * <li>the handler tries to cancels active requests that take too long (default
 * is maxRequestTimeSeconds*5), which should mean no more session deadlocks.</li>
 * <li>you can subclass this handler to provide for better responses.</li>
 * <li>you can provide a simple style sheet for the default refresh page.</li>
 * </ul>
 * 
 * @author ak
 */
public class ERXDelayedRequestHandler extends WORequestHandler {
	private static final Logger log = LoggerFactory.getLogger(ERXDelayedRequestHandler.class);

	public static String KEY = "_edr_";

	private ERXExpiringCache<String, DelayedRequest> _futures;
	private ERXExpiringCache<String, String> _urls;
	private ExecutorService _executor;
	private String _cssUrl;

	private int _refreshTimeSeconds;
	private int _maxRequestTimeMillis;

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
		private volatile Thread _currentThread;

		public DelayedRequest(WORequest request) {
			super();
			_request = WOApplication.application().createRequest(request.method(), request.uri(), request.httpVersion(), request.headers(), request.content(), request.userInfo());
//			_request = (WORequest) request.clone();
			_future = _executor.submit(this);
			_id = ERXRandomGUID.newGid();
			_start = new NSTimestamp();
		}

		public WOResponse call() throws Exception {
			synchronized (this) {
				_currentThread = Thread.currentThread();
			}
			try {
				final ERXApplication app = ERXApplication.erxApplication();
				WOResponse response = app.dispatchRequestImmediately(request());
				// testing
				// Thread.sleep(16000);
				// log.info("Done: {}", this);
				return response;
			}
			finally {
				synchronized (this) {
					ERXRuntimeUtilities.clearThreadInterrupt(_currentThread);
					_currentThread = null;
				}
			}
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
			return _currentThread == null;
		}

		public boolean cancel() {
			// long start = System.currentTimeMillis();
			synchronized (this) {
				if (_currentThread != null) {
					ERXRuntimeUtilities.addThreadInterrupt(_currentThread, "ERXDelayedRequestHandler: stop requested " + this);

					// while(System.currentTimeMillis() - start < 5000 &&
					// !isDone()) {
					if (future().cancel(true)) {
						log.info("Cancelled: {}: {}", _currentThread, isDone());
					}
					// }
					log.info("Thread done after cancel: {}", isDone());
				}
			}
			return isDone();
		}

		@Override
		public String toString() {
			return "<DelayedRequest: " + request().uri() + " id: " + id() + " isDone: " + future().isDone() + " start: " + start() + ">";
		}
	}

	/**
	 * Creates a request handler instance.
	 * 
	 * @param refreshTimeSeconds
	 *            time in seconds for the refresh of the page
	 * @param maxRequestTimeSeconds
	 *            time in seconds that a request can take at most before the delayed page
	 *            is returned
	 * @param cancelRequestAfterSeconds
	 *            time in seconds that a request can take at most before it is cancelled
	 * @param cssUrl
	 *            url for a style sheet for the message page
	 */
	public ERXDelayedRequestHandler(int refreshTimeSeconds, int maxRequestTimeSeconds, int cancelRequestAfterSeconds, String cssUrl) {
		_cssUrl = cssUrl;
		_refreshTimeSeconds = refreshTimeSeconds;
		_maxRequestTimeMillis = maxRequestTimeSeconds*1000;
		_executor = Executors.newCachedThreadPool();
		_futures = new ERXExpiringCache<String, DelayedRequest>(cancelRequestAfterSeconds) {
			@Override
			protected synchronized void removeEntryForKey(Entry<DelayedRequest> entry, String key) {
				DelayedRequest request = entry.object();
				synchronized (request) {
					if (!request.isDone()) {
						if (!request.cancel()) {
							log.error("Delayed was running, but couldn't be cancelled: {}", request);
						}
						else {
							log.info("Stopped delayed request that was still running: {}", request);
						}
					}
				}
				super.removeEntryForKey(entry, key);
			}
		};
		_urls = new ERXExpiringCache<String, String>(refresh() * 50);
	}

	/**
	 * Creates a handler with the supplied values for refreshTimeSeconds, maxRequestTimeSeconds and
	 * maxRequestTimeSeconds.
	 * 
	 * @param refreshTimeSeconds
	 * @param maxRequestTimeSeconds
	 * @param cancelRequestAfterSeconds
	 */
	public ERXDelayedRequestHandler(int refreshTimeSeconds, int maxRequestTimeSeconds, int cancelRequestAfterSeconds) {
		this(refreshTimeSeconds, maxRequestTimeSeconds, cancelRequestAfterSeconds, null);
	}
	
	
	/**
	 * Creates a handler with the supplied values for refreshTimeSeconds and
	 * maxRequestTimeSeconds. cancelRequestAfterSeconds is set o 5*maxRequestTimeSeconds.
	 * 
	 * @param refreshTimeSeconds
	 * @param maxRequestTimeSeconds
	 */
	public ERXDelayedRequestHandler(int refreshTimeSeconds, int maxRequestTimeSeconds) {
		this(refreshTimeSeconds, maxRequestTimeSeconds, maxRequestTimeSeconds*5, null);
	}

	/**
	 * Creates a handler with the default values of 5 second refresh and 5
	 * seconds maxRequestTime. Requests taking longer than 25 seconds are cancelled.
	 */
	public ERXDelayedRequestHandler() {
		this(5, 5);
	}

	/**
	 * Handles the request and returns the applicable response.
	 */
	@Override
	public WOResponse handleRequest(final WORequest request) {
		ERXApplication app = ERXApplication.erxApplication();
		WOResponse response = null;
		if (canHandleRequest(request)) {
			String uri = request.uri();
			DelayedRequest delayedRequest;
			String id;
			log.debug("Handling: {}", uri);

			String key = request.requestHandlerKey();
			if (KEY.equals(key)) {
				id = request.stringFormValueForKey("id");
				delayedRequest = _futures.objectForKey(id);
				if (delayedRequest == null) {
					String url = _urls.objectForKey(id);
					if (url == null) {
						return createErrorResponse(request);
					}
					response = new ERXResponse(ERXHttpStatusCodes.FOUND);
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
	 * @param request
	 */
	protected boolean canHandleRequest(WORequest request) {
		String contentType = request.headerForKey("content-type");
		if (contentType != null  && contentType.startsWith("multipart/form-data")) {
			return false;
		}
		ERXApplication app = ERXApplication.erxApplication();
		String key = request.requestHandlerKey();
		return key == null || KEY.equals(key) || app.componentRequestHandlerKey().equals(key) || app.directActionRequestHandlerKey().equals(key);
	}

	/**
	 * Override to handle specific actions for the current future.
	 * 
	 * @param request
	 * @param delayedRequest
	 * @param id
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
							args += "&" + WOApplication.application().sessionIdKey() + "=" + sessionID;
						}
						args += "&__start=" + delayedRequest.start().getTime();
						args += "&__time=" + System.currentTimeMillis();
						url = app.createContextForRequest((WORequest) request.clone()).urlWithRequestHandlerKey(KEY, "wait", args);
					}
					else {
						url = url.replaceAll("__time=(.*)", "__time=" + System.currentTimeMillis());
					}
					log.debug("Delaying: {}", request.uri());
					response = createRefreshResponse(request, url);
				}
			}
			// AK: this double assignment is not an error. The future will try
			// to get the value. When we time out, the old value from above will
			// be returned. If we don't, then the real response is used.
			response = delayedRequest.response(maxRequestTimeMillis());
			_futures.removeObjectForKey(id);
			_urls.setObjectForKey(delayedRequest.request().uri(), id);
		}
		catch (InterruptedException e1) {
			throw NSForwardException._runtimeExceptionForThrowable(e1.getCause());
		}
		catch (ExecutionException e1) {
			throw NSForwardException._runtimeExceptionForThrowable(e1.getCause());
		}
		catch (CancellationException e) {
			log.info("Cancelled, redirecting: {}", request.uri());
			response = createStoppedResponse(request);
		}
		catch (TimeoutException e) {
			log.debug("Timed out, redirecting: {}", request.uri());
		}
		return response;
	}

	/**
	 * Create an error page when the future wasn't found anymore. This happens
	 * when the user backtracks and it is no longer in the cache. Note that the
	 * session has not been awakened.
	 * 
	 * @param request the current request
	 * @return error response
	 */
	@SuppressWarnings("unchecked")
	protected WOResponse createErrorResponse(WORequest request) {
		final ERXApplication app = ERXApplication.erxApplication();
		String args = (request.sessionID() != null ? "/" + request.sessionID() : "");
		// dirty trick: use a non-existing context id to get the page-expired
		// reply.
		String url = request.applicationURLPrefix() + "/wo" + args + "/9999999999.0";
		WORequest expired = app.createRequest("GET", url, "HTTP/1.0", request.headers(), null, null);
		WOResponse result = app.dispatchRequestImmediately(expired);
		return result;
	}

	/**
	 * Create a "stopped" page. Note that the session has not been awakened yet
	 * and you probably shouldn't do it either. The default implementation
	 * redirect to the entry.
	 * 
	 * @param request the request object
	 * @return 302 response
	 */
	protected WOResponse createStoppedResponse(WORequest request) {
		String sessionIdKey = WOApplication.application().sessionIdKey();
		String args = (request.sessionID() != null ? sessionIdKey + "=" + request.sessionID() : "");

		String url = request.applicationURLPrefix() + "?" + args;
		ERXResponse result = new ERXResponse();
		result.setHeader(url, "location");
		result.setStatus(302);
		return result;
	}

	protected String cssUrl(WORequest request) {
		return _cssUrl;
	}

	/**
	 * Create a refresh page. Note that the session has not been awakened yet
	 * and you probably shouldn't do it either.
	 * 
	 * @param request the current request
	 * @param url URL to open after refresh
	 * @return refresh page response
	 */
	protected WOResponse createRefreshResponse(WORequest request, String url) {
		ERXResponse result = new ERXResponse();
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
		result.appendContentString("<p class=\"busyMessage\">The action you selected is taking longer than " + (maxRequestTimeMillis() / 1000) + " seconds. The result will be shown as soon as it is ready.</p>\n");
		result.appendContentString("<p class=\"refreshMessage\">This page will refresh automatically in " + refresh() + " seconds.</p>\n");
		result.appendContentString("<p class=\"actions\">");
		result.appendContentString("<a href=\"" + url + "\" class=\"refreshLink\">Refresh now</a> ");
		result.appendContentString("<a href=\"" + url + "&action=stop\" class=\"stopLink\">Stop now</a>");
		result.appendContentString("</p>\n</body>\n</html>");
		return result;
	}

	/**
	 * Returns the refresh time in seconds for the message page.
	 * 
	 * @return the refresh time in seconds
	 */
	protected int refresh() {
		return _refreshTimeSeconds;
	}

	/**
	 * Returns the maximum time in milliseconds for allowed for a request before
	 * returning the message page.
	 * 
	 * @return the maximum request time in milliseconds
	 */
	protected int maxRequestTimeMillis() {
		return _maxRequestTimeMillis;
	}

	/**
	 * Returns all active delayed requests.
	 * 
	 * @return array of delayed requests
	 */
	public NSArray<DelayedRequest> activeRequests() {
		NSMutableArray<DelayedRequest> result = new NSMutableArray<DelayedRequest>();
		for (String id : _futures.allKeys()) {
			DelayedRequest request = _futures.objectForKey(id);
			if (request != null) {
				result.addObject(request);
			}
		}
		return result;
	}
}
