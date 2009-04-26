package er.rest.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestRequestNode;
import er.rest.routes.model.IERXEntity;

public class ERXRestFormat {
	private static Map<String, ERXRestFormat> _formats = new ConcurrentHashMap<String, ERXRestFormat>();

	public static ERXRestFormat JSON = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), "json");
	public static ERXRestFormat JS = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), "js");
	public static ERXRestFormat PLIST = ERXRestFormat.registerFormatNamed(new ERXPListRestParser(), new ERXPListRestWriter(), "plist");
	public static ERXRestFormat XML = ERXRestFormat.registerFormatNamed(new ERXXmlRestParser(), new ERXXmlRestWriter(), "xml");
	public static ERXRestFormat HTML = ERXRestFormat.registerFormatNamed(null, null, "html");

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

	public String toString(Object obj) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, ERXKeyFilter.filterWithAttributes()).toString(writer());
	}

	public String toString(IERXEntity entity, List<?> list) {
		return ERXRestRequestNode.requestNodeWithObjectAndFilter(entity, list, ERXKeyFilter.filterWithAttributes()).toString(writer());
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
