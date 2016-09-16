package er.googlechart.util;

import java.util.LinkedList;
import java.util.List;

import com.webobjects.foundation.NSKeyValueCoding;

/**
 * Utility methods for encoding.
 * 
 * @author mschrag
 */
public class GCEncoding {
  private static GCAbstractEncoding[] ENCODINGS = { new GCSimpleEncoding(), new GCExtendedEncoding(), new GCTextEncoding() };

  public static GCAbstractEncoding recommendedEncoding(boolean normalize, List<List<Number>> dataSets) {
    for (GCAbstractEncoding encoding : GCEncoding.ENCODINGS) {
      if (encoding.canEncode(normalize, dataSets)) {
        return encoding;
      }
    }
    throw new IllegalArgumentException("There was no encoder capable of encoding the given data sets.");
  }

  public static GCAbstractEncoding recommendedEncoding(Number maxValue, List<List<Number>> dataSets) {
    for (GCAbstractEncoding encoding : GCEncoding.ENCODINGS) {
      if (encoding.canEncode(maxValue, dataSets)) {
        return encoding;
      }
    }
    throw new IllegalArgumentException("There was no encoder capable of encoding the given data sets.");
  }

  protected static List<Number> _convertToNumberList(List dataSet) {
    List<Number> numbers = new LinkedList<>();
    for (Object datum : dataSet) {
      Number number = GCEncoding.numberFromObject(datum);
      numbers.add(number);
    }
    return numbers;
  }

  @SuppressWarnings("unchecked")
  public static List<List<Number>> convertToNumberLists(List dataSets) {
    List<List> outerList;
    if (dataSets == null || (!dataSets.isEmpty() && dataSets.get(0) instanceof List)) {
      outerList = dataSets;
    }
    else if (!dataSets.isEmpty() && dataSets.get(0).getClass().isArray()) {
      outerList = new LinkedList<>();
      for (Object obj : dataSets) {
        Object[] innerObjects = (Object[]) obj;
        List innerList = new LinkedList();
        for (Object innerObj : innerObjects) {
          innerList.add(innerObj);
        }
        outerList.add(innerList);
      }
    }
    else {
      outerList = new LinkedList<>();
      outerList.add(dataSets);
    }

    List<List<Number>> numberLists = new LinkedList<List<Number>>();
    if (outerList != null) {
      for (List dataSet : outerList) {
        numberLists.add(GCEncoding._convertToNumberList(dataSet));
      }
    }
    return numberLists;
  }

  public static Number numberFromObject(Object datum) {
    Number number;
    if (datum instanceof Number) {
      number = (Number) datum;
    }
    else if (datum instanceof String) {
      number = Integer.parseInt((String) datum);
    }
    else if (datum instanceof Boolean) {
      number = null;
    }
    else if (datum instanceof NSKeyValueCoding.Null) {
      number = null;
    }
    else {
      throw new IllegalArgumentException("Unable to convert " + datum + " to a number.");
    }
    return number;
  }

  public static boolean hasDecimalInList(List<Number> dataSet) {
    boolean hasDecimals = false;
    if (dataSet != null) {
      for (Number datum : dataSet) {
        if (datum instanceof Float || datum instanceof Double) {
          hasDecimals = true;
          break;
        }
      }
    }
    return hasDecimals;
  }

  public static boolean hasDecimalInLists(List<List<Number>> dataSets) {
    boolean hasDecimals = false;
    for (List<Number> dataSet : dataSets) {
      hasDecimals = GCEncoding.hasDecimalInList(dataSet);
      if (hasDecimals) {
        break;
      }
    }
    return hasDecimals;
  }

  public static float maxValueInList(List<Number> dataSet) {
    float maxValue = 0;
    if (dataSet != null) {
      for (Number datum : dataSet) {
        if (datum != null) {
          maxValue = Math.max(maxValue, datum.floatValue());
        }
      }
    }
    return maxValue;
  }

  public static float maxValueInLists(List<List<Number>> dataSets) {
    float maxValue = 0;
    for (List<Number> dataSet : dataSets) {
      maxValue = Math.max(maxValue, GCEncoding.maxValueInList(dataSet));
    }
    return maxValue;
  }

  public static float minValueInList(List<Number> dataSet) {
    float minValue = 0;
    if (dataSet != null) {
      for (Number datum : dataSet) {
        if (datum != null) {
          minValue = Math.min(minValue, datum.floatValue());
        }
      }
    }
    return minValue;
  }

  public static float minValueInLists(List<List<Number>> dataSets) {
    float minValue = 0;
    for (List<Number> dataSet : dataSets) {
      minValue = Math.max(minValue, GCEncoding.minValueInList(dataSet));
    }
    return minValue;
  }
}
