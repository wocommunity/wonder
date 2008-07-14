package er.sproutcore.views.button;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.sproutcore.SCItem;
import er.sproutcore.SCUtilities;

public class SCRadioView extends SCButtonView {

    public SCRadioView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public String cssName(WOContext context) {
        return "sc-button-view ";
    }

    @Override
    public String buttonStyle(WOContext context) {
        return "button radio normal";
    }
    
    protected void pullBindings(WOContext context, SCItem item) {
        super.pullBindings(context, item);
        item.addProperty("theme", "radio");
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        String value = label(context);
        if (value != null) {
            String url = SCUtilities.staticUrl("blank.gif");
            response.appendContentString("<img src='" + url + "' class=\"button\"><span class=\"label\">" );
        }
        
        super.doAppendToResponse(response, context);
        
        if (value != null) {
            response.appendContentString("</span></span>");
        }
    }
}
