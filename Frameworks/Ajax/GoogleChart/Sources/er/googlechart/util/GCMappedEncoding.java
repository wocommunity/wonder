package er.googlechart.util;

import java.util.List;

/**
 * The superclass of simple and extended encodings.
 * 
 * @author mschrag
 */
public abstract class GCMappedEncoding extends GCAbstractEncoding {
  @Override
  public boolean canEncode(boolean normalize, List<List<Number>> dataSets) {
    boolean canEncode = true;
    boolean hasDecimals = GCEncoding.hasDecimalInLists(dataSets);
    if (hasDecimals) {
      canEncode = false;
    }
    else {
      int maxValue = (int) GCEncoding.maxValueInLists(dataSets);
      canEncode = maxValue < numberOfEncodingValues();
    }
    return canEncode;
  }
  
  @Override
  public boolean canEncode(Number maxValue, List<List<Number>> dataSets) {
    return canEncode(true, dataSets);
  }

  protected abstract String encode(int value);

  protected abstract int numberOfEncodingValues();

  protected abstract String missingValue();

  @Override
  protected String separator() {
    return ",";
  }

  @Override
  protected String _encode(Number maxValue, List<Number> dataSet) {
    StringBuilder sb = new StringBuilder();
    if (dataSet != null) {
      int numberOfEncodingValues = numberOfEncodingValues();
      for (Number number : dataSet) {
        if (number == null) {
          sb.append(missingValue());
        }
        else if (number.intValue() < 0) {
          throw new IllegalArgumentException("The negative number " + number + " is not allowed in this encoding.");
        }
        else {
          int value;
          if (maxValue == null || numberOfEncodingValues == Integer.MAX_VALUE) {
            value = number.intValue();
          }
          else {
            if (maxValue != null && number.intValue() > maxValue.intValue()) {
              throw new IllegalArgumentException("The value " + number + " is greater than the provided max value " + maxValue + ".");
            }
            value = Math.round((numberOfEncodingValues - 1) * number.floatValue() / maxValue.floatValue());
          }
          sb.append(encode(value));
        }
      }
    }
    return sb.toString();
  }
}