package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

public interface IAjaxElement {
	public WOActionResults handleRequest(WORequest request, WOContext context);
}
