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
    if (myBindings != null) {
      boolean hasPreviousOptions = false;
      WOComponent component = _context.component();
      Enumeration bindingsEnum = myBindings.keyEnumerator();
      while (bindingsEnum.hasMoreElements()) {
        String bindingName = (String) bindingsEnum.nextElement();
        WOAssociation association = (WOAssociation) myBindings.objectForKey(bindingName);
        Object bindingValue = association.valueInComponent(component);
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
    if (myChildren != null) {
      myChildren.appendToResponse(_response, _context);
    }
    _response.appendContentCharacter('}');
  }
}
