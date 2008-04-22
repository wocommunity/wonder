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
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.foundation.NSForwardException;

/**
 * When this request handler is set via <code>registerRequestHandlerForKey(new
 * ERXDelayedRequestHandler(), ERXDelayedRequestHandler.KEY)</code>, then a request that
 * takes too long is automatically detached and a poor man's long response
 * is returned. This is pretty cool in that the users don't get the adaptor
 * timeout and also get immediate feedback with no code changes on your
 * part. You can subclass this handler to provide for better responses.
 * @author ak
 * 
 */
public class ERXDelayedRequestHandler extends WORequestHandler {

	protected static final Logger log = Logger.getLogger(ERXDelayedRequestHandler.class);

	public static String KEY = "_edr_";
	
	private ERXExpiringCache<String, DelayedRequest> _futures;
	private ERXExpiringCache<String, String> _urls;
	private ExecutorService _executor;

	private int _refresh;
	private int _maxRequestTime;
	
	/**
	 * Helper to wrap a future and the accompaniyung request.
	 * @author ak
	 *
	 */
	protected class DelayedRequest implements Callable<WOResponse> {
		
		protected WORequest _request;
		protected Future<WOResponse> _future;
		protected String _id;
		
		public DelayedRequest(WORequest request) {
			super();
			_request = request;
			_future = _executor.submit(this);
			_id = ERXRandomGUID.newGid();
		}

		public WOResponse call() throws Exception {
			final ERXApplication app = ERXApplication.erxApplication();
			WOResponse response = app.dispatchRequestImmediately(request());
			// testing
			// Thread.sleep(6000);s
			return response;
		}

		public WORequest request() {
			return _request;
		}
		
		public String id() {
			return _id;
		}
		
		public Future<WOResponse> future() {
			return _future;
		}
	}
	
	/**
	 * Creates a request handler instance.
	 * 
	 * @param refresh
	 *            time for the refresh of the page
	 * @param maxRequestTime
	 *            time that a request can take at most before the delayed
	 *            page is returned.
	 */
	public ERXDelayedRequestHandler(int refresh, int maxRequestTime) {
		_refresh = refresh;
		_maxRequestTime = maxRequestTime;
		_futures = new ERXExpiringCache(_refresh * 5);
		_urls = new ERXExpiringCache(_refresh * 50);
		_executor = Executors.newCachedThreadPool();
	}
	
	/**
	 * Creates a handler with the default values of 5 second refresh and 5
	 * seconds maxRequestTime.
	 */
	public ERXDelayedRequestHandler() {
		this(5, 5000);
	}

	/**
	 * Handles the request and returns the applicable response.
	 */
	@Override
	public WOResponse handleRequest(final WORequest request) {
		WOResponse response = null;
		final ERXApplication app = ERXApplication.erxApplication();
		String uri = request.uri();
		DelayedRequest delayedRequest;
		String id;
		log.debug("Handling: " + uri);

		if(KEY.equals(request.requestHandlerKey())) {
			id = request.stringFormValueForKey("id");
			delayedRequest = _futures.objectForKey(id);
			if(delayedRequest == null) {
				String url = _urls.objectForKey(id);
				if(url == null) {
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
		} else {
			delayedRequest = new DelayedRequest(request);
			id = delayedRequest.id();
			_futures.setObjectForKey(delayedRequest, id);
		}
		response = handle(request, delayedRequest, id);
		return response;
	}

	/**
	 * Override to handle specific actions for the current future.
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
			Future<WOResponse> future = delayedRequest.future();
			if(!future.isDone()) {
				if("stop".equals(action)) {
					if(future.cancel(true)) {
						_futures.removeObjectForKey(id);
						_urls.setObjectForKey(delayedRequest.request().uri(), id);
						response = createStoppedResponse(request);
						return response;
					}
				} else {
					String url = request.uri();
					if(!KEY.equals(request.requestHandlerKey())) {
						String args = "id=" + id;
						String sessionID = request.sessionID();
						if(sessionID != null) {
							args += "&wosid=" + sessionID;
						}
						args += "&_time=" + System.currentTimeMillis();
						url = app.createContextForRequest((WORequest) request.clone()).urlWithRequestHandlerKey(KEY, "wait", args); 
					}
					log.debug("Delaying: " + request.uri());
					response = createRefreshResponse(request, url);
				}
			}
			// AK: this double assignment is not an error. When we time out,
			// the old value from above will be returned. If we don't, then
			// the real response is used.
			response = future.get(maxRequestTime(), TimeUnit.MILLISECONDS);
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
	 * Create an error page when the future wasn't found anymore. This happens hen the
	 * user backtracks and it is no longer in the cache. Note that the session has not been
	 * awakened.
	 * 
	 * @param request
	 * @return
	 */
	protected WOResponse createErrorResponse(WORequest request) {
		final ERXApplication app = ERXApplication.erxApplication();
		return app.handleException(new EOObjectNotAvailableException("Action not found"), app.createContextForRequest(request));
	}

	/**
	 * Create a "stopped" page. Note that the session has not been
	 * awakened yet and you probably shouldn't do it either. 
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

	/**
	 * Create a refresh page. Note that the session has not been awakened
	 * yet and you probably shouldn't do it either.
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
		result.appendContentString("<html><head><meta http-equiv=\"refresh\" content=\"" + refresh() + "; url=" + url + "\">");
		result.appendContentString("<title>Please stand by...</title>");
		result.appendContentString("</head><body>");
		result.appendContentString("<h1>Please stand by...</h1>");
		result.appendContentString("<p>The action you selected is taking longer than " + (maxRequestTime() / 1000) + " seconds. The result will be shown as soon as it is ready.</p>");
		result.appendContentString("<p>This page will refresh automatically in " + refresh() + " seconds.</p><p>");
		result.appendContentString("<a href=\"" + url + "\">Refresh now</a> ");
		result.appendContentString("<a href=\"" + url + "&action=stop\">Stop now</a>");
		result.appendContentString("</p></body></html>");
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
	 * Returns the maximum time in milliseconds for allowed for a request
	 * before returning the message page.
	 * 
	 * @return
	 */
	protected int maxRequestTime() {
		return _maxRequestTime;
	}
	
}