package er.rest.format;

import java.io.ByteArrayOutputStream;

import org.apache.commons.lang3.CharEncoding;

import com.webobjects.foundation.NSData;

import er.extensions.foundation.ERXPropertyListSerialization;
import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

public class ERXBinaryPListRestWriter extends ERXRestWriter {
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		if (node != null) {
			node._removeRedundantTypes();
		}
		appendHeadersToResponse(node, response, context);
		response.setContentEncoding(contentEncoding());
		Object object = node.toNSCollection(delegate);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ERXPropertyListSerialization.writePropertyListToStream(object, out, ERXPropertyListSerialization.PListFormat.NSPropertyListBinaryFormat_v1_0, CharEncoding.UTF_8);
		response.appendContentData(new NSData(out.toByteArray()));
	}

	@Override
	public String contentType() {
		return "application/x-plist";
	}

	@Override
	protected String contentTypeHeaderValue() {
		return contentType();
	}
}
