package er.chronic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.webobjects.foundation.NSTimestamp;

import er.chronic.utils.Span;

/**
 * ChronicFormatter attempts to parse a date using an expected date format, and then
 * falls back to using Chronic in the event of a parse failure.
 * 
 * @author probert
 */
public class ChronicFormatter extends SimpleDateFormat {
	private Options _options;
	private boolean _guessingEarly;

	/**
	 * Constructs a new ChronicFormatter.
	 * 
	 * @param pattern the SimpleDateFormat pattern to parse
	 */
	public ChronicFormatter(String pattern) {
		this(pattern, null, true);
	}

	/**
	 * Constructs a new ChronicFormatter.
	 * 
	 * @param pattern the SimpleDateFormat pattern to parse
	 * @param options the chronic options to parse with
	 */
	public ChronicFormatter(String pattern, Options options) {
		this(pattern, options, true);
	}

	/**
	 * Constructs a new ChronicFormatter.
	 * 
	 * @param pattern the SimpleDateFormat pattern to parse
	 * @param options the chronic options to parse with
	 * @param guessingEarly if true, guess the start of a span if the parsed date returns a span
	 */
	public ChronicFormatter(String pattern, Options options, boolean guessingEarly) {
		super(pattern);
		_options = options;
		_guessingEarly = guessingEarly;
	}

	/**
	 * Returns the options used by this formatter.
	 * 
	 * @return the options used by this formatter
	 */
	public Options options() {
		if (_options == null) {
			_options = new Options();
		}
		return _options;
	}

	/**
	 * Sets the options used by this formatter.
	 * 
	 * @param options the options used by this formatter
	 */
	public void setOptions(Options options) {
		_options = options;
	}

	/**
	 * Returns whether or not this parser guesses the beginning of a span if a date parses as a span.
	 * 
	 * @return whether or not this parser guesses the beginning of a span
	 */
	public boolean isGuessingEarly() {
		return _guessingEarly;
	}

	/**
	 * Sets whether or not this parser guesses the beginning of a span if a date parses as a span.
	 * 
	 * @param guessingEarly whether or not this parser guesses the beginning of a span
	 */
	public void setGuessingEarly(boolean guessingEarly) {
		_guessingEarly = guessingEarly;
	}

	@Override
	public NSTimestamp parseObject(String text) throws ParseException {
		NSTimestamp parsedTimestamp = null;
		try {
			// Attempt to parse the string with the given pattern.
			Date parsedDate = super.parse(text);
			parsedTimestamp = new NSTimestamp(parsedDate);
		}
		catch (ParseException e) {
			// If the input doesn't match the pattern, use Chronic to parse the
			// input.
			Span span = Chronic.parse(text, options());
			if (span == null) {
				throw e;
			}
			if (span.isSingularity() || isGuessingEarly()) {
				parsedTimestamp = new NSTimestamp(span.getBeginCalendar().getTime());
			}
			else {
				parsedTimestamp = new NSTimestamp(span.getEndCalendar().getTime());
			}
		}
		return parsedTimestamp;
	}

}
