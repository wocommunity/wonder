package er.r2d2w.components.calendar;

import java.text.SimpleDateFormat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.localization.ERXLocalizer;
import er.r2d2w.foundation.R2DDateRangeGrouper;

public class R2D2WSimpleCalendarCell extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final String format = "d";
	private SimpleDateFormat dateFormat;

	public R2D2WSimpleCalendarCell(WOContext context) {
        super(context);
    }
	
	public void reset() {
		dateFormat = null;
		super.reset();
	}
    
    public R2DDateRangeGrouper rangeGrouper() {
    	return (R2DDateRangeGrouper)valueForBinding("rangeGrouper");
    }
    
    public WOActionResults selectDateAction() {
        WOActionResults nextPage = context().page();
        R2DDateRangeGrouper grouper = rangeGrouper();
        grouper.setSelectedDate(grouper.currentDate());
        return nextPage;
    }

	/**
	 * @return the dateFormat
	 */
	public SimpleDateFormat dateFormat() {
		if(dateFormat == null) {
			dateFormat = new SimpleDateFormat(format, ERXLocalizer.currentLocalizer().locale());
		}
		return dateFormat;
	}


}