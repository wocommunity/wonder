package er.sproutcore.views.button;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCDisclosureView extends SCButtonView {
	public SCDisclosureView(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    }

	@Override
	public String defaultTheme(WOContext context) {
		return "disclosure";
	}

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-disclosure-view");
		return cssNames;
	}

	@Override
	protected void doAppendToResponse(WOResponse response, WOContext context) {
		String url = blankUrl();
		response.appendContentString("<img class=\"button\" src=\"" + url + "\" />");
		super.doAppendToResponse(response, context);
	}
}
