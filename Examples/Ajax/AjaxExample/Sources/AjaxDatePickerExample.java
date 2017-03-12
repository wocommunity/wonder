
import java.text.DateFormatSymbols;
import java.text.Format;
import java.util.Locale;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

public class AjaxDatePickerExample extends WOComponent {
	
	public NSTimestamp someValue = new NSTimestamp();
	public Format formatter = new NSTimestampFormatter("%m/%d/%Y");

	public Format frenchFormatter = new NSTimestampFormatter("%e %b %Y", new DateFormatSymbols(Locale.FRENCH));	
	public NSArray<String> frenchMonths = new NSArray<>(new String[]{
			"janvier", "février", "mars", "avril", "mai", "juin",
			"juillet", "août", "septembre", "octobre", "novembre", "décembre"});

	public NSArray<String> frenchDays = new NSArray<>(new String[]{
			"dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"});

	public String customFormat = "dd MMM yyyy";
	public NSTimestamp customValue = new NSTimestamp();
	
    public AjaxDatePickerExample(WOContext context) {
        super(context);
    }
}