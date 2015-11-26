package er.extensions.appserver;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXMutableURL;

/**
 * <span class="en">
 * ERXRedirect is like a WORedirect except that you can give it a
 * component instance to redirect to (as well as several other convenient
 * methods of redirecting). This is useful for situations like in an Ajax
 * request where you want to do a full page reload that points to the component
 * that you would normally return from your action method. If your redirect is
 * in an Ajax request, this will generate a script tag that reassigns
 * document.location.href to the generated url.
 * </span>
 *
 * <span class="ja">
 * ERXRedirect は WORedirect と同様ですが、レダイレクトはコンポーネント・インスタンスへ実行できることです。
 * 他にもレダイレクトに便利なメソッドもあります。
 * 
 * Ajax リクエストなどでとても有効です。例えば、全ページのリロード（普段はアクション・メソッドのコール）へポイントします。
 * リダイレクトが Ajax リクエストの場合にはスクリプト・タグの document.location.href で URL 生成されます。
 * </span>
 * 
 * @author mschrag
 */
public class ERXRedirect extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String _url;
	private String _requestHandlerKey;
	private String _requestHandlerPath;
	private Boolean _secure;
	private boolean _includeSessionID;

	private String _directActionClass;
	private String _directActionName;
	private WOComponent _originalComponent;
	private WOComponent _component;
	private NSDictionary<String, ? extends Object> _queryParameters;

	public ERXRedirect(WOContext context) {
		super(context);
		_originalComponent = context.page();
		_includeSessionID = false;
	}

	/**
	 * <span class="en">
	 * Sets whether or not a secure URL should be generated. This does not apply
	 * if you set a URL directly.
	 * 
	 * @param secure
	 *            whether or not a secure URL should be generated
	 * </span>
	 * 
	 * <span class="ja">
	 * 生成される URL がセキュリティー URL であるかどうかをセットします
	 * URL を直接でセットする時には使用されません。
	 * 
	 * @param secure - セキュリティー URL を生成するかどうか
	 * </span>
	 */
	public void setSecure(boolean secure) {
		_secure = Boolean.valueOf(secure);
	}
	
	/**
	 * Sets whether or not a direct action URL should contain the session ID.
	 * This defaults to <code>false</code> to maintain backward compatibility.
	 *  
	 * @param includeSessionID
	 *            whether or not a sessionID should be included
	 */
	public void setIncludeSessionID(boolean includeSessionID) {
		_includeSessionID = includeSessionID;
	}

	/**
	 * <span class="en">
	 * Sets the request handler key to redirect to. You typically want to also
	 * set requestHandlerPath if you set this.
	 * 
	 * @param requestHandlerKey
	 *            the redirected request handler key
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクト先のリダイレクト・リクエスト・ハンドラーをセットします
	 * requestHandlerPath も同時にセットすること
	 * 
	 * @param requestHandlerKey - リダイレクト・リクエスト・ハンドラー
	 * </span>
	 */
	public void setRequestHandlerKey(String requestHandlerKey) {
		_requestHandlerKey = requestHandlerKey;
	}

	/**
	 * <span class="en">
	 * Sets the request handler path to redirect to. This requires that you also
	 * set requestHandlerKey.
	 * 
	 * @param requestHandlerPath
	 *            the request handler path to redirect to
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクト先のリクエスト・ハンドラー・パスをセットします
	 * requestHandlerKey も同時にセットすること
	 * 
	 * @param requestHandlerPath - リクエスト・ハンドラー・パス
	 * </span>
	 */
	public void setRequestHandlerPath(String requestHandlerPath) {
		_requestHandlerPath = requestHandlerPath;
	}

	/**
	 * <span class="en">
	 * Sets the direct action class to redirect to. You typically want to also
	 * set directActionName if you set this.
	 * 
	 * @param directActionClass
	 *            the direct action class to redirect to
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクト先のダイレクトアクション・クラスをセットします
	 * directActionName も同時にセットすること
	 * 
	 * @param directActionClass - ダイレクトアクション・クラス
	 * </span>
	 */
	public void setDirectActionClass(String directActionClass) {
		_directActionClass = directActionClass;
	}

	/**
	 * <span class="en">
	 * The direct action name to redirect to.
	 * 
	 * @param directActionName
	 *            the direct action name
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクト先のダイレクトアクション名をセットします
	 * 
	 * @param directActionName - ダイレクトアクション名
	 * </span>
	 */
	public void setDirectActionName(String directActionName) {
		_directActionName = directActionName;
	}

	/**
	 * <span class="en">
	 * Sets the URL to redirect to.
	 * 
	 * @param url
	 *            the URL to redirect to
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクト先の URL をセットします
	 * 
	 * @param url - リダイレクト先の URL
	 * </span>
	 */
	public void setUrl(String url) {
		_url = url;
	}

	/**
	 * <span class="en">
	 * Sets the redirect component to be the original page that we were just on.
	 * </span>
	 * 
	 * <span class="ja">
	 * 現時点にいるコンポーネントをリダイレクト先コンポーネントにすること
	 * </span>
	 */
	public void setComponentToPage() {
		_component = _originalComponent;
	}

	/**
	 * <span class="en">
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
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクト先のコンポーネント・インスタンスをセットします。
	 * このコンポーネントはカレント・コンテクストのページとして置き換わり、
	 * コンテクスト ID のリクエストがリダイレクトするコンポーネントを戻すカレント・コンテクストへの URL が生成されます。
	 * リダイレクト・コンポーネントをセットすることでコンポーネントは一般ページ・キャシュ（Ajax ページ・キャシュではなく）
	 * に登録され、このリクエストでの Ajax キャシュは使用禁止されます。結果としてコンポーネントへのリダイレクトは
	 * （ハイパーリンクと同じく）バックトラック・キャシュを作成します。
	 * 
	 * @param component - リダイレクト先のコンポーネント・インスタンス
	 * </span>
	 */
	public void setComponent(WOComponent component) {
		_component = component;
	}

	/**
	 * <span class="en">
	 * Sets the query parameters for this redirect.
	 * 
	 * @param queryParameters
	 *            the query parameters for this redirect
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクトのクエリー・パラメータをセットします
	 * 
	 * @param queryParameters - リダイレクトのクエリー・パラメータ
	 * </span>
	 */
	public void setQueryParameters(NSDictionary<String, ? extends Object> queryParameters) {
		_queryParameters = queryParameters;
	}

	/**
	 * <span class="en">
	 * Returns the query parameters dictionary as a string.
	 * 
	 * @return the query parameters as a string
	 * </span>
	 * 
	 * <span class="ja">
	 * クエリー・パラメータ・ディクショナリーを文字列として戻します。
	 * 
	 * @return クエリー・パラメータ文字列
	 * </span>
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
	
	protected NSDictionary<String, Object> directActionQueryParameters() {
		NSMutableDictionary<String, Object> params = null;
		if (_queryParameters != null) {
			params = (NSMutableDictionary<String, Object>) _queryParameters.mutableClone();
		} else {
			params = new NSMutableDictionary<String, Object>();
		}
		if (!_includeSessionID) {
			params.takeValueForKey(Boolean.FALSE.toString(), WOApplication.application().sessionIdKey());
		}
		return params;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		String url;
		
		// Use secure binding if present, otherwise default to request setting
 		boolean secure = (_secure == null) ? ERXRequest.isRequestSecure(context.request()) : _secure.booleanValue();
 		
 		// Check whether we are currently generating complete URL's. We'll use this in finally() to reset the context to it's behavior before calling this.
 		boolean generatingCompleteURLs = context.doesGenerateCompleteURLs();
 
 		// Generate a full URL if changing between secure and insecure
		boolean generateCompleteURLs = secure != ERXRequest.isRequestSecure(context.request());
		if (generateCompleteURLs) {
		  context.generateCompleteURLs();
		}
		
		try {
			WOComponent component = _component;
			if (component != null) {
				
				// Build request handler path with session ID if needed
		        WOSession aSession = session();
				String aContextId = context.contextID();
				StringBuilder requestHandlerPath = new StringBuilder();
				if (WOApplication.application().pageCacheSize() == 0) {
					if (aSession.storesIDsInURLs()) {
						requestHandlerPath.append(component.name());
						requestHandlerPath.append('/');
						requestHandlerPath.append(aSession.sessionID());
						requestHandlerPath.append('/');
						requestHandlerPath.append(aContextId);
						requestHandlerPath.append(".0");
					}
					else {
						requestHandlerPath.append(component.name());
						requestHandlerPath.append('/');
						requestHandlerPath.append(aContextId);
						requestHandlerPath.append(".0");
					}
				}
				else if (aSession.storesIDsInURLs()) {
					requestHandlerPath.append(aSession.sessionID());
					requestHandlerPath.append('/');
					requestHandlerPath.append(aContextId);
					requestHandlerPath.append(".0");
				}
				else {
					requestHandlerPath.append(aContextId);
					requestHandlerPath.append(".0");
				}
				url = context._urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), requestHandlerPath.toString(), queryParametersString(), secure);
				context._setPageComponent(component);
			}
			else if (_url != null) {
				if (_secure != null) {
					throw new IllegalArgumentException("You specified a value for 'url' and for 'secure', which is not supported.");
				}
				url = _url;
				
				// the external url don't need it but if the url is a internal CMS Link then queryParamers is nice to have
				if (_queryParameters != null && _queryParameters.count() > 0)
					url += "?" + queryParametersString();
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
				url = context.directActionURLForActionNamed(requestHandlerPath, directActionQueryParameters(), secure, 0, false);
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
		finally {
			// Switch the context back to the original url behaviour.
			if (generatingCompleteURLs) {
				context.generateCompleteURLs();
			} else {
				context.generateRelativeURLs();
			}
		}
	}
}
