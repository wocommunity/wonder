package er.chronic.utils;

import java.util.List;

public class Range {
  private long _begin;
  private long _end;

  public Range(long begin, long end) {
    _begin = begin;
    _end = end;
  }

  public long getBegin() {
    return _begin;
  }

  public long getEnd() {
    return _end;
  }

  public long getWidth() {
    return getEnd() - getBegin();
  }

  /**
   * Returns true if the start and end are the same (i.e. this is a single value).
   */
  public boolean isSingularity() {
    return getEnd() == getBegin();
  }

  public boolean contains(long value) {
    return _begin <= value && _end >= value;
  }

  @Override
  public int hashCode() {
    return (int) (_begin * _end);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Range && ((Range) obj)._begin == _begin && ((Range) obj)._end == _end;
  }

  public <T> List<T> subList(List<T> list) {
    return list.subList((int) _begin, (int) _end);
  }
  
  @Override
  public String toString() {
    return "[Range: " + _begin + "-" + _end + "]";
  }
}
