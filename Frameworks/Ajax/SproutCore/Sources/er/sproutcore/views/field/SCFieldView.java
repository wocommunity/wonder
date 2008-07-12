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
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }

    public void appendAttributesToResponse(WOResponse response, WOContext context) {
        if(type() != null) {
            response._appendTagAttributeAndValue("type", type(), false);
        }
    }
}
