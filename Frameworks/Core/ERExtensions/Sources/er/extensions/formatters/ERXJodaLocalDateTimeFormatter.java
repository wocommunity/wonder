package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ERXJodaLocalDateTimeFormatter extends Format {
	private final DateTimeFormatter formatter;

	public ERXJodaLocalDateTimeFormatter(String pattern) {
		this(pattern, null, null, null);
	}
	
	public ERXJodaLocalDateTimeFormatter(String pattern, DateTimeZone zone) {
		this(pattern, null, null, zone);
	}
	
	public ERXJodaLocalDateTimeFormatter(String pattern, TimeZone zone) {
		this(pattern, null, null, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaLocalDateTimeFormatter(String pattern, Locale locale, TimeZone zone) {
		this(pattern, null, locale, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaLocalDateTimeFormatter(String pattern, Chronology chronology, Locale locale, DateTimeZone zone) {
		DateTimeFormatter f = DateTimeFormat.forPattern(pattern);
		if(chronology != null) { f = f.withChronology(chronology); }
		if(locale != null) { f = f.withLocale(locale); }
		if(zone != null) { f = f.withZone(zone); }
		formatter = f;
	}
	
	public ERXJodaLocalDateTimeFormatter(Locale locale, String style) {
		this(DateTimeFormat.patternForStyle(style, locale));
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		formatter.printTo(buffer, (LocalDateTime)obj);
		return buffer;
	}

	@Override
	public LocalDateTime parseObject(String str, ParsePosition pos) {
		DateTime dt = formatter.parseDateTime(str);
		pos.setIndex(str.length());
		LocalDateTime ldt = new LocalDateTime(dt);
		return ldt;
	}

}
