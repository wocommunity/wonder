package er.extensions.formatters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formatter class to format and parse {@link LocalDateTime} objects.
 * <p>
 * This class is thread-safe.
 * 
 * @author jw
 */
public class ERXLocalDateTimeFormatter extends ERXDateTimeFormatter {
	private static final long serialVersionUID = 1L;

	/**
	 * Basic constructor.
	 * 
	 * @param formatter
	 *            the formatter to encapsulate
	 */
	public ERXLocalDateTimeFormatter(DateTimeFormatter formatter) {
		super(formatter, LocalDateTime::from);
	}

	/**
	 * Constructor that takes an additional string pattern. DateTimeFormatter
	 * objects unfortunately don't retain their pattern so you can't extract it
	 * from the object anymore. Some component like AjaxDatePicker though need
	 * that information so you can provide it with this constructor.
	 * 
	 * @param formatter
	 *            the formatter to encapsulate
	 * @param pattern
	 *            the pattern used by the formatter
	 */
	public ERXLocalDateTimeFormatter(DateTimeFormatter formatter, String pattern) {
		super(formatter, LocalDateTime::from, pattern);
	}
}
