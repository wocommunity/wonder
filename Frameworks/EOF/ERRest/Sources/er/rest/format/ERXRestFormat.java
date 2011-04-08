package er.rest.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eocontrol.EOClassDescription;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;
import er.rest.gianduia.ERXGianduiaRestParser;
import er.rest.gianduia.ERXGianduiaRestWriter;

public class ERXRestFormat {
	public static final String HTML_KEY = "html";
	public static final String JSON_KEY = "json";
	public static final String JS_KEY = "js";
	public static final String RAILS_KEY = "rails";
	public static final String PLIST_KEY = "plist";
	public static final String SPROUTCORE_KEY = "sc";
	public static final String XML_KEY = "xml";

	private static Map<String, ERXRestFormat> _formats = new ConcurrentHashMap<String, ERXRestFormat>();
	
	static {
		// MS: The whole naming thing is stupid, I know ... we need to separate mime type from extensions from the name 
		ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), new ERXRestFormatDelegate(), ERXRestFormat.JSON_KEY, "application/json");
		ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), new ERXRestFormatDelegate(), ERXRestFormat.JS_KEY, "text/js");
		ERXRestFormat.registerFormatNamed(new ERXPListRestParser(), new ERXPListRestWriter(), new ERXRestFormatDelegate(), ERXRestFormat.PLIST_KEY, "text/plist");
		ERXRestFormat.registerFormatNamed(new ERXBinaryPListRestParser(), new ERXBinaryPListRestWriter(), new ERXRestFormatDelegate(), "bplist", "application/x-plist");
		ERXRestFormat.registerFormatNamed(new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("id", "type", "nil", true, true, true, true), ERXRestFormat.RAILS_KEY, "application/xml", "text/xml");
		ERXRestFormat.registerFormatNamed(new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate(), ERXRestFormat.XML_KEY, "application/xml", "text/xml");
		ERXRestFormat.registerFormatNamed(null, new ERXSimpleRestWriter(), new ERXRestFormatDelegate(), ERXRestFormat.HTML_KEY, "text/html");
		ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXGianduiaRestWriter(false), new ERXRestFormatDelegate(), "gndj", "application/gndj");
		ERXRestFormat.registerFormatNamed(new ERXGianduiaRestParser(), new ERXGianduiaRestWriter(true), new ERXRestFormatDelegate(), "gndp", "application/gndp");
		ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXSproutCoreRestWriter(), new ERXRestFormatDelegate("guid", "type", "nil", true, true, false, false), ERXRestFormat.SPROUTCORE_KEY, "application/sc");
	}

	private String _name;
	private IERXRestParser _parser;
	private IERXRestWriter _writer;
	private ERXRestFormat.Delegate _delegate;

	// These are going to be killed soon ...
	@Deprecated
	public static final ERXRestFormat JSON = json();
	@Deprecated
	public static final ERXRestFormat JS = ERXRestFormat.formatNamed(ERXRestFormat.JS_KEY);
	@Deprecated
	public static final ERXRestFormat PLIST = plist();
	@Deprecated
	public static final ERXRestFormat RAILS = ERXRestFormat.formatNamed(ERXRestFormat.RAILS_KEY);
	@Deprecated
	public static final ERXRestFormat XML = xml();
	@Deprecated
	public static final ERXRestFormat HTML = html();
	@Deprecated
	public static final ERXRestFormat GIANDUIA_JSON = ERXRestFormat.formatNamed("gndj");
	@Deprecated
	public static final ERXRestFormat GIANDUIA_PERSISTENT_STORE = ERXRestFormat.formatNamed("gndp");
	@Deprecated
	public static final ERXRestFormat SPROUTCORE = ERXRestFormat.formatNamed(ERXRestFormat.SPROUTCORE_KEY);
	
	/**
	 * Returns the registered html format.
	 * 
	 * @return the registered html format
	 */
	public static ERXRestFormat html() {
		return formatNamed(ERXRestFormat.HTML_KEY);
	}
	
	/**
	 * Returns the registered json format.
	 * 
	 * @return the registered json format
	 */
	public static ERXRestFormat json() {
		return formatNamed(ERXRestFormat.JSON_KEY);
	}
	
	/**
	 * Returns the registered plist format.
	 * 
	 * @return the registered plist format
	 */
	public static ERXRestFormat plist() {
		return formatNamed(ERXRestFormat.PLIST_KEY);
	}
	
	/**
	 * Returns the registered xml format.
	 * 
	 * @return the registered xml format
	 */
	public static ERXRestFormat xml() {
		return formatNamed(ERXRestFormat.XML_KEY);
	}
	
	/**
	 * Constructs a new ERXRestFormat.
	 * 
	 * @param name the name of the format
	 * @param parser the parser
	 * @param writer the writer
	 * @param delegate the delegate to use while parsing and writing
	 */
	public ERXRestFormat(String name, IERXRestParser parser, IERXRestWriter writer, ERXRestFormat.Delegate delegate) {
		_name = name;
		_parser = parser;
		_writer = writer;
		_delegate = delegate;
	}

	/**
	 * Returns the name of this format.
	 * 
	 * @return the name of this format
	 */
	public String name() {
		return _name;
	}

	public IERXRestParser parser() {
		return _parser;
	}

	public IERXRestWriter writer() {
		return _writer;
	}

	public ERXRestFormat.Delegate delegate() {
		return _delegate;
	}

	/**
	 * Returns a parsed ERXRestRequestNode using this format's parser.
	 * 
	 * @param str the string to parse
	 * @return the parsed request node
	 */
	public ERXRestRequestNode parse(String str) {
		return parser().parseRestRequest(new ERXStringRestRequest(str), _delegate);
	}
	
	/**
	 * Returns the formatted version of the given object using a recursive "All" filter and the default rest delegate.
	 * 
	 * @param obj the object to render
	 * @return obj rendered using this format
	 */
	public String toString(Object obj) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, ERXKeyFilter.filterWithAllRecursive(), IERXRestDelegate.Factory.delegateForEntityNamed(IERXRestDelegate.Factory.entityNameForObject(obj), ERXEC.newEditingContext())).toString(this);
	}

	/**
	 * Returns the formatted version of the given object using a recursive "All" filter.
	 * 
	 * @param obj the object to render
	 * @param delegate the rest delegate to use
	 * @return obj rendered using this format
	 */
	public String toString(Object obj, IERXRestDelegate delegate) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, ERXKeyFilter.filterWithAllRecursive(), delegate).toString(this);
	}

	/**
	 * Returns the formatted version of the given object.
	 * 
	 * @param obj the object to render
	 * @param filter the filter to apply to the object
	 * @param delegate the rest delegate to use
	 * @return obj rendered using this format
	 */
	public String toString(Object obj, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, delegate).toString(this);
	}

	/**
	 * Returns the formatted version of the given list.
	 * 
	 * @param classDescription the class description for the elements of the list
	 * @param list the list
	 * @param filter the filter
	 * @param delegate the rest delegate to use
	 * @return list rendered using this format
	 */
	public String toString(EOClassDescription classDescription, List<?> list, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(classDescription, list, filter, delegate).toString(this);
	}

	@Override
	public String toString() {
		return "[ERXRestFormat: " + _name + "]";
	}

	public static ERXRestFormat formatNamed(String name) {
		ERXRestFormat format = _formats.get(name.toLowerCase());
		if (format == null) {
			format = new ERXRestFormat(name, null, null, new ERXRestFormat.NoOpDelegate());
		}
		return format;
	}

	public static ERXRestFormat registerFormatNamed(IERXRestParser parser, IERXRestWriter writer, ERXRestFormat.Delegate delegate, String... names) {
		ERXRestFormat format = new ERXRestFormat(names[0], parser, writer, delegate);
		for (String name : names) {
			ERXRestFormat.registerFormatNamed(format, name);
		}
		return format;
	}

	public static ERXRestFormat registerFormatNamed(ERXRestFormat format, String name) {
		_formats.put(name.toLowerCase(), format);
		return format;
	}

	public static interface Delegate {
		public void nodeDidParse(ERXRestRequestNode node);

		public void nodeWillWrite(ERXRestRequestNode node);
	}

	public static class NoOpDelegate implements Delegate {
		public void nodeDidParse(ERXRestRequestNode node) {
			// DO NOTHING
		}

		public void nodeWillWrite(ERXRestRequestNode node) {
			// DO NOTHING
		}
	}
}
