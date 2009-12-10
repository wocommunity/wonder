package er.ticktock.ui;

import java.text.FieldPosition;
import java.util.TimeZone;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

public class TimeDisplayNow extends WOComponent {

    public TimeDisplayNow(WOContext context) {
        super(context);
    }

    public String timeZoneID;

    public NSTimeZone systemTimeZone() { return NSTimeZone.systemTimeZone(); }

    public NSTimeZone desiredNSTimeZone() {
    	return NSTimeZone.timeZoneWithName(timeZoneID, false);
    }

    public TimeZone desiredTimeZone() { return TimeZone.getTimeZone(timeZoneID); }

    NSTimestamp _currentTimestamp;

    public NSTimestamp currentTimestamp() {
    	if (_currentTimestamp == null)
    		_currentTimestamp = new NSTimestamp();
    	return _currentTimestamp;
    }

    public NSTimestamp desiredTimestamp;

    public String desiredTimestampInDesiredTimeZone() {
    	NSTimestampFormatter formatter = new NSTimestampFormatter();
    	NSTimeZone tz = NSTimeZone.timeZoneWithName(timeZoneID, false);
    	formatter.setDefaultFormatTimeZone(tz);
    	StringBuffer buffer = formatter.format(_currentTimestamp, new StringBuffer(), new FieldPosition(0));
    	return buffer.toString();
    }
}