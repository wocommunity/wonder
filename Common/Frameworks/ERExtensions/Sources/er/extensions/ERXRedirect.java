package er.extensions;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXComponentRedirect is like a WORedirect except that you can give it a
 * compoennt instance to redirect to (as well as several other convenient
 * methods of redirecting). This is useful for situations like in an Ajax
 * request where you want to do a full page reload that points to the component
 * that you would normally return from your action method. If your redirect is
 * in an Ajax request, this will generate a script tag that reassigns
 * document.location.href to the generated url.
 * 
 * @author mschrag
 */
public class ERXRedirect extends WOComponent {
	private String _url;
	private String _requestHandlerKey;
	private String _requestHandlerPath;
	private Boolean _secure;

	private String _directActionClass;
	private String _directActionName;
	private WOComponent _originalComponent;
	private WOComponent _component;
	private NSDictionary _queryParameters;

	public ERXRedirect(WOContext context) {
		super(context);
		_originalComponent = context.page();
	}

	/**
	 * Sets whether or not a secure URL should be generated. This does not apply
	 * if you set a URL directly.
	 * 
	 * @param secure
	 *            whether or not a secure URL should be generated
	 */
	public void setSecure(boolean secure) {
		_secure = Boolean.valueOf(secure);
	}

	/**
	 * Sets the request handler key to redirect to. You typically want to also
	 * set requestHandlerPath if you set this.
	 * 
	 * @param requestHandlerKey
	 *            the redirected request handler key
	 */
	public void setRequestHandlerKey(String requestHandlerKey) {
		_requestHandlerKey = requestHandlerKey;
	}

	/**
	 * Sets the request handler path to redirect to. This requires that you also
	 * set requestHandlerKey.
	 * 
	 * @param requestHandlerPath
	 *            the request handler path to redirect to
	 */
	public void setRequestHandlerPath(String requestHandlerPath) {
		_requestHandlerPath = requestHandlerPath;
	}

	/**
	 * Sets the direct action class to redirect to. You typically want to also
	 * set directActionName if you set this.
	 * 
	 * @param directActionClass
	 *            the direct action class to redirect to
	 */
	public void setDirectActionClass(String directActionClass) {
		_directActionClass = directActionClass;
	}

	/**
	 * The direct action name to redirect to.
	 * 
	 * @param directActionName
	 *            the direct action name
	 */
	public void setDirectActionName(String directActionName) {
		_directActionName = directActionName;
	}

	/**
	 * Sets the URL to redirect to.
	 * 
	 * @param url
	 *            the URL to redirect to
	 */
	public void setUrl(String url) {
		_url = url;
	}

	/**
	 * Sets the redirect component to be the original page that we were just on.
	 */
	public void setComponentToPage() {
		_component = _originalComponent;
	}

	/**
	 * Sets the component instance to redirect to. This component gets replaced
	 * as the page in the current context, and a URL is generated to the current
	 * context, which causes the request for that context ID to return the
	 * component you are redirecting to. When you set a redirect component, the
	 * component is also put into the normal page cache (rather than the ajax
	 * page cache), and the ajax cache is disabled for this request. As a
	 * result, redirecting to a component WILL burn a backtrack cache entry
	 * (just like a normal hyperlink).
	 * 
	 * @param component
	 *            the component instance to redirect to
	 */
	public void setComponent(WOComponent component) {
		_component = component;
	}

	/**
	 * Sets the query parameters for this redirect.
	 * 
	 * @param queryParameters
	 *            the query parameters for this redirect
	 */
	public void setQueryParameters(NSDictionary queryParameters) {
		_queryParameters = queryParameters;
	}

	/**
	 * Returns the query parameters dictionary as a string.
	 * 
	 * @return the query parameters as a string
	 */
	protected String queryParametersString() {
		String queryParametersString = null;
		if (_queryParameters != null && _queryParameters.count() > 0) {
			ERXMutableURL u = new ERXMutableURL();
			u.setQueryParameters(_queryParameters);
			queryParametersString = u.toExternalForm();
		}
		return queryParametersString;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		String url;

		boolean secure = (_secure == null) ? ERXRequest.isRequestSecure(context.request()) : _secure.booleanValue();

		WOComponent component = _component;
		if (component != null) {
			String requestHandlerPath = context.contextID() + ".0";
			url = context._urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), requestHandlerPath, queryParametersString(), secure);
			context._setPageComponent(component);
		}
		else if (_url != null) {
			if (_secure != null) {
				throw new IllegalArgumentException("You specified a value for 'url' and for 'secure', which is not supported.");
			}
			url = _url;
		}
		else if (_requestHandlerKey != null) {
			url = context._urlWithRequestHandlerKey(_requestHandlerKey, _requestHandlerPath, queryParametersString(), secure);
		}
		else if (_directActionName != null) {
			String requestHandlerPath;
			if (_directActionClass != null) {
				requestHandlerPath = _directActionClass + "/" + _directActionName;
			}
			else {
				requestHandlerPath = _directActionName;
			}
			url = context._urlWithRequestHandlerKey(WOApplication.application().directActionRequestHandlerKey(), requestHandlerPath, queryParametersString(), secure);
		}
		else {
			throw new IllegalStateException("You must provide a component, url, requestHandlerKey, or directActionName to this ERXRedirect.");
		}

		if (ERXAjaxApplication.isAjaxRequest(context.request())) {
			boolean hasUpdateContainer = context.request().stringFormValueForKey(ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID) != null;
			if (hasUpdateContainer) {
				response.appendContentString("<script type=\"text/javascript\">");
			}
			else {
				response.setHeader("text/javascript", "Content-Type");
			}
			response.appendContentString("document.location.href='" + url + "';");
			if (hasUpdateContainer) {
				response.appendContentString("</script>");
			}
		}
		else {
			response.setHeader(url, "location");
			response.setStatus(302);
		}

		if (component != null) {
			ERXAjaxApplication.setForceStorePage(response);
		}
	}
}
