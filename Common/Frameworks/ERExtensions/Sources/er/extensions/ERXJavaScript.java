package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.appserver._private.WOStaticURLUtilities;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSStringUtilities;

/**
 * Modern version of a javascript component. 
 * <ul>
 *   <li> HTML 4 compliant ("script" and attributes lowercased)
 *   <li> hideInComment is ON by default if there is content
 *   <li> can contain script text
 *   <li> can have the script render to an external DA url that is cached in the session
 *   <li> you can specify which framework the script comes from.
 * </ul>
 * @binding scriptSource SRC attribute, either a full URL or the filename of the script 
 * @binding scriptFile the filename of the script when it should be 
 *    included in the page (only for compatibility, simply use the content)
 * @binding scriptFramework name of the framework for the script
 * @binding scriptString the script text when it should be 
 *    included in the page (only for compatibility, simply use the content)
 * @binding scriptKey if set, the content will get rendered into an external script src
 * @binding hideInComment boolean that specifies if the script content should
 *   be included in HTML comments, true by default of the script tag contains a script
 */

public class ERXJavaScript extends WOHTMLDynamicElement {

    private static ERXExpiringCache cache(WOSession session) {
    	ERXExpiringCache cache = (ERXExpiringCache) session.objectForKey("ERXJavaScript.cache");
    	if(cache == null) {
    		cache = new ERXExpiringCache(60);
    		session.setObjectForKey(cache, "ERXJavaScript.cache");
    	}
    	return cache;
    }

    public static class Script extends WODirectAction {

    	public Script(WORequest worequest) {
			super(worequest);
    	}

    	public WOActionResults performActionNamed(String name) {
    		WOResponse response = (WOResponse) cache(session()).objectForKey(name);
    		return response;
    	}
    }
    
	/** logging support */
	public static final Logger log = Logger.getLogger(ERXJavaScript.class);

	WOAssociation _scriptFramework;
	WOAssociation _scriptFile;
	WOAssociation _scriptString;
	WOAssociation _scriptSource;
	WOAssociation _scriptKey;
	WOAssociation _hideInComment;
	WOAssociation _language;

	public ERXJavaScript(String s, NSDictionary nsdictionary, WOElement woelement) {
		super("script", nsdictionary, woelement);
		_scriptFile = (WOAssociation)_associations.removeObjectForKey("scriptFile");
		_scriptString = (WOAssociation)_associations.removeObjectForKey("scriptString");
		_scriptSource = (WOAssociation)_associations.removeObjectForKey("scriptSource");
		_language = (WOAssociation)_associations.removeObjectForKey("language");
		_scriptKey = (WOAssociation)_associations.removeObjectForKey("scriptKey");
		_hideInComment = (WOAssociation)_associations.removeObjectForKey("hideInComment");
		_scriptFramework = (WOAssociation) _associations.removeObjectForKey("scriptFramework");
		if((_scriptFile != null && _scriptString != null) 
				|| (_scriptFile != null && _scriptSource != null) 
				|| (_scriptString != null && _scriptSource != null)) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Only one of 'scriptFile' or 'scriptString' or 'scriptSource' attributes can be specified.");
		}
	}

	public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		woresponse._appendContentAsciiString(" type=\"text/javascript\"");
		String src = null;
		if(_scriptSource != null) {
			String srcFromBindings = (String)_scriptSource.valueInComponent(wocomponent);
			if(srcFromBindings != null) {
				if(!WOStaticURLUtilities.isRelativeURL(srcFromBindings)) {
					src = srcFromBindings;
				} else {
					if(!WOStaticURLUtilities.isFragmentURL(srcFromBindings)) {
						String framework = null;
						if(_scriptFramework != null) {
							framework = (String) _scriptFramework.valueInComponent(wocomponent);
						}
						src = wocontext._urlForResourceNamed(srcFromBindings, framework, true);
						if(src == null) {
							src = wocomponent.baseURL() + "/" + srcFromBindings;
						}
					} else {
						log.warn("relative fragment URL" + srcFromBindings);
					}
				}
			}
		}
		Object key = null;
		if(src == null && _scriptKey != null) {
			key = _scriptKey.valueInComponent(wocomponent);
			if(key != null) {
				ERXExpiringCache cache = cache(wocontext.session());
				boolean render = cache.isStale(key);
				render |= ERXApplication.isDevelopmentModeSafe();
				if(render) {
					WOResponse newresponse = new WOResponse();
					super.appendChildrenToResponse(newresponse, wocontext);
					newresponse.setHeader("application/x-javascript", "content-type");
					cache.setObjectForKey(newresponse, key);
				}
				src = wocontext.directActionURLForActionNamed(Script.class.getName() + "/" + key, null);
			}
		}
		if(src != null) {
			woresponse._appendContentAsciiString(" src=\"");
			woresponse.appendContentString(src);
			woresponse.appendContentCharacter('"');
		}
		super.appendAttributesToResponse(woresponse, wocontext);
	}


	public void appendChildrenToResponse(WOResponse woresponse, WOContext wocontext) {
			String script = "";
			boolean hideInComment = true;
			WOComponent wocomponent = wocontext.component();
			if(_hideInComment != null) {
				hideInComment = _hideInComment.booleanValueInComponent(wocomponent);
			}
			if(hideInComment) {
				woresponse._appendContentAsciiString("<!--");
			}
			woresponse.appendContentCharacter('\n');
			if(_scriptFile != null) {
				String filename = (String) _scriptFile.valueInComponent(wocomponent);
				if(filename != null) {
					String framework = null;
					if(_scriptFramework != null) {
						framework = (String) _scriptFramework.valueInComponent(wocomponent);
					}
					java.net.URL url = WOApplication.application().resourceManager().pathURLForResourceNamed(filename, framework, wocontext._languages());
					if(url == null) {
						url = wocontext.component()._componentDefinition().pathURLForResourceNamed(filename, framework, wocontext._languages());
					}
					if(url == null) {
						throw new WODynamicElementCreationException("<" + getClass().getName() + "> : cannot find script file '" + filename + "'");
					}
					script = _NSStringUtilities.stringFromPathURL(url);
				}
				woresponse.appendContentString(script);
			} else if(_scriptString != null) {
				Object obj1 = _scriptString.valueInComponent(wocomponent);
				if(obj1 != null) {
					script = obj1.toString();
				}
				woresponse.appendContentString(script);
			} else {
				super.appendChildrenToResponse(woresponse, wocontext);
			}
			woresponse.appendContentCharacter('\n');
			if(hideInComment) {
				woresponse._appendContentAsciiString("//-->");
			}
	}

	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		if(wocontext == null || woresponse == null) {
			return;
		}
		String s = elementName();
		if(s != null) {
			_appendOpenTagToResponse(woresponse, wocontext);
		}
		if(_scriptSource == null && hasChildrenElements() 
				&& _scriptKey == null) {
			appendChildrenToResponse(woresponse, wocontext);
		}
		if(s != null) {
			_appendCloseTagToResponse(woresponse, wocontext);
		}
	}
    
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(getClass().getName());
		sb.append(" scriptFile=" + _scriptFile);
		sb.append(" scriptString=" + _scriptString);
		sb.append(" scriptFramework=" + _scriptFramework);
		sb.append(" scriptSource=" + _scriptSource);
		sb.append(" hideInComment=" + _hideInComment);
		sb.append(" language=" + _language);
		sb.append(">");
		return sb.toString();
	}
}