package er.rest.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestRequestNode;
import er.rest.routes.model.IERXEntity;

public class ERXRestFormat {
	private static Map<String, ERXRestFormat> _formats = new ConcurrentHashMap<String, ERXRestFormat>();

	public static ERXRestFormat JSON = ERXRestFormat.registerFormatNamed(ERXJSONRestParser.class, ERXJSONRestWriter.class, "json");
	public static ERXRestFormat PLIST = ERXRestFormat.registerFormatNamed(ERXPListRestParser.class, ERXPListRestWriter.class, "plist");
	public static ERXRestFormat XML = ERXRestFormat.registerFormatNamed(ERXXmlRestParser.class, ERXXmlRestWriter.class, "xml");
	public static ERXRestFormat HTML = ERXRestFormat.registerFormatNamed(null, null, "html");

	private String _name;
	private Class<? extends IERXRestParser> _parserClass;
	private Class<? extends IERXRestWriter> _writerClass;

	public ERXRestFormat(String name, Class<? extends IERXRestParser> parserClass, Class<? extends IERXRestWriter> writerClass) {
		_name = name;
		_parserClass = parserClass;
		_writerClass = writerClass;
	}

	public String name() {
		return _name;
	}

	public IERXRestParser parser() {
		try {
			return _parserClass.newInstance();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to create parser '" + _parserClass + "'.");
		}
	}

	public IERXRestWriter writer() {
		try {
			return _writerClass.newInstance();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to create writer '" + _writerClass + "'.");
		}
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

	public static ERXRestFormat registerFormatNamed(Class<? extends IERXRestParser> parserClass, Class<? extends IERXRestWriter> writerClass, String name) {
		ERXRestFormat format = new ERXRestFormat(name, parserClass, writerClass);
		ERXRestFormat.registerFormatNamed(format, name);
		return format;
	}

	public static ERXRestFormat registerFormatNamed(ERXRestFormat format, String name) {
		_formats.put(name.toLowerCase(), format);
		return format;
	}
}
