package er.ajax;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public abstract class AjaxResponseAppender {
	public abstract void appendToResponse(WOResponse response, WOContext context);
}
