package er.r2d2w.foundation;

import java.text.SimpleDateFormat;

import com.webobjects.foundation.NSTimestamp;


public class R2DDateRange {

	protected NSTimestamp _date;
	protected long _duration;

	public R2DDateRange() {
		this(new NSTimestamp(), 0, false);
	}

	private R2DDateRange(NSTimestamp date, long duration, boolean checkValues) {
		if(date == null) { 
			throw new IllegalArgumentException("Range endpoint greater than Long.MAX_VALUE");
		}
		if (checkValues) {
			if (duration < 0) {
				throw new IllegalArgumentException(
						new StringBuilder("Cannot create an")
						.append(getClass().getName())
						.append(" with negative length.")
						.toString()
						);
			}
			if (date.getTime() - 1 > Long.MAX_VALUE - duration) {
				throw new IllegalArgumentException(
						"Range endpoint greater than Long.MAX_VALUE");
			}
		}
		_date = date;
		_duration = duration;
	}

	public R2DDateRange(long time, long duration) {
		this(new NSTimestamp(time), duration, true);
	}
	
	public R2DDateRange(NSTimestamp date, long duration) {
		this(date, duration, true);
	}
	
	public R2DDateRange(NSTimestamp startDate, NSTimestamp endDate) {
		this(startDate, endDate.getTime() - startDate.getTime(), true);
	}

	public R2DDateRange(R2DDateRange range) {
		this(range == null ? new NSTimestamp() : range.date(), 
				range == null ? 0 : range.duration(), 
				false);
	}

	/**
	 * Cover method for date().getTime();
	 * @return The number of milliseconds since the reference date that the NSTimestamp 'date' represents.
	 */
	public long getTime() {
		return _date.getTime();
	}
	
	public NSTimestamp date() {
		return _date;
	}

	public long duration() {
		return _duration;
	}

	public static R2DDateRange fromString(String string) {
		int count = string.length();
		if (count < 5) {
			throw new IllegalArgumentException("Improperly formatted string");
		}
		
		int openIndex = string.indexOf('{');
		if (openIndex == -1) {
			throw new IllegalArgumentException("Improperly formatted string");
		}
		
		int commaIndex = string.indexOf(',');
		if (commaIndex == -1) {
			throw new IllegalArgumentException("Improperly formatted string");
		}
		
		int closeIndex = string.indexOf('}');
		if (closeIndex == -1 || commaIndex <= openIndex + 1 || closeIndex <= commaIndex + 1) {
			throw new IllegalArgumentException("Improperly formatted string");
		
		} else {
			long location = (new Long(string.substring(openIndex + 1, commaIndex).trim())).longValue();
			long length = (new Long(string.substring(commaIndex + 1, closeIndex).trim())).longValue();
			return new R2DDateRange(location, length);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		sb.append("{");
		sb.append(sdf.format(date()));
		sb.append(" Etc/GMT, ");
		sb.append(duration());
		sb.append("ms}");
		return sb.toString();
	}

	public R2DDateRange rangeByUnioningRange(R2DDateRange otherRange) {
		R2DMutableDateRange range = new R2DMutableDateRange(this);
		range.unionRange(otherRange);
		return range;
	}

	public R2DDateRange rangeByIntersectingRange(R2DDateRange otherRange) {
		R2DMutableDateRange range = new R2DMutableDateRange(this);
		range.intersectRange(otherRange);
		return range;
	}

	public void subtractRange(R2DDateRange otherRange, R2DMutableDateRange resultRange1, R2DMutableDateRange resultRange2) {
		long time1;
		long duration1;
		long time2;
		long duration2;
		if (otherRange != null) {
			long time = getTime();
			long maxRange = maxRange();
			long otherTime = otherRange.getTime();
			long otherMaxRange = otherRange.maxRange();
			if (otherMaxRange <= time || maxRange <= otherTime) {
				time1 = duration1 = time2 = duration2 = 0;
			} else if (otherTime <= time) {
				time1 = otherMaxRange;
				duration1 = maxRange > otherMaxRange ? maxRange - otherMaxRange : 0;
				time2 = duration2 = 0;
			} else if (maxRange <= otherMaxRange) {
				time1 = time;
				duration1 = otherTime - time;
				time2 = duration2 = 0;
			} else {
				time1 = time;
				duration1 = otherTime - time;
				time2 = otherMaxRange;
				duration2 = maxRange - otherMaxRange;
			}
		} else {
			time1 = duration1 = time2 = duration2 = 0;
		}
		if (resultRange1 != null) {
			resultRange1.setDate( new NSTimestamp(time1));
			resultRange1.setDuration(duration1);
		}
		if (resultRange2 != null) {
			resultRange2.setDate( new NSTimestamp(time2));
			resultRange2.setDuration(duration2);
		}
	}

	public long maxRange() {
		return getTime() + duration();
	}

	public boolean isEmpty() {
		return duration() == 0;
	}

	public boolean containsMoment(long time) {
		long compareTime = getTime();
		return compareTime <= time && time - duration() < compareTime;
	}
	
	public boolean containsMoment(NSTimestamp date) {
		return (date == null)?false:containsMoment(date.getTime());
	}

	public boolean intersectsRange(R2DDateRange otherRange) {
		if (otherRange == null) { return false; }
		long loc = getTime();
		long otherLoc = otherRange.getTime();
		return loc - otherRange.duration() < otherLoc && otherLoc - duration() < loc;
	}

	public boolean isSubrangeOfRange(R2DDateRange otherRange) {
		if (otherRange == null) { return false; }
		long loc = getTime();
		long otherLoc = otherRange.getTime();
		return otherLoc <= loc && (loc - 1) + duration() <= (otherLoc - 1) + otherRange.duration();
	}

	public boolean isEqualToRange(R2DDateRange otherRange) {
		if (otherRange == null) { return false; }
		if (otherRange == this) { return true; }
		return getTime() == otherRange.getTime() && duration() == otherRange.duration();
	}

	public boolean equals(Object object) {
		return (object instanceof R2DDateRange) ? isEqualToRange((R2DDateRange) object) : false;
	}

	public int hashCode() {
		return Long.valueOf(getTime()).hashCode() ^ Long.valueOf(duration()).hashCode();
	}

	public R2DMutableDateRange mutableClone() {
		return new R2DMutableDateRange(this);
	}
	
}
