
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxPushRequestHandler;

public class PushExample extends WOComponent {
    public PushExample(WOContext context) {
        super(context);
    }

    public String url() {
    	return context().urlWithRequestHandlerKey(AjaxPushRequestHandler.AjaxCometRequestHandlerKey, "test", "wosid=" +session().sessionID());
    }
    
	public WOActionResults push() {
		AjaxPushRequestHandler.push(session().sessionID(), "test");
		return new WOResponse();
	}
}