package er.extensions.enums;

import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * er.extensions.enums.ERXContinentEnums
 */
public enum ERXContinentEnums {

  Africa, Antarctica, Asia, Australia, Europe, NorthAmerica, SouthAmerica;

  /** 
   * <span class="ja">翻訳できる完全名</span>
   */
  public String fullName() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(NSKeyValueCodingAdditions.KeyPathSeparator);
    sb.append(name());
    return sb.toString();
  }

}
