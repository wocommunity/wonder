package er.sproutcore.views;

import java.util.Set;

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
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-split-view");
		cssNames.add((String) valueForBinding("direction", "horizontal", context.component()));
		cssNames.add((String) valueForBinding("splitter", "default", context.component()));
		return cssNames;
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
