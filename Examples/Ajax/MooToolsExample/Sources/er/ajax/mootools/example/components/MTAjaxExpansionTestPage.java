package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.mootools.MTAjaxUtils;

public class MTAjaxExpansionTestPage extends Main {
	
	public boolean _optionsVisible;
	
    public MTAjaxExpansionTestPage(WOContext context) {
        super(context);
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	MTAjaxUtils.addStylesheetResourceInHead(context, response, "app", "css/ToggleDetails.css");
    }
    
    public WOActionResults toggleOptions() {
    	_optionsVisible = !_optionsVisible;
    	return null;
    }

}