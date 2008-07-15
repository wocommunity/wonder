package er.sproutcore.views.field;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.sproutcore.views.SCView;

public class SCFieldView extends SCView {

    public SCFieldView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public String elementName(WOContext context) {
        return "input";
    }

    public String type() {
        return null;
    }

    protected String label(WOContext context) {
        String value = null;
        value = (String) valueForBinding("label", value, context.component());
        //value = (String) valueForBinding("value", value, context.component());
        value = (String) valueForBinding("title", value, context.component());
        return value;
    }

    @Override
	protected void prependToResponse(WOResponse response, WOContext context) {
        String label = label(context);
        if (label != null) {
        	response.appendContentString("<label>");
        }
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
        
        String label = label(context);
        if (label != null) {
            response.appendContentString("<span class=\"label\">" );
            response.appendContentString(label);
            response.appendContentString("</span>");
        	response.appendContentString("</label>");
        }
    }

    @Override
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
        if(type() != null) {
            response._appendTagAttributeAndValue("type", type(), false);
        }
    }
}
