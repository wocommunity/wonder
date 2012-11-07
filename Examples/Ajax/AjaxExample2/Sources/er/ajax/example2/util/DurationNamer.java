package er.ajax.example2.util;

import java.util.Calendar;

import com.webobjects.foundation.NSTimestamp;

import er.ajax.example2.helper.StringHelper;

public class DurationNamer {
  public static String timeFrom(NSTimestamp time, NSTimestamp baseTime) {
	StringBuilder results = new StringBuilder();
    StringHelper helper = new StringHelper();

    int years = DateUtils.difference(time, baseTime, Calendar.YEAR) - 1;
    int months;
    int days;
    int hours;
    int minutes;

    if (years > 1) {
      String year_label = helper.pluralize("year", years);
      results.append(years + " " + year_label);
    }
    else {
      months = DateUtils.difference(time, baseTime, Calendar.MONTH) - 1;
      if (months > 11) {
        results.append("about 1 year ago");
      }
      else if (months > 1) {
        String month_label = helper.pluralize("month", months);
        results.append(months + " " + month_label);
      }
      else {
        days = DateUtils.difference(time, baseTime, Calendar.DAY_OF_YEAR) - 1;
        if (days > 25) {
          results.append("about 1 month");
        }
        else if (days > 7) {
          int weeks = days / 7;
          String weeks_label = helper.pluralize("week", weeks);
          results.append(weeks + " " + weeks_label);
        }
        else if (days > 1) {
          if (days <= 2) {
            hours = DateUtils.difference(time, baseTime, Calendar.HOUR_OF_DAY) - 1;
            if (hours < 30) {
              results.append("about 1 day");
            }
            else {
              String days_label = helper.pluralize("day", days);
              results.append(days + " " + days_label);
            }
          }
          else {
            String days_label = helper.pluralize("day", days);
            results.append(days + " " + days_label);
          }
        }
        else {
          hours = DateUtils.difference(time, baseTime, Calendar.HOUR_OF_DAY) - 1;
          if (hours >= 20) {
            results.append("about 1 day");
          }
          else if (hours > 1) {
            String hours_label = helper.pluralize("hour", hours);
            results.append(hours + " " + hours_label);
          }
          else {
            minutes = DateUtils.difference(time, baseTime, Calendar.MINUTE) - 1;
            if (minutes >= 45) {
              results.append("about an hour");
            }
            else if (minutes > 20 && minutes < 45) {
              results.append("about half an hour");
            }
            else if (minutes > 1) {
              String minutes_label = helper.pluralize("minute", minutes);
              results.append(minutes + " " + minutes_label);
            }
            else {
              return "this very minute";
            }
          }
        }
      }
    }

    if (baseTime.after(time)) {
      results.append(" ago");
    }
    else if (baseTime.before(time)) {
      results.append(" from now");
    }
    else {
      results.append("now");
    }
    
    return results.toString();
  }

  public static String timeFromNow(NSTimestamp time) {
    return timeFrom(time, new NSTimestamp());
  }
}
