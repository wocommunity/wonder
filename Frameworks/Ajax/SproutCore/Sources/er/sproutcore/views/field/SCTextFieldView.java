package er.sproutcore.views.field;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxValue;
import er.sproutcore.views.SCProperty;

public class SCTextFieldView extends SCFieldView {

    public SCTextFieldView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("field_value");
    	addProperty("value", "field_value");
    	addProperty("hint", AjaxOption.STRING);
    	addProperty("validate", "validator");
    	addProperty("label", "fieldlabel");
    }

    @Override
    public String type() {
        return "text";
    }
 
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
    	super.doAppendToResponse(response, context);
    }
    
}
