package er.ajax;

import java.util.Enumeration;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxOptions extends WODynamicElement {
  private NSMutableDictionary _bindings;
  private WOElement _children;

  public AjaxOptions(String name, NSDictionary bindings, WOElement children) {
    super(name, bindings, children);
    _bindings = bindings.mutableClone();
    _children = children;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    response.appendContentCharacter('{');
    NSMutableDictionary options = _bindings;
    WOAssociation optionsBinding = (WOAssociation) _bindings.objectForKey("options");
    if (optionsBinding != null) {
      NSDictionary passedInOptions = (NSDictionary) optionsBinding.valueInComponent(context.component());
      if (passedInOptions != null) {
        options = passedInOptions.mutableClone();
        options.addEntriesFromDictionary(_bindings);
      }
    }
    AjaxOptions._appendToResponse(options, response, context);
    if (_children != null) {
      _children.appendToResponse(response, context);
    }
    response.appendContentCharacter('}');
  }

  public static void _appendToResponse(NSDictionary options, WOResponse response, WOContext context) {
    StringBuffer sb = new StringBuffer();
    AjaxOptions._appendToBuffer(options, sb, context);
    response.appendContentString(sb.toString());
  }
  
  public static void _appendToBuffer(NSDictionary options, StringBuffer stringBuffer, WOContext context) {
    if (options != null) {
      WOComponent component = context.component();
      boolean hasPreviousOptions = false;
      Enumeration bindingsEnum = options.keyEnumerator();
      while (bindingsEnum.hasMoreElements()) {
        String bindingName = (String) bindingsEnum.nextElement();
        if (!"options".equals(bindingName)) {
          Object bindingValue = options.objectForKey(bindingName);
          if (bindingValue instanceof WOAssociation) {
            WOAssociation association = (WOAssociation) bindingValue;
            bindingValue = association.valueInComponent(component);
          }
          if (bindingValue != null) {
            if (hasPreviousOptions) {
              stringBuffer.append(", ");
            }
            stringBuffer.append(bindingName);
            stringBuffer.append(':');
            stringBuffer.append(bindingValue.toString());
            hasPreviousOptions = true;
          }
        }
      }
    }
  }

  public static void appendToBuffer(NSDictionary options, StringBuffer stringBuffer, WOContext context) {
    stringBuffer.append('{');
    AjaxOptions._appendToBuffer(options, stringBuffer, context);
    stringBuffer.append('}');
  }

  public static void appendToResponse(NSDictionary options, WOResponse response, WOContext context) {
    response.appendContentCharacter('{');
    AjaxOptions._appendToResponse(options, response, context);
    response.appendContentCharacter('}');
  }
}
