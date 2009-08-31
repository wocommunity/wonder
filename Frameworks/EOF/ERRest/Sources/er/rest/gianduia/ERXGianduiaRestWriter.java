package er.rest.gianduia;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.webobjects.appserver.WOApplication;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXRestFormatDelegate;
import er.rest.format.IERXRestResponse;
import er.rest.format.IERXRestWriter;

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
	protected void appendObjectToResponse(Object object, Map<Object, ERXRestRequestNode> conversionMap, IERXRestResponse response) {
		if (object == null) {
			response.appendContentString("undefined");
		}
		else if (ERXRestUtils.isPrimitive(object)) {
			appendValueToResponse(object, conversionMap, response);
		}
		else if (object instanceof Map) {
			appendMapToResponse((Map<Object, Object>) object, conversionMap, response);
		}
		else if (object instanceof List) {
			appendListToResponse((List<Object>) object, conversionMap, response);
		}
		else {
			throw new IllegalArgumentException("Unknown Gianduia object: " + object);
		}
	}

	protected void appendValueToResponse(Object value, Map<Object, ERXRestRequestNode> conversionMap, IERXRestResponse response) {
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

	protected void appendListToResponse(List<Object> list, Map<Object, ERXRestRequestNode> conversionMap, IERXRestResponse response) {
		boolean persistentStoreFormat = false;
		Object id = null;
		if (_persistentStoreFormat) {
			ERXRestRequestNode requestNode = conversionMap.get(list);
			id = requestNode.id();
			persistentStoreFormat = (id != null);
			//this.setResponseObjectsForRequestWithId
		}
		if (persistentStoreFormat) {
			response.appendContentString("this.setResponseObjectsForRequestWithId(");
		}
		response.appendContentString("[");
		Iterator<Object> listIter = list.iterator();
		while (listIter.hasNext()) {
			Object obj = listIter.next();
			appendObjectToResponse(obj, conversionMap, response);
			if (listIter.hasNext()) {
				response.appendContentString(", ");
			}
		}
		response.appendContentString("]");
		if (persistentStoreFormat) {
			response.appendContentString(", \"" + id + "\")");
		}
	}

	protected void appendMapToResponse(Map<Object, Object> map, Map<Object, ERXRestRequestNode> conversionMap, IERXRestResponse response) {
		Map<Object, Object> cleanedMap = map;
		boolean persistentStoreFormat = false;
		if (_persistentStoreFormat) {
			cleanedMap = new HashMap<Object, Object>(map);
			String type = (String) cleanedMap.remove(ERXRestFormatDelegate.TYPE_KEY);
			Object id = cleanedMap.remove(ERXRestFormatDelegate.ID_KEY);
			persistentStoreFormat = (type != null && id != null);
			if (persistentStoreFormat) {
				response.appendContentString("this.objectWithURIRepresentation(\"x-coredata://" + _persistentStoreName + "/" + type + "/p" + id + "\",");
			}
		}
		response.appendContentString("{");
		Iterator<Map.Entry<Object, Object>> mapIter = cleanedMap.entrySet().iterator();
		while (mapIter.hasNext()) {
			Map.Entry<Object, Object> entry = mapIter.next();
			response.appendContentString(String.valueOf(entry.getKey()));
			response.appendContentString(":");
			appendObjectToResponse(entry.getValue(), conversionMap, response);
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
		Map<Object, ERXRestRequestNode> conversionMap = new HashMap<Object, ERXRestRequestNode>();
		Object object = node.toJavaCollection(delegate, conversionMap);
		appendObjectToResponse(object, conversionMap, response);
	}
}
