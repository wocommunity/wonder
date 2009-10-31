
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxPushRequestHandler;
import er.ajax.AjaxUtils;

public class PushExample extends WOComponent {
	
    public PushExample(WOContext context) {
        super(context);
        if(!thread.isAlive()) {
            thread.start();
        }
    }

    public String url() {
    	return context().urlWithRequestHandlerKey(AjaxPushRequestHandler.AjaxCometRequestHandlerKey, "test", "wosid=" +session().sessionID());
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
    	AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");
    }
    
    private static String pushingID = null;
    
    static Thread thread = new Thread(new Runnable() {

		public void run() {
			boolean running = true;
			while(running) {
				String id = pushingID;
				if(id != null) {
					AjaxPushRequestHandler.push(id, "pushed: " + System.currentTimeMillis());
				}
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					running = false;
				}
			}
		}});
    
    
	public WOActionResults push() {
		if(pushingID == null) {
			pushingID = session().sessionID();
		} else {
			pushingID = null;
		}
		WOResponse response = new WOResponse();
		response.setContent("Sent some data");
		return response;
	}
}