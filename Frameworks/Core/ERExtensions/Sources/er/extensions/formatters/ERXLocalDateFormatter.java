package er.extensions.formatters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Formatter class to format and parse {@link LocalDate} objects.
 * 
 * @author jw
 */
public class ERXLocalDateFormatter extends ERXDateTimeFormatter {
	private static final long serialVersionUID = 1L;

	public ERXLocalDateFormatter(DateTimeFormatter formatter) {
		super(formatter, LocalDate::from);
	}
}
