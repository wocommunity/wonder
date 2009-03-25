package er.attachment.metadata;

/**
 * ERMetadataEntry represents a single entry in a metadata directory.
 * 
 * @author mschrag
 */
public class ERMetadataEntry {
  private int _type;

  private String _name;

  private Object _value;
  
  private Class _dataType;

  /**
   * Constructs a new ERMetadataEntry.
   * 
   * @param type the directory-specific identifier for the entry
   * @param name the directory-specific name of the entry
   * @param value the value of the entry
   * @param dataType the dataType of the entry
   */
  public ERMetadataEntry(int type, String name, Object value, Class dataType) {
    _type = type;
    _name = name;
    _value = value;
    _dataType = dataType;
  }

  /**
   * Returns the data type of the entry.
   * 
   * @return the data type of the entry
   */
  public Class getDataType() {
    return _dataType;
  }
  
  /**
   * Returns the directory-specific identifier for the entry.
   * 
   * @return the directory-specific identifier for the entry
   */
  public int getType() {
    return _type;
  }

  /**
   * Returns the directory-specific name for the entry.
   * 
   * @return the directory-specific name for the entry
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the value of the entry.
   * 
   * @return the value of the entry
   */
  public Object getValue() {
    return _value;
  }

  /**
   * Returns the string value of the entry.
   * 
   * @return the string value of the entry
   */
  public String getStringValue() {
    String value;
    if (_value instanceof String) {
      value = (String)_value;
    }
    else {
      value = String.valueOf(_value);
    }
    return value.trim();
  }

  /**
   * Returns the int value of the entry.
   * 
   * @param firstWord if true, only the first word is converted to an int
   * @return the int value of the entry
   */
  public int getIntValue(boolean firstWord) {
    int intValue;
    if (_value instanceof Number) {
      intValue = ((Number)_value).intValue();
    }
    else if (_value instanceof String) {
      String valueStr = (String)_value;
      if (firstWord) {
        int spaceIndex = valueStr.indexOf(' ');
        if (spaceIndex != -1) {
          valueStr = valueStr.substring(0, spaceIndex);
        }
      }
      intValue = Integer.parseInt(valueStr);
    }
    else {
      throw new IllegalArgumentException("Unable to convert value type " + _value + " to an int.");
    }
    return intValue;
  }

  /**
   * Returns whether or not this metadata entry is valid (// MS ... I think this is cruft leftover from the original impl).
   * 
   * @return whether or not this metadata entry is valid
   */
  public boolean isValid() {
    return (_value != null && getStringValue().length() < 1024);
  }

  @Override
  public String toString() {
    return "[MetadataEntry: type = " + _type + "; name = " + _name + "; value = " + _value + "]";
  }
  
  public static boolean isValid(ERMetadataEntry entry) {
    return entry != null && entry.isValid();
  }
}