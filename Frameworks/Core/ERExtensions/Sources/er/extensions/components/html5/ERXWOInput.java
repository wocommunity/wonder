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

import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXPatcher;

/**
 * <span class="en">
 * 
 * </span>
 * 
 * <span class="ja">
 * WOInput HTML5 拡張
 * </span>
 * 
 * @author ishimoto
 *
 */
public class ERXWOInput extends WOInput {

  //********************************************************************
  //  Binding Properties
  //********************************************************************

  protected WOAssociation _readonly;
  protected WOAssociation _required;
  protected WOAssociation _blankIsNull;

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERXWOInput(String tagname, NSDictionary<String, WOAssociation> nsdictionary, WOElement woelement) {
    super(tagname, nsdictionary, woelement);
    if(_value == null || !_value.isValueSettable())
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'value' attribute not present or is a constant");

    _readonly = _associations.removeObjectForKey("readonly");
    _required = _associations.removeObjectForKey("required");

    _blankIsNull = _associations.removeObjectForKey("blankIsNull");
  }

  //********************************************************************
  //  Methods
  //********************************************************************

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
  protected void _appendCloseTagToResponse(WOResponse woresponse, WOContext wocontext) {
  }

  //********************************************************************
  //  RR - Methods
  //********************************************************************

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

  /**
   * <span class="ja">
   * XML 互換性の為にオーバライド
   * </span>
   */
  @Override
  public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
    WOResponse newResponse = ERXPatcher.DynamicElementsPatches.cleanupXHTML ? new ERXResponse() : woresponse;
    super.appendToResponse(newResponse, wocontext);

    ERXPatcher.DynamicElementsPatches.processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
    if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
      woresponse.appendContentString(newResponse.contentString());
    }
  }

}
