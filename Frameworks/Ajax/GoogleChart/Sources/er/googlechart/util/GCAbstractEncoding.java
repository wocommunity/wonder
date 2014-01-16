package er.googlechart.util;

import java.util.List;

import com.webobjects.foundation.NSMutableArray;

/**
 * The superclass of all encodings.
 * 
 * @author mschrag
 */
public abstract class GCAbstractEncoding {
  public String encode(List<List<Number>> dataSets) {
    return encode(true, dataSets);
  }

  public String encode(boolean normalize, List<List<Number>> dataSets) {
    Float maxValue = null;
    if (normalize) {
      maxValue = Float.valueOf(GCEncoding.maxValueInLists(dataSets));
    }
    return encode(maxValue, dataSets);
  }

  public String encode(Number maxValue, List<List<Number>> dataSets) {
    StringBuilder sb = new StringBuilder();
    sb.append(encodingKey());
    sb.append(':');
    if (dataSets != null && !dataSets.isEmpty()) {
      NSMutableArray<String> encodedDataSets = new NSMutableArray<String>();
      for (List<Number> dataSet : dataSets) {
        encodedDataSets.addObject(_encode(maxValue, dataSet));
      }
      sb.append(encodedDataSets.componentsJoinedByString(separator()));
    }
    return sb.toString();
  }

  public abstract boolean canEncode(boolean normalize, List<List<Number>> dataSets);

  public abstract boolean canEncode(Number maxValue, List<List<Number>> dataSets);

  protected abstract String encodingKey();

  protected abstract String _encode(Number maxValue, List<Number> dataSet);

  protected abstract String separator();
}