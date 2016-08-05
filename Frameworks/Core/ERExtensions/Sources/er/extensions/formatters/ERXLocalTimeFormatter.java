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

	/**
	 * Basic constructor.
	 * 
	 * @param formatter
	 *            the formatter to encapsulate
	 */
	public ERXLocalTimeFormatter(DateTimeFormatter formatter) {
		super(formatter, LocalTime::from);
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
	public ERXLocalTimeFormatter(DateTimeFormatter formatter, String pattern) {
		super(formatter, LocalTime::from, pattern);
	}
}
