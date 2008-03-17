package er.ajax.json;

import java.util.NoSuchElementException;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCResult;
import org.jabsorb.serializer.Serializer;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;

import er.extensions.ERXMutableURL;

public class JSONRequestHandler extends WORequestHandler {
	public static final String RequestHandlerKey = "json";

	private JSONRPCBridge _jsonBridge;

	public static JSONRequestHandler register() {
		JSONRequestHandler requestHandler = new JSONRequestHandler();
		WOApplication.application().registerRequestHandler(requestHandler, JSONRequestHandler.RequestHandlerKey);
		return requestHandler;
	}

	public static void _initializeBridge() {
		try {
			JSONRPCBridge.registerLocalArgResolver(WOResponse.class, WOResponse.class, new WOResponseArgResolver());
			JSONRPCBridge.registerLocalArgResolver(WORequest.class, WORequest.class, new WORequestArgResolver());
			JSONRPCBridge.registerLocalArgResolver(WOContext.class, WOContext.class, new WOContextArgResolver());
			JSONRPCBridge.registerLocalArgResolver(WOSession.class, WOContext.class, new WOSessionArgResolver());
			JSONRPCBridge.registerLocalArgResolver(JSONRPCBridge.class, WOContext.class, new WOSessionArgResolver());
			JSONRPCBridge.getSerializer().registerSerializer(new EOEnterpriseObjectSerializer());
			JSONRPCBridge.getSerializer().registerSerializer(new NSArraySerializer());
			JSONRPCBridge.getSerializer().registerSerializer(new NSDictionarySerializer());
			JSONRPCBridge.getSerializer().registerSerializer(new NSTimestampSerializer());
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize JSON.");
		}
	}

	public JSONRequestHandler() {
		_jsonBridge = new JSONRPCBridge();
		JSONRequestHandler._initializeBridge();
	}

	public void registerSerializer(Serializer serializer) throws Exception {
		JSONRPCBridge.getSerializer().registerSerializer(serializer);
	}

	public static void registerClass(Class clazz) throws Exception {
		JSONRequestHandler.registerClass(clazz.getSimpleName(), clazz);
	}

	public static void registerClass(String name, Class clazz) throws Exception {
		JSONRPCBridge.getGlobalBridge().registerClass(name, clazz);
	}

	public void registerService(String name, Object serviceObject) {
		_jsonBridge.registerObject(name, serviceObject);
	}

	@Override
	public WOResponse handleRequest(WORequest request) {
		WOApplication application = WOApplication.application();
		WOContext context = application.createContextForRequest(request);
		WOResponse response = application.createResponseInContext(context);

		Object output;
		try {
			String inputString = request.contentString();
			JSONObject input = new JSONObject(inputString);
			String wosid = request.cookieValueForKey("wosid");
			if (wosid == null) {
				ERXMutableURL url = new ERXMutableURL();
				url.setQueryParameters(request.queryString());
				wosid = url.queryParameter("wosid");
				if (wosid == null && input.has("wosid")) {
					wosid = input.getString("wosid");
				}
			}
			context._setRequestSessionID(wosid);
			WOSession session = null;
			if (context._requestSessionID() != null) {
				session = WOApplication.application().restoreSessionWithID(wosid, context);
			}
			try {
				output = _jsonBridge.call(new Object[] { request, response, context }, input);
			}
			finally {
				if (context._requestSessionID() != null) {
					WOApplication.application().saveSessionForContext(context);
				}
			}
		}
		catch (NoSuchElementException e) {
			e.printStackTrace();
			output = JSONRPCResult.MSG_ERR_NOMETHOD;
		}
		catch (JSONException e) {
			e.printStackTrace();
			output = JSONRPCResult.MSG_ERR_PARSE;
		}
		catch (Throwable t) {
			t.printStackTrace();
			output = JSONRPCResult.MSG_ERR_PARSE;
		}

		//System.out.println("JSONRequestHandler.handleRequest: output = " + output);
		if (output != null) {
			response.appendContentString(output.toString());
		}
		return response;
	}
}
