package er.rest.format;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.webobjects.appserver.WOApplication;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

public class ERXGianduiaRestWriter implements IERXRestWriter {
	private String _persistentStoreName;
	private boolean _persistentStoreFormat;

	public ERXGianduiaRestWriter(boolean persistentStoreFormat) {
		this(persistentStoreFormat, ERXProperties.stringForKeyWithDefault("ERXRest.gianduia.persistentStoreName", null));
	}

	public ERXGianduiaRestWriter(boolean persistentStoreFormat, String persistentStoreName) {
		_persistentStoreFormat = persistentStoreFormat;
		if (persistentStoreName == null) {
			WOApplication application = WOApplication.application();
			if (application == null) {
				_persistentStoreName = "ERRest";
			}
			else {
				_persistentStoreName = application.name();
			}
		}
		else {
			_persistentStoreName = persistentStoreName;
		}
	}

	@SuppressWarnings("unchecked")
	protected void appendObjectToResponse(Object object, IERXRestResponse response) {
		if (object == null) {
			response.appendContentString("undefined");
		}
		else if (ERXRestUtils.isPrimitive(object)) {
			appendValueToResponse(object, response);
		}
		else if (object instanceof Map) {
			appendMapToResponse((Map<Object, Object>) object, response);
		}
		else if (object instanceof List) {
			appendListToResponse((List<Object>) object, response);
		}
		else {
			throw new IllegalArgumentException("Unknown Gianduia object: " + object);
		}
	}

	protected void appendValueToResponse(Object value, IERXRestResponse response) {
		if (value instanceof Number) {
			response.appendContentString(String.valueOf(value));
		}
		else if (value instanceof Date) {
			response.appendContentString("new Date(" + ((Date)value).getTime() + ")");
		}
		else if (value instanceof String) {
			response.appendContentString("\"" + ERXStringUtilities.escape(new char[] { '"' }, '\\', (String) value) + "\"");
		}
		else {
			response.appendContentString("\"" + ERXRestUtils.coerceValueToString(value) + "\"");
		}
	}

	protected void appendListToResponse(List<Object> list, IERXRestResponse response) {
		response.appendContentString("[");
		Iterator<Object> listIter = list.iterator();
		while (listIter.hasNext()) {
			Object obj = listIter.next();
			appendObjectToResponse(obj, response);
			if (listIter.hasNext()) {
				response.appendContentString(", ");
			}
		}
		response.appendContentString("]");
	}

	protected void appendMapToResponse(Map<Object, Object> map, IERXRestResponse response) {
		boolean persistentStoreFormat = false;
		if (_persistentStoreFormat) {
			String type = (String) map.remove(ERXRestFormatDelegate.TYPE_KEY);
			Object id = map.remove(ERXRestFormatDelegate.ID_KEY);
			persistentStoreFormat = (type != null && id != null);
			if (persistentStoreFormat) {
				response.appendContentString("this.objectWithURIRepresentation(\"x-coredata://" + _persistentStoreName + "/" + type + "/p" + id + "\",");
			}
		}
		response.appendContentString("{");
		Iterator<Map.Entry<Object, Object>> mapIter = map.entrySet().iterator();
		while (mapIter.hasNext()) {
			Map.Entry<Object, Object> entry = mapIter.next();
			response.appendContentString(String.valueOf(entry.getKey()));
			response.appendContentString(":");
			appendObjectToResponse(entry.getValue(), response);
			if (mapIter.hasNext()) {
				response.appendContentString(", ");
			}
		}
		response.appendContentString("}");
		if (persistentStoreFormat) {
			response.appendContentString(")");
		}
	}

	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		response.setHeader("application/json", "Content-Type");
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate) {
		appendHeadersToResponse(node, response);
		Object object = node.toJavaCollection(delegate);
		appendObjectToResponse(object, response);
	}
}
