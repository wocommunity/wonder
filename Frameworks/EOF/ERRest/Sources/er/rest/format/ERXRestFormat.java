package er.rest.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eocontrol.EOClassDescription;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXEORestDelegate;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;

public class ERXRestFormat {
	private static Map<String, ERXRestFormat> _formats = new ConcurrentHashMap<String, ERXRestFormat>();

	public static ERXRestFormat JSON = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), new ERXRestFormat.DefaultDelegate(), "json");
	public static ERXRestFormat JS = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), new ERXRestFormat.DefaultDelegate(), "js");
	public static ERXRestFormat PLIST = ERXRestFormat.registerFormatNamed(new ERXPListRestParser(), new ERXPListRestWriter(), new ERXRestFormat.DefaultDelegate(), "plist");
	public static ERXRestFormat XML = ERXRestFormat.registerFormatNamed(new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormat.DefaultDelegate(), "xml");
	public static ERXRestFormat HTML = ERXRestFormat.registerFormatNamed(null, null, new ERXRestFormat.DefaultDelegate(), "html");
	public static ERXRestFormat GIANDUIA = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXGianduiaRestWriter(), new ERXRestFormat.DefaultDelegate(), "gnd");
	public static ERXRestFormat SPROUTCORE = ERXRestFormat.registerFormatNamed(new ERXJSONRestParser(), new ERXJSONRestWriter(), new ERXRestFormat.DefaultDelegate("guid", "type", "nil", true), "sc");

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

	public String toString(Object obj, ERXKeyFilter filter) {
		return toString(obj, filter, new ERXEORestDelegate(ERXEC.newEditingContext()));
	}

	public String toString(EOClassDescription classDescription, List<?> list, ERXKeyFilter filter) {
		return toString(classDescription, list, filter, new ERXEORestDelegate(ERXEC.newEditingContext()));
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

	public static ERXRestFormat registerFormatNamed(IERXRestParser parser, IERXRestWriter writer, ERXRestFormat.Delegate delegate, String name) {
		ERXRestFormat format = new ERXRestFormat(name, parser, writer, delegate);
		ERXRestFormat.registerFormatNamed(format, name);
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

	public static class DefaultDelegate implements Delegate {
		public static final String ID_KEY = "id";
		public static final String TYPE_KEY = "type";
		public static final String NIL_KEY = "nil";

		private String _idKey;
		private String _typeKey;
		private String _nilKey;
		private boolean _writeNilKey;

		public DefaultDelegate() {
			this(DefaultDelegate.ID_KEY, DefaultDelegate.TYPE_KEY, DefaultDelegate.NIL_KEY, true);
		}

		public DefaultDelegate(String idKey, String typeKey, String nilKey, boolean writeNilKey) {
			_idKey = idKey;
			_typeKey = typeKey;
			_nilKey = nilKey;
			_writeNilKey = writeNilKey;
		}

		public void nodeDidParse(ERXRestRequestNode node) {
			Object id = node.removeAttributeOrChildNodeNamed(_idKey);
			node.setID(id);

			String type = (String) node.removeAttributeOrChildNodeNamed(_typeKey);
			node.setType(type);

			Object nil = node.removeAttributeOrChildNodeNamed(_nilKey);
			if (nil != null) {
				node.setNull("true".equals(nil) || Boolean.TRUE.equals(nil));
			}
		}

		public void nodeWillWrite(ERXRestRequestNode node) {
			Object id = node.id();
			if (id != null) {
				node.setAttributeForKey(String.valueOf(id), _idKey);
			}

			String type = node.type();
			if (type != null) {
				node.setAttributeForKey(type, _typeKey);
			}

			if (node.isNull() && _writeNilKey) {
				node.setAttributeForKey("true", _nilKey);
			}
		}
	}
}
