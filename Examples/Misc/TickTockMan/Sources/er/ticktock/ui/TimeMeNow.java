package er.ticktock.ui;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

public class TimeMeNow extends WOComponent {

	public TimeMeNow(WOContext context) {
		super(context);
	}

	public NSTimestamp startTime = new NSTimestamp();
	long startMillis = System.currentTimeMillis();

	public NSTimestamp endTime = null;
	long endMillis = 0L;

	public WOComponent clickNow() {
		endTime = new NSTimestamp();
		endMillis = System.currentTimeMillis();
		return null;
	}

	public int difference() {
		if (endMillis == 0L)
			return 0;
		else
			return (Long.valueOf((endMillis - startMillis) / 1000)).intValue() + 1;
	}
}
