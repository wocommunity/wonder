package er.ajax.example2.helper;

import java.util.Calendar;

import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.ajax.example2.util.DateUtils;

public class NSTimestampHelper {
  public String format(NSTimestamp time) {
    return format(time, true);
  }
  
	public String format(NSTimestamp time, boolean useTimes) {
		if (time != null) {
		  NSTimestampFormatter formatDateTime;
		  if (useTimes) {
		    formatDateTime = new NSTimestampFormatter("%m/%d/%Y %I:%M %p");
		  }
		  else {
        formatDateTime = new NSTimestampFormatter("%m/%d/%Y");
		  }
			return formatDateTime.format(time);
		}
		return null;
	}

	public String mdy(NSTimestamp time) {
		if(time != null) {
			NSTimestampFormatter formatDateTime = new NSTimestampFormatter("%m/%d/%Y");
			return formatDateTime.format(time);
		}
		return null;
	}
	
	public String ago(NSTimestamp time) {
		if(time != null) {
			return DateUtils.timeFromNow(time);
		}
		return "";
	}
	
	public String timeOnly(NSTimestamp time) {
		if (time != null) {
			NSTimestampFormatter formatDateTime = new NSTimestampFormatter("%I:%M%p");
			String results = formatDateTime.format(time);
			results = results.toLowerCase();
			
			if(results.charAt(0) == '0') {
				results = results.subSequence(1, results.length()).toString();
			}	
			return results;
		}
		return null;
	}
	
	public String withDayName(NSTimestamp date, boolean includeTime) {
    return withDayName(date, includeTime, true);
  }
	
  public String withDayName(NSTimestamp date, boolean includeTime, boolean includeYear) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    StringBuilder buffer = new StringBuilder();
    
    buffer.append(DateUtils.DAYS_OF_WEEK[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
    buffer.append(", ");
    
    buffer.append(DateUtils.MONTHS[calendar.get(Calendar.MONTH)]);
    buffer.append(" ");
    
    buffer.append(calendar.get(Calendar.DATE));

    
    if(includeYear) {
      buffer.append(" ");    	
    	buffer.append(calendar.get(Calendar.YEAR));
    	buffer.append(" ");
    } 
    
    if(includeTime) {
    	int hour = calendar.get(Calendar.HOUR);
    	if(hour == 0) hour = 12;
      buffer.append(hour);
      buffer.append(":");
      
      int minute = calendar.get(Calendar.MINUTE);
      buffer.append(String.format("%02d", Integer.valueOf(minute)));
      buffer.append("");
      
      if(calendar.get(Calendar.AM_PM) == Calendar.PM) {
      	buffer.append("pm");
      } else {
      	buffer.append("am");
      }
    }
    
    return buffer.toString();
  }

  public String shortDateWithDayName(NSTimestamp date, boolean includeYear) {
  	if(includeYear) {
  		return DateUtils.SHORT_MONTH_NAME_AND_DAY_OF_WEEK_AND_YEAR_FORMATTER.format(date);
  	}
		return DateUtils.SHORT_MONTH_NAME_AND_DAY_OF_WEEK_FORMATTER.format(date);
  }
  
  public String withMonthName(NSTimestamp date, boolean useTimes) {
  	if(date == null) { return null; }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    StringBuilder buffer = new StringBuilder();
    
    buffer.append(DateUtils.MONTHS[calendar.get(Calendar.MONTH)]);
    buffer.append(" ");
    
    buffer.append(calendar.get(Calendar.DATE));

    
    buffer.append(", ");    	
  	buffer.append(calendar.get(Calendar.YEAR));
  	buffer.append(" ");
  
  	if(useTimes) {
	  	int hour = calendar.get(Calendar.HOUR);
	  	if(hour == 0) hour = 12;
	    buffer.append(hour);
	    buffer.append(":");
	    
	    int minute = calendar.get(Calendar.MINUTE);
	    buffer.append(String.format("%02d", Integer.valueOf(minute)));
	    buffer.append("");
	    
	    if(calendar.get(Calendar.AM_PM) == Calendar.PM) {
	    	buffer.append(" PM");
	    } else {
	    	buffer.append(" AM");
	    }
  	}
    return buffer.toString();
  }  
}
