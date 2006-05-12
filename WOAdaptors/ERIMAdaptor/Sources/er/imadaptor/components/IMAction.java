package er.imadaptor.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.imadaptor.InstantMessengerAdaptor;

public class IMAction extends WODynamicElement {
  protected WOAssociation myAction;

  public IMAction(String _name, NSDictionary _assocationsDictionary, WOElement _template) {
    super("link", _assocationsDictionary, _template);
    myAction = (WOAssociation) _assocationsDictionary.objectForKey("action");
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    String actionUrl = _context._componentActionURL(false);
    _response.setHeader(actionUrl, InstantMessengerAdaptor.IM_ACTION_URL_KEY);
    super.appendToResponse(_response, _context);
  }

  public WOActionResults invokeAction(WORequest _request, WOContext _context) {
    String s = null;
    WOActionResults results = null;
    if (_context.elementID().equals(_context.senderID())) {
      actionInvoked(_request, _context);
      WOComponent component = _context.component();
      results = (WOActionResults) myAction.valueInComponent(component);
      if (results == null) {
        results = _context.page();
      }
    }
    return results;
  }

  protected void actionInvoked(WORequest _request, WOContext _context) {
  }
}
