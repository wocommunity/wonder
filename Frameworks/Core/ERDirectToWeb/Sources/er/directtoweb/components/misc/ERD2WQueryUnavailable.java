package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryUnavailable;

/**
 * <span class="ja">
 * このプロパティ・レベル・コンポーネントは検索が「不可能」を表示します。
 * 
 * 検索機能が不可に設定されているか、NSData などでの検索が無意味の場合など
 * </span>
 */
public class ERD2WQueryUnavailable extends D2WQueryUnavailable {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

  public ERD2WQueryUnavailable(WOContext aContext) {
    super(aContext);
  }

}
