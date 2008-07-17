package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.sproutcore.SCUtilities;

public class SCSliderView extends SCView {

    public SCSliderView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }
    
	@Override
	protected void addProperties() {
		super.addProperties();

		addProperty("minimum");
		addProperty("maximum");
		addProperty("step");
	}

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        Object width = valueForBinding("width", context.component());
        String style = (width == null ? "" : "style=\"width: " + width + "px\" ");
        String html = "<span class=\"outer\"><span " + style + "class=\"inner\"></span><img src=\"" + SCUtilities.staticUrl("blank.gif") + "\" class=\"sc-handle\" /></span>";
        response.appendContentString(html);
    }
}
