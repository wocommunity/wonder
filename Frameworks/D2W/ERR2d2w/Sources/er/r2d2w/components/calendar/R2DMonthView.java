package er.r2d2w.components.calendar;

import java.text.SimpleDateFormat;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.r2d2w.foundation.R2DDateRangeGrouper;

public class R2DMonthView extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_CALENDAR_CAPTION_FORMAT = "MMMMM yyyy";
	public static final String DEFAULT_CALENDAR_HEADER_FORMAT = "EEEE";
	
	private SimpleDateFormat calendarCaptionFormat;
	private SimpleDateFormat calendarHeaderFormat;
	
    public R2DMonthView(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	calendarCaptionFormat = null;
    	calendarHeaderFormat = null;
    	super.reset();
    }
    
    public R2DDateRangeGrouper rangeGrouper() {
    	return (R2DDateRangeGrouper)valueForBinding("rangeGrouper");
    }
    
	public SimpleDateFormat calendarHeaderFormat() {
		if(calendarHeaderFormat == null) {
			String format = (String)d2wContext().valueForKey("calendarHeaderFormat");
			format = ERXStringUtilities.stringIsNullOrEmpty(format)?DEFAULT_CALENDAR_HEADER_FORMAT:format;
			calendarHeaderFormat = new SimpleDateFormat(format, ERXLocalizer.currentLocalizer().locale());
		}
		return calendarHeaderFormat;
	}

	public String styleClasses() {
		String result = null;
		R2DDateRangeGrouper grouper = rangeGrouper();
		if(grouper.isToday()) { result = ERXStringUtilities.stringByAppendingCSSClass(result, "today"); }
		if(grouper.isSelectedDate()) { result = ERXStringUtilities.stringByAppendingCSSClass(result, "selected"); }
		if(!grouper.isInMonth()) { result = ERXStringUtilities.stringByAppendingCSSClass(result, "out"); }
		return result;
	}

	public SimpleDateFormat calendarCaptionFormat() {
		if(calendarCaptionFormat == null) {
			String format = (String)d2wContext().valueForKey("calendarCaptionFormat");
			format = ERXStringUtilities.stringIsNullOrEmpty(format)?DEFAULT_CALENDAR_CAPTION_FORMAT:format;
			calendarCaptionFormat = new SimpleDateFormat(format, ERXLocalizer.currentLocalizer().locale());
		}
		return calendarCaptionFormat;
	}
	
}