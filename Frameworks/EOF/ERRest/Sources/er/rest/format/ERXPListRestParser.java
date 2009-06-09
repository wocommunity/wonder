package er.rest.format;

import java.util.List;
import java.util.Map;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSPropertyListSerialization;

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
			List list = (List) object;
			for (Object obj : list) {
				if (ERXRestUtils.isPrimitive(obj)) {
					requestNode.addChild(new ERXRestRequestNode(null, obj, false));
				}
				else {
					requestNode.addChild(createRequestNodeForObject(null, obj, true, delegate));
				}
			}
		}
		else if (object instanceof Map) {
			Map map = (Map) object;
			for (Object key : map.keySet()) {
				String strKey = (String) key;
				Object value = map.get(key);
				if (ERXRestUtils.isPrimitive(value)) {
					requestNode.addChild(new ERXRestRequestNode(strKey, value, false));
				}
				else {
					requestNode.addChild(createRequestNodeForObject(strKey, value, false, delegate));
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unknown JSON value '" + object + "'.");
		}

		delegate.nodeDidParse(requestNode);

		return requestNode;
	}

	public ERXRestRequestNode parseRestRequest(WORequest request, ERXRestFormat.Delegate delegate) {
		return parseRestRequest(request.contentString(), delegate);
	}

	public ERXRestRequestNode parseRestRequest(String contentStr, ERXRestFormat.Delegate delegate) {
		ERXRestRequestNode rootRequestNode = null;

		if (contentStr != null && contentStr.length() > 0) {
			// MS: Support direct updating of primitive type keys -- so if you don't want to
			// wrap your request in XML, this will allow it
			// if (!contentStr.trim().startsWith("<")) {
			// contentStr = "<FakeWrapper>" + contentStr.trim() + "</FakeWrapper>";
			// }

			Object rootObj = NSPropertyListSerialization.propertyListFromString(contentStr);
			rootRequestNode = createRequestNodeForObject(null, rootObj, true, delegate);
		}

		return rootRequestNode;
	}
}
