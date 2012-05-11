package er.extensions.components.html5;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOInput;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXPatcher;

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
public class ERXWONumberField extends WOInput {

  //********************************************************************
  //  Binding Properties
  //********************************************************************

  protected WOAssociation _min;
  protected WOAssociation _max;
  protected WOAssociation _step;
  protected WOAssociation _readonly;
  protected WOAssociation _required;
  protected WOAssociation _autofocus;
  protected WOAssociation _blankIsNull;

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERXWONumberField(String tagname, NSDictionary<String, WOAssociation> nsdictionary, WOElement woelement) {
    super("input", nsdictionary, woelement);
    if(_value == null || !_value.isValueSettable())
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'value' attribute not present or is a constant");

    _min = _associations.removeObjectForKey("min");
    _max = _associations.removeObjectForKey("max");
    _step = _associations.removeObjectForKey("step");

    _readonly = _associations.removeObjectForKey("readonly");
    _required = _associations.removeObjectForKey("required");
    _autofocus = _associations.removeObjectForKey("autofocus");

    _blankIsNull = _associations.removeObjectForKey("blankIsNull");
  }

  @Override
  public String type() {
    return "number";
  }

  @Override
  protected boolean isDisabledInContext(WOContext context) {
    WOAssociation disabled = (WOAssociation) ERXKeyValueCodingUtilities.privateValueForKey(this, "_disabled");
    return disabled != null && disabled.booleanValueInComponent(context.component());
  }

  protected boolean isReadonlyInContext(WOContext context) {
    return _readonly != null && _readonly.booleanValueInComponent(context.component());
  }

  protected boolean isRequiredInContext(WOContext context) {
    return _required != null && _required.booleanValueInComponent(context.component());
  }

  protected boolean isAutofocusInContext(WOContext context) {
    return _autofocus != null && _autofocus.booleanValueInComponent(context.component());
  }

  @Override
  public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
    WOComponent component = wocontext.component();
    if(!isDisabledInContext(wocontext) && wocontext.wasFormSubmitted() && !isReadonlyInContext(wocontext)) {
      String name = nameInContext(wocontext, component);
      if(name != null) {
        String stringValue;
        boolean blankIsNull = _blankIsNull == null || _blankIsNull.booleanValueInComponent(component);
        if (blankIsNull) {
          stringValue = worequest.stringFormValueForKey(name);
        }
        else {
          Object objValue = worequest.formValueForKey(name);
          stringValue = (objValue == null) ? null : objValue.toString();
        }
        Object result = stringValue;
        _value.setValue(result, component);
      }
    }
  }

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

  protected void _appendCloseTagToResponse(WOResponse woresponse, WOContext wocontext) {
  }

  @Override
  public String toString() {
    StringBuffer stringbuffer = new StringBuffer();
    stringbuffer.append("<");
    stringbuffer.append(getClass().getName());
    stringbuffer.append(" min=" + _min);
    stringbuffer.append(" max=" + _max);
    stringbuffer.append(" step=" + _step);
    stringbuffer.append(">");
    return stringbuffer.toString();
  }

  /**
   * <span class="ja">
   * XML 互換性の為にオーバライド
   * </span>
   */
  @Override
  public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
    WOResponse newResponse = ERXPatcher.DynamicElementsPatches.cleanupXHTML ? new WOResponse() : woresponse;
    super.appendToResponse(newResponse, wocontext);

    ERXPatcher.DynamicElementsPatches.processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
    if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
      woresponse.appendContentString(newResponse.contentString());
    }
  }
}