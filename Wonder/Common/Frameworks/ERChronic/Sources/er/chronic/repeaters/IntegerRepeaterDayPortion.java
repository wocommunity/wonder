package er.chronic.repeaters;

import er.chronic.utils.Range;

public class IntegerRepeaterDayPortion extends RepeaterDayPortion<Integer> {
  public IntegerRepeaterDayPortion(Integer type) {
    super(type);
  }

  @Override
  protected Range createRange(Integer type) {
    Range range = new Range(type.intValue() * 60 * 60, (type.intValue() + 12) * 60 * 60);
    return range;
  }

  @Override
  protected int _getWidth(Range range) {
    int width = (12 * 60 * 60);
    return width;
  }
}
