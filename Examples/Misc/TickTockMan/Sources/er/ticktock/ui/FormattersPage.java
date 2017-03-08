package er.ticktock.ui;

import java.text.SimpleDateFormat;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

public class FormattersPage extends WOComponent {

	public FormattersPage(WOContext context) {
		super(context);
	}

	static NSArray<String> _units;

	static NSArray nstExplains = new NSArray(new Object[] {
			"abbreviated weekday name",
			"full weekday name",
			"abbreviated month name",
			"full month name",
			"shorthand for \"%X %x\", the locale format for date, time",
			"day of the month as a decimal number (01-31)",
			"same as %d but does not print the leading 0 for days 1 - 9",
			"milliseconds as a decimal number (000-999)",
			"hour based on a 24-hour clock as a decimal number (00-23)",
			"hour based on a 12-hour clock as a decimal number (01-12)",
			"day of the year as a decimal number (001-366)",
			"month as a decimal number (01-12)",
			"minute as a decimal number (00-59)",
			"AM/PM designation for the locale",
			"second as a decimal number (00-59)",
			"weekday as a decimal number (0-6), where Sunday is 0.",
			"date using the date representation for the locale",
			"time using the time representation for the locale",
			"year without century (00-99)",
			"year with century (such as 1990)",
			"time zone name (such as \"Europe/Paris\" or \"PST\")",
			"time zone offset from GMT (such as \"+0200\" or \"-1200\")" } );

	static NSArray sdfExplains = new NSArray(new Object[] {
			"Era designator",
			"Year",
			"Month in year",
			"Week in year",
			"Week in month",
			"Day in year",
			"Day in month",
			"Day of week in month",
			"Day in week",
			"Am/pm marker",
			"Hour in day (0-23)",
			"Hour in day (1-24)",
			"Hour in am/pm (0-11)",
			"Hour in am/pm (1-12)",
			"Minute in hour",
			"Second in minute",
			"Millisecond",
			"Time zone",
			"Time zone" } );

	public NSArray<String> units() {
		if (_units == null)
			_units = new NSArray<>(new String[] { "year", "month", "day", "hour", "minute", "seconds" });
		return _units;
	}

	NSTimestamp _currentTS;

	public NSTimestamp currentTS() {
		if (_currentTS == null)
			_currentTS = new NSTimestamp();
		return _currentTS;
	}

	public void setCurrentTS(Object value) {
		_currentTS = (NSTimestamp)value;
	}

	public NSArray nstKeys = new NSArray(new Object[] { "%a", "%A", "%b", "%B", "%c", "%d", "%e", "%F", "%H", "%I", "%j", "%m", "%M", "%p", "%S", "%w", "%x", "%X", "%y", "%Y", "%Z", "%z" } );


	NSDictionary nstFormats = new NSDictionary(nstExplains, nstKeys);

	public String currentNSTFormat;

	public String currentNSTExplain() {
		return (String)nstFormats.valueForKey(currentNSTFormat);
	}

	public String currentFormattedNST() {
		NSTimestampFormatter formatter = new NSTimestampFormatter(currentNSTFormat);
		return formatter.format(currentTS());
	}

	public NSArray sdfKeys = new NSArray(new Object[] { "G", "y", "M", "w", "W", "D", "d", "F", "E", "a", "H", "k", "K", "h", "m", "s", "S", "z", "Z"} );

	NSDictionary sdfFormats = new NSDictionary(sdfExplains, sdfKeys);

	public String currentSDFFormat;

	public String currentSDFExplain() {
		return (String)sdfFormats.valueForKey(currentSDFFormat);
	}

	public String currentFormattedSDF() {
		SimpleDateFormat formatter = new SimpleDateFormat(currentSDFFormat);
		return formatter.format(currentTS());
	}

	public String dateString = currentTS().toString();

	public String setDateMessage;

	public WOComponent setDate() { return null; }

	public Integer incrementDateBy;

	public String unit;
	public String chosenUnit;

	public WOComponent incrementDate() {

		if (chosenUnit == null) return null;

		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int minute = 0;
		int seconds = 0;

		if ("year".equals(chosenUnit)) year = incrementDateBy.intValue();
		if ("month".equals(chosenUnit)) month = incrementDateBy.intValue();
		if ("day".equals(chosenUnit)) day = incrementDateBy.intValue();
		if ("hour".equals(chosenUnit)) hour = incrementDateBy.intValue();
		if ("minute".equals(chosenUnit)) minute = incrementDateBy.intValue();
		if ("seconds".equals(chosenUnit)) seconds = incrementDateBy.intValue();

		_currentTS = _currentTS.timestampByAddingGregorianUnits(year, month, day, hour, minute, seconds);

		return null;
	}
}