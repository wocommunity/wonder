package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ERXJodaDateTimeFormatter extends Format {
	private final DateTimeFormatter formatter;
	
	public ERXJodaDateTimeFormatter(String pattern) {
		this(pattern, null, null, null);
	}
	
	public ERXJodaDateTimeFormatter(String pattern, DateTimeZone zone) {
		this(pattern, null, null, zone);
	}
	
	public ERXJodaDateTimeFormatter(String pattern, TimeZone zone) {
		this(pattern, null, null, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaDateTimeFormatter(String pattern, Locale locale, TimeZone zone) {
		this(pattern, null, locale, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaDateTimeFormatter(String pattern, Chronology chronology, Locale locale, DateTimeZone zone) {
		DateTimeFormatter f = DateTimeFormat.forPattern(pattern);
		if(chronology != null) { f = f.withChronology(chronology); }
		if(locale != null) { f = f.withLocale(locale); }
		if(zone != null) { f = f.withZone(zone); }
		formatter = f;
	}
	
	public ERXJodaDateTimeFormatter(Locale locale, String style) {
		this(DateTimeFormat.patternForStyle(style, locale));
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		formatter.printTo(buffer, (DateTime)obj);
		return buffer;
	}

	@Override
	public DateTime parseObject(String str, ParsePosition pos) {
		DateTime dt = formatter.parseDateTime(str);
		pos.setIndex(str.length());
		return dt;
	}

}
