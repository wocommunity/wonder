package er.sproutcore.views.button;

import java.lang.annotation.Documented;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.sproutcore.views.SCView;

public class SCButtonView extends SCView {

    public SCButtonView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }


    @Override
    public NSArray propertyKeys() {
        return super.propertyKeys().arrayByAddingObjectsFromArray(new NSArray(new Object[] { 
                "action", "target", "isDefault", "isCancel", "value", "theme", "size", "size", 
                "buttonBehaviour", "toggleOnValue", "toggleOffValue", "image",
                "isSelected",
                "label", "isEditable", "escapeHTML" }));
    }

    protected Object defaultElementName() {
        return "a";
    }
    
    @Override
    public String css(WOContext context) {
    	return super.css(context) + "button regular normal";
    }

    protected String label(WOContext context) {
        String value = null;
        value = (String) valueForBinding("label", value, context.component());
        value = (String) valueForBinding("value", value, context.component());
        value = (String) valueForBinding("title", value, context.component());
        return value;
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        String value = label(context);
        if (value != null) {
            response.appendContentString("<span class=\"button-inner\"><span class=\"label\">" + value);
        }
        
        super.doAppendToResponse(response, context);
        
        if (value != null) {
            response.appendContentString("</span></span>");
        }
    }
}
