package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.chrono.BaseChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ERXJodaLocalTimeFormatter extends Format implements ERXJodaFormat {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private transient DateTimeFormatter formatter;
	private final String _pattern;
	private final BaseChronology _chronology;
	private final Locale _locale;
	private final DateTimeZone _zone;

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
	
	public ERXJodaLocalTimeFormatter(String pattern, BaseChronology chronology, Locale locale, DateTimeZone zone) {
		_pattern = pattern;
		_chronology = chronology;
		_locale = locale;
		_zone = zone;
	}
	
	public ERXJodaLocalTimeFormatter(Locale locale, String style) {
		this(DateTimeFormat.patternForStyle(style, locale));
	}

	protected DateTimeFormatter formatter() {
		if(formatter == null) {
			formatter = DateTimeFormat.forPattern(_pattern);
			if(_chronology != null) { formatter = formatter.withChronology(_chronology); }
			if(_locale != null) { formatter = formatter.withLocale(_locale); }
			if(_zone != null) { formatter = formatter.withZone(_zone); }
		}
		return formatter;
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		formatter().printTo(buffer, (LocalTime)obj);
		return buffer;
	}

	@Override
	public LocalTime parseObject(String str, ParsePosition pos) {
		DateTime dt = formatter().parseDateTime(str);
		pos.setIndex(str.length());
		LocalTime lt = new LocalTime(dt);
		return lt;
	}

	public String pattern() {
		return _pattern;
	}

	public BaseChronology chronology() {
		return _chronology;
	}

	public Locale locale() {
		return _locale;
	}

	public DateTimeZone zone() {
		return _zone;
	}

}
