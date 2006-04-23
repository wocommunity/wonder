package er.ajax;

import java.util.Enumeration;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class AjaxOptions extends WODynamicElement {
  private NSDictionary myBindings;
  private WOElement myChildren;

  public AjaxOptions(String _name, NSDictionary _bindings, WOElement _children) {
    super(_name, _bindings, _children);
    myBindings = _bindings;
    myChildren = _children;
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    _response.appendContentCharacter('{');
    AjaxOptions._appendToResponse(myBindings, _response, _context);
    if (myChildren != null) {
      myChildren.appendToResponse(_response, _context);
    }
    _response.appendContentCharacter('}');
  }

  public static void _appendToResponse(NSDictionary _options, WOResponse _response, WOContext _context) {
    if (_options != null) {
      WOComponent component = _context.component();
      boolean hasPreviousOptions = false;
      Enumeration bindingsEnum = _options.keyEnumerator();
      while (bindingsEnum.hasMoreElements()) {
        String bindingName = (String) bindingsEnum.nextElement();
        Object bindingValue = _options.objectForKey(bindingName);
        if (bindingValue instanceof WOAssociation) {
          WOAssociation association = (WOAssociation) bindingValue;
          bindingValue = association.valueInComponent(component);
        }
        if (bindingValue != null) {
          if (hasPreviousOptions) {
            _response.appendContentString(", ");
          }
          _response.appendContentString(bindingName);
          _response.appendContentCharacter(':');
          _response.appendContentString(bindingValue.toString());
          hasPreviousOptions = true;
        }
      }
    }
  }

  public static void appendToResponse(NSDictionary _options, WOResponse _response, WOContext _context) {
    _response.appendContentCharacter('{');
    AjaxOptions._appendToResponse(_options, _response, _context);
    _response.appendContentCharacter('}');
  }
}
