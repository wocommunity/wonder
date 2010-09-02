package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.concurrent.TimeUnit;



/**
 * @author kieran
 *
 */
public class ERXTimeDurationFormatter extends NumberFormat {
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	private boolean showLargestUnitOnly = false;
	private boolean omitSecondsPart = false;
	
	public ERXTimeDurationFormatter(TimeUnit timeUnit, boolean showLargestUnitOnly, boolean omitSecondsPart) {
		super();
		
		this.timeUnit = timeUnit;
		this.showLargestUnitOnly = showLargestUnitOnly;
		this.omitSecondsPart = false;
	}
	


	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		return format((long)number, toAppendTo, pos);
	}
    
    @Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition fieldPosition) {
       long seconds = TimeUnit.SECONDS.convert(number, timeUnit);
       return toAppendTo.append(timePeriodDescription(seconds, showLargestUnitOnly, omitSecondsPart));
    }

    
	/**
	 * @param secondsValue
	 * @param resultIfNull
	 * @param showLargestUnitOnly rounded to the nearest hour, minute of second as appropriate
	 * @param omitSecondsPart when the value is greater than one minute
	 * @return a user friendly description of a time duration seconds value
	 */
	private String timePeriodDescription(long value, boolean showLargestUnitOnly, boolean omitSecondsPart) {
		boolean shouldStopAddingComponents = false;

		if (value == 0) {
			StringBuilder b = new StringBuilder();
			b.append("less than 1 ");
			if (omitSecondsPart) {
				b.append("minute");
			} else {
				b.append("second");
			} //~ if (omitSecondsPart)
			return b.toString();
		}
		
		boolean isNegative = value < 0L;
		if (isNegative) {
			value = -value;
		} //~ if (isNegative)
		
		long secondsPart = value % 60L;
		// Convert value to remaining minutes
		value = (value - secondsPart) / 60L;
		
		long minutesPart = value % 60L;
		// Convert value to remaining hours
		value = (value - minutesPart) / 60L;
		
		long hoursPart = value % 24L;
		// Convert value to remaining days
		value = (value - hoursPart) / 24L;
		
		StringBuilder b = new StringBuilder();
		if (value > 0) {
			b.append(value);
			if (value > 1) {
				b.append(" days");
			} else {
				b.append(" day");
			} //~ if (value > 1)
			if (showLargestUnitOnly) shouldStopAddingComponents = true;
	
		}
		
		if (hoursPart > 0 && !shouldStopAddingComponents) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(hoursPart);
			if (hoursPart > 1) {
				b.append(" hours");
			} else {
				b.append(" hour");
			} //~ if (hoursPart > 1)
			if (showLargestUnitOnly) shouldStopAddingComponents = true;
			
		}
		
		if (minutesPart > 0 && !shouldStopAddingComponents) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(minutesPart);
			if (minutesPart > 1) {
				b.append(" minutes");
			} else {
				b.append(" minute");
			} //~ if (minutesPart > 1)
			if (showLargestUnitOnly) shouldStopAddingComponents = true;
		}
		
		if (secondsPart > 0  && !shouldStopAddingComponents && !omitSecondsPart) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(secondsPart);
			if (secondsPart > 1) {
				b.append(" seconds");
			} else {
				b.append(" second");
			} //~ if (secondsPart > 1)
			if (showLargestUnitOnly) shouldStopAddingComponents = true;
		}
		
		if (isNegative) {
			b.insert(0, "- ");
		} //~ if (isNegative)
		
		return b.toString();
	}





	/**
	 * I know it is lame, but parsing is unsupported.
	 * 
	 * @see java.text.NumberFormat#parse(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		throw new UnsupportedOperationException("This class does not support parsing.");
	}
}
