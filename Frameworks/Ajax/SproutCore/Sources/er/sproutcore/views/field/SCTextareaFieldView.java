package er.sproutcore.views.field;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCTextareaFieldView extends SCTextFieldView {

    public SCTextareaFieldView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("hint");
    	addProperty("field_value");
    	addProperty("value");
    }
    
    @Override
    public String elementName(WOContext context) {
        return "textarea";
    }
 
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
