package er.sproutcore.views;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class SCTabView extends SCComponent {

	public SCTabView(WOContext context) {
		super(context);
	}

	@Override
	protected void addProperties() {
		super.addProperties();
		addProperty("now_showing");
		addProperty("lazy_tabs");
	}

	@Override
	public String containerClass() {
		return "tab segmented " + containerID();
	}

	@Override
	protected void doAppendToResponse(WOResponse response, WOContext context) {
		super.doAppendToResponse(response, context);
	}

	// TODO: Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = null; // super.cssNames();
		//cssNames.add("tab");
		//if segments, add 'segment' too. see core_views.rb
		return cssNames;
	}
}
