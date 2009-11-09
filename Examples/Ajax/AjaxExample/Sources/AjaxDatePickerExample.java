
import java.text.*;
import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

public class AjaxDatePickerExample extends WOComponent {
	
	public NSTimestamp someValue = new NSTimestamp();
	public Format formatter = new NSTimestampFormatter("%m/%d/%Y");

	public Format frenchFormatter = new NSTimestampFormatter("%e %b %Y", new DateFormatSymbols(Locale.FRENCH));	
	public NSArray<String> frenchMonths = new NSArray<String>(new String[]{
			"janvier", "f�vrier", "mars", "avril", "mai", "juin",
			"juillet", "ao�t", "septembre", "octobre", "novembre", "d�cembre"});

	public NSArray<String> frenchDays = new NSArray<String>(new String[]{
			"dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"});

	public String customFormat = "dd MMM yyyy";
	public NSTimestamp customValue = new NSTimestamp();
	
    public AjaxDatePickerExample(WOContext context) {
        super(context);
    }
}