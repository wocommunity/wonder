package ns.foundation;

import java.util.Date;
import java.util.GregorianCalendar;

public class NSDate extends Date implements NSCoding {
  public static final int NSOrderedAscending = -1;
  public static final int NSOrderedSame = 0;
  public static final int NSOrderedDescending = 1;

  public static final double TimeIntervalSince1970 = 978307200.0D;
  public static final NSDate DateFor1970 = new NSDate(-978307200.0D);

  public NSDate() {
  }

  public NSDate(double seconds) {
    super((long) new NSDate().getTime() + timeIntervalToMilliseconds(seconds));
  }

  public NSDate(double seconds, NSDate sinceDate) {
    super((long) sinceDate.getTime() + timeIntervalToMilliseconds(seconds));
  }

  public double timeIntervalSinceReferenceDate() {
    GregorianCalendar referenceDate = new GregorianCalendar();
    referenceDate.set(2001, 0, 0, 0, 0, 0);
    return timeIntervalSinceDate(referenceDate.getTime());
  }

  public double timeIntervalSinceDate(Date date) {
    return millisecondsToTimeInterval(this.getTime() - date.getTime());
  }

  public double timeIntervalSinceNow() {
    return timeIntervalSinceDate(new NSDate());
  }

  public NSDate earlierDate(NSDate date) {
    if (date == null) {
      return this;
    }
    if (after(date)) {
      return date;
    }
    return this;
  }

  public NSDate laterDate(NSDate date) {
    if (date == null)
      return this;
    if (before(date))
      return date;
    return this;
  }

  public int compare(NSDate date) {
    if (before(date))
      return NSOrderedAscending;
    if (after(date))
      return NSOrderedDescending;
    return NSOrderedSame;
  }

  public boolean isEqualToDate(NSDate date) {
    return equals(date);
  }

  public static double currentTimeIntervalSinceReferenceDate() {
    return new NSDate().timeIntervalSinceReferenceDate();
  }

  public static Object distantFuture() {
    NSDate result = new NSDate();
    result.setTime(Long.MAX_VALUE);
    return result;
  }

  public static Object distantPast() {
    NSDate result = new NSDate();
    result.setTime(Long.MIN_VALUE);
    return result;
  }

  public NSDate dateByAddingTimeInterval(double seconds) {
    return new NSDate(seconds, this);
  }

  @Override
  public void encodeWithCoder(NSCoder coder) {
    // TODO
  }

  public static Object decodeObject(NSCoder coder) {
    // TODO
    return null;
  }

  @Override
  public boolean equals(Object paramObject) {
    if (paramObject instanceof NSDate)
      return isEqualToDate((NSDate) paramObject);
    return super.equals(paramObject);
  }

  @Override
  public int hashCode() {
    return (int) Math.round(timeIntervalSinceReferenceDate());
  }

  public static long timeIntervalToMilliseconds(double seconds) {
    return Math.round(seconds * 1000.0D);
  }

  public static double millisecondsToTimeInterval(long millis) {
    return (millis / 1000.0D);
  }

  @Override
  public Class<?> classForCoder() {
    return getClass();
  }
}