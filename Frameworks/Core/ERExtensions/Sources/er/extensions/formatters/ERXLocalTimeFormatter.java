package er.extensions.formatters;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Formatter class to format and parse {@link LocalTime} objects.
 * <p>
 * This class is thread-safe.
 * 
 * @author jw
 */
public class ERXLocalTimeFormatter extends ERXDateTimeFormatter {
	private static final long serialVersionUID = 1L;

	public ERXLocalTimeFormatter(DateTimeFormatter formatter) {
		super(formatter, LocalTime::from);
	}
}
