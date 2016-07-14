package er.extensions.formatters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formatter class to format and parse {@link LocalDateTime} objects.
 * 
 * @author jw
 */
public class ERXLocalDateTimeFormatter extends ERXDateTimeFormatter {
	private static final long serialVersionUID = 1L;

	public ERXLocalDateTimeFormatter(DateTimeFormatter formatter) {
		super(formatter, LocalDateTime::from);
	}
}
