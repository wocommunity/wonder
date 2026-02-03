package er.r2d2w.delegates;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXExtensions;
import er.extensions.batching.ERXBatchNavigationBar;
import er.extensions.components.ERXSortOrder;
import er.extensions.eof.ERXConstant;
import er.r2d2w.components.calendar.R2D2WCalendarNavigationBar;

public class PreferenceHandlerDelegate {
	/**
	 * Initializes the preference handler instance
	 */
	public static final PreferenceHandler INSTANCE = PreferenceHandler.INSTANCE;
	
	public enum PreferenceHandler {
		INSTANCE;
		
		static {
			for(PreferenceHandler handler : values()) {
		        NSNotificationCenter.defaultCenter().addObserver(handler,
		                new NSSelector<Void>("handleBatchSizeChange", ERXConstant.NotificationClassArray),
		                ERXBatchNavigationBar.BatchSizeChanged,
		                null);
		        NSNotificationCenter.defaultCenter().addObserver(handler,
		                new NSSelector<Void>("handleCalendarViewChange", ERXConstant.NotificationClassArray),
		                R2D2WCalendarNavigationBar.CALENDAR_VIEW_CHANGED,
		                null);
		        NSNotificationCenter.defaultCenter().addObserver(handler,
		                new NSSelector<Void>("handleSortOrderingChange", ERXConstant.NotificationClassArray),
		                ERXSortOrder.SortOrderingChanged,
		                null);
			}
		}
	
	    public void handleBatchSizeChange(NSNotification n) { handleChange("batchSize", n); }
	    public void handleCalendarViewChange(NSNotification n) { handleChange("calendarView", n); }
	    public void handleSortOrderingChange(NSNotification n) { handleChange("sortOrdering", n); }
	
	    public void handleChange(String prefName, NSNotification n) {
	    	NSKeyValueCoding context=(NSKeyValueCoding)n.userInfo().objectForKey("d2wContext");
	        if (context!=null) {
	        	String prefKey = ERXExtensions.userPreferencesKeyFromContext(prefName, context);
	        	NSKeyValueCoding userPreferences = (NSKeyValueCoding)context.valueForKey("userPreferences");
	            if(userPreferences != null) { userPreferences.takeValueForKey(n.object(), prefKey); }
	        }
	    }
	}

}
