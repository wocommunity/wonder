package er.sproutcore.views.button;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCRadioView extends SCButtonView {

    public SCRadioView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public String cssName(WOContext context) {
        return "sc-button-view ";
    }

    @Override
	public String defaultTheme(WOContext context) {
    	return "radio";
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        String url = blankUrl();
        response.appendContentString("<img class=\"button\" src=\"" + url + "\">" );
    	
        String label = label(context);
        if (label != null) {
            response.appendContentString("<span class=\"label\">" );
        }
        
        super.doAppendToResponse(response, context);
        
        if (label != null) {
            response.appendContentString("</span>");
        }
    }
}
