package er.sproutcore.views.button;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxValue;
import er.sproutcore.SCItem;
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
    protected Object evaluateValueForBinding(WOContext context, String name, Object value) {
    	Object evaluatedValue = value;
    	if ("isSelected".equals(name)) {
    		if ("mixed".equals(value)) {
    			evaluatedValue = new AjaxValue(AjaxOption.SCRIPT, "SC.MIXED_STATE");
    		}
    	}
    	return evaluatedValue;
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
    
    @Override
    protected void pullBindings(WOContext context, SCItem item) {
    	super.pullBindings(context, item);
    	String theme = defaultTheme(context);
    	if (!"button".equals(theme)) {
    		item.addProperty("theme", theme);
    	}
    }
    
    public String theme(WOContext context) {
    	return (String)valueForBinding("theme", defaultTheme(context), context.component());
    }
    
    public String size(WOContext context) {
    	return (String)valueForBinding("size", defaultSize(context), context.component());
    }

    public String defaultTheme(WOContext context) {
    	return "regular";
    }
    
    public String defaultSize(WOContext context) {
    	return "normal";
    }
    
    public String buttonStyle(WOContext context) {
    	StringBuffer css = new StringBuffer();
    	css.append("button");
    	css.append(" ");
    	css.append(theme(context));
    	css.append(" ");
    	css.append(size(context));
    	return css.toString();
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
