package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Objects;

/**
 * Base formatter class for wrapping a {@link DateTimeFormatter} into something
 * that extends {@link Format} so you can safely use it with components that
 * would expect old formatter types.
 * 
 * @author jw
 */
public abstract class ERXDateTimeFormatter extends Format {
	private static final long serialVersionUID = 1L;
	private final DateTimeFormatter formatter;
	private final TemporalQuery<?> query;
	private final String pattern;

	/**
	 * Basic constructor.
	 * 
	 * @param formatter
	 *            the formatter to encapsulate
	 * @param query
	 *            the method to extract correct date object from parsed results
	 */
	public ERXDateTimeFormatter(DateTimeFormatter formatter, TemporalQuery<?> query) {
		this(formatter, query, null);
	}

	/**
	 * Constructor that takes an additional string pattern. DateTimeFormatter
	 * objects unfortunately don't retain their pattern so you can't extract it
	 * from the object anymore. Some component like AjaxDatePicker though need
	 * that information so you can provide it with this constructor.
	 * 
	 * @param formatter
	 *            the formatter to encapsulate
	 * @param query
	 *            the method to extract correct date object from parsed results
	 * @param pattern
	 *            the pattern used by the formatter
	 */
	public ERXDateTimeFormatter(DateTimeFormatter formatter, TemporalQuery<?> query, String pattern) {
		this.formatter = Objects.requireNonNull(formatter, "formatter must not be null");
		this.query = Objects.requireNonNull(query, "query must not be null");
		this.pattern = pattern;
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (obj instanceof TemporalAccessor) {
			formatter.formatTo((TemporalAccessor) obj, toAppendTo);
		}
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Object date = formatter.parse(source, query);
		pos.setIndex(source.length());
		return date;
	}

	/**
	 * The formatter to be used.
	 * 
	 * @return the formatter
	 */
	public DateTimeFormatter formatter() {
		return formatter;
	}

	/**
	 * The method reference that will create the correct object type from parsed
	 * result.
	 * 
	 * @return query for the parsed temporal object
	 */
	protected TemporalQuery<?> query() {
		return query;
	}

	/**
	 * The pattern used for the formatter. This information has to be given
	 * during construction of this object as DateTimeFormatter objects don't
	 * contain their original pattern as string.
	 * 
	 * @return the pattern or null if none was provided
	 */
	public String pattern() {
		return pattern;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append('<').append(getClass().getName())
				.append(" formatter=").append(formatter)
				.append(" pattern=").append(pattern).append('>');
		return sb.toString();
	}
}
