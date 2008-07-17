package er.sproutcore.views;

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
}
