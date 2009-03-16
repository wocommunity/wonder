package er.ajax.example2.util;

import com.webobjects.foundation.NSTimestamp;

public class DateRange {
  private NSTimestamp _startDate;

  private NSTimestamp _endDate;

  public DateRange(NSTimestamp startDate, NSTimestamp endDate) {
    _startDate = startDate;
    _endDate = endDate;
  }

  public NSTimestamp startDate() {
    return _startDate;
  }

  public NSTimestamp endDate() {
    return _endDate;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (_startDate != null) {
      hashCode += _startDate.hashCode();
    }
    if (_endDate != null) {
      hashCode += _endDate.hashCode();
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DateRange && ComparisonUtils.equals(_startDate, ((DateRange) obj)._startDate) && ComparisonUtils.equals(_endDate, ((DateRange) obj)._endDate);
  }

  @Override
  public String toString() {
    return "[DateRange: " + _startDate + " - " + _endDate + "]";
  }
}
