package er.extensions.foundation;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOCGIFormValues;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLAttribute;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.appserver._private.WONoContentElement;
import com.webobjects.appserver._private.WOStaticURLUtilities;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSDictionaryUtilities;

/**
 * ERXHyperlinkResource is very similar to WOHyperlink action Binding are the some,
 * but there is one big difference. There is a "linkResource" Binding.
 * 
 * Like the ERXStaticResource this Object creates a URL just in Time for 
 * your URL with a new Syntax:
 * 
 * href
 *    http://{url}
 *    https://{url}
 * 
 * Direct Action
 *    da://{actionClass}:{directActionName}
 *    da://{directActionName}
 *    wa://{actionClass}:{directActionName}
 *    wa://{directActionName}
 * 
 * REST
 *    rest://{restAction}
 *    ra://{restAction}
 * 
 * Page
 *    page://{pageName}
 * 
 * SnoWOman
 *    cms://{cmsUrl}
 * 
 * WOResource
 *    static://{frameworkName}:{fileName}
 *    static://{fileName}
 *    
 * 
 * Reason 1 : Bindings
 * normal way
 *    &lt;wo:hyperlink actionClass="{actionClass}" directActionName="{directActionName}" ... /&gt;
 * 
 * HyperlinkResource
 *    &lt;wo:ERXHyperlinkResource linkResource="da://{actionClass}:{directActionName}" ... /&gt;
 * 
 * Reason 2 : CMS
 * HyperlinkResource Object makes it easy to create Objects in CMS Systems.
 * It will also heavily used in SnoWOman and other coming Frameworks.
 * It is easy to write a URL into the Database and retrieve the Link
 * via this Object.
 */
public class ERXHyperlinkResource extends WOHTMLDynamicElement {

	//********************************************************************
	//  WOAssociation
	//********************************************************************

	protected WOAssociation                      _action;
	protected WOAssociation                      _string;
	protected WOAssociation                      _linkResource;
	protected WOAssociation                      _disabled;
	protected WOAssociation                      _fragmentIdentifier;
	protected WOAssociation                      _escapeHTML;
	private final WOAssociation                  _queryDictionary;
	private NSDictionary<String, WOAssociation>  _otherQueryAssociations;

	//********************************************************************
	//  Constructor
	//********************************************************************

	@SuppressWarnings("unchecked")
	public ERXHyperlinkResource(String aName, NSDictionary<String, WOAssociation> someAssociations, WOElement template) {
		super("a", someAssociations, template);

		_otherQueryAssociations = _NSDictionaryUtilities.extractObjectsForKeysWithPrefix(_associations, "?", true);
		_otherQueryAssociations = ((_otherQueryAssociations != null) && (_otherQueryAssociations.count() > 0)) ? _otherQueryAssociations : null;

		_disabled = _associations.removeObjectForKey(WOHTMLAttribute.Disabled);
		_queryDictionary = _associations.removeObjectForKey(WOHTMLAttribute.QueryDictionary);
		_escapeHTML = _associations.removeObjectForKey(WOHTMLAttribute.EscapeHTML);
		_string = _associations.removeObjectForKey(WOHTMLAttribute.String);
		_fragmentIdentifier = _associations.removeObjectForKey(WOHTMLAttribute.FragmentIdentifier);
		_action = _associations.removeObjectForKey(WOHTMLAttribute.Action);
		_linkResource = _associations.removeObjectForKey("linkResource");

		if (_linkResource == null && _action == null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Missing required attribute: 'linkResource' or 'action' ");
		}
	}

	@Override
	public String toString() {
		return "<" + getClass().getName() + " string: " + _string + " linkResource: " + _linkResource + " queryDictionary: " + _queryDictionary
				+ " otherQueryAssociations: " + _otherQueryAssociations + " fragmentIdentifier: " + _fragmentIdentifier + " disabled: " + _disabled + " secure: " + _secure
				+ ">";
	}

	//********************************************************************
	//  RR-Methods
	//********************************************************************

