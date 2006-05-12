package er.imadaptor.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.imadaptor.InstantMessengerAdaptor;

/**
 * IMConfirmation has a single binding "confirmed".  If the response
 * from the IM buddy matches any of a set of common "yes", "no", etc 
 * words, confirmed is set to the appropriate value.  If neither a yes
 * nor a no word is found, confirmed is set to null.  You should bind this
 * to a Boolean rather than a boolean so that you can detect the third
 * state properly and re-ask the question.
 * 
 * @author mschrag
 */
public class IMConfirmationAction extends IMAction {
  private WOAssociation myConfirmed;

  public IMConfirmationAction(String _name, NSDictionary _associations, WOElement _children) {
    super(_name, _associations, _children);
    myConfirmed = (WOAssociation) _associations.objectForKey("confirmed");
  }

  protected void actionInvoked(WORequest _request, WOContext _context) {
    String message = _request.stringFormValueForKey(InstantMessengerAdaptor.MESSAGE_KEY);
    String lowercaseMessage = message.trim().toLowerCase();
    NSMutableSet yes = new NSMutableSet();
    yes.addObject("yes");
    yes.addObject("y");
    yes.addObject("yep");
    yes.addObject("true");

    NSMutableSet no = new NSMutableSet();
    no.addObject("no");
    no.addObject("n");
    no.addObject("nope");
    no.addObject("nah");

    WOComponent component = _context.component();
    if (yes.containsObject(lowercaseMessage)) {
      myConfirmed.setValue(Boolean.TRUE, component);
    }
    else if (no.containsObject(lowercaseMessage)) {
      myConfirmed.setValue(Boolean.FALSE, component);
    }
    else {
      myConfirmed.setValue(null, component);
    }
  }
}