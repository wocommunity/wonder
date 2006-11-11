package er.ajax;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOComponentRequestHandler;

public class AjaxRequestHandler extends WOComponentRequestHandler {
	public static final String AjaxRequestHandlerKey = "ajax";
	private static boolean _useAjaxRequestHandler = false;
	
	public AjaxRequestHandler() {
		AjaxRequestHandler._useAjaxRequestHandler = true;
	}
	
	public WOResponse handleRequest(WORequest request) {
		AjaxUtils.updateMutableUserInfoWithAjaxInfo(request);
		WOResponse response = super.handleRequest(request);
		return response;
	}
	
	public static boolean useAjaxRequestHandler() {
		return _useAjaxRequestHandler;
	}
}
