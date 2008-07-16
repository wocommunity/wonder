package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class SCTabView extends SCComponent {
	
	public SCTabView(WOContext context) {
		super(context);
		moveProperty("enabled", "isEnabled");
		removeProperty("class");
		removeProperty("id");
		removeProperty("segments");
		removeProperty("item");
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