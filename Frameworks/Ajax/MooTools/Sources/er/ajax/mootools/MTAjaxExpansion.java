package er.ajax.mootools;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxExpansion;

public class MTAjaxExpansion extends AjaxExpansion {
    
	public MTAjaxExpansion(WOContext context) {
        super(context);
    }

	@Override
	protected void addRequiredWebResources(WOResponse response) {
		MTAjaxUtils.addScriptResourceInHead(context(), response, "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), response, "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
	}
	
}