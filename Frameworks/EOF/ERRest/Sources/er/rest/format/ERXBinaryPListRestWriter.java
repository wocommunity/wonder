package er.rest.format;

import java.io.ByteArrayOutputStream;

import org.apache.commons.lang3.CharEncoding;

import com.webobjects.foundation.NSData;

import er.extensions.foundation.ERXPropertyListSerialization;
import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

public class ERXBinaryPListRestWriter implements IERXRestWriter {
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context) {
		response.setHeader("application/x-plist", "Content-Type");
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		if (node != null) {
			node._removeRedundantTypes();
		}
		appendHeadersToResponse(node, response, context);
		Object object = node.toNSCollection(delegate);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ERXPropertyListSerialization.writePropertyListToStream(object, out, ERXPropertyListSerialization.PListFormat.NSPropertyListBinaryFormat_v1_0, CharEncoding.UTF_8);
		response.appendContentData(new NSData(out.toByteArray()));
	}
}
