package er.imadaptor.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.imadaptor.InstantMessengerAdaptor;

public class IMTextAction extends IMAction {
  private NSMutableDictionary myAssociations;
  private WOAssociation myValue;
  private WOAssociation myAllowBlanks;

  public IMTextAction(String _name, NSDictionary _associations, WOElement _element) {
    super(_name, _associations, _element);
    myValue = (WOAssociation) _associations.objectForKey("value");
    myAllowBlanks = (WOAssociation) _associations.objectForKey("allowBlanks");
  }

  protected void actionInvoked(WORequest _request, WOContext _context) {
    WOComponent component = _context.component();
    String message = InstantMessengerAdaptor.message(_request);
    boolean allowBlanks = (myAllowBlanks != null && ((Boolean)myAllowBlanks.valueInComponent(component)).booleanValue());
    if (allowBlanks || (message != null || message.trim().length() > 0)) {
      myValue.setValue(message, component);
    }
    else {
      myValue.setValue(null, component);
    }
  }
}
