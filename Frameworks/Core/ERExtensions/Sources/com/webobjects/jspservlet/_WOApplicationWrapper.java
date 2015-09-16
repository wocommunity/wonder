package com.webobjects.jspservlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOInputStreamData;
import com.webobjects.appserver._private.WONoCopyPushbackInputStream;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDelayedCallbackCenter;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableRange;

/**
 * Fixes a bug in servletDispatchRequest() that manifests when a WOResponse
 * uses a content stream rather than an NSData.
 * 
 * @author faizel
 *
 */
public class _WOApplicationWrapper
implements _WOServletAppInterface
{
	private WOApplication appInstance = null;


	public _WOApplicationWrapper() {}

	public String applicationName() {
		return this.appInstance.name();
	}

	public void setApplication(Object uncastApp) {
		this.appInstance = ((WOApplication)uncastApp);
	}

	public Object applicationObject() {
		return this.appInstance;
	}

	public void setSessionStoreClassName(String sessionStoreClassName) {
		this.appInstance.setSessionStoreClassName(sessionStoreClassName);
	}

	public void setContextClassName(String contextClassName) {
		this.appInstance.setContextClassName(contextClassName);
	}

	public void servletDispatchRequest(Map<String, Object> userInfo, boolean isDeployed) throws IOException {
		HttpServletRequest request = (HttpServletRequest)userInfo.get("HttpServletRequest");
		HttpServletResponse response = (HttpServletResponse)userInfo.get("HttpServletResponse");

		StringBuffer urlBuffer = new StringBuffer();

		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String pathInfo = request.getPathInfo();
		String queryString = request.getQueryString();

		if (contextPath != null) {
			urlBuffer.append(contextPath);
		}
		if (servletPath != null) {
			urlBuffer.append(servletPath);
		}
		if (pathInfo != null) {
			urlBuffer.append(pathInfo);
		}
		if (queryString != null) {
			urlBuffer.append('?');
			urlBuffer.append(queryString);
		}

		String aURL = new String(urlBuffer);

		Map<String, Object> ourUserInfo = new HashMap(userInfo.size());
		for (Iterator<String> mapEnum = userInfo.keySet().iterator(); mapEnum.hasNext();) {
			String key = mapEnum.next();
			ourUserInfo.put(key, userInfo.get(key));
		}


		Map<String, ? extends List<String>> headers = _headersFromRequest(request, null, isDeployed);


		NSData contentData = null;

		int contentLength = request.getContentLength();
		if (contentLength > 0) {
			WONoCopyPushbackInputStream pbsis = new WONoCopyPushbackInputStream(new BufferedInputStream(request.getInputStream()), contentLength);
			contentData = new WOInputStreamData(pbsis, contentLength);
		}

		WORequest ourRequest = this.appInstance.createRequest(request.getMethod(), aURL, "HTTP/1.0", headers, contentData, ourUserInfo);

		WODynamicURL woURL = ourRequest._uriDecomposed();


		if ((woURL.requestHandlerKey() == null) && (woURL.requestHandlerPath() == null) && (woURL.queryString() == null) && (this.appInstance.name().equals(woURL.applicationName())))
		{

			if (!this.appInstance.shouldRestoreSessionOnCleanEntry(ourRequest))
			{
				HttpSession aSession = request.getSession(false);
				if (aSession != null) {
					try
					{
						aSession.invalidate();
					}
					catch (IllegalStateException ise) {
						NSLog._conditionallyLogPrivateException(ise);
					}
				}
			}
		}

		WOResponse ourResponse = this.appInstance.dispatchRequest(ourRequest);



		NSDelayedCallbackCenter.defaultCenter().eventEnded();

		_mergeHeaders(ourResponse, response);

		int bufferSize = 0;
		long ourContentLength = 0L;
		NSData ourContent = null;
		InputStream is = ourResponse.contentInputStream();

		if (is != null) {
			bufferSize = ourResponse.contentInputStreamBufferSize();
			ourContentLength = ourResponse.contentInputStreamLength();
		} else {
			ourContent = ourResponse.content();
			ourContentLength = ourContent == null ? 0L : ourContent.length();
		}

		if (ourContentLength > 0L) {
			response.setContentLength((int)ourContentLength);
		}

		String contentType = ourResponse.headerForKey("content-type");
		if (contentType != null) {
			response.setContentType(contentType);
		}

		response.setHeader("x-webobjects-servlet", "YES");

		response.setStatus(ourResponse.status());

		ServletOutputStream out;
		// FD-2015-01-07: check for non-null input stream as well as non-null content data
		if ((ourContent != null || is != null) && (ourContentLength > 0L)) {
			out = response.getOutputStream();

			if (is == null) {
				NSMutableRange range = new NSMutableRange();
				@SuppressWarnings("null") // as neither ourContent and is may be null
				byte[] contentBytesNoCopy = ourContent.bytesNoCopy(range);
				out.write(contentBytesNoCopy, range.location(), range.length());
			} else {
				try {
					byte[] buffer = new byte[bufferSize];
					while (ourContentLength > 0L) {
						int read = is.read(buffer, 0, ourContentLength > bufferSize ? bufferSize : (int)ourContentLength);
						if (read == -1)
							break;
						ourContentLength -= read;
						out.write(buffer, 0, read);
					}
					try
					{
						is.close();
					} catch (Exception e) {
						NSLog.err.appendln("<_WOApplicationWrapper>: Failed to close content InputStream: " + e);
						if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupJSPServlets)) {
							NSLog.err.appendln(e);
						}
					}


					out.flush();
				}
				finally
				{
					try
					{
						is.close();
					} catch (Exception e) {
						NSLog.err.appendln("<_WOApplicationWrapper>: Failed to close content InputStream: " + e);
						if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupJSPServlets)) {
							NSLog.err.appendln(e);
						}
					}
				}
			}
		}
	}

	private static NSArray<String> _adaptorVersion = new NSArray(new String[] { "5.2" });

	private static Map<String, ? extends List<String>> _headersFromRequest(HttpServletRequest request, Map<String, ? extends List<String>> extraHeaders, boolean isDeployed) {
		Map<String, List<String>> headers = new HashMap();


		for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
			String key = (String)e.nextElement();
			ArrayList<String> values = new ArrayList(1);
			for (Enumeration e2 = request.getHeaders(key); e2.hasMoreElements();) {
				values.add((String)e2.nextElement());
			}
			headers.put(key.toLowerCase(), values);
		}

		Iterator<String> mapEnum;
		if (extraHeaders != null) {
			for (mapEnum = extraHeaders.keySet().iterator(); mapEnum.hasNext();) {
				String key = mapEnum.next();
				List<String> value = extraHeaders.get(key);
				if (value == null) {
					headers.remove(key.toLowerCase());
				} else {
					headers.put(key.toLowerCase(), new ArrayList(value));
				}
			}
		}


		if (isDeployed) {
			headers.put("x-webobjects-adaptor-version", _adaptorVersion);
		}


		NSArray<String> values = new NSArray(new String[] { request.getServerName() });
		headers.put("x-webobjects-servlet-server-name", values);

		values = new NSArray(new String[] { Integer.toString(request.getServerPort()) });
		headers.put("x-webobjects-servlet-server-port", values);

		values = new NSArray(new String[] { request.getRemoteAddr() });
		headers.put("remote_addr", values);

		return headers;
	}

	private static void _mergeHeaders(WOResponse woResponse, HttpServletResponse servletResponse) {
		for (Iterator iterator = woResponse.headerKeys().iterator(); iterator.hasNext();) { 
			String key = (String)iterator.next();
			String lowercaseKey = key.toLowerCase();
			if (!"content-length".equals(lowercaseKey)) {
				for (String value : woResponse.headersForKey(lowercaseKey)) {
					servletResponse.addHeader(key, value);
				}
			}
		}
	}

	public String servletResponseForComponentWithName(String name, Map<String, Object> bindings, Map<String, ? extends List<String>> extraHeaders, Map<String, Object> userInfo, String urlPrefix, String appName, boolean mergeResponseHeaders, boolean isDeployed)
	{
		NSMutableDictionary<String, Object> ourBindings = new NSMutableDictionary(bindings);
		NSMutableDictionary<String, Object> ourUserInfo = new NSMutableDictionary(userInfo);

		Map<String, ? extends List<String>> ourHeaders = _headersFromRequest((HttpServletRequest)userInfo.get("HttpServletRequest"), extraHeaders, isDeployed);

		WOResponse woResponse = this.appInstance.responseForComponentWithName(name, ourBindings, ourHeaders, ourUserInfo, urlPrefix, appName);

		if (mergeResponseHeaders) {
			_mergeHeaders(woResponse, (HttpServletResponse)userInfo.get("HttpServletResponse"));


			((HttpServletResponse)userInfo.get("HttpServletResponse")).setHeader("x-webobjects-servlet", "YES");
		}

		return woResponse.contentString();
	}


	public String servletResponseForDirectActionWithNameAndClass(String actionName, String className, Map<String, Object> formValues, InputStream contentStream, Map<String, ? extends List<String>> extraHeaders, Map<String, Object> userInfo, String urlPrefix, String appName, boolean mergeResponseHeaders, boolean isDeployed)
	{
		NSMutableDictionary<String, Object> ourFormValues = new NSMutableDictionary(formValues);
		NSMutableDictionary<String, Object> ourUserInfo = new NSMutableDictionary(userInfo);

		Map<String, ? extends List<String>> ourHeaders = _headersFromRequest((HttpServletRequest)userInfo.get("HttpServletRequest"), extraHeaders, isDeployed);

		WOResponse woResponse = this.appInstance.responseForDirectActionWithNameAndClass(actionName, className, ourFormValues, contentStream, ourHeaders, ourUserInfo, urlPrefix, appName);

		if (mergeResponseHeaders) {
			_mergeHeaders(woResponse, (HttpServletResponse)userInfo.get("HttpServletResponse"));


			((HttpServletResponse)userInfo.get("HttpServletResponse")).setHeader("x-webobjects-servlet", "YES");
		}

		return woResponse.contentString();
	}
}
