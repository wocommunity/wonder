package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXMutableDictionary.ThreadSafeDictionary;

/**
 * ERXInlineTemplate allows to specify a component's template dynamically. <br />
 * The content which would usually go into the ".html" file within a WOComponent's bundle, is specified using the "html"
 * binding, the ".wod" part is pecified by the "wod" binding. <br />
 * <br />
 * When using {@link WOOgnl} with "ognl.helperFunctions = true" and "ognl.inlineBindings = true", you can leave out the
 * WOD part. <br />
 * <br />
 * When keys are accessed, the component first determines the first element of the path (e.g. key "foo" for path
 * "foo.bar") and looks, if there is a binding with that key. <br />
 * If there is such a binding, the value is retrieved and the rest of the keyPath applied to it
 * (valueForBinding("foo").valueForKeyPath("bar")). <br />
 * If no such binding is available, the value is stored in a NSMutableDictionary. The remaining keyPath is applied as
 * above. <br />
 * <br />
 * When an error occurs, an error message is displayed. The message can be altered using the "errorTemplate" binding.
 * <br />
 * <br />
 * Optionally, a "cacheKey" (String) can be specified, under which the parsed WOElement will be cached. To allow
 * updating, a "cacheVersion" (Object) is available. When the version changes, the value is recalculated.
 * 
 * @binding HTML HTML-part of the component (required)
 * @binding WOD WOD-part of the component (optional)
 * @binding cacheKey Key under which to cache the WOElement (optional)
 * @binding cacheVersion Hint to determine if the cached object is up-to-date (optional)
 * @binding errorTemplate Template to use for displaying error messages. Uses {@link ERXSimpleTemplateParser} for display.
 *                Method name and HTML-escaped message are provided by the "method" and "message" keys. (optional)
 * 
 * @author th
 * 
 */
public class ERXInlineTemplate extends ERXNonSynchronizingComponent {
	public static Logger log = Logger.getLogger(ERXInlineTemplate.class);

	private static final String ERROR_TEMPLATE_DEFAULT = "<div style=\"background-color: #faa; border: 2px dotted red;\"><b>ERROR in ERXInlineTemplate.@@method@@</b><br />@@message@@</div>";

	private static final String ERROR_TEMPLATE_BINDING = "errorTemplate";

	private static final String CACHE_KEY_BINDING = "cacheKey";

	private static final String CACHE_VERSION_BINDING = "cacheVersion";

	private static final String TEMPLATE_HTML_BINDING = "html";

	private static final String TEMPLATE_WOD_BINDING = "wod";

	private static ThreadSafeDictionary _cache = (ThreadSafeDictionary) ERXMutableDictionary.synchronizedDictionary();

	private Error _deferredError = null;

	public ERXInlineTemplate(WOContext context) {
		super(context);
	}

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
		String template = (String) valueForBinding(ERROR_TEMPLATE_BINDING);
		if (template == null) {
			template = ERROR_TEMPLATE_DEFAULT;
		}
		return template;
	}

	public boolean proxyParent() {
		return ERXComponentUtilities.booleanValueForBinding(this, "proxyParent");
	}
	
	public void takeValueForKeyPath(Object value, String keyPath) {
		try {
			NSMutableArray keyPathComponents = NSArray.componentsSeparatedByString(keyPath, ".").mutableClone();
			String firstKey = (String) keyPathComponents.removeObjectAtIndex(0);
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
			else {
				if (log.isDebugEnabled()) {
					log.debug("set dynamic binding " + keyPath);
				}
				dynamicBindings().takeValueForKeyPath(value, keyPath);
			}
		}
		catch (Throwable t) {
			_deferredError = new Error("takeValueForKeyPath", t);
		}
	}

	public Object valueForKeyPath(String keyPath) {
		try {
			NSMutableArray keyPathComponents = NSArray.componentsSeparatedByString(keyPath, ".").mutableClone();
			String firstKey = (String) keyPathComponents.removeObjectAtIndex(0);
			Object value;
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
			else {
				if (log.isDebugEnabled()) { 
					log.debug("get dynamic binding " + keyPath);
				}
				value = dynamicBindings().valueForKeyPath(keyPath);
			}
			return value;
		}
		catch (Throwable t) {
			// save throwable
			_deferredError = new Error("takeValueForKeyPath", t);
			return null;
		}
	}

	public void takeValueForKey(Object obj, String s) {
		takeValueForKeyPath(obj, s);
	}

	public Object valueForKey(String s) {
		return valueForKeyPath(s);
	}

	public WOElement template() {
		try {
			WOElement element = null;
			String cacheKey = (String) valueForBinding(CACHE_KEY_BINDING);
			if (cacheKey != null) { // should cache
				CacheEntry cacheEntry = (CacheEntry) _cache.valueForKey(cacheKey);
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
			return WOComponent.templateWithHTMLString(new Error("template", t).formatWithTemplate(errorTemplate()), "", null);
		}
	}

	private WOElement _template() {
		String templateHtmlString = (String) valueForBinding(TEMPLATE_HTML_BINDING);
		String templateWodString = (String) valueForBinding(TEMPLATE_WOD_BINDING);
		WOElement element = WOComponent.templateWithHTMLString((templateHtmlString == null) ? "" : templateHtmlString, (templateWodString == null ? "" : templateWodString), null);
		return element;
	}

	class CacheEntry {
		private WOElement _element;

		private Object _version;

		public CacheEntry(Object version, WOElement element) {
			this._version = version;
			this._element = element;
		}

		public WOElement element() {
			return _element;
		}

		public Object version() {
			return _version;
		}
	}

	public static class Error {
		private Throwable _t;

		private String _method;

		public Error(String method, Throwable t) {
			ERXInlineTemplate.log.error(t);
			_t = t;
			_method = method;
		}

		public String message() {
			if (_t.getMessage() == null && _t instanceof NSForwardException) {
				NSForwardException e = (NSForwardException) _t;
				_t = e.originalException();
			}
			String s = _t.getMessage();
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