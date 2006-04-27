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
  private NSMutableDictionary myBindings;
  private WOElement myChildren;

  public AjaxOptions(String _name, NSDictionary _bindings, WOElement _children) {
    super(_name, _bindings, _children);
    myBindings = _bindings.mutableClone();
    myChildren = _children;
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    _response.appendContentCharacter('{');
    NSMutableDictionary options = myBindings;
    WOAssociation optionsBinding = (WOAssociation) myBindings.objectForKey("options");
    if (optionsBinding != null) {
      NSDictionary passedInOptions = (NSDictionary) optionsBinding.valueInComponent(_context.component());
      if (passedInOptions != null) {
        options = passedInOptions.mutableClone();
        options.addEntriesFromDictionary(myBindings);
      }
    }
    AjaxOptions._appendToResponse(options, _response, _context);
    if (myChildren != null) {
      myChildren.appendToResponse(_response, _context);
    }
    _response.appendContentCharacter('}');
  }

  public static void _appendToResponse(NSDictionary _options, WOResponse _response, WOContext _context) {
    StringBuffer sb = new StringBuffer();
    AjaxOptions._appendToBuffer(_options, sb, _context);
    _response.appendContentString(sb.toString());
  }
  
  public static void _appendToBuffer(NSDictionary _options, StringBuffer _stringBuffer, WOContext _context) {
    if (_options != null) {
      WOComponent component = _context.component();
      boolean hasPreviousOptions = false;
      Enumeration bindingsEnum = _options.keyEnumerator();
      while (bindingsEnum.hasMoreElements()) {
        String bindingName = (String) bindingsEnum.nextElement();
        if (!"options".equals(bindingName)) {
          Object bindingValue = _options.objectForKey(bindingName);
          if (bindingValue instanceof WOAssociation) {
            WOAssociation association = (WOAssociation) bindingValue;
            bindingValue = association.valueInComponent(component);
          }
          if (bindingValue != null) {
            if (hasPreviousOptions) {
              _stringBuffer.append(", ");
            }
            _stringBuffer.append(bindingName);
            _stringBuffer.append(':');
            _stringBuffer.append(bindingValue.toString());
            hasPreviousOptions = true;
          }
        }
      }
    }
  }

  public static void appendToBuffer(NSDictionary _options, StringBuffer _stringBuffer, WOContext _context) {
    _stringBuffer.append('{');
    AjaxOptions._appendToBuffer(_options, _stringBuffer, _context);
    _stringBuffer.append('}');
  }

  public static void appendToResponse(NSDictionary _options, WOResponse _response, WOContext _context) {
    _response.appendContentCharacter('{');
    AjaxOptions._appendToResponse(_options, _response, _context);
    _response.appendContentCharacter('}');
  }
}
