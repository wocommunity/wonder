package er.extensions.components.html5;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * <span class="en">
 * 
 * </span>
 * 
 * <span class="ja">
 * type = "email" - メールアドレスの入力欄を作成する (*5)
 * name属性 - フォーム部品に名前をつける
 * value属性 - 送信される値を指定する
 * size属性 - 表示文字数を指定（1以上の正の整数）
 * maxlength属性 - 入力できる最大文字数を指定する
 * pattern属性 - 正規表現で入力値のパターンを指定する (*5)
 * placeholder属性 - 入力欄に初期表示する内容を指定する (*5)
 * readonly属性 - ユーザーによるテキスト編集を不可にして読み取り専用にする
 * required属性 - 入力必須にする (*5)
 * </span>
 * 
 * @author ishimoto
 */
public class ERXWOEmailField extends ERXWOInput {

  public final String EMAIL_PATTERN ="^[0-9a-zA-Z]+[\\w-]+@[\\w\\.-]+\\.\\w{2,}$";

  //********************************************************************
  //  Binding Properties
  //********************************************************************

  protected WOAssociation _size;
  protected WOAssociation _maxlength;
  protected WOAssociation _pattern;
  protected WOAssociation _placeholder;

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERXWOEmailField(String tagname, NSDictionary<String, WOAssociation> nsdictionary, WOElement woelement) {
    super("input", nsdictionary, woelement);

    _size = _associations.removeObjectForKey("size");
    _maxlength = _associations.removeObjectForKey("maxlength");

    _pattern = _associations.removeObjectForKey("pattern");
    _placeholder = _associations.removeObjectForKey("placeholder");
  }

  @Override
  public String type() {
    return "email";
  }

  @Override
  protected void _appendValueAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
    WOComponent component = wocontext.component();

    Object valueInComponent = _value.valueInComponent(component);
    if(valueInComponent != null) {
      String stringValue = valueInComponent.toString();
      woresponse._appendTagAttributeAndValue("value", stringValue, true);
    }

    if(_size != null) {
      Object sizeInComponent = _size.valueInComponent(component);
      if(sizeInComponent != null) {
        String stringValue = sizeInComponent.toString();
        woresponse._appendTagAttributeAndValue("size", stringValue, true);
      }     
    }

    if(_maxlength != null) {
      Object maxlengthInComponent = _maxlength.valueInComponent(component);
      if(maxlengthInComponent != null) {
        String stringValue = maxlengthInComponent.toString();
        woresponse._appendTagAttributeAndValue("maxlength", stringValue, true);
      }     
    }

    if(_placeholder != null) {
      Object placeholderInComponent = _placeholder.valueInComponent(component);
      if(placeholderInComponent != null) {
        String stringValue = placeholderInComponent.toString();
        woresponse._appendTagAttributeAndValue("placeholder", stringValue, true);
      }     
    }

    if(_pattern != null) {
      Object patternInComponent = _pattern.valueInComponent(component);
      if(patternInComponent != null) {
        String stringValue = patternInComponent.toString();
        woresponse._appendTagAttributeAndValue("pattern", stringValue, true);
      }     
    } else {
      woresponse._appendTagAttributeAndValue("pattern", EMAIL_PATTERN, true);
    }

    if (isRequiredInContext(wocontext)) {
      woresponse._appendTagAttributeAndValue("required", "required", false);
    }

    if (isReadonlyInContext(wocontext)) {
      woresponse._appendTagAttributeAndValue("readonly", "readonly", false);
    }
  }

  @Override
  public String toString() {
    StringBuilder stringbuffer = new StringBuilder();
    stringbuffer.append('<');
    stringbuffer.append(getClass().getName());
    stringbuffer.append(" placeholder=");
    stringbuffer.append(_placeholder);
    stringbuffer.append(" pattern=");
    stringbuffer.append(_pattern);
    stringbuffer.append(" size=");
    stringbuffer.append(_size);
    stringbuffer.append(" maxlength=");
    stringbuffer.append(_maxlength);
    stringbuffer.append('>');
    return stringbuffer.toString();
  }
}