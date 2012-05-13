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
 * type = "url" - URLの入力欄を作成する (*5)
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
public class ERXWOUrlField extends WOInput {

  public final String URL_PATTERN ="^(http|https)://[0-9A-Za-z/#&?%\\.\\-\\+_=]+$";
  
  //********************************************************************
  //  Binding Properties
  //********************************************************************

  protected WOAssociation _size;
  protected WOAssociation _maxlength;
  protected WOAssociation _pattern;
  protected WOAssociation _placeholder;
  protected WOAssociation _readonly;
  protected WOAssociation _required;
  protected WOAssociation _blankIsNull;

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERXWOUrlField(String tagname, NSDictionary<String, WOAssociation> nsdictionary, WOElement woelement) {
    super("input", nsdictionary, woelement);
    if(_value == null || !_value.isValueSettable())
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'value' attribute not present or is a constant");

    _size = _associations.removeObjectForKey("size");
    _maxlength = _associations.removeObjectForKey("maxlength");

    _pattern = _associations.removeObjectForKey("pattern");
    _placeholder = _associations.removeObjectForKey("placeholder");

    _readonly = _associations.removeObjectForKey("readonly");
    _required = _associations.removeObjectForKey("required");

    _blankIsNull = _associations.removeObjectForKey("blankIsNull");
  }

  @Override
  public String type() {
    return "url";
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
      woresponse._appendTagAttributeAndValue("pattern", URL_PATTERN, true);
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
    stringbuffer.append(" placeholder=" + _placeholder);
    stringbuffer.append(" pattern=" + _pattern);
    stringbuffer.append(" size=" + _size);
    stringbuffer.append(" maxlength=" + _maxlength);
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