package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSTimestamp;

import er.ajax.mootools.MTAjaxUtils;

public class MTJSDatePickerTestPage extends Main {

	public NSTimestamp _today;

	public MTJSDatePickerTestPage(WOContext context) {
        super(context);
        _updateTime();
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		MTAjaxUtils.addStylesheetResourceInHead(context, response, "app", "datepicker_dashboard/datepicker_dashboard.css");
	}
	
	private void _updateTime() {
		_today = new NSTimestamp();
	}
	
	public WOActionResults updateTime() {
		return null;
	}


}