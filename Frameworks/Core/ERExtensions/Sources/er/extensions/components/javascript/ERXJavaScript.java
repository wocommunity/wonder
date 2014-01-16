package er.extensions.components.javascript;

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

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXResourceManager;
import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXExpiringCache;
import er.extensions.foundation.ERXProperties;

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
 * @binding filename (same as scriptSource, but matches ERXStyleSheet)
 * @binding scriptFile the filename of the script when it should be 
 *    included in the page (only for compatibility, simply use the content)
 * @binding scriptFramework name of the framework for the script
 * @binding framework (same as scriptFramework, but matches ERXStyleSheet)
 * @binding scriptString the script text when it should be 
 *    included in the page (only for compatibility, simply use the content)
 * @binding scriptKey if set, the content will get rendered into an external script src
 * @binding hideInComment boolean that specifies if the script content should
 *   be included in HTML comments, true by default of the script tag contains a script
 *   
 * @property er.extensions.ERXJavaScript.hideInComment sets globally if the script
 *   content should be included within HTML comments, defaults to <code>true</code>
 */
public class ERXJavaScript extends WOHTMLDynamicElement {

    @SuppressWarnings("unchecked")
	private static ERXExpiringCache<Object, WOResponse> cache(WOSession session) {
    	ERXExpiringCache<Object, WOResponse> cache = (ERXExpiringCache<Object, WOResponse>) session.objectForKey("ERXJavaScript.cache");
    	if(cache == null) {
    		cache = new ERXExpiringCache<Object, WOResponse>(60);
    		session.setObjectForKey(cache, "ERXJavaScript.cache");
    	}
    	return cache;
    }

    public static class Script extends WODirectAction {

    	public Script(WORequest worequest) {
			super(worequest);
    	}

    	@Override
		public WOActionResults performActionNamed(String name) {
    		WOResponse response = ERXJavaScript.cache(session()).objectForKey(name);
    		return response;
    	}
    }
    
	/** logging support */
	public static final Logger log = Logger.getLogger(ERXJavaScript.class);

	WOAssociation _framework;
	WOAssociation _scriptFramework;
	WOAssociation _filename;
	WOAssociation _scriptFile;
	WOAssociation _scriptString;
	WOAssociation _scriptSource;
	WOAssociation _scriptKey;
	WOAssociation _hideInComment;
	WOAssociation _language;

	public ERXJavaScript(String s, NSDictionary<String, WOAssociation> nsdictionary, WOElement woelement) {
		super("script", nsdictionary, woelement);
		_scriptFile = _associations.removeObjectForKey("scriptFile");
		_scriptString = _associations.removeObjectForKey("scriptString");
		_scriptSource = _associations.removeObjectForKey("scriptSource");
		_filename = _associations.removeObjectForKey("filename");
		_language = _associations.removeObjectForKey("language");
		_scriptKey = _associations.removeObjectForKey("scriptKey");
		_hideInComment = _associations.removeObjectForKey("hideInComment");
		_scriptFramework = _associations.removeObjectForKey("scriptFramework");
		_framework = _associations.removeObjectForKey("framework");
		if((_scriptFile != null && _scriptString != null) 
				|| (_scriptFile != null && (_scriptSource != null || _filename != null)) 
				|| (_scriptString != null && (_scriptSource != null || _filename != null))) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Only one of 'scriptFile' or 'scriptString' or 'scriptSource/filename' attributes can be specified.");
		}
		if (_scriptFramework != null && _framework != null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Only one of 'scriptFramework' or 'framework' can be specified.");
		}
		if (_scriptSource != null && _filename != null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Only one of 'scriptFile' or 'filename' can be specified.");
		}
	}

	@Override
	public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		woresponse._appendContentAsciiString(" type=\"text/javascript\"");
		
		String framework = null;
		String scriptName = null;
		
		String src = null;
		if(_scriptSource != null || _filename != null) {
			String srcFromBindings;
			if (_scriptSource != null) {
				srcFromBindings = (String)_scriptSource.valueInComponent(wocomponent);
			}
			else {
				srcFromBindings = (String) _filename.valueInComponent(wocomponent);
			}
			if(srcFromBindings != null) {
				if(!WOStaticURLUtilities.isRelativeURL(srcFromBindings)) {
					src = srcFromBindings;
				} else {
					if(!WOStaticURLUtilities.isFragmentURL(srcFromBindings)) {
						if(_scriptFramework != null) {
							framework = (String) _scriptFramework.valueInComponent(wocomponent);
						}
						else if (_framework != null) {
							framework = (String) _framework.valueInComponent(wocomponent);
						}
						scriptName = srcFromBindings;
						src = wocontext._urlForResourceNamed(srcFromBindings, framework, true);
						if(src == null) {
							src = wocomponent.baseURL() + "/" + srcFromBindings;
						}
						else if (ERXResourceManager._shouldGenerateCompleteResourceURL(wocontext)) {
							src = ERXResourceManager._completeURLForResource(src, null, wocontext);
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
				ERXExpiringCache<Object, WOResponse> cache = ERXJavaScript.cache(wocontext.session());
				boolean render = cache.isStale(key);
				render |= ERXApplication.isDevelopmentModeSafe();
				if(render) {
					WOResponse newresponse = new ERXResponse();
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
		
		if (scriptName != null) {
			ERXResponseRewriter.resourceAddedToHead(wocontext, framework, scriptName);
		}
	}


	@Override
	public void appendChildrenToResponse(WOResponse woresponse, WOContext wocontext) {
			String script = "";
			boolean hideInComment = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXJavaScript.hideInComment", true);
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
					else if (_framework != null) {
						framework = (String) _framework.valueInComponent(wocomponent);
					}
					java.net.URL url = WOApplication.application().resourceManager().pathURLForResourceNamed(filename, framework, wocontext._languages());
					if(url == null) {
						url = wocontext.component()._componentDefinition().pathURLForResourceNamed(filename, framework, wocontext._languages());
					}
					if(url == null) {
						throw new WODynamicElementCreationException("<" + getClass().getName() + "> : cannot find script file '" + filename + "'");
					}
					script = _NSStringUtilities.stringFromPathURL(url);
					if (ERXResourceManager._shouldGenerateCompleteResourceURL(wocontext)) {
						script = ERXResourceManager._completeURLForResource(script, null, wocontext);
					}
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

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		if(wocontext == null || woresponse == null) {
			return;
		}
		String s = elementName();
		if(s != null) {
			_appendOpenTagToResponse(woresponse, wocontext);
		}
		if(_scriptSource == null && _filename == null && ( hasChildrenElements() || _scriptString != null)
				&& _scriptKey == null) {
			appendChildrenToResponse(woresponse, wocontext);
		}
		if(s != null) {
			_appendCloseTagToResponse(woresponse, wocontext);
		}
	}
    
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		sb.append(getClass().getName());
		sb.append(" scriptFile=" + _scriptFile);
		sb.append(" scriptString=" + _scriptString);
		sb.append(" scriptFramework=" + _scriptFramework);
		sb.append(" framework=" + _framework);
		sb.append(" scriptSource=" + _scriptSource);
		sb.append(" filename=" + _filename);
		sb.append(" hideInComment=" + _hideInComment);
		sb.append(" language=" + _language);
		sb.append('>');
		return sb.toString();
	}
}