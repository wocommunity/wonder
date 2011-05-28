package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ERXJodaLocalDateFormatter extends Format {
	private final String patternUsed;
	private final DateTimeFormatter formatter;

	public ERXJodaLocalDateFormatter(String pattern) {
		this(pattern, null, null, null);
	}
	
	public ERXJodaLocalDateFormatter(String pattern, DateTimeZone zone) {
		this(pattern, null, null, zone);
	}
	
	public ERXJodaLocalDateFormatter(String pattern, TimeZone zone) {
		this(pattern, null, null, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaLocalDateFormatter(String pattern, Locale locale, TimeZone zone) {
		this(pattern, null, locale, DateTimeZone.forTimeZone(zone));
	}
	
	public ERXJodaLocalDateFormatter(String pattern, Chronology chronology, Locale locale, DateTimeZone zone) {
		patternUsed = pattern;
		DateTimeFormatter f = DateTimeFormat.forPattern(pattern);
		if(chronology != null) { f = f.withChronology(chronology); }
		if(locale != null) { f = f.withLocale(locale); }
		if(zone != null) { f = f.withZone(zone); }
		formatter = f;
	}
	
	public ERXJodaLocalDateFormatter(Locale locale, String style) {
		this(DateTimeFormat.patternForStyle(style, locale));
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		formatter.printTo(buffer, (LocalDate)obj);
		return buffer;
	}

	@Override
	public LocalDate parseObject(String str, ParsePosition pos) {
		DateTime dt = formatter.parseDateTime(str);
		pos.setIndex(str.length());
		LocalDate ld = new LocalDate(dt);
		return ld;
	}

	public String toPattern() {
		return patternUsed;
	}
}
