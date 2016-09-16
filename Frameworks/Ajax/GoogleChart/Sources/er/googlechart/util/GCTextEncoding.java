package er.googlechart.util;

import java.util.List;

import com.webobjects.foundation.NSMutableArray;

/**
 * Text encoding (see http://code.google.com/apis/chart/#text)
 * 
 * @author mschrag
 */
public class GCTextEncoding extends GCAbstractEncoding {

  @Override
  public boolean canEncode(boolean normalize, List<List<Number>> dataSets) {
    if (!normalize) {
      return false;
    }
    return true;
  }
  
  @Override
  public boolean canEncode(Number maxValue, List<List<Number>> dataSets) {
    return maxValue == null && canEncode(true, dataSets);
  }

  @Override
  protected String separator() {
    return "|";
  }

  @Override
  protected String encodingKey() {
    return "t";
  }

  @Override
  protected String _encode(Number maxValue, List<Number> dataSet) {
    NSMutableArray<String> values = new NSMutableArray<>();
    if (dataSet != null) {
      if (maxValue == null) {
        maxValue = Float.valueOf(100.0f);
      }
      for (Number number : dataSet) {
        if (number == null) {
          values.addObject("-1");
        }
        else {
          float value = number.floatValue() / maxValue.floatValue();
          values.addObject(String.format("%1$.1f", Float.valueOf(100.0f * value)));
        }
      }
    }
    return values.componentsJoinedByString(",");
  }
}