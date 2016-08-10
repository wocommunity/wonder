package er.extensions.formatters;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.webobjects.foundation.NSTimestamp;

/**
 * A simple format object for NSTimestamps. It supports the same symbols 
 * as NSTimestampFormatter and SimpleDateFormat.
 */
public class ERXNSTimestampFormatter extends Format {
	private static final long serialVersionUID = 1L;
	private final SimpleDateFormat _formatter;
	private static final String[] searchList = {
		"%a", "%A", "%b", "%B", "%c", "%d", "%e", "%F", "%H", "%I", "%j", "%m", "%M", "%p", "%S", "%w", "%x", "%X", "%y", "%Y", "%Z", "%z", "%%"
	};
	private static final String[] replacementList = {
		"EEE", "EEEE", "MMM", "MMMM", "dd/MM/yyyy hh:mm aa", "dd", "d", "SSS", "HH", "hh", "DDD", "MM", "mm", "aa", "ss", "EE", "dd/MM/yyyy", "hh:mm aa", "yy", "yyyy", "zzzz", "zzzz", "%"
	};

	public ERXNSTimestampFormatter() {
		_formatter = new SimpleDateFormat();
	}

	public ERXNSTimestampFormatter(String pattern) {
		_formatter = new SimpleDateFormat(processPattern(pattern));
	}

	public ERXNSTimestampFormatter(String pattern, Locale locale) {
		_formatter = new SimpleDateFormat(processPattern(pattern), locale);
	}

	public ERXNSTimestampFormatter(String pattern, DateFormatSymbols symbols) {
		_formatter = new SimpleDateFormat(processPattern(pattern), symbols);
	}

	public ERXNSTimestampFormatter(String pattern, TimeZone timezone) {
		_formatter = new SimpleDateFormat(processPattern(pattern));
		_formatter.setTimeZone(timezone);
	}

	public ERXNSTimestampFormatter(String pattern, Locale locale, TimeZone timezone) {
		_formatter = new SimpleDateFormat(processPattern(pattern), locale);
		_formatter.setTimeZone(timezone);
	}

	@Override
	public StringBuffer format(Object date, StringBuffer stringbuffer, FieldPosition fieldposition) {
		return _formatter.format(date, stringbuffer, fieldposition);
	}

	@Override
	public Object parseObject(String s, ParsePosition parseposition) {
		Date date = (Date)_formatter.parseObject(s, parseposition);
		return new NSTimestamp(date);
	}
	
	private static String processPattern(String pattern) {
		String result = StringUtils.replaceEach(pattern, searchList, replacementList);
		return result;
	}
}
