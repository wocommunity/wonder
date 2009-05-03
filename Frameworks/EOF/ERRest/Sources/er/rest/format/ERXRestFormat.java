package er.rest.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eocontrol.EOClassDescription;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKeyFilter;
import er.rest.IERXRestDelegate;
import er.rest.ERXEORestDelegate;
import er.rest.ERXRestRequestNode;

public class ERXRestFormat {
	private static Map<String, ERXRestFormat> _formats = new ConcurrentHashMap<String, ERXRestFormat>();

	public static ERXRestFormat JSON = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), "json");
	public static ERXRestFormat JS = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), "js");
	public static ERXRestFormat PLIST = ERXRestFormat.registerFormatNamed(new ERXPListRestParser(), new ERXPListRestWriter(), "plist");
	public static ERXRestFormat XML = ERXRestFormat.registerFormatNamed(new ERXXmlRestParser(), new ERXXmlRestWriter(), "xml");
	public static ERXRestFormat HTML = ERXRestFormat.registerFormatNamed(null, null, "html");
	public static ERXRestFormat GIANDUIA = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXGianduiaRestWriter(), "gnd");

	private String _name;
	private IERXRestParser _parser;
	private IERXRestWriter _writer;

	public ERXRestFormat(String name, IERXRestParser parser, IERXRestWriter writer) {
		_name = name;
		_parser = parser;
		_writer = writer;
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

	public String toString(Object obj, ERXKeyFilter filter) {
		return toString(obj, filter, new ERXEORestDelegate(ERXEC.newEditingContext()));
	}

	public String toString(EOClassDescription classDescription, List<?> list, ERXKeyFilter filter) {
		return toString(classDescription, list, filter, new ERXEORestDelegate(ERXEC.newEditingContext()));
	}

	public String toString(Object obj, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, delegate).toString(writer());
	}

	public String toString(EOClassDescription classDescription, List<?> list, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(classDescription, list, filter, delegate).toString(writer());
	}

	@Override
	public String toString() {
		return "[ERXRestFormat: " + _name + "]";
	}

	public static ERXRestFormat formatNamed(String name) {
		ERXRestFormat format = _formats.get(name.toLowerCase());
		if (format == null) {
			format = new ERXRestFormat(name, null, null);
		}
		return format;
	}

	public static ERXRestFormat registerFormatNamed(IERXRestParser parser, IERXRestWriter writer, String name) {
		ERXRestFormat format = new ERXRestFormat(name, parser, writer);
		ERXRestFormat.registerFormatNamed(format, name);
		return format;
	}

	public static ERXRestFormat registerFormatNamed(ERXRestFormat format, String name) {
		_formats.put(name.toLowerCase(), format);
		return format;
	}
}
