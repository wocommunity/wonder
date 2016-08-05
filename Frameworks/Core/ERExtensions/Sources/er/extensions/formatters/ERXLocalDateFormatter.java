package er.extensions.formatters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Formatter class to format and parse {@link LocalDate} objects.
 * <p>
 * This class is thread-safe.
 * 
 * @author jw
 */
public class ERXLocalDateFormatter extends ERXDateTimeFormatter {
	private static final long serialVersionUID = 1L;

	/**
	 * Basic constructor.
	 * 
	 * @param formatter
	 *            the formatter to encapsulate
	 */
	public ERXLocalDateFormatter(DateTimeFormatter formatter) {
		super(formatter, LocalDate::from);
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
	public ERXLocalDateFormatter(DateTimeFormatter formatter, String pattern) {
		super(formatter, LocalDate::from, pattern);
	}
}
