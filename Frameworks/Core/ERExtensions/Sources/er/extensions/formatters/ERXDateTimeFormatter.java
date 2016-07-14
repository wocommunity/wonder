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
	private DateTimeFormatter formatter;
	private TemporalQuery<?> query;

	public ERXDateTimeFormatter(DateTimeFormatter formatter, TemporalQuery<?> query) {
		this.formatter = Objects.requireNonNull(formatter, "formatter must not be null");
		this.query = Objects.requireNonNull(query, "query must not be null");
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (obj instanceof TemporalAccessor) {
			formatter().formatTo((TemporalAccessor) obj, toAppendTo);
		}
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Object date = formatter().parse(source, query());
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

	protected void setFormatter(DateTimeFormatter formatter) {
		this.formatter = Objects.requireNonNull(formatter, "formatter must not be null");
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
}
