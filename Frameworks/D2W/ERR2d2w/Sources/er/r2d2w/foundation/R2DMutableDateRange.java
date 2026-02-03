package er.r2d2w.foundation;

import com.webobjects.foundation.NSTimestamp;


public class R2DMutableDateRange extends R2DDateRange {

	public R2DMutableDateRange() {
		super();
	}

	public R2DMutableDateRange(long time, long duration) {
		super(time, duration);
	}

	public R2DMutableDateRange(NSTimestamp date, long duration) {
		super(date, duration);
	}

	public R2DMutableDateRange(NSTimestamp startDate, NSTimestamp endDate) {
		super(startDate, endDate);
	}
	
	public R2DMutableDateRange(R2DDateRange range) {
		super(range);
	}

	public void setDate(NSTimestamp date) {
		if(date == null) {
			throw new IllegalArgumentException("Date cannot be null");
		}
		if (date.getTime() - 1 > Long.MAX_VALUE - duration()) {
			throw new IllegalArgumentException("Range endpoint greater than Long.MAX_VALUE");
		} else {
			_date = date;
		}
	}

	public void setDuration(long duration) {
		if (duration < 0) {
			throw new IllegalArgumentException(
					new StringBuilder("Cannot set negative duration (")
					.append(duration)
					.append(")")
					.toString());
		}
		if (getTime() - 1 > Long.MAX_VALUE - duration) {
			throw new IllegalArgumentException("Range endpoint greater than Long.MAX_VALUE");
		} else {
			_duration = duration;
		}
	}

	public void unionRange(R2DDateRange otherRange) {
		if (otherRange != null) {
			long time = getTime();
			long maxRange = maxRange();
			long otherTime = otherRange.getTime();
			long otherMaxRange = otherRange.maxRange();
			long newMaxRange = otherMaxRange >= maxRange ? otherMaxRange: maxRange;
			long newTime = time >= otherTime ? otherTime: time;
			setDate( new NSTimestamp(newTime));
			setDuration(newMaxRange - newTime);
		}
	}

	public void intersectRange(R2DDateRange otherRange) {
		if (otherRange != null) {
			long time = getTime();
			long maxRange = maxRange();
			long otherTime = otherRange.getTime();
			long otherMaxRange = otherRange.maxRange();
			long minRange = maxRange >= otherMaxRange ? otherMaxRange
					: maxRange;
			if (otherTime <= time && time < otherMaxRange) {
				setDuration(minRange - time);
			} else if (time <= otherTime && otherTime < maxRange) {
				setDate( new NSTimestamp(otherTime));
				setDuration(minRange - otherTime);
			} else {
				setDuration(0);
			}
		}
	}

	public R2DDateRange immutableClone() {
		return new R2DDateRange(this);
	}

}
