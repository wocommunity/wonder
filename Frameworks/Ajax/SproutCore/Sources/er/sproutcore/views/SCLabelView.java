package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCLabelView extends SCView {

    public SCLabelView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        String value = null;
        value = (String) valueForBinding("label", value, context.component());
        value = (String) valueForBinding("value", value, context.component());
        value = (String) valueForBinding("title", value, context.component());
        if (value != null) {
            response.appendContentString(value);
        } else {
            super.doAppendToResponse(response, context);
        }
    }
}
