import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxPushRequestHandler;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXResponse;

public class PushExample extends WOComponent {

	public PushExample(WOContext context) {
		super(context);
		if (!_pushThread.isAlive()) {
			_pushThread.start();
		}
	}

	public String url() {
		return context().urlWithRequestHandlerKey(AjaxPushRequestHandler.AjaxCometRequestHandlerKey, "test",
				WOApplication.application().sessionIdKey() + "=" + session().sessionID());
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");
	}

	private static Set<String> _pushSessionIDs = Collections.synchronizedSet(new HashSet<String>());

	private static Thread _pushThread = new Thread(new Runnable() {
		public void run() {
			boolean running = true;
			while (running) {
				Set<String> pushSessionIDs = new HashSet<String>(_pushSessionIDs);
				for (String pushSessionID : pushSessionIDs) {
					if (AjaxPushRequestHandler.isResponseOpen(pushSessionID, "test")) {
						String str = "<br>push to '" + pushSessionID+ "': " + System.currentTimeMillis();
						System.out.println("ERXKeepAliveResponse.push: sending '" + str + "'");
						AjaxPushRequestHandler.push(pushSessionID, "test", str);
						try {
							Thread.sleep(2000);
						}
						catch (InterruptedException e) {
							running = false;
						}
					}
					else {
						System.out.println("PushExample._pushThread.new Runnable() {...}.run: removing " + pushSessionID);
						_pushSessionIDs.remove(pushSessionID);
					}
				}
			}
		}
	});

	public WOActionResults push() {
		if (_pushSessionIDs.contains(session().sessionID())) {
			System.out.println("PushExample.push:  stopping " + session().sessionID());
			_pushSessionIDs.remove(session().sessionID());
		}
		else {
			System.out.println("PushExample.push: starting " + session().sessionID());
			_pushSessionIDs.add(session().sessionID());
		}
		return new ERXResponse("Sent some data");
	}
}