package er.chronic.utils;

import java.util.Calendar;

public class Time {
  public static Calendar construct(int year) {
    return Time.construct(year, 1);
  }
  
  public static Calendar construct(int year, int month) {
    if (year <= 37) {
      year += 2000;
    }
    // MS: windowing seems to leave out >= 38 and went straight to 69? odd ... we're switching back
    //else if (year <= 137 && year >= 69) {
    else if (year <= 137 && year >= 38) {
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

  public static Calendar cloneAndAdd(Calendar basis, int field, float amount) {
    Calendar next = (Calendar) basis.clone();
//    next.add(field, (int) amount);
    double amountFloor = Math.floor(amount);
    if (amountFloor == amount) {
      next.add(field, (int) amount);
    }
    else if (field == Calendar.YEAR) {
      double remainder = amount - amountFloor;
      next.add(Calendar.YEAR, (int)amountFloor);
      // MS: This is going to break on leap days and 31 day months (well, I guess? what does 3.5 years mean in the general case?)
      next.add(Calendar.SECOND, (int)(remainder * 365 * 24 * 60 * 60));
    }
    else if (field == Calendar.MONTH) {
      double remainder = amount - amountFloor;
      next.add(Calendar.MONTH, (int)amountFloor);
      // MS: This is going to break on leap days and 31 day months (well, I guess? what does 3.5 months mean in the general case?)
      next.add(Calendar.SECOND, (int)(remainder * 30 * 24 * 60 * 60));
    }
    else if (field == Calendar.DAY_OF_MONTH) {
      double remainder = amount - amountFloor;
      next.add(Calendar.DAY_OF_MONTH, (int)amountFloor);
      // MS: This is going to break on leap days
      next.add(Calendar.SECOND, (int)(remainder * 24 * 60 * 60));
    }
    else if (field == Calendar.HOUR) {
      next.add(Calendar.MILLISECOND, (int)(amount * 60 * 60 * 1000));
    }
    else if (field == Calendar.MINUTE) {
      next.add(Calendar.MILLISECOND, (int)(amount * 60 * 1000));
    }
    else if (field == Calendar.SECOND) {
      next.add(Calendar.MILLISECOND, (int)(amount * 1000));
    }
    else if (field == Calendar.MILLISECOND) {
      throw new IllegalArgumentException("Fractional milliseconds (" + amount + ") are not supported.");
    }
    return next;
  }
}
