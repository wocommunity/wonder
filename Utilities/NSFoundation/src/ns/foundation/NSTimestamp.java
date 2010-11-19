package ns.foundation;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class NSTimestamp extends Timestamp {

  public static final long DistantPastJavaTime = -62125920000000L;
  public static final long DistantFutureJavaTime = 8223372036854775807L;
  public static final long DistantPastJSTime = -8640000000000000L; // 100,000,000 days before Epoch
  public static final long DistantFutureJSTime = 8640000000000000L; // 100,000,000 days after Epoch

  public static final NSTimestamp DistantPast = new NSTimestamp(DistantPastJavaTime);
  public static final NSTimestamp DistantFuture = new NSTimestamp(DistantFutureJavaTime);
  
	private static final String UNSUPPORTED = " is not a supported operation of com.webobjects.foundation.NSTimestamp";
	
	public NSTimestamp() {
		this(new Date().getTime());
	}
	
	public NSTimestamp(Date date) {
		this(date.getTime());
	}
	
	public NSTimestamp(long time) {
		super(time);
	}
	
	public NSTimestamp(long milliseconds, int nanoseconds) {
		super(milliseconds);
		
		if (nanoseconds > 0) {
			long justMilliseconds = milliseconds % 1000;
			long nanos = (justMilliseconds * 1000000) + nanoseconds;
			super.setNanos((int)nanos);
		}
	}
	
	public NSTimestamp(long time, NSTimestamp date) {
		this(date.getTime() + time);
	}
	
	public NSTimestamp(Timestamp sqlTimestamp) {
		super(sqlTimestamp.getTime());
		super.setNanos(sqlTimestamp.getNanos());
	}
	
  @Override
  public boolean after(Timestamp ts) {
    return (getTime() > ts.getTime())
        || (getTime() == ts.getTime() 
        && getNanos() > ts.getNanos());
  }

  @Override
  public boolean before(Timestamp ts) {
    return (getTime() < ts.getTime())
        || (getTime() == ts.getTime()
        && getNanos() < ts.getNanos());
  }
	
	public int compare(NSTimestamp ts) {
		int result = compareTo(ts);
		if (result < 0)
			return NSComparator.OrderedAscending;
		else if (result == 0)
			return NSComparator.OrderedSame;
		else
			return NSComparator.OrderedDescending;
	}
	
	@Override
  public boolean equals(Timestamp ts) {
	  if (getNanos() == ts.getNanos())
	      return true;
	  return false;
	}

	@Override
  public boolean equals(Object ts) {
	  if (ts instanceof Timestamp) {
	    return this.equals((Timestamp)ts);
	  }
	  return super.equals(ts);
	}
	
	@Override
	public int getNanos() {
		int milliseconds =  (super.getNanos() / 1000000) * 1000000;
		return super.getNanos() - milliseconds;
	}

	public NSTimestamp timestampByAddingGregorianUnits(int years, 
			int months, int days, int hours, int minutes, int seconds)  {
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(getTime());
		
		if (seconds != 0)
			cal.add(Calendar.SECOND, seconds);
		if (minutes != 0)
			cal.add(Calendar.MINUTE, minutes);
		if (hours != 0)
			cal.add(Calendar.HOUR, hours);
		if (days != 0)
			cal.add(Calendar.DATE, days);
		if (months != 0)
			cal.add(Calendar.MONTH, months);
		if (years != 0)
			cal.add(Calendar.YEAR, years);
		
		return new NSTimestamp(cal.getTime().getTime(), getNanos());
	}
	
	@Override
	@Deprecated
	public void setDate(int date) {
		throw new UnsupportedOperationException("setDate" + UNSUPPORTED);
	}
	
	@Override
	@Deprecated
	public void setMonth(int month) {
		throw new UnsupportedOperationException("setMonth" + UNSUPPORTED);
	}
	
	@Override
	@Deprecated
	public void setYear(int year) {
		throw new UnsupportedOperationException("setYear" + UNSUPPORTED);
	}
	
	@Override
	@Deprecated
	public void setHours(int hours) {
		throw new UnsupportedOperationException("setHours" + UNSUPPORTED);
	}
	
	@Override
	@Deprecated
	public void setMinutes(int minutes) {
		throw new UnsupportedOperationException("setMinutes" + UNSUPPORTED);
	}
	
	@Override
	@Deprecated
	public void setSeconds(int seconds) {
		throw new UnsupportedOperationException("setSeconds" + UNSUPPORTED);
	}
	
	@Override
	@Deprecated
	public void setNanos(int n) {
		throw new UnsupportedOperationException("setNanos" + UNSUPPORTED);
	}
	
	@Override
	@Deprecated
	public void setTime(long time) {
		throw new UnsupportedOperationException("setTime" + UNSUPPORTED);
	}
	
}
