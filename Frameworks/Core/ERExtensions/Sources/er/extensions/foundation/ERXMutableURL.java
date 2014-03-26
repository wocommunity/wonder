package er.extensions.foundation;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.CharEncoding;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXMutableURL provides a mutable model of a URL, including support for
 * storing relative "URLs" in addition to the traditional absolute URL provided
 * by the core Java URL object.
 * 
 * @author mschrag
 */
public class ERXMutableURL {
	private String _protocol;
	private String _host;
	private String _path;
	private String _ref;
	private Integer _port;
	private Map<String, List<String>> _queryParameters;

	/**
	 * Constructs a blank ERXMutableURL.
	 */
	public ERXMutableURL() {
		_queryParameters = new LinkedHashMap<String, List<String>>();
	}

	/**
	 * Constructs an ERXMutableURL with all of the properties of the given URL.
	 * 
	 * @param url
	 *            the URL to copy data from
	 * @throws MalformedURLException if the URL is invalid
	 */
	public ERXMutableURL(URL url) throws MalformedURLException {
		this();
		setURL(url);
	}

	/**
	 * Constructs an ERXMutableURL with all of the properties of the given
	 * external form of a URL.
	 * 
	 * @param str
	 *            a URL external form
	 * @throws MalformedURLException if the URL is invalid
	 */
	public ERXMutableURL(String str) throws MalformedURLException {
		this();
		setURL(str);
	}

	/**
	 * Sets the contents of this ERXMutableURL to be the same as the given URL.
	 * 
	 * @param url
	 *            the url to copy the contents from
	 * @throws MalformedURLException
	 *             if the URL is malformed
	 */
	public synchronized void setURL(URL url) throws MalformedURLException {
		_protocol = url.getProtocol();
		_host = url.getHost();
		int port = url.getPort();
		if (port == -1 || port == url.getDefaultPort()) {
			_port = null;
		}
		else {
			_port = Integer.valueOf(port);
		}
		_path = url.getPath();
		_ref = url.getRef();
		setQueryParameters(url.getQuery());
	}

	/**
	 * Sets the contents of this ERXMutableURL to be the same as the given URL
	 * external form.
	 * 
	 * @param str
	 *            the external form of a URL to copy the contents from
	 * @throws MalformedURLException
	 *             if the external form of the URL is malformed
	 */
	public synchronized void setURL(String str) throws MalformedURLException {
		boolean relativeURL = false;
		boolean startsWithSlash = false;
		if (str != null) {
			str = str.replaceAll("&amp;", "&");
			if (str.indexOf("://") == -1) {
				relativeURL = true;
				String fakeHost;
				if (str.startsWith("/")) {
					startsWithSlash = true;
					fakeHost = "http://fakehost";
				}
				else {
					fakeHost = "http://fakehost/";
				}
				str = fakeHost + str;
			}
		}
		setURL(new URL(str));
		if (relativeURL) {
			setHost(null);
			setProtocol(null);
			if (!startsWithSlash && _path != null && _path.length() > 0) {
				setPath(_path.substring(1));
			}
		}
	}

	/**
	 * Returns true if there is a host defined for this URL.
	 * 
	 * @return true if there is a host defined for this URL
	 */
	public boolean isFullyQualified() {
		return _host != null;
	}

	/**
	 * Returns true if this is an absolute URL.
	 * 
	 * @return true if this is an absolute URL
	 */
	public boolean isAbsolute() {
		return _path != null && _path.startsWith("/");
	}

	/**
	 * Sets the protocol of this URL (http, https, etc).
	 * 
	 * @param protocol
	 *            the new protocol
	 */
	public void setProtocol(String protocol) {
		_protocol = protocol;
	}

	/**
	 * Returns the protocol of this URL.
	 * 
	 * @return the protocol of this URL
	 */
	public String protocol() {
		return _protocol;
	}

	/**
	 * Sets the host of this URL.
	 * 
	 * @param host
	 *            the host of this URL
	 */
	public void setHost(String host) {
		_host = host;
	}

	/**
	 * Returns the host of this URL.
	 * 
	 * @return the host of this URL
	 */
	public String host() {
		return _host;
	}

	/**
	 * Sets the path of this URL.
	 * 
	 * @param path
	 *            the path of this URL
	 */
	public void setPath(String path) {
		_path = path;
	}

	/**
	 * Returns the path of this URL.
	 * 
	 * @return the path of this URL
	 */
	public String path() {
		return _path;
	}
	
