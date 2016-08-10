package er.attachment.metadata;

/**
 * <span class="en">
 * ERMetadataEntry represents a single entry in a metadata directory.
 * </span>
 * 
 * <span class="ja">
 * ERMetadataEntry はメタデータ・ディレクトリ内のシングル・エントリーを表現します。
 * </span>
 * 
 * @author mschrag
 */
public class ERMetadataEntry {
  private int _type;

  private String _name;

  private Object _value;
  
  private Class _dataType;

  /**
   * <span class="en">
   * Constructs a new ERMetadataEntry.
   * 
   * @param type the directory-specific identifier for the entry
   * @param name the directory-specific name of the entry
   * @param value the value of the entry
   * @param dataType the dataType of the entry
   * </span>
   * 
   * <span class="ja">
	 * ERMetadataEntry を作成します。
	 * 
	 * @param type - エントリーのディレクトリ認識タイプ
	 * @param name - エントリーのディレクトリ認識名
	 * @param value - エントリーの値
	 * @param dataType - エントリーのデータ・タイプ
	 * </span>
   */
  public ERMetadataEntry(int type, String name, Object value, Class dataType) {
    _type = type;
    _name = name;
    _value = value;
    _dataType = dataType;
  }

  /**
   * <span class="en">
   * Returns the data type of the entry.
   * 
   * @return the data type of the entry
   * </span>
   * 
   * <span class="ja">
	 * エントリーのデータ・タイプを戻します。
	 * 
	 * @return エントリーのデータ・タイプ
	 * </span>
   */
  public Class getDataType() {
    return _dataType;
  }
  
  /**
   * <span class="en">
   * Returns the directory-specific identifier for the entry.
   * 
   * @return the directory-specific identifier for the entry
   * </span>
   * 
   * <span class="ja">
	 * エントリーのディレクトリ認識タイプを戻します。
	 * 
	 * @return エントリーのディレクトリ認識タイプ
	 * </span>
   */
  public int getType() {
    return _type;
  }

  /**
   * <span class="en">
   * Returns the directory-specific name for the entry.
   * 
   * @return the directory-specific name for the entry
   * </span>
   * 
   * <span class="ja">
	 * エントリーのディレクトリ認識名を戻します。
	 * 
	 * @return エントリーのディレクトリ認識名
	 * </span>
   */
  public String getName() {
    return _name;
  }

  /**
   * <span class="en">
   * Returns the value of the entry.
   * 
   * @return the value of the entry
   * </span>
   * 
   * <span class="ja">
	 * エントリーの値を戻します。
	 * 
	 * @return エントリーの値
	 * </span>
   */
  public Object getValue() {
    return _value;
  }

  /**
   * <span class="en">
   * Returns the string value of the entry.
   * 
   * @return the string value of the entry
   * </span>
   * 
   * <span class="ja">
	 * エントリーの値を String として戻します。
	 * 
	 * @return エントリーの値を String として
	 * </span>
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
   * <span class="en">
   * Returns the int value of the entry.
   * 
   * @param firstWord if true, only the first word is converted to an int
   * 
   * @return the int value of the entry
   * </span>
   * 
   * <span class="ja">
	 * エントリーの値を int として戻します。
	 * 
	 * @param firstWord - true の場合、最初のワードのみが変換される
	 * 
	 * @return エントリーの値を int として
	 * </span>
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
   * <span class="en">
   * Returns whether or not this metadata entry is valid (// MS ... I think this is cruft leftover from the original impl).
   * 
   * @return whether or not this metadata entry is valid
   * </span>
   * 
   * <span class="ja">
	 * メタデータ・エントリーが有効かどうかをチェックします。
	 * 
	 * @return メタデータ・エントリーが有効かどうかをチェックします
	 * </span>
   */
  public boolean isValid() {
    return (_value != null && getStringValue().length() < 1024);
  }

  @Override
  public String toString() {
    return "[MetadataEntry: type = " + _type + "; name = " + _name + "; value = " + _value + "]";
  }
  
	/**
	 * <span class="ja">
	 * メタデータ・エントリーが有効かどうかをチェックします。
	 * 
	 * @param entry - チェックするエントリー
	 * 
	 * @return　メタデータ・エントリーが有効かどうかをチェックします
	 * </span>
	 */
  public static boolean isValid(ERMetadataEntry entry) {
    return entry != null && entry.isValid();
  }
}