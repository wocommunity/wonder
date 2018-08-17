package er.rest.format;

import java.util.List;
import java.util.Map;

import com.webobjects.foundation.NSPropertyListSerialization;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

/**
 * ERXPListRestRequestParser is an implementation of the IERXRestRequestParser interface that supports plist document
 * requests.
 * 
 * @author mschrag
 */
public class ERXPListRestParser implements IERXRestParser {
	protected ERXRestRequestNode createRequestNodeForObject(String name, Object object, boolean rootNode, ERXRestFormat.Delegate delegate) {
		ERXRestRequestNode requestNode = new ERXRestRequestNode(name, rootNode);

		if (object == null) {
			// just leave the value null
		}
		else if (object instanceof List) {
			requestNode.setArray(true);
			List<?> list = (List<?>) object;
			for (Object obj : list) {
				if (ERXRestUtils.isPrimitive(obj)) {
					ERXRestRequestNode primitiveChild = new ERXRestRequestNode(null, object, false);
					requestNode.addChild(primitiveChild);
					if (delegate != null) {
						delegate.nodeDidParse(primitiveChild);
					}
				}
				else {
					requestNode.addChild(createRequestNodeForObject(null, obj, true, delegate));
				}
			}
		}
		else if (object instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) object;
			for (Object key : map.keySet()) {
				String strKey = (String) key;
				Object value = map.get(key);
				if (ERXRestUtils.isPrimitive(value)) {
					ERXRestRequestNode primitiveChild = new ERXRestRequestNode(strKey, value, false);
					requestNode.addChild(primitiveChild);
					if (delegate != null) {
						delegate.nodeDidParse(primitiveChild);
					}
				}
				else {
					requestNode.addChild(createRequestNodeForObject(strKey, value, false, delegate));
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unknown PLIST value '" + object + "'.");
		}

		if (delegate != null) {
			delegate.nodeDidParse(requestNode);
		}

		return requestNode;
	}

	@Override
	public ERXRestRequestNode parseRestRequest(IERXRestRequest request, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		ERXRestRequestNode rootRequestNode = null;
		String contentString = request.stringContent();
		if (contentString != null && contentString.length() > 0) {
			// MS: Support direct updating of primitive type keys -- so if you don't want to
			// wrap your request in XML, this will allow it
			// if (!contentStr.trim().startsWith("<")) {
			// contentStr = "<FakeWrapper>" + contentStr.trim() + "</FakeWrapper>";
			// }

			Object rootObj = NSPropertyListSerialization.propertyListFromString(contentString);
			rootRequestNode = createRequestNodeForObject(null, rootObj, true, delegate);
		}
		else {
			rootRequestNode = new ERXRestRequestNode(null, true);
			rootRequestNode.setNull(true);
		}

		return rootRequestNode;
	}
}
