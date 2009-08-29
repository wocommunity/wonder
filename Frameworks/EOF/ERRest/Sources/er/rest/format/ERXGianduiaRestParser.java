package er.rest.format;

import java.net.MalformedURLException;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import com.webobjects.appserver.WORequest;

import er.extensions.foundation.ERXMutableURL;
import er.rest.ERXRestRequestNode;

// ignore me .. i don't do anything real yet
public class ERXGianduiaRestParser implements IERXRestParser {
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

			ERXMutableURL url = new ERXMutableURL();
			try {
				url.setQueryParameters(contentStr);
			}
			catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			String requestJSON = url.queryParameter("requestJSON");
			
			JSON rootJSON = JSONSerializer.toJSON(requestJSON, ERXJSONRestWriter._config);
			System.out.println("ERXGianduiaRestParser.parseRestRequest: " + rootJSON);
			rootRequestNode = ERXJSONRestParser.createRequestNodeForJSON(null, rootJSON, true, delegate);
		}

		return rootRequestNode;
	}
}
