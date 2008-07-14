package er.sproutcore.views.button;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.sproutcore.SCUtilities;

public class SCDisclosureView extends SCButtonView {

    public SCDisclosureView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }
  
    @Override
    public String buttonStyle(WOContext context) {
        String style = "button normal " + valueForBinding("theme", "disclosure", context.component());
        return style;
    }
    
    @Override
    public String cssName(WOContext context) {
        return super.cssName(context) + " sc-button-view";
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        response.appendContentString("<img class=\"button\" src=\"" + SCUtilities.staticUrl("blank.gif") + "\" />");
        super.doAppendToResponse(response, context);
    }
}
