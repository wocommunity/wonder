package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxOption;

public class SCLabelView extends SCView {
    public SCLabelView(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("formatter", AjaxOption.SCRIPT);
    	addPropertyWithDefault("localize", false);
    	addProperty("editable", "isEditable");
    	addPropertyWithDefault("escape_html", "escapeHTML", true);
    	addProperty("value");
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        String value = null;
        value = (String) valueForBinding("label", value, context.component());
        value = (String) valueForBinding("value", value, context.component());
        value = (String) valueForBinding("title", value, context.component());
        if (value != null) {
            response.appendContentString(value);
        } else {
            super.doAppendToResponse(response, context);
        }
    }
}
