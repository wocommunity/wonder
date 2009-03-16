package er.sproutcore.views.field;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCSelectFieldView extends SCFieldView {

    public SCSelectFieldView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("name_key");
    	addProperty("sort_key");
    	addProperty("value_key");
    	addProperty("empty", "emptyName");
    	addProperty("enabled", "isEnabled");
    }

    @Override
    public String elementName(WOContext context) {
        return "select";
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
