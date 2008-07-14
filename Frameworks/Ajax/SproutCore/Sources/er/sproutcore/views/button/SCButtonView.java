package er.sproutcore.views.button;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.sproutcore.views.SCView;

public class SCButtonView extends SCView {

    public SCButtonView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
        moveProperty("enabled", "isEnabled");
        moveProperty("selected", "isSelected");
        moveProperty("default", "isDefault");
        moveProperty("cancel", "isCancel");
        removeProperty("width");
        removeProperty("label");
    }
    
    @Override
    protected boolean skipPropertyIfNull(String propertyName) {
      return "action".equals(propertyName);
    }

    @Override
    protected Object defaultElementName() {
        return "a";
    }
    
    @Override
    public String css(WOContext context) {
    	String css = super.css(context);
    	css += " " + buttonStyle(context);
    	css += (booleanValueForBinding("enabled", true, context.component()) ? "" : " disabled");
        Object selected = valueForBinding("selected", context.component());
        css += (selected instanceof String ? " " + selected : "");
        css += (selected instanceof Boolean && ((Boolean)selected) ? " selected" : "");
    	return css;
    }

    public String buttonStyle(WOContext context) {
    	String style = "button normal " + valueForBinding("theme", "regular", context.component());
    	return style;
    }

    protected String label(WOContext context) {
        String value = null;
        value = (String) valueForBinding("label", value, context.component());
        //value = (String) valueForBinding("value", value, context.component());
        value = (String) valueForBinding("title", value, context.component());
        return value;
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        String width = (String) valueForBinding("width", context.component());
        String style = (width == null ? "" : "style=\"width: " + width +"px\" ");
        String value = label(context);
        if(value == null) {
            value = "";
        }
        response.appendContentString("<span class=\"button-inner\">");
        if (value != null) {
            response.appendContentString("<span " + style +"class=\"label\">" + value);
        }
        
        super.doAppendToResponse(response, context);
        
        if (value != null) {
            response.appendContentString("</span>");
        }
        response.appendContentString("</span>");
    }
}
