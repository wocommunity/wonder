package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

public class MTAUCPeriodicalTestPage extends Main {
    
	public NSTimestamp _now;
	
	public MTAUCPeriodicalTestPage(WOContext context) {
        super(context);
        _updateTime();
	}
	
	private void _updateTime() {
		_now = new NSTimestamp();
	}
	
	public WOActionResults updateTime() {
        _updateTime();
		return null;
	}

	public String pageTitle() {
		return "This demonstrates applying the frequency binding (4s) to the MTAjaxUpdateContainer";
	}
	
}