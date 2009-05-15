package er.rest.format;

import java.util.Set;

import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonValueProcessorMatcher;

import com.webobjects.foundation.NSTimestamp;

import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

public class ERXJSONRestWriter implements IERXRestWriter {
	public static JsonConfig _config;

	static {
		ERXJSONRestWriter._config = new JsonConfig();
		_config.registerJsonValueProcessor(NSTimestamp.class, new NSTimestampProcessor());
		_config.setJsonValueProcessorMatcher(new ERXRestValueProcessorMatcher());
	}

	public static final class ERXRestValueProcessorMatcher extends JsonValueProcessorMatcher {
		@Override
		public Object getMatch(Class target, Set set) {
			if (target != null && set != null && set.contains(target)) {
				return target;
			}
			else {
				return null;
			}
		}
	}

	public static class NSTimestampProcessor implements JsonValueProcessor {
		public Object processArrayValue(Object obj, JsonConfig jsonconfig) {
			return ERXRestUtils.coerceValueToString(obj);
		}

		public Object processObjectValue(String s, Object obj, JsonConfig jsonconfig) {
			return ERXRestUtils.coerceValueToString(obj);
		}
	}

	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		response.setHeader("application/json", "Content-Type");
	}
	
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate) {
		appendHeadersToResponse(node, response);
		Object object = node.toJavaCollection(delegate);
		if (object == null) {
			response.appendContentString("undefined");
		}
		else if (ERXRestUtils.isPrimitive(object)) {
			response.appendContentString(String.valueOf(object));
		}
		else {
			response.appendContentString(JSONSerializer.toJSON(object, ERXJSONRestWriter._config).toString());
		}
	}
}
