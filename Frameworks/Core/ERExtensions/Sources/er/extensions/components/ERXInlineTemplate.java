package er.extensions.components;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXMutableDictionary;
import er.extensions.foundation.ERXSimpleTemplateParser;

/**
 * ERXInlineTemplate allows to specify a component's template dynamically.
 * <p>
 * The content which would usually go into the ".html" file within a WOComponent's bundle, is specified using the "html"
 * binding, the ".wod" part is specified by the "wod" binding.
 * <p>
 * When using {@link WOOgnl} with "ognl.helperFunctions = true" and "ognl.inlineBindings = true", you can leave out the
 * WOD part.
 * <p>
 * When keys are accessed, the component first determines the first element of the path (e.g. key "foo" for path
 * "foo.bar") and looks, if there is a binding with that key.
 * If there is such a binding, the value is retrieved and the rest of the keyPath applied to it
 * (valueForBinding("foo").valueForKeyPath("bar")).
 * If there is no binding with that name and "proxyParent" is true, the keyPath is resolved against the parent component.
 * Otherwise, dynamicBindings ({@link ERXComponent#dynamicBindings()}) are used.
 * You can switch off the usage of dynamicBindings by setting the binding "defaultToDynamicBindings" to false. 
 * Then a warning will be logged for unknown keys.
 * <p>
 * When an error occurs, an error message is displayed. The message can be altered using the "errorTemplate" binding.
 * <p>
 * Optionally, a "cacheKey" (String) can be specified, under which the parsed WOElement will be cached. To allow
 * updating, a "cacheVersion" (Object) is available. When the version changes, the value is recalculated.
 * 
 * @binding html HTML-part of the component (required)
 * @binding wod WOD-part of the component (optional)
 * @binding cacheKey Key under which to cache the WOElement (optional)
 * @binding cacheVersion Hint to determine if the cached object is up-to-date (optional)
 * @binding errorTemplate Template to use for displaying error messages. Uses {@link ERXSimpleTemplateParser} for display.
 *                Method name and HTML-escaped message are provided by the "method" and "message" keys. (optional)
 * @binding proxyParent whether to proxy key path lookup to the parent (default is false)
 * @binding defaultToDynamicBindings whether to use dynamicBindings for unknown keys (default is true)
 * 
 * @author th
 */
