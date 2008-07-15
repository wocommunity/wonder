package er.sproutcore.views.button;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCDisclosureView extends SCButtonView {

	public SCDisclosureView(String arg0, NSDictionary arg1, WOElement arg2) {
		super(arg0, arg1, arg2);
	}

	@Override
	public String defaultTheme(WOContext context) {
		return "disclosure";
	}

	@Override
	public String cssName(WOContext context) {
		return "sc-disclosure-view";
	}

	@Override
	protected void doAppendToResponse(WOResponse response, WOContext context) {
		String url = blankUrl();
		response.appendContentString("<img class=\"button\" src=\"" + url + "\" />");
		super.doAppendToResponse(response, context);
	}
}
