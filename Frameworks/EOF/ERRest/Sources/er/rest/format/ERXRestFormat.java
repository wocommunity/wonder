package er.rest.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eocontrol.EOClassDescription;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;
import er.rest.gianduia.ERXGianduiaRestParser;
import er.rest.gianduia.ERXGianduiaRestWriter;

public class ERXRestFormat {
	private static Map<String, ERXRestFormat> _formats = new ConcurrentHashMap<String, ERXRestFormat>();

	public static ERXRestFormat JSON = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), new ERXRestFormatDelegate(), "json", "application/json");
	public static ERXRestFormat JS = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), new ERXRestFormatDelegate(), "js", "text/js");
	public static ERXRestFormat PLIST = ERXRestFormat.registerFormatNamed(new ERXPListRestParser(), new ERXPListRestWriter(), new ERXRestFormatDelegate(), "plist", "text/plist");
	public static ERXRestFormat XML = ERXRestFormat.registerFormatNamed(new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate(), "xml", "application/xml", "text/xml");
	public static ERXRestFormat HTML = ERXRestFormat.registerFormatNamed(null, null, new ERXRestFormatDelegate(), "html", "text/html");
	public static ERXRestFormat GIANDUIA_JSON = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXGianduiaRestWriter(false), new ERXRestFormatDelegate(), "gndj", "application/gndj");
	public static ERXRestFormat GIANDUIA_PERSISTENT_STORE = ERXRestFormat.registerFormatNamed(new ERXGianduiaRestParser(), new ERXGianduiaRestWriter(true), new ERXRestFormatDelegate(), "gndp", "application/gndp");
	public static ERXRestFormat SPROUTCORE = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXSproutCoreRestWriter(), new ERXRestFormatDelegate("guid", "type", "nil", true, true), "sc", "application/sc");

	private String _name;
	private IERXRestParser _parser;
	private IERXRestWriter _writer;
	private ERXRestFormat.Delegate _delegate;

	public ERXRestFormat(String name, IERXRestParser parser, IERXRestWriter writer, ERXRestFormat.Delegate delegate) {
		_name = name;
		_parser = parser;
		_writer = writer;
		_delegate = delegate;
	}

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

	public String toString(Object obj, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, delegate).toString(this);
	}

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
