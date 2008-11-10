package er.sproutcore.views;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCRadioGroupView extends SCView {

    public SCRadioGroupView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("radio");
		cssNames.add("sc-radio-group-view");
        String layout = layout(context);
        if (layout != null) {
        	cssNames.add(layout);
        }
		return cssNames;
	}

    public String layout(WOContext context) {
        return (String) valueForBinding("layout", "vertical", context.component());
    }
}