	/**
	 * Appends the given path to the end of the existing path.
	 * 
	 * @param path the path to append
	 * @return this
	 */
	public ERXMutableURL appendPath(String path) {
		if (_path == null) {
			_path = path;
		}
		else if (_path.endsWith("/")) {
			_path = _path + path;
		}
		else {
			_path = _path + "/" + path;
		}
		return this;
	}

	/**
	 * Sets the port of this URL.
	 * 
	 * @param port
	 *            the port of this URL
	 */
	public void setPort(Integer port) {
		_port = port;
	}

	/**
	 * Returns the port of this URL (can be null).
	 * 
	 * @return the port of this URL (can be null)
	 */
	public Integer port() {
		return _port;
	}

	/**
	 * Sets the ref of this URL (the #whatever part).
	 * 
	 * @param ref
	 *            the ref of this URL (the #whatever part)
	 */
	public void setRef(String ref) {
		_ref = ref;
	}

	/**
	 * Returns the ref of this URL.
	 * 
	 * @return the ref of this URL
	 */
	public String ref() {
		return _ref;
	}

	/**
	 * Replaces the query parameters of this URL with the given k=v&k2=v2 format
	 * string.
	 * 
	 * @param queryParameters
	 *            the query parameters
	 * @throws MalformedURLException
	 *             if the string is malformed
	 */
	public synchronized void setQueryParameters(String queryParameters) throws MalformedURLException {
		clearQueryParameters();
		addQueryParameters(queryParameters);
	}
	
