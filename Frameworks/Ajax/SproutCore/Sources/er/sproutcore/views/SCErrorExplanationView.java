package er.sproutcore.views;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCErrorExplanationView extends SCView {

    public SCErrorExplanationView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public String elementName(WOContext context) {
        return "ul";
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("errors");
		return cssNames;
	}
}
