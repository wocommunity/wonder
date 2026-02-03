package er.r2d2w.components.calendar;

import java.text.SimpleDateFormat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSTimestamp;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;
import er.r2d2w.foundation.R2DDateRangeGrouper;

public class R2D2WCalendarNavigationBar extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


	public static final String CALENDAR_VIEW_CHANGED = "CalendarViewChanged";
	
	private static final String defaultFormat = "MM/dd/yyyy";
	
	private NSArray<String> calendarViews;
	private R2DDateRangeGrouper.CalendarView currentView;
	private SimpleDateFormat formatter;
	private String textFieldID;
	private String viewItem;

	public void reset() {
		calendarViews = null;
		currentView = null;
		formatter = null;
		textFieldID = null;
		viewItem = null;
		super.reset();
	}
	
    public R2D2WCalendarNavigationBar(WOContext context) {
        super(context);
    }
    
    public R2DDateRangeGrouper rangeGrouper() {
    	return (R2DDateRangeGrouper)valueForBinding("rangeGrouper");
    }
        
    public NSArray<String> availableCalendarViews() {
    	if(calendarViews == null) {
    		calendarViews = ERXValueUtilities.arrayValueWithDefault(d2wContext().valueForKey("availableCalendarViews"), NSArray.EmptyArray);
    	}
    	return calendarViews;
    }

	public Boolean hasMultipleViews() {
		return availableCalendarViews().count() > 1;
	}

	public WOActionResults nextCalendar() {
		rangeGrouper().calendarView().nextCalendar(rangeGrouper());
		return context().page();
	}

	public WOActionResults previousCalendar() {
		rangeGrouper().calendarView().previousCalendar(rangeGrouper());
		return context().page();
	}

	public WOActionResults todayAction() {
		rangeGrouper().setSelectedDate(new NSTimestamp());
		return context().page();
	}

	public Boolean isNotToday() {
		return !rangeGrouper().isToday();
	}
	
	/**
	 * @return the viewItem
	 */
	public String viewItem() {
		return viewItem;
	}

	/**
	 * @param viewItem the viewItem to set
	 */
	public void setViewItem(String viewItem) {
		if(!ERXValueUtilities.isNull(viewItem)) {
			currentView = Enum.valueOf(R2DDateRangeGrouper.CalendarView.class, viewItem);
		}
		this.viewItem = viewItem;
	}

	public Boolean isCurrentView() {
		return currentView().equals(rangeGrouper().calendarView());
	}
	
	public R2DDateRangeGrouper.CalendarView currentView() {
		return currentView;
	}

	public String viewString() {
		return (String)d2wContext().valueForKey(currentView().localizationKey());
	}

	public WOActionResults viewLink() {
		NSNotificationCenter.defaultCenter().postNotification(
				CALENDAR_VIEW_CHANGED,
                currentView().toString(),
                new NSDictionary<String,Object>(d2wContext(), "d2wContext")
                );
    	rangeGrouper().setCalendarView(currentView());
		return null;
	}
	
	/**
	 * @return the textFieldID
	 */
	public String textFieldID() {
		if(textFieldID == null) {
			textFieldID = "id" + context().elementID();
		}
		return textFieldID;
	}

	/**
	 * Since the R2DDateRangeGrouper already adjusts for time zone, an unadjusted formatter is
	 * required to avoid errors in the text field.
	 * @return a new localized SimpleDateFormat with a default time zone
	 */
	public SimpleDateFormat dateFormat() {
		if(formatter == null) {
			ERXLocalizer loc = ERXLocalizer.currentLocalizer();
			String format = loc.localizedStringForKey("R2D2WCalendarNavigationBar.format");
			format = ERXStringUtilities.stringIsNullOrEmpty(format)?defaultFormat:format;
			formatter = new SimpleDateFormat(format, loc.locale());
		}
		return formatter;
	}

}