package er.rest.format;

import java.io.ByteArrayOutputStream;

import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.foundation.ERXPropertyListSerialization;
import er.rest.ERXRestRequestNode;

public class ERXBinaryPListRestWriter implements IERXRestWriter {
	
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response) {
		
		response.setHeader("application/x-plist", "Content-Type");
		
	}

	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate) {
		appendHeadersToResponse(node, response);
		Object object = node.toNSCollection(delegate);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ERXPropertyListSerialization.writePropertyListToStream(object, out, ERXPropertyListSerialization.PListFormat.NSPropertyListBinaryFormat_v1_0, "UTF-8");
		response.appendContentData(new NSData(out.toByteArray()));
	}
}
