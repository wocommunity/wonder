package er.chronic.utils;

import java.util.Calendar;

public class Time {
  public static Calendar construct(int year, int month) {
    if (year <= 37) {
      year += 2000;
    }
    else if (year <= 137 && year >= 69) {
      year += 1900;
    }

    if (year <= 1900) {
      throw new IllegalArgumentException("Illegal year '" + year + "'");
    }
    
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1);
    return cal;
  }

  public static Calendar construct(int year, int month, int day) {
    Calendar cal = Time.construct(year, month);
    cal.set(Calendar.DAY_OF_MONTH, day);
    return cal;
  }

  public static Calendar construct(int year, int month, int day, int hour) {
    Calendar cal = Time.construct(year, month, day);
    cal.set(Calendar.HOUR_OF_DAY, hour);
    return cal;
  }

  public static Calendar construct(int year, int month, int day, int hour, int minute) {
    Calendar cal = Time.construct(year, month, day, hour);
    cal.set(Calendar.MINUTE, minute);
    return cal;
  }

  public static Calendar construct(int year, int month, int day, int hour, int minute, int second) {
    Calendar cal = Time.construct(year, month, day, hour, minute);
    cal.set(Calendar.SECOND, second);
    return cal;
  }

  public static Calendar construct(int year, int month, int day, int hour, int minute, int second, int millisecond) {
    Calendar cal = Time.construct(year, month, day, hour, minute, second);
    cal.set(Calendar.MILLISECOND, millisecond);
    return cal;
  }

  public static Calendar y(Calendar basis) {
    Calendar clone = Calendar.getInstance();
    clone.clear();
    clone.set(Calendar.YEAR, basis.get(Calendar.YEAR));
    return clone;
  }

  public static Calendar yJan1(Calendar basis) {
    Calendar clone = Time.y(basis, 1, 1);
    return clone;
  }

  public static Calendar y(Calendar basis, int month) {
    Calendar clone = Time.y(basis);
    clone.set(Calendar.MONTH, month - 1);
    return clone;
  }

  public static Calendar y(Calendar basis, int month, int day) {
    Calendar clone = Time.y(basis, month);
    clone.set(Calendar.DAY_OF_MONTH, day);
    return clone;
  }

  public static Calendar ym(Calendar basis) {
    Calendar clone = Time.y(basis);
    clone.set(Calendar.MONTH, basis.get(Calendar.MONTH));
    return clone;
  }

  public static Calendar ymd(Calendar basis) {
    Calendar clone = Time.ym(basis);
    clone.set(Calendar.DAY_OF_MONTH, basis.get(Calendar.DAY_OF_MONTH));
    return clone;
  }

  public static Calendar ymdh(Calendar basis) {
    Calendar clone = Time.ymd(basis);
    clone.set(Calendar.HOUR_OF_DAY, basis.get(Calendar.HOUR_OF_DAY));
    return clone;
  }

  public static Calendar ymdhm(Calendar basis) {
    Calendar clone = Time.ymdh(basis);
    clone.set(Calendar.MINUTE, basis.get(Calendar.MINUTE));
    return clone;
  }

  public static Calendar cloneAndAdd(Calendar basis, int field, long amount) {
    Calendar next = (Calendar) basis.clone();
    next.add(field, (int) amount);
    return next;
  }
}
