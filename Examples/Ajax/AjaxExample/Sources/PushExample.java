
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxPushRequestHandler;
import er.ajax.AjaxUtils;

public class PushExample extends WOComponent {
	
    public PushExample(WOContext context) {
        super(context);
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
    
	public WOActionResults push() {
		AjaxPushRequestHandler.push(session().sessionID(), "test: " + System.currentTimeMillis());
		WOResponse response = new WOResponse();
		response.setContent("Sent some data");
		return response;
	}
}