	@Override
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		String nextPageName = null;
		WOActionResults invokedElement = null;
		final WOComponent component = aContext.component();
		if (aContext.elementID().equals(aContext.senderID())) {
			if (!((_disabled != null) && _disabled.booleanValueInComponent(component))) {

				if (_linkResource != null) {
					Object nextPageValue = _linkResource.valueInComponent(component);
					if (nextPageValue != null) {
						nextPageName = nextPageValue.toString();

						if(nextPageName.startsWith("page://")) {
							int i = nextPageName.indexOf("://") + 3;
							nextPageName = nextPageName.substring(i);
							if (nextPageName != null) {
								invokedElement = WOApplication.application().pageWithName(nextPageName, aContext);
							}
						}
					}
				}

				if(_action != null) {
					invokedElement = (WOActionResults) _action.valueInComponent(component);
				}

			} else {
				invokedElement = new WONoContentElement();
			}
			if (invokedElement == null) {
				invokedElement = aContext.page();
			}
		}
		return invokedElement;
	}

	//********************************************************************
	//  Methods
	//********************************************************************

	@Override
	protected void _appendOpenTagToResponse(WOResponse aResponse, WOContext aContext) {
		// スーパークラスのメソッドをオーバライドする必要があります：disable 機能の追加
		if (!isDisabledInContext(aContext)) {
			super._appendOpenTagToResponse(aResponse, aContext);
		}
	}

	@Override
	protected void _appendCloseTagToResponse(WOResponse aResponse, WOContext aContext) {
		// スーパークラスのメソッドをオーバライドする必要があります：disable 機能の追加
		if (!isDisabledInContext(aContext)) {
			super._appendCloseTagToResponse(aResponse, aContext);
		}
	}

	protected void _appendQueryStringToResponse(WOResponse aResponse, WOContext aContext, String aRequestHandlerPath, boolean htmlEscapeURL, boolean defaultIncludeSessionID) {
		NSDictionary<String, Object> aQueryDict = computeQueryDictionaryInContext(aRequestHandlerPath != null ? aRequestHandlerPath : "", _queryDictionary, _otherQueryAssociations,
				defaultIncludeSessionID, aContext);
		if (aQueryDict.count() > 0) {
			String aQueryString = WOCGIFormValues.getInstance().encodeAsCGIFormValues(aQueryDict, htmlEscapeURL);
			if (aQueryString.length() > 0) {
				int questionMarkIndex = (aRequestHandlerPath != null ? aRequestHandlerPath.indexOf("?") : -1);
				if (questionMarkIndex > 0) {
					aResponse.appendContentString(htmlEscapeURL ? "&amp;" : "&");
				} else {
					aResponse.appendContentCharacter('?');
				}
				aResponse.appendContentString(aQueryString);
			}
		}
	}

	protected void _appendFragmentToResponse(WOResponse aResponse, WOContext aContext) {
		String fragmentIdentifier = fragmentIdentifierInContext(aContext);
		if (fragmentIdentifier.length() > 0) {
			aResponse.appendContentCharacter('#');
			aResponse.appendContentString(fragmentIdentifier);
		}
	}

	protected void _appendCGIActionURLToResponse(WOResponse aResponse, WOContext aContext, boolean htmlEscapeURL, String actionPath) {
		NSDictionary<String, Object> aQueryDict = computeQueryDictionaryInContext(actionPath, _queryDictionary, _otherQueryAssociations, true, aContext);
		aResponse.appendContentString(aContext._directActionURL(actionPath, aQueryDict, secureInContext(aContext), 0, htmlEscapeURL));
		_appendFragmentToResponse(aResponse, aContext);
	}

	protected void _appendComponentActionURLToResponse(WOResponse response, WOContext context, boolean escapeHTML) {
		String actionURL = context.componentActionURL(WOApplication.application().componentRequestHandlerKey(), secureInContext(context));
		response.appendContentString(actionURL);
		_appendQueryStringToResponse(response, context, actionURL, escapeHTML, true);
		_appendFragmentToResponse(response, context);
	}

	/**
	 * <span class="ja">
	 * static URL バインディング処理
	 * 
	 * @param response - リスポンス
	 * @param context - コンテキスト
	 * @param escapeHTML - HTML 回避するかどうか
	 * @param staticURL 
	 * </span>
	 */
	protected void _appendStaticURLToResponse(WOResponse response, WOContext context, boolean escapeHTML, String staticURL) {
		if (WOStaticURLUtilities.isRelativeURL(staticURL) && !WOStaticURLUtilities.isFragmentURL(staticURL)) {
			String resourceURL = context._urlForResourceNamed(staticURL, null, false);
			if (resourceURL != null) {
				response.appendContentString(resourceURL);
				staticURL = resourceURL;
			} else {
				response.appendContentString(context.component().baseURL());
				response.appendContentCharacter('/');
				response.appendContentString(staticURL);
			}
		} else {
			// This is a non relative url already.
			response.appendContentString(staticURL);
		}    
		_appendQueryStringToResponse(response, context, staticURL, escapeHTML, false);
		_appendFragmentToResponse(response, context);
	}

	protected void _appendOpeningHrefToResponse(WOResponse response, WOContext context) {
		response.appendContentCharacter(' ');
		response.appendContentString(WOHTMLAttribute.Href);
		response.appendContentCharacter('=');
		response.appendContentCharacter('"');
		String prefix = prefixInContext(context);
		if (prefix.length() > 0)
			response.appendContentString(prefix);
	}

	protected void _appendClosingHrefToResponse(WOResponse response, WOContext context) {
		String suffix = suffixInContext(context);
		if (suffix.length() > 0)
			response.appendContentString(suffix);
		response.appendContentCharacter('"');
	}

	public void appendContentStringToResponse(WOResponse aResponse, WOContext aContext) {
		if (_string != null) {
			WOComponent aComponent = aContext.component();
			Object val = _string.valueInComponent(aComponent);
			if (val != null) {
				String valueToAppend = val.toString();
				boolean shouldEscapeHTML = true;
				if (_escapeHTML != null) {
					shouldEscapeHTML = _escapeHTML.booleanValueInComponent(aComponent);
				}
				if (shouldEscapeHTML) {
					aResponse.appendContentHTMLString(valueToAppend);
				} else {
					aResponse.appendContentString(valueToAppend);
				}
			}
		}
	}

	@Override
	public void appendChildrenToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendChildrenToResponse(aResponse, aContext);
		appendContentStringToResponse(aResponse, aContext);
	}

	protected void _appendDataAjaxFalseToResponse(WOResponse response, WOContext context) {
		response.appendContentString(" data-ajax=\"false\"");
	}

	protected String fragmentIdentifierInContext(WOContext context) {
		Object value = (_fragmentIdentifier != null ? _fragmentIdentifier.valueInComponent(context.component()) : null);
		return (value != null ? value.toString() : "");
	}

	protected boolean isDisabledInContext(WOContext context) {
		return (((_disabled != null) && _disabled.booleanValueInComponent(context.component())) || !isRenderedInContext(context));
	}

	protected String linkResourceUri(WOResponse aResponse, WOContext aContext) {
		String uri = null;
		if(_linkResource != null) {
			WOComponent aComponent = aContext.component();
			Object val = _linkResource.valueInComponent(aComponent);
			if (val != null) {
				uri = val.toString();
			}     
		}
		return uri;
	}

	@Override
	public void appendAttributesToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendAttributesToResponse(aResponse, aContext);
		_appendOpeningHrefToResponse(aResponse, aContext);

		if (_action != null) {
			_appendComponentActionURLToResponse(aResponse, aContext, true);
			_appendClosingHrefToResponse(aResponse, aContext);
			return;
		} 

		if(_linkResource != null) {
			String uri = linkResourceUri(aResponse, aContext);
			if(!ERXStringUtilities.stringIsNullOrEmpty(uri)) {

				if(uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("static://")) {
					_appendStaticURLToResponse(aResponse, aContext, true, urlForHyperlinkResource(aContext, uri));
					_appendClosingHrefToResponse(aResponse, aContext);
					_appendDataAjaxFalseToResponse(aResponse, aContext);
					return;
				} 

				if(uri.startsWith("page://")) {
					_appendStaticURLToResponse(aResponse, aContext, true, urlForHyperlinkResource(aContext, uri));
					_appendClosingHrefToResponse(aResponse, aContext);
					return;
				}

				if(uri.startsWith("da://") || uri.startsWith("wa://")) {
					int i = uri.indexOf("://") + 3;
					String s = uri.substring(i);    

					String actionClass = null;
					String directActionName = null;

					i = s.indexOf(":");
					if(i > 0) {
						actionClass = s.substring(0, i);
						directActionName = s.substring(i + 1);
					} else {
						directActionName = s;      
					}

					if ((actionClass != null) || (directActionName != null)) {
						String actionPath = computeActionStringInContext(actionClass, directActionName, aContext);
						_appendCGIActionURLToResponse(aResponse, aContext, true, actionPath); 
					}          
					_appendClosingHrefToResponse(aResponse, aContext);
					_appendDataAjaxFalseToResponse(aResponse, aContext);
					return;
				}

				if(uri.startsWith("rest://") || uri.startsWith("ra://")) {
					_appendStaticURLToResponse(aResponse, aContext, true, urlForHyperlinkResource(aContext, uri));
					_appendClosingHrefToResponse(aResponse, aContext);
					_appendDataAjaxFalseToResponse(aResponse, aContext);
					return;
				}

				if(uri.startsWith("cms://")) {
					_appendStaticURLToResponse(aResponse, aContext, true, urlForHyperlinkResource(aContext, uri));
					_appendClosingHrefToResponse(aResponse, aContext);
					_appendDataAjaxFalseToResponse(aResponse, aContext);
					return;
				}

			}
		} 
		_appendClosingHrefToResponse(aResponse, aContext);
	}

	//********************************************************************
	//  Helper Static Methods
	//********************************************************************

	/**
	 * <span class="en">
	 * Binding "actionClass" and "directActionName" calculation
	 * 
	 * @param actionClass - Action Class
	 * @param directActionName - Direct Action Name
	 * @param aContext - Context
	 * 
	 * @return Result
	 * </span>
	 * 
	 * <span class="ja">
	 * バインディング "actionClass" と "directActionName" を処理します。
	 * 
	 * @param actionClass - アクション・クラス
	 * @param directActionName - ダイレクトアクション名
	 * @param aContext - コンテキスト
	 * 
	 * @return 結果文字列
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	protected static String computeActionStringInContext(String actionClass, String directActionName, WOContext aContext) {
		String anActionString = null;

		if ((actionClass != null) && (directActionName != null)) {
			if (actionClass.equals("DirectAction"))
				anActionString = directActionName;
			else
				anActionString = actionClass + "/" + directActionName;
		} else if (actionClass != null)
			anActionString = actionClass;
		else if (directActionName != null)
			anActionString = directActionName;
		else {
			throw new IllegalStateException("<" + "ERXHyperlinkResource" 
					+ "> Both 'actionClass' and 'directActionName' are either absent or evaluated to null. Cannot generate dynamic url without an actionClass or directActionName.");
		}
		return anActionString;
	}

	public static String urlForHyperlinkResource(WOContext context, String uri) {
		if(!ERXStringUtilities.stringIsNullOrEmpty(uri)) {

			if((uri.startsWith("http://")) || (uri.startsWith("https://"))) {
				return uri;

			} else if(uri.startsWith("static://")) {
				return ERXStaticResource.urlForResourceNamed(context, uri);

			} else if((uri.startsWith("da://")) || (uri.startsWith("wa://"))) {
				int i = uri.indexOf("://") + 3;
				String s = uri.substring(i);    

				String actionClass = null;
				String directActionName = null;

				i = s.indexOf(":");
				if(i > 0) {
					actionClass = s.substring(0, i);
					directActionName = s.substring(i + 1);
				} else {
					directActionName = s;      
				}

				if ((actionClass != null) || (directActionName != null)) {
					String actionPath = computeActionStringInContext(actionClass, directActionName, context);
					if(!ERXStringUtilities.stringIsNullOrEmpty(actionPath)) {
						return context._directActionURL(actionPath, null, false, 0, false);
					}
				}

			} else if(uri.startsWith("cms://")) {
				// TODO SnoWOman coming in the Next Version

			} else if((uri.startsWith("rest://")) || (uri.startsWith("ra://"))) {
				// TODO ERRest Future Version

			} else if(uri.startsWith("page://")) {
				int i = uri.indexOf("://") + 3;
				String pageName = uri.substring(i);

				if(!ERXStringUtilities.stringIsNullOrEmpty(pageName)) {
					return context.componentActionURL(WOApplication.application().componentRequestHandlerKey(), context.secureMode());
				}
			}
		}

		return null;
	}
}
