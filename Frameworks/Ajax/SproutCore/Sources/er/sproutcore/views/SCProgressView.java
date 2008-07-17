package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCProgressView extends SCView {

    public SCProgressView(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("enabled", "isEnabled");
    	addProperty("indeterminate", "isIndeterminate");
    	addProperty("value");
    	addProperty("maximum");
    	addProperty("minimum");
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
       String html = "<div class=\"outer-head\"></div><div class=\"inner\"><div class=\"inner-head\"></div><div class=\"inner-tail\"></div></div><div class=\"outer-tail\"></div>";
       response.appendContentString(html);
    }
}
