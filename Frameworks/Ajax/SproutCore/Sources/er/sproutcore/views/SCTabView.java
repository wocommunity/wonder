package er.sproutcore.views;

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
}