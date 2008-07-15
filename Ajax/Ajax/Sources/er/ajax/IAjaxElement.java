package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

public interface IAjaxElement {
	public Object valueForBinding(String name, WOComponent component);

	public Object valueForBinding(String name, Object defaultValue, WOComponent component);

	public WOActionResults handleRequest(WORequest request, WOContext context);
}