	/**
	 * Appends the query parameters of this URL with the given k=v&k2=v2 format
	 * string.
	 * 
	 * @param queryParameters
	 *            the query parameters
	 * @throws MalformedURLException
	 *             if the string is malformed
	 */
	public synchronized void addQueryParameters(String queryParameters) throws MalformedURLException {
		if (queryParameters != null) {
			StringTokenizer queryStringTokenizer = new StringTokenizer(queryParameters, "&");
			while (queryStringTokenizer.hasMoreTokens()) {
				String queryStringToken = queryStringTokenizer.nextToken();
				int equalsIndex = queryStringToken.indexOf('=');
				try {
					String key;
					String value;
					if (equalsIndex == -1) {
						key = queryStringToken.trim();
						value = null;
					}
					else {
						key = queryStringToken.substring(0, equalsIndex).trim();
						value = queryStringToken.substring(equalsIndex + 1);
					}
					if (key == null || key.length() == 0) {
						throw new MalformedURLException("The query string parameter '" + queryStringToken + " has an empty key in '" + queryParameters + "'.");
					}
					key = URLDecoder.decode(key, CharEncoding.UTF_8);
					if (value != null) {
						value = URLDecoder.decode(value, CharEncoding.UTF_8);
					}
					addQueryParameter(key, value);
				}
				catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Every VM is supposed to support UTF-8 encoding.", e);
				}
			}
		}
	}

	/**
	 * Replaces the query parameters of this URL with those defined in the given
	 * Map.
	 * 
	 * @param queryParameters
	 *            the new query parameters
	 */
	public synchronized void setQueryParametersMap(Map<String, List<String>> queryParameters) {
		_queryParameters = queryParameters;
	}

	/**
	 * Clears the query parameters of this URL.
	 */
	public synchronized void clearQueryParameters() {
		_queryParameters.clear();
	}

	/**
	 * Replaces the query parameters of this URL with those defined in the given
	 * NSDictionary.
	 * 
	 * @param queryParameters
	 *            the new query parameters
	 */
	public synchronized void setQueryParameters(NSDictionary<String, ? extends Object> queryParameters) {
		clearQueryParameters();
		addQueryParameters(queryParameters);
	}

	/**
	 * Adds additional query parameters to this URL from those defined in the
	 * given NSDictionary.
	 * 
	 * @param queryParameters
	 *            the new query parameters
	 */
	@SuppressWarnings("unchecked")
	public synchronized void addQueryParameters(NSDictionary<String, ? extends Object> queryParameters) {
		if (queryParameters != null) {
			Enumeration<String> keyEnum = queryParameters.keyEnumerator();
			while (keyEnum.hasMoreElements()) {
				String key = keyEnum.nextElement();
				Object valueObj = queryParameters.objectForKey(key);
				if (valueObj instanceof NSArray) {
					NSArray<String> valueArray = (NSArray<String>) valueObj;
					Enumeration<String> valueArrayEnum = valueArray.objectEnumerator();
					while (valueArrayEnum.hasMoreElements()) {
						String value = valueArrayEnum.nextElement();
						addQueryParameter(key, value);
					}
				}
				else {
					addQueryParameter(key, valueObj.toString());
				}
			}
		}
	}

	/**
	 * Adds additional query parameters to this URL from those defined in the
	 * given Map.
	 * 
	 * @param queryParameters
	 *            the new query parameters
	 */
	public synchronized void addQueryParametersMap(Map<String, String> queryParameters) {
		if (queryParameters != null) {
			Iterator<Map.Entry<String, String>> queryParameterIter = queryParameters.entrySet().iterator();
			while (queryParameterIter.hasNext()) {
				Map.Entry<String, String> queryParameter = queryParameterIter.next();
				addQueryParameter(queryParameter.getKey(), queryParameter.getValue());
			}
		}
	}

	/**
	 * Adds an additional query parameter to this URL.
	 * 
	 * @param key
	 *            the key of the new parameter
	 * @param value
	 *            the value of the new parameter
	 */
	public synchronized void addQueryParameter(String key, String value) {
		List<String> values = _queryParameters.get(key);
		if (values == null) {
			values = new LinkedList<String>();
			_queryParameters.put(key, values);
		}
		if (value != null) {
			values.add(value);
		}
	}

	/**
	 * Returns true if the given key is a query parameter key in this URL.
	 * 
	 * @param key
	 *            the key of the parameter to lookup
	 * @return true if the given key is a query parameter key in this URL
	 */
	public synchronized boolean containsQueryParameter(String key) {
		return _queryParameters.containsKey(key);
	}

	/**
	 * Removes the query parameters with the given key.
	 * 
	 * @param key
	 *            the key of the query parameters to remove
	 */
	public synchronized void removeQueryParameter(String key) {
		_queryParameters.remove(key);
	}

	/**
	 * Removes the query parameter value for the given key for multivalue
	 * parameters.
	 * 
	 * @param key
	 *            the key of the query parameters to lookup
	 * @param value
	 *            the value to remove.
	 */
	public synchronized void removeQueryParameter(String key, String value) {
		List<String> queryParameters = queryParameters(key);
		if (queryParameters != null) {
			queryParameters.remove(value);
		}
	}

	/**
	 * Returns the query parameters of this URL as a Map.
	 * 
	 * @return the query parameters of this URL as a Map
	 */
	public synchronized Map<String, List<String>> queryParameters() {
		return _queryParameters;
	}

	/**
	 * Returns the query parameters of this URL as a Map uniqued by key (which
	 * avoids multivalue properties at the expense of predictability).
	 * 
	 * @return the query parameters of this URL as a Map
	 */
	public synchronized Map<String, String> uniqueQueryParameters() {
		Map<String, String> uniqueQueryParameters = new LinkedHashMap<String, String>();
		Iterator<Map.Entry<String, List<String>>> queryParameterIter = _queryParameters.entrySet().iterator();
		while (queryParameterIter.hasNext()) {
			Map.Entry<String, List<String>> queryParameter = queryParameterIter.next();
			String key = queryParameter.getKey();
			Iterator<String> valuesIter = queryParameter.getValue().iterator();
			while (valuesIter.hasNext()) {
				String value = valuesIter.next();
				uniqueQueryParameters.put(key, value);
			}
		}
		return uniqueQueryParameters;
	}

	/**
	 * Returns the query parameters for the given key.
	 * 
	 * @param key
	 *            the key to lookup
	 * @return the query parameters for the given key
	 */
	public synchronized List<String> queryParameters(String key) {
		return _queryParameters.get(key);
	}

	/**
	 * Returns the first query parameter for the given key.
	 * 
	 * @param key
	 *            the key to lookup
	 * @return the first query parameter for the given key
	 */
	public synchronized String queryParameter(String key) {
		String queryParameter = null;
		List<String> queryParameters = queryParameters(key);
		if (queryParameters != null && queryParameters.size() > 0) {
			queryParameter = queryParameters.get(0);
		}
		return queryParameter;
	}

	/**
	 * Sets the given query parameter to the given value.
	 * 
	 * @param key
	 *            the key to set
	 * @param value
	 *            the value to set it to
	 */
	public synchronized void setQueryParameter(String key, String value) {
		LinkedList<String> queryParameters = new LinkedList<String>();
		if (value != null) {
			queryParameters.add(value);
		}
		_queryParameters.put(key, queryParameters);
	}

	/**
	 * Returns a String form of this URL.
	 * 
	 * @return a String form of this URL
	 */
	public synchronized String toExternalForm() {
		StringBuffer sb = new StringBuffer();
		if (_protocol != null) {
			sb.append(_protocol);
			sb.append("://");
		}
		if (_host != null) {
			sb.append(_host);
		}
		if (_port != null) {
			boolean includePort = true;
			if ("http".equalsIgnoreCase(_protocol) && Integer.valueOf(80).equals(_port)) {
				includePort = false;
			}
			else if ("https".equalsIgnoreCase(_protocol) && Integer.valueOf(443).equals(_port)) {
				includePort = false;
			}
			if (includePort) {
				sb.append(':');
				sb.append(_port);
			}
		}
		if (_path != null) {
			if (!_path.startsWith("/")) {
				sb.append('/');
			}
			sb.append(_path);
		}
		if (_queryParameters != null && !_queryParameters.isEmpty()) {
			if (_host != null || _path != null) {
				sb.append('?');
			}
			queryParametersAsString(sb);
		}
		if (_ref != null) {
			sb.append('#');
			sb.append(_ref);
		}
		return sb.toString();
	}

	/**
	 * Returns the query parameters of this URL as a String (in x=y&a=b syntax).
	 * 
	 * @return the query parameters of this URL as a String
	 */
	public String queryParametersAsString() {
		StringBuffer sb = new StringBuffer();
		queryParametersAsString(sb);
		return sb.toString();
	}
	
	protected void queryParametersAsString(StringBuffer sb) {
		try {
			Iterator<Map.Entry<String, List<String>>> queryParameterIter = _queryParameters.entrySet().iterator();
			while (queryParameterIter.hasNext()) {
				Map.Entry<String, List<String>> queryParameter = queryParameterIter.next();
				String key = queryParameter.getKey();
				Iterator<String> valuesIter = queryParameter.getValue().iterator();
				if (!valuesIter.hasNext()) {
					sb.append(URLEncoder.encode(key, CharEncoding.UTF_8));
				}
				while (valuesIter.hasNext()) {
					String value = valuesIter.next();
					sb.append(URLEncoder.encode(key, CharEncoding.UTF_8));
					if (value != null) {
						if (key.length() > 0) {
							sb.append('=');
						}
						sb.append(URLEncoder.encode(value, CharEncoding.UTF_8));
					}
					if (valuesIter.hasNext()) {
						sb.append('&');
					}
				}
				if (queryParameterIter.hasNext()) {
					sb.append('&');
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Every VM is supposed to support UTF-8 encoding.", e);
		}
	}
	
	/**
	 * Returns a java.net.URL object of this URL (which might fail if you have a
	 * relative URL).
	 * 
	 * @return a java.net.URL that represents this URL
	 * @throws MalformedURLException
	 *             if this URL cannot be represented as a java.net.URL
	 */
	public URL toURL() throws MalformedURLException {
		return new URL(toExternalForm());
	}

	@Override
	public String toString() {
		return toExternalForm();
	}

	public static void main(String[] args) throws MalformedURLException {
		System.out.println("ERXMutableURL.main: " + new ERXMutableURL("http://java.sun.com:80/docs/books/tutorial/index.html?name=networking#DOWNLOADING"));
		System.out.println("ERXMutableURL.main: " + new ERXMutableURL("https://java.sun.com:443/docs/books/tutorial/index.html?name=networking#DOWNLOADING"));
		System.out.println("ERXMutableURL.main: " + new ERXMutableURL("http://java.sun.com:12/index.html?name=networking#DOWNLOADING"));
		System.out.println("ERXMutableURL.main: " + new ERXMutableURL("http://java.sun.com:80/docs/books/tutorial/index.html?name=networking&amp;name2=networking2#DOWNLOADING"));
		ERXMutableURL mu = new ERXMutableURL("http://java.sun.com:80/docs/books/tutorial/index.html?name=networking&name2=networking2#DOWNLOADING");
		mu.setRef(null);
		mu.removeQueryParameter("name2");
		mu.removeQueryParameter("name");
		System.out.println("ERXMutableURL.main: " + mu);
		System.out.println("ERXMutableURL.main: " + new ERXMutableURL("/docs/books/tutorial/index.html?name=networking&amp;name2=networking2#DOWNLOADING"));
	}
}