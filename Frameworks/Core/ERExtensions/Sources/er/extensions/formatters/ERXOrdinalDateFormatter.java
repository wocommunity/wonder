package er.extensions.formatters;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

/**
 * Date formatter that supports days with ordinal values like 1st, 2nd, 3rd.
 * etc. Not localized (English only at present).
 * 
 * To include an ordinal suffix supply a format string that looks something like
 * this: "d'th' MMM, yyyy" where 'th' will be replaced by the correct ordinal
 * suffix. All standard NSTimestampFormatter tokens are supported.
 * 
 * @author qdolan
 */

@SuppressWarnings("deprecation")
public class ERXOrdinalDateFormatter extends NSTimestampFormatter {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private final Pattern ORDINAL_PATTERN = Pattern.compile("(?:([1-3]?[0-9])(?:st|nd|rd|th))");
	
	public ERXOrdinalDateFormatter() {
		super();
	}

	public ERXOrdinalDateFormatter(String aPattern) {
		super(aPattern);
	}

	public ERXOrdinalDateFormatter(String aPattern, DateFormatSymbols symbols) {
		super(aPattern, symbols);
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		NSTimestamp timestamp;
		if (obj instanceof NSTimestamp)
			timestamp = (NSTimestamp) obj;
		else if (obj instanceof java.util.Date)
			timestamp = new NSTimestamp((java.util.Date) obj);
		else if (obj instanceof java.sql.Date)
			timestamp = new NSTimestamp((java.sql.Date) obj);
		else
			throw new IllegalArgumentException("Unable to format " + obj.getClass() + " : " + obj.toString());

		String formatted = super.format(timestamp, new StringBuffer(), pos).toString();
		Matcher matcher = ORDINAL_PATTERN.matcher(formatted);
		if (pattern().contains("'th") && matcher.find()) {
			formatted = matcher.replaceAll("$1" + ordinalSuffix(timestamp));
		}
		return toAppendTo.append(formatted);
	}

	private String ordinalSuffix(NSTimestamp timestamp) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeZone(defaultFormatTimeZone());
		calendar.setTime(timestamp);
		switch (calendar.get(GregorianCalendar.DAY_OF_MONTH)) {
		case 1:
		case 21:
		case 31:
			return "st";
		case 2:
		case 22:
			return "nd";
		case 3:
		case 23:
			return "rd";
		default:
			return "th";
		}
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Matcher matcher = ORDINAL_PATTERN.matcher(source);
		return super.parseObject(matcher.replaceAll("$1th"), pos);
	}
}
