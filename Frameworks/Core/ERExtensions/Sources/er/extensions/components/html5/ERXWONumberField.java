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
 * type = "number" - 数値の入力欄を作成する (*5)
 * autofocus属性 - フォーム・コントロールのオートフォーカス (*5)
 * name属性 - フォーム部品に名前をつける
 * value属性 - 送信される値を指定する
 * max属性 - 入力できる最大値を指定する (*5)
 * min属性 - 入力できる最小値を指定する (*5)
 * readonly属性 - ユーザーによるテキスト編集を不可にして読み取り専用にする
 * required属性 - 入力必須にする (*5)
 * step属性 - 入力欄で刻むステップ値を指定する（type="number"の場合、初期値は1） (*5)
 * </span>
 * 
 * @author ishimoto
 */
public class ERXWONumberField extends ERXWOInput {

  //********************************************************************
  //  Binding Properties
  //********************************************************************

  protected WOAssociation _min;
  protected WOAssociation _max;
  protected WOAssociation _step;
  protected WOAssociation _autofocus;

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERXWONumberField(String tagname, NSDictionary<String, WOAssociation> nsdictionary, WOElement woelement) {
    super("input", nsdictionary, woelement);

    _min = _associations.removeObjectForKey("min");
    _max = _associations.removeObjectForKey("max");
    _step = _associations.removeObjectForKey("step");

    _autofocus = _associations.removeObjectForKey("autofocus");
  }

  @Override
  public String type() {
    return "number";
  }

  protected boolean isAutofocusInContext(WOContext context) {
    return _autofocus != null && _autofocus.booleanValueInComponent(context.component());
  }

  @Override
  protected void _appendValueAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
    WOComponent component = wocontext.component();

    Object valueInComponent = _value.valueInComponent(component);
    if(valueInComponent != null) {
      String stringValue = valueInComponent.toString();
      woresponse._appendTagAttributeAndValue("value", stringValue, true);
    }

    if(_min != null) {
      Object minInComponent = _min.valueInComponent(component);
      if(minInComponent != null) {
        String stringValue = minInComponent.toString();
        woresponse._appendTagAttributeAndValue("min", stringValue, true);
      }     
    }

    if(_max != null) {
      Object maxInComponent = _max.valueInComponent(component);
      if(maxInComponent != null) {
        String stringValue = maxInComponent.toString();
        woresponse._appendTagAttributeAndValue("max", stringValue, true);
      }
    }

    if(_step != null) {
      Object stepInComponent = _step.valueInComponent(component);
      if(stepInComponent != null) {
        String stringValue = stepInComponent.toString();
        woresponse._appendTagAttributeAndValue("step", stringValue, true);
      }
    }   

    if (isAutofocusInContext(wocontext)) {
      woresponse._appendTagAttributeAndValue("autofocus", "autofocus", false);
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
    stringbuffer.append(" min=");
    stringbuffer.append(_min);
    stringbuffer.append(" max=");
    stringbuffer.append(_max);
    stringbuffer.append(" step=");
    stringbuffer.append(_step);
    stringbuffer.append('>');
    return stringbuffer.toString();
  }
}