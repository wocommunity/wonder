package er.ajax.example2.util;

import java.util.Calendar;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class Year {
  private int _year;
  
  public Year(int year) {
		_year = year;
	}
  
  @Override
  public boolean equals(Object obj) {
  	Year year = (Year) obj;
  	return (year.year() == _year);
  	
  }
  
  public static Year thisYear() {
  	Calendar cal = Calendar.getInstance();
  	int year = cal.get(Calendar.YEAR);
  	return new Year(year);
  }
  
  public NSArray<Month> months() {
  	return Month.allMonths(_year);
  }
  
  public NSArray<Year> previousYears(int number) {
  	NSMutableArray<Year> years = new NSMutableArray<>();
  	for(int i = number; i >= 0; i--) {
  		years.add(new Year(_year - i));
  	}
  	
  	return years;
  }

	public int year() {
		return _year;
	}

	public void setYear(int year) {
		_year = year;
	}
}
