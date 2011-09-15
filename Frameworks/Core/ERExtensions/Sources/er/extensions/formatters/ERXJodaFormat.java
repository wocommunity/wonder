package er.extensions.formatters;

import java.util.Locale;

import org.joda.time.DateTimeZone;
import org.joda.time.chrono.BaseChronology;

public interface ERXJodaFormat {

	public String pattern();

	public BaseChronology chronology();

	public Locale locale();

	public DateTimeZone zone();

}
