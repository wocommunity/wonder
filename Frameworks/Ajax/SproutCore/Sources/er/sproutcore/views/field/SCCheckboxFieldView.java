package er.sproutcore.views.field;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCCheckboxFieldView extends SCFieldView {

	public SCCheckboxFieldView(String arg0, NSDictionary arg1, WOElement arg2) {
		super(arg0, arg1, arg2);
	}
	
	@Override
	public String cssName(WOContext context) {
		return null;
	}

	@Override
	public String css(WOContext context) {
		String css = super.css(context);
		css += (booleanValueForBinding("enabled", true, context.component()) ? "" : " disabled");
		return css;
	}

	@Override
	public String type() {
		return "checkbox";
	}

	@Override
	protected void doAppendToResponse(WOResponse response, WOContext context) {
		super.doAppendToResponse(response, context);
	}
}
