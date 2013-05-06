package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxComponent;

public class MTAccordionComponent extends AjaxComponent {

	private static final long serialVersionUID = 1L;

	public MTAccordionComponent(WOContext context) {
        super(context);
    }

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

	public String label() {
		return (String) valueForBinding("label");
	}

	@Override
	protected void addRequiredWebResources(WOResponse res) {
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
	}
}
