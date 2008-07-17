package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCSplitView extends SCView {
    public SCSplitView(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("direction", "layoutDirection");
    	addProperty("can_collapse_views");
    }
    
    @Override
    public String css(WOContext context) {
        return super.css(context) + " " +  valueForBinding("direction", "horizontal", context.component());
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
