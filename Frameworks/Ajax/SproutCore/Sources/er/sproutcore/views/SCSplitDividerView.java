package er.sproutcore.views;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCSplitDividerView extends SCView {
	public SCSplitDividerView(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	@Override
	protected void doAppendToResponse(WOResponse response, WOContext context) {
		super.doAppendToResponse(response, context);
	}

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-split-divider-view");
		return cssNames;
	}
}
