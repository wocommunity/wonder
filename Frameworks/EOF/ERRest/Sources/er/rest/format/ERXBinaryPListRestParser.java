package er.rest.format;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.CharEncoding;

import er.extensions.foundation.ERXPropertyListSerialization;
import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

/**
 * ERXPListRestRequestParser is an implementation of the IERXRestRequestParser interface that supports plist document
 * requests.
 * 
 * @author mschrag
 */
public class ERXBinaryPListRestParser implements IERXRestParser {
	
	
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
					ERXRestRequestNode primitiveChild = new ERXRestRequestNode(null, object, false);
					requestNode.addChild(primitiveChild);
					delegate.nodeDidParse(primitiveChild);
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
					ERXRestRequestNode primitiveChild = new ERXRestRequestNode(strKey, value, false);
					requestNode.addChild(primitiveChild);
					delegate.nodeDidParse(primitiveChild);
				}
				else {
					requestNode.addChild(createRequestNodeForObject(strKey, value, false, delegate));
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unknown Binary PLIST value '" + object + "'.");
		}

		delegate.nodeDidParse(requestNode);

		return requestNode;
	}

	public ERXRestRequestNode parseRestRequest(IERXRestRequest request, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		ERXRestRequestNode rootRequestNode = null;

		if (request != null) {
			InputStream in = request.streamContent();
			Object rootObj = ERXPropertyListSerialization.propertyListWithStream(in, ERXPropertyListSerialization.PListFormat.NSPropertyListBinaryFormat_v1_0, CharEncoding.UTF_8);
			rootRequestNode = createRequestNodeForObject(null, rootObj, true, delegate);
		}
		else {
			rootRequestNode = new ERXRestRequestNode(null, true);
			rootRequestNode.setNull(true);
		}

		return rootRequestNode;
	}
}
