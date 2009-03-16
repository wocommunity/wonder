package er.uber.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;

public class REST extends UberComponent {
    public REST(WOContext context) {
        super(context);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
    	super.appendToResponse(response, context);
    }
}