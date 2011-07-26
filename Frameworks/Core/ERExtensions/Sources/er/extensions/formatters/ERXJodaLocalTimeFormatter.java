package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ERXJodaLocalTimeFormatter extends Format {
	private final DateTimeFormatter formatter;

	public ERXJodaLocalTimeFormatter(String pattern) {
		this(pattern, null, null, null);
	}
	
	public ERXJodaLocalTimeFormatter(String pattern, DateTimeZone zone) {
		this(pattern, null, null, zone);
	}
	
	public ERXJodaLocalTimeFormatter(String pattern, TimeZone zone) {
		this(pattern, null, null, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaLocalTimeFormatter(String pattern, Locale locale, TimeZone zone) {
		this(pattern, null, locale, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaLocalTimeFormatter(String pattern, Chronology chronology, Locale locale, DateTimeZone zone) {
		DateTimeFormatter f = DateTimeFormat.forPattern(pattern);
		if(chronology != null) { f = f.withChronology(chronology); }
		if(locale != null) { f = f.withLocale(locale); }
		if(zone != null) { f = f.withZone(zone); }
		formatter = f;
	}
	
	public ERXJodaLocalTimeFormatter(Locale locale, String style) {
		this(DateTimeFormat.patternForStyle(style, locale));
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		formatter.printTo(buffer, (LocalTime)obj);
		return buffer;
	}

	@Override
	public LocalTime parseObject(String str, ParsePosition pos) {
		DateTime dt = formatter.parseDateTime(str);
		pos.setIndex(str.length());
		LocalTime lt = new LocalTime(dt);
		return lt;
	}

}