public class ERXInlineTemplate extends ERXNonSynchronizingComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(ERXInlineTemplate.class);

	private static final String ERROR_TEMPLATE_DEFAULT = "<div class=\"ERXInlineTemplateError\" style=\"background-color: #faa; border: 2px dotted red;\">@@message@@</div>";

	private static final String ERROR_TEMPLATE_BINDING = "errorTemplate";

	private static final String CACHE_KEY_BINDING = "cacheKey";

	private static final String CACHE_VERSION_BINDING = "cacheVersion";

	private static final String TEMPLATE_HTML_BINDING = "html";

	private static final String TEMPLATE_WOD_BINDING = "wod";

	private static final String PROXY_PARENT_BINDING = "proxyParent";

	private static final String DEFAULT_TO_DYNAMIC_BINDINGS_BINDING = "defaultToDynamicBindings";

	private static NSMutableDictionary<String, CacheEntry> _cache = ERXMutableDictionary.synchronizedDictionary();

	protected Error _deferredError = null;

	public ERXInlineTemplate(WOContext context) {
		super(context);
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		if (_deferredError != null) {
			woresponse.appendContentString(_deferredError.formatWithTemplate(errorTemplate()));
		}
		else {
			try {
				super.appendToResponse(woresponse, wocontext);
			}
			catch (Throwable t) {
				woresponse.appendContentString(new Error("appendToResponse", t).formatWithTemplate(errorTemplate()));
			}
		}
		_deferredError = null;
	}

	public String errorTemplate() {
		return stringValueForBinding(ERROR_TEMPLATE_BINDING, ERROR_TEMPLATE_DEFAULT);
	}

	public boolean proxyParent() {
		return booleanValueForBinding(PROXY_PARENT_BINDING);
	}
	
	public boolean defaultToDynamicBindings() {
		return booleanValueForBinding(DEFAULT_TO_DYNAMIC_BINDINGS_BINDING, true);
	}
	
	@Override
	public void takeValueForKeyPath(Object value, String keyPath) {
		try {
			NSMutableArray<String> keyPathComponents = NSArray.componentsSeparatedByString(keyPath, ".").mutableClone();
			String firstKey = keyPathComponents.removeObjectAtIndex(0);
			if (bindingKeys().contains(firstKey)) {
				if (keyPathComponents.count() > 0) {
					Object o = valueForBinding(firstKey);
					String remainingKeyPath = keyPathComponents.componentsJoinedByString(".");
					if (log.isDebugEnabled()) {
						log.debug("set binding using keypath " + firstKey + " / " + remainingKeyPath);
					}
					NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(o, value, remainingKeyPath);
				}
				else {
					if (log.isDebugEnabled()) {
						log.debug("set binding value " + firstKey);
					}
					setValueForBinding(value, firstKey);
				}
			}
			else if (proxyParent()) {
				if (log.isDebugEnabled()) {
					log.debug("set parent binding " + keyPath);
				}
				parent().takeValueForKeyPath(value, keyPath);
			}
			else if (defaultToDynamicBindings()){
				if (log.isDebugEnabled()) {
					log.debug("set dynamic binding " + keyPath);
				}
				dynamicBindings().takeValueForKeyPath(value, keyPath);
			} else {
				log.warn("Unknown keyPath: "+keyPath);
			}
		}
		catch (Throwable t) {
			_deferredError = new Error("takeValueForKeyPath", t);
		}
	}

	@Override
	public Object valueForKeyPath(String keyPath) {
		try {
			NSMutableArray<String> keyPathComponents = NSArray.componentsSeparatedByString(keyPath, ".").mutableClone();
			String firstKey = keyPathComponents.removeObjectAtIndex(0);
			Object value = null;
			if (bindingKeys().contains(firstKey)) {
				Object o = valueForBinding(firstKey);
				if (keyPathComponents.count() > 0) {
					String remainingKeyPath = keyPathComponents.componentsJoinedByString(".");
					if (log.isDebugEnabled()) {
						log.debug("get binding using keypath " + firstKey + " / " + remainingKeyPath);
					}
					value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, remainingKeyPath);
				}
				else {
					if (log.isDebugEnabled()) {
						log.debug("get binding value " + firstKey);
					}
					value = o;
				}
			}
			else if (proxyParent()) {
				if (log.isDebugEnabled()) { 
					log.debug("get parent binding " + keyPath);
				}
				value = parent().valueForKeyPath(keyPath);
			}
			else if (defaultToDynamicBindings()) {
				if (log.isDebugEnabled()) {
					log.debug("get dynamic binding " + keyPath);
				}
				value = dynamicBindings().valueForKeyPath(keyPath);
			}
			else {
				log.warn("Unknown keyPath: " + keyPath);
			}

			return value;
		}
		catch (Throwable t) {
			// save throwable
			_deferredError = new Error("takeValueForKeyPath", t);
			return null;
		}
	}

	@Override
	public void takeValueForKey(Object obj, String s) {
		takeValueForKeyPath(obj, s);
	}

	@Override
	public Object valueForKey(String s) {
		return valueForKeyPath(s);
	}

	@Override
	public WOElement template() {
		try {
			WOElement element = null;
			String cacheKey = (String) valueForBinding(CACHE_KEY_BINDING);
			if (cacheKey != null) { // should cache
				CacheEntry cacheEntry = _cache.objectForKey(cacheKey);
				Object requestedVersion = valueForBinding(CACHE_VERSION_BINDING);
				if (cacheEntry != null && (requestedVersion == null || requestedVersion.equals(cacheEntry.version()))) {
					// requestedVersion matches or is null
					if (log.isDebugEnabled()) {
						log.debug("using cache: " + cacheKey + " / " + cacheEntry.version());
					}
					element = cacheEntry.element();
				}
				else { // no matching cache entry
					if (log.isDebugEnabled()) {
						log.debug("updating cache: " + cacheKey + " / " + (cacheEntry == null ? null : cacheEntry.version()) + " -> " + requestedVersion);
					}
					element = _template();
					cacheEntry = new CacheEntry(requestedVersion, element);
					_cache.takeValueForKey(cacheEntry, cacheKey);
				}
			}
			else { // no caching
				if (log.isDebugEnabled()) {
					log.debug("caching disabled");
				}
				element = _template();
			}

			return element;
		}
		catch (Throwable t) {
			String html = new Error("template", t).formatWithTemplate(errorTemplate());
			return WOComponent.templateWithHTMLString("", "", html, "", null, 
					WOApplication.application().associationFactoryRegistry(), WOApplication.application().namespaceProvider());
		}
	}

	private WOElement _template() {
		String html = stringValueForBinding(TEMPLATE_HTML_BINDING, "");
		String wod = stringValueForBinding(TEMPLATE_WOD_BINDING, "");
		WOElement element = WOComponent.templateWithHTMLString("", "", html, wod, null, 
				WOApplication.application().associationFactoryRegistry(), WOApplication.application().namespaceProvider());
		return element;
	}

	class CacheEntry {
		private WOElement _element;

		private Object _version;

		public CacheEntry(Object version, WOElement element) {
			_version = version;
			_element = element;
		}

		public WOElement element() {
			return _element;
		}

		public Object version() {
			return _version;
		}
	}

	public static class Error implements Serializable {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		private Throwable _t;

		private String _method;

		public Error(String method, Throwable t) {
			ERXInlineTemplate.log.error(method + ": " + t, t);
			_t = t;
			_method = method;
		}

		public String message() {
			String s = ERXExceptionUtilities.toParagraph(_t);
			if (s != null) {
				s = s.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br />");
			}
			return s;
		}

		public String method() {
			return _method;
		}

		public String formatWithTemplate(String template) {
			return ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(template, ERXSimpleTemplateParser.DEFAULT_DELIMITER, this);
		}
	}
}
