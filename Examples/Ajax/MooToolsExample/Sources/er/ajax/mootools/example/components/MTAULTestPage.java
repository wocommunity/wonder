package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

public class MTAULTestPage extends Main {

	public NSTimestamp _now;
	
	public MTAULTestPage(WOContext context) {
        super(context);
		_updateTime();
    }
	
	public void _updateTime() {
		_now = new NSTimestamp();
	}
	
	public WOActionResults updateTime() {
		_updateTime();
		return null;
	}

	public WOActionResults updateTimeSlow() {
		setTask(new Task());
		task().start();
		do {
			System.out.println(task().getStatus());
		} while(! task().getStatus().equals("Finished"));
		return updateTime();
	}
	
}
