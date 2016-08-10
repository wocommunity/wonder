package er.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.extensions.foundation.ERXStringUtilities;

/**
 * <span class="ja">
 * このコンポーネントは項目のヒントを表示します。
 * 
 * ルール又はユーザ・ディクショナリーで hint を指定します。
 * String ローカライズ・ファイルのキーワードは 'Hint.' で始まります。
 * 
 * そうすると項目の右側にメッセージが表示されます。
 * ただし、"inspect" と "edit" タスク時のみで表示される
 * 
 * @d2wKey hint - 表示するヒント
 * </span>
 * 
 * @author ishimoto
 */
public class ERDDisplayHint extends ERD2WStatelessComponent {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

  public ERDDisplayHint(WOContext aContext) {
    super(aContext);
  }

  public boolean displayHint() {
    D2WContext d2w = (D2WContext) valueForBinding("d2wContext");   
    String task = d2w.task();

    if("inspect".equals(task) || "edit".equals(task)) {
      return true;
    }
    return false;
  }

  public String stringForHint() {
    D2WContext d2w = (D2WContext) valueForBinding("d2wContext");

    StringBuilder sb = new StringBuilder();
    sb.append("Hint.");

    Object o = d2w.valueForKey("hint");
    if(ERXStringUtilities.stringIsNullOrEmpty(String.valueOf(o))) {
      sb.append("SampleText");
    } else {
      sb.append(String.valueOf(o));
    }    
    return sb.toString();
  }

}
