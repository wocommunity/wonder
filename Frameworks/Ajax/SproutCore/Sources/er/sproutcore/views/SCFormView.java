package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCFormView extends SCView {
    public SCFormView(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("content");
    	addProperty("enabled", "isEnabled");
    	addProperty("valid", "isValid");
    	addProperty("commiting", "isCommitting");
    	addProperty("dirty", "isDirty");
    	addProperty("errors");
    	addProperty("error_count", "errorCount");
    	addProperty("autocommit");
    	addProperty("prompt", "commitPrompt");
    }

    @Override
    public String elementName(WOContext context) {
        return "form";
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